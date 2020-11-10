/*******************************************************************************
 * Copyright (c) 2018 Lei Zhang.
 ******************************************************************************/

package dassignment;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Random;

class Store
{
	public Store( String id, double x, double y, String labelA, String labelO, String labelH, String labelP, double demand )
	{
		super();
		this.id = id;
		this.x = x;
		this.y = y;
		this.labelA = labelA;
		this.labelO = labelO;
		this.labelH = labelH;
		this.labelP = labelP;
		this.demand = demand;
	}

	public Store()
	{

	}

	private String id;
	private double x;
	private double y;
	private String labelA;
	private String labelO;
	private String labelH;
	private String labelP;
	private double demand;
	private ArrayList<DistributionCenter> highPriorityMatchList = new ArrayList<>();
	private ArrayList<DistributionCenter> lowPriorityMatchList = new ArrayList<>();
	private DistributionCenter selectedD = null;

	public DistributionCenter getSelectedD()
	{
		return selectedD;
	}

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public double getX()
	{
		return x;
	}

	public void setX(double x)
	{
		this.x = x;
	}

	public double getY()
	{
		return y;
	}

	public void setY(double y)
	{
		this.y = y;
	}

	public String getLabelA()
	{
		return labelA;
	}

	public void setLabelA(String labelA)
	{
		this.labelA = labelA;
	}

	public String getLabelO()
	{
		return labelO;
	}

	public void setLabelO(String labelO)
	{
		this.labelO = labelO;
	}

	public String getLabelH()
	{
		return labelH;
	}

	public void setLabelH(String labelH)
	{
		this.labelH = labelH;
	}

	public String getLabelP()
	{
		return labelP;
	}

	public void setLabelP(String labelP)
	{
		this.labelP = labelP;
	}

	public double getDemand()
	{
		return demand;
	}

	public void setDemand(double demand)
	{
		this.demand = demand;
	}

	public double distanceTo( DistributionCenter d )
	{
		double dx = this.getX() - d.getX();
		double dy = this.getY() - d.getY();
		return Math.sqrt(dx * dx + dy * dy);
	}

	public void configMatchList(ArrayList<DistributionCenter> DList )
	{
		for ( DistributionCenter d : DList)
		{
			boolean oFlag = this.getLabelO().equals(d.getLabelO());
			if (!oFlag)
			{
				continue;
			}

			boolean hFlag = this.getLabelH().equals(d.getLabelH()) || this.getLabelH().equals("wild");
			if (!hFlag)
			{
				continue;
			}

			boolean pFlag = this.getLabelP().equals(d.getLabelP());
			if (pFlag)
			{
				highPriorityMatchList.add(d);
			}
			else
			{
				lowPriorityMatchList.add(d);
			}
		}
		DComparator comparator = new DComparator();
	}


	//Randomly re-assignment a matching D
	public boolean updateSelect()
	{
		Random random = new Random();
		ArrayList<DistributionCenter> highPriorityPendingList = new ArrayList<>();
		for ( DistributionCenter d : this.highPriorityMatchList)
		{
			if (d.canAddS(this))
			{
				highPriorityPendingList.add(d);
			}
		}
		boolean hasHighPriorityMatch = !highPriorityPendingList.isEmpty();
		boolean hasLowPriorityMatch = !lowPriorityMatchList.isEmpty();
		if (hasHighPriorityMatch)
		{
			int index = random.nextInt(highPriorityPendingList.size());
			return select(highPriorityPendingList.get(index));
		}
		else if (hasLowPriorityMatch)
		{
			int index = random.nextInt(lowPriorityMatchList.size());
			return select(lowPriorityMatchList.get(index));
		}
		else
		{
			return false;
		}
	}


	//Select or reselect a D
	public boolean select( DistributionCenter d )
	{
		if (d == null)
		{
			clearSelect();
			return false;
		}
		if (selectedD == d)
		{
			return true;
		}

		boolean canAdd = d.addS(this);
		if (canAdd)
		{
			if (selectedD != null)
			{
				selectedD.removeS(this);
			}
			this.selectedD = d;
			return true;
		}
		else
		{
			return false;
		}
	}


	public void clearSelect()
	{
		if (selectedD != null)
		{
			selectedD.removeS(this);
			selectedD = null;
		}
	}


	//Initialize selection
	public boolean init()
	{
		if (highPriorityMatchList.isEmpty() && lowPriorityMatchList.isEmpty()) {
			return false;
		}

		DistributionCenter nearestD = null;
		for ( DistributionCenter d : this.highPriorityMatchList)
		{
			if (d.canAddS(this))
			{
				if (nearestD == null || this.distanceTo(d) < this.distanceTo(nearestD))
				{
					nearestD = d;
				}
			}
		}
		if (nearestD != null)
		{
			select(nearestD);
			return true;
		}

		nearestD = null;
		for ( DistributionCenter d : this.lowPriorityMatchList)
		{
			if (d.canAddS(this))
			{
				if (nearestD == null || this.distanceTo(d) < this.distanceTo(nearestD))
				{
					nearestD = d;
				}
			}
		}
		if (nearestD != null)
		{
			select(nearestD);
			return true;
		}

		return false;
	}

	private DistributionCenter originalSelectedIndex = null;


	public void save()
	{
		originalSelectedIndex = selectedD;
	}


	public void rollback()
	{
		select(originalSelectedIndex);
	}

	static class DComparator implements Comparator<DistributionCenter>
	{
		@Override
		public int compare( DistributionCenter o1, DistributionCenter o2 )
		{
			double res = o1.remainingCapacity() - o2.remainingCapacity();
			if (res < 0.0)
			{
				return -1;
			}
			else if (res > 0.0)
			{
				return 1;
			}
			return 0;
		}
	}

}
