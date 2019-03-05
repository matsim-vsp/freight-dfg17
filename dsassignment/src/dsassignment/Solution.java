/*******************************************************************************
 * Copyright (c) 2018 Lei Zhang.
 ******************************************************************************/

package dsassignment;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

public class Solution
{
	private static final int DISTANCE_INTERVAL = 2000; //Distance interval
	private static final int DISTANCE_TYPE_CNT = 30; //Distance classification
	private static final int INIT_TIME = 1000; //The number of initialization attempts. If the number of times is exceeded, the initialization will stop.
	private static final int ITERATION_TIME = 1000000; //Number of iterations
	private static final int UPDATE_CNT_PER_ITERATION = 1; //The number of S updates per iteration
	private static final int MIDDLE_RESULT_OUTPUT_INTERVAL = 10000; 

	public void process() throws EncryptedDocumentException, InvalidFormatException, IOException
	{
		String xlsFilename = "./input/in_all.xlsx";
		File xlsFile = new File(xlsFilename);
		if (!xlsFile.exists())
		{
			System.out.println("Excel file does not exist");
			return;
		}

		String outputDirName = "./result";
		File outputDir = new File(outputDirName);
		if (!outputDir.exists())
		{
			outputDir.mkdirs();
		}

		
		Workbook workbook = WorkbookFactory.create(xlsFile);

		Sheet sheetS = workbook.getSheet("S");
		Sheet sheetD = workbook.getSheet("D");
		Sheet sheetObserver = workbook.getSheet("Observation_distribution");
		if (sheetS == null || sheetD == null || sheetObserver == null)
		{
			System.err.println("Incomplete form");
			return;
		}

		HashMap<Integer, ArrayList<Integer>> observeMap = createObserveMap(sheetObserver);
		ArrayList<S> SList = createSList(sheetS);
		ArrayList<D> DList = createDList(sheetD);
	
		if (init(SList, DList))
		{
			System.out.println("Initialization successful");
		}
		else
		{
			System.out.println("Initialization failed");
			return;
		}

		
		String dirName = "./result";
		File dir = new File(dirName);
		deleteFile(dir);

		Random random = new Random();
		double minRelativeEntropy = Double.MAX_VALUE;
		for (int iter = 1; iter <= ITERATION_TIME; ++iter)
		{
			HashSet<S> rollbackSet = new HashSet<>();
			for (int i = 0; i < UPDATE_CNT_PER_ITERATION; ++i)
			{
				int index = random.nextInt(SList.size() - i);
				S s = SList.get(index);
				exchange(SList, index, SList.size() - 1 - i);
				s.save();
				s.updateSelect();
				rollbackSet.add(s);
			}
			HashMap<Integer, ArrayList<Integer>> map = createMapFromSList(SList);
			double relativeEntropy = relativeEntropy(map, observeMap);
			if (relativeEntropy <= minRelativeEntropy)
			{
				minRelativeEntropy = relativeEntropy;
			}
			else
			{
				for (S s : rollbackSet)
				{
					s.rollback();
				}
			}
			if (iter % MIDDLE_RESULT_OUTPUT_INTERVAL == 0)
			{
				System.out.println("Iterated" + iter + "rounds");
				saveFile(SList, minRelativeEntropy, "./result/middle/" + iter + ".csv");
			}
		}
		saveFile(SList, minRelativeEntropy, "./result/FinalResult.csv");
	}

