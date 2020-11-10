/*******************************************************************************
 * Copyright (c) 2018 Lei Zhang.
 ******************************************************************************/

package dassignment;

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

/**
 * This class reads xlsFilename.  It expects sheets with the following names:<ul>
 * <li> "S" (= stores; they order goods) </li>
 * <li> "D" (= distribution centers; they send the good) </li>
 * <li> "Observation_distribution", which contains the distance distribution for the shipments</li>
 */
class Solution
{
	/**
	 * The bins in which the distances are registered.  Presumably needs to be the same as in the "Observation_distribution".
	 */
	private static final int DISTANCE_INTERVAL = 2000;
	/**
	 * The number of bins.
	 */
	private static final int DISTANCE_TYPE_CNT = 30; //Distance classification


	private static final int INIT_TIME = 1000; //The number of initialization attempts. If the number of times is exceeded, the initialization will stop.
	private static final int ITERATION_TIME = 1000000; //Number of iterations
	private static final int UPDATE_CNT_PER_ITERATION = 1; //The number of S updates per iteration
	private static final int MIDDLE_RESULT_OUTPUT_INTERVAL = 10000; 
	private static final double T0 = 27;

	void process() throws EncryptedDocumentException, InvalidFormatException, IOException
	{
		String xlsFilename = "./scenarios/dassignment/in_all_with_labels.xlsx";
		File xlsFile = new File(xlsFilename);
		if (!xlsFile.exists())
		{
			System.out.println("Excel file does not exist: " + xlsFile.getAbsolutePath());
			return;
		}

		String outputDirName = "./output/result";
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
		ArrayList<Store> sList = createSList(sheetS );
		ArrayList<DistributionCenter> dList = createDList(sheetD );
	
		if (init(sList, dList))
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

		Random random = new Random(4711);
		double minRelativeEntropy = Double.MAX_VALUE;
		double T = T0;
		for (int iter = 1; iter <= ITERATION_TIME; ++iter)
		{
			HashSet<Store> rollbackSet = new HashSet<>();
			for (int i = 0; i < UPDATE_CNT_PER_ITERATION; ++i)
			{
				int index = random.nextInt(sList.size() - i);
				Store s = sList.get(index );
				exchange(sList, index, sList.size() - 1 - i); //Maybe randomize also the second one (b), kai/KMT apr19
				s.save();
				s.updateSelect();
				rollbackSet.add(s);
			}
			HashMap<Integer, ArrayList<Integer>> map = createMapFromSList(sList);
			double relativeEntropy = relativeEntropy(map, observeMap);
			if (relativeEntropy <= minRelativeEntropy)
			{
				minRelativeEntropy = relativeEntropy;
			}
			else
			{
			double acceptProbability = Math.exp(-(relativeEntropy - minRelativeEntropy) / T);
				T = random.nextDouble() * T;
				if (random.nextDouble() < acceptProbability)
				{
					minRelativeEntropy = relativeEntropy;
				}
				else
				{
				for ( Store s : rollbackSet)
				{
					s.rollback();
				}
				}
			}
			if (iter % MIDDLE_RESULT_OUTPUT_INTERVAL == 0)
			{
				System.out.println("Iterated" + iter + "rounds");
				saveFile(sList, minRelativeEntropy, "./result/middle/" + iter + ".csv");
			}
		}
		saveFile(sList, minRelativeEntropy, "./result/FinalResult.csv");
	}

	private void exchange( ArrayList<Store> list, int a, int b )
	{
		Store sa = list.get(a );
		Store sb = list.get(b );
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

	private ArrayList<Store> createSList( Sheet sheet )
	{
		ArrayList<Store> list = new ArrayList<>();

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
			Store s = new Store(id, x, y, labelA, labelO, labelH, labelP, demand);
			list.add(s);
		}

		return list;
	}

	private ArrayList<DistributionCenter> createDList( Sheet sheet )
	{
		ArrayList<DistributionCenter> list = new ArrayList<>();

		
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
			DistributionCenter d = new DistributionCenter(id, x, y, labelO, labelH, labelP, capacity);
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

	private HashMap<Integer, ArrayList<Integer>> createMapFromSList(ArrayList<Store> list )
	{
		HashMap<Integer, ArrayList<Integer>> map = new HashMap<>();
		for( Store s : list ) {
			int a = Integer.parseInt( s.getLabelA() );
			ArrayList<Integer> frequencyList = null;
			if( !map.containsKey( a ) ){
				frequencyList = new ArrayList<>();
				for( int cnt = 0 ; cnt < DISTANCE_TYPE_CNT ; ++cnt ){
					frequencyList.add( 0 );
				}
				map.put( a, frequencyList );
			} else{
				frequencyList = map.get( a );
			}
			double dis = s.distanceTo( s.getSelectedD() );
			int index = (int) (dis / DISTANCE_INTERVAL);
			if( index >= DISTANCE_TYPE_CNT ){
				index = DISTANCE_TYPE_CNT - 1;
			}
			frequencyList.set( index, frequencyList.get( index ) + 1 );
		}
		return map;
	}

	
	//Convert the frequency table to a probability table
	private ArrayList<Double> frequency2Probability(ArrayList<Integer> list)
	{
		int total = 0;
		for( Integer integer : list ) {
			total += integer;
		}
		return frequency2Probability(list, total);
	}

	private ArrayList<Double> frequency2Probability(ArrayList<Integer> list, int total)
	{
		ArrayList<Double> res = new ArrayList<>();
		for( int frequency : list ){
			res.add( 1.0 * frequency / total );
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

	private boolean init( ArrayList<Store> sList, ArrayList<DistributionCenter> dList ) {

		for( Store stores : sList ){
			stores.configMatchList( dList );
		}
		for (int t = 0; t < INIT_TIME; ++t) {
			for( Store stores : sList ){
				stores.clearSelect();
			}
			boolean initSuccess = true;
			for( Store stores : sList ){
				initSuccess = stores.init();
				if( !initSuccess ){
					break;
				}
			}
			if (initSuccess) {
				return true;
			}
		}
		return false;
	}

	void saveFile( ArrayList<Store> list, double re, String filename ) throws IOException
	{
		list.sort(new Comparator<Store>()
		{
			@Override
			public int compare( Store o1, Store o2 )
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
		try {
			writer = new FileWriter(file);
			writer.write("relative entropy," + re + "\n");
			writer.write("SID, DID\n");
			for( Store s : list ){
				DistributionCenter d = s.getSelectedD();
				writer.write( s.getId() + "," + d.getId() + "\n" );
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