	private void exchange(ArrayList<S> list, int a, int b)
	{
		S sa = list.get(a);
		S sb = list.get(b);
		list.set(a, sb);
		list.set(b, sa);
	}

	
	//Convert a row in the POI to a string array
	private ArrayList<String> rowToString(Row row)
	{
		ArrayList<String> array = new ArrayList<>();
		for (int i = 0; i < row.getLastCellNum(); ++i)
		{
			if (row.getCell(i) == null)
			{
				array.add(null);
				continue;
			}
			CellType type = row.getCell(i).getCellTypeEnum();
			String value = row.getCell(i).toString();
			switch (type)
			{
				case BLANK:
					array.add(null);
					break;
				case NUMERIC:
					if (value.endsWith(".0"))
					{
						array.add(value.substring(0, value.length() - 2));
					}
					else
					{
						array.add(value);
					}
					break;
				case STRING:
					array.add(value);
					break;
				default:
					break;
			}
		}
		return array;
	}

	private ArrayList<S> createSList(Sheet sheet)
	{
		ArrayList<S> list = new ArrayList<>();

		//Get the number of rows
		int rowCount = sheet.getLastRowNum() + 1;

		//Convert
		for (int i = 1; i < rowCount; ++i)
		{
			ArrayList<String> row = rowToString(sheet.getRow(i));
			int index = 0;
			String id = row.get(index++);
			double x = Double.parseDouble(row.get(index++));
			double y = Double.parseDouble(row.get(index++));
			String labelA = row.get(index++);
			String labelO = row.get(index++);
			String labelH = row.get(index++);
			String labelP = row.get(index++);
			double demand = Double.parseDouble(row.get(index++));
			S s = new S(id, x, y, labelA, labelO, labelH, labelP, demand);
			list.add(s);
		}

		return list;
	}

	private ArrayList<D> createDList(Sheet sheet)
	{
		ArrayList<D> list = new ArrayList<>();

		
		int rowCount = sheet.getLastRowNum() + 1;

		
		for (int i = 1; i < rowCount; ++i)
		{
			ArrayList<String> row = rowToString(sheet.getRow(i));
			int index = 0;
			String id = row.get(index++);
			double x = Double.parseDouble(row.get(index++));
			double y = Double.parseDouble(row.get(index++));
			String labelO = row.get(index++);
			String labelH = row.get(index++);
			String labelP = row.get(index++);
			double capacity = Double.parseDouble(row.get(index++));
			D d = new D(id, x, y, labelO, labelH, labelP, capacity);
			list.add(d);
		}

		return list;
	}

	private HashMap<Integer, ArrayList<Integer>> createObserveMap(Sheet sheet)
	{
		HashMap<Integer, ArrayList<Integer>> map = new HashMap<>();

		
		int rowCount = sheet.getLastRowNum() + 1;

		
		int columnCount = sheet.getRow(0).getLastCellNum();

		ArrayList<String> header = rowToString(sheet.getRow(0));
		ArrayList<Integer> keys = new ArrayList<>();
		for (int columnIndex = 0; columnIndex < columnCount; columnIndex += 4)
		{
			String keyStr = header.get(columnIndex);
			int key = Integer.parseInt(keyStr.substring(1));
			map.put(key, new ArrayList<>());
			keys.add(key);
		}

		for (int rowIndex = 1; rowIndex < rowCount; ++rowIndex)
		{
			ArrayList<String> row = rowToString(sheet.getRow(rowIndex));
			for (int columnIndex = 2; columnIndex < columnCount; columnIndex += 4)
			{
				map.get(keys.get(columnIndex / 4)).add(Integer.parseInt(row.get(columnIndex)));
			}
		}
		return map;
	}

	private HashMap<Integer, ArrayList<Integer>> createMapFromSList(ArrayList<S> list)
	{
		HashMap<Integer, ArrayList<Integer>> map = new HashMap<>();
		for (int i = 0; i < list.size(); ++i)
		{
			S s = list.get(i);
			int a = Integer.parseInt(s.getLabelA());
			ArrayList<Integer> frequencyList = null;
			if (!map.containsKey(a))
			{
				frequencyList = new ArrayList<>();
				for (int cnt = 0; cnt < DISTANCE_TYPE_CNT; ++cnt)
				{
					frequencyList.add(0);
				}
				map.put(a, frequencyList);
			}
			else
			{
				frequencyList = map.get(a);
			}
			double dis = s.distanceTo(s.getSelectedD());
			int index = (int) (dis / DISTANCE_INTERVAL);
			if (index >= DISTANCE_TYPE_CNT)
			{
				index = DISTANCE_TYPE_CNT - 1;
			}
			frequencyList.set(index, frequencyList.get(index) + 1);
		}
		return map;
	}

	
	//Convert the frequency table to a probability table
	private ArrayList<Double> frequency2Probability(ArrayList<Integer> list)
	{
		int total = 0;
		for (int i = 0; i < list.size(); ++i)
		{
			total += list.get(i);
		}
		return frequency2Probability(list, total);
	}

	private ArrayList<Double> frequency2Probability(ArrayList<Integer> list, int total)
	{
		ArrayList<Double> res = new ArrayList<>();
		for (int i = 0; i < list.size(); ++i)
		{
			int frequency = list.get(i);
			res.add(1.0 * frequency / total);
		}
		return res;
	}

	
	//Calculate Symmetric Kullback-Leibler-Divergence
	private double relativeEntropy(HashMap<Integer, ArrayList<Integer>> m1, HashMap<Integer, ArrayList<Integer>> m2)
	{
		double res = 0.0;
		for (Integer key : m1.keySet())
		{
			if (m2.containsKey(key))
			{
				ArrayList<Double> P = frequency2Probability(m1.get(key));
				ArrayList<Double> Q = frequency2Probability(m2.get(key));
				res += relativeEntropy(P, Q) + relativeEntropy(Q, P);
			}
		}
		return res;
	}

	
	private double relativeEntropy(ArrayList<Double> P, ArrayList<Double> Q)
	{
		if (P.size() != Q.size())
		{
			return -1.0;
		}
		double res = 0.0;
		for (int i = 0; i < P.size(); ++i)
		{
			if (P.get(i) == 0.0 || Q.get(i) == 0.0)
			{
				continue;
			}
			res += P.get(i) * Math.log(P.get(i) / Q.get(i));
		}
		return res;
	}

	private boolean init(ArrayList<S> SList, ArrayList<D> DList)
	{
		
		for (int i = 0; i < SList.size(); ++i)
		{
			SList.get(i).configMatchList(DList);
		}
		for (int t = 0; t < INIT_TIME; ++t)
		{
			for (int i = 0; i < SList.size(); ++i)
			{
				SList.get(i).clearSelect();
			}
			boolean initSuccess = true;
			for (int i = 0; i < SList.size(); ++i)
			{
				initSuccess = SList.get(i).init();
				if (!initSuccess)
				{
					break;
				}
			}
			if (initSuccess)
			{
				return true;
			}
		}
		return false;
	}

	void saveFile(ArrayList<S> list, double re, String filename) throws IOException
	{
		list.sort(new Comparator<S>()
		{
			@Override
			public int compare(S o1, S o2)
			{
				return o1.getId().compareTo(o2.getId());
			}
		});

		File file = new File(filename);
		if (!file.exists())
		{
			file.getParentFile().mkdirs();
			file.createNewFile();
		}

		FileWriter writer = null;
		try
		{
			writer = new FileWriter(file);
			writer.write("relative entropy," + re + "\n");
			writer.write("SID, DID\n");
			for (int i = 0; i < list.size(); ++i)
			{
				S s = list.get(i);
				D d = s.getSelectedD();
				writer.write(s.getId() + "," + d.getId() + "\n");
			}
		}
		finally
		{
			writer.close();
		}
	}

	private static boolean deleteFile(File dirFile)
	{
		if (!dirFile.exists())
		{
			return false;
		}

		if (dirFile.isFile())
		{
			return dirFile.delete();
		}
		else
		{

			for (File file : dirFile.listFiles())
			{
				deleteFile(file);
			}
		}

		return dirFile.delete();
	}
}
