/*******************************************************************************
 * Copyright (c) 2018 Lei Zhang.
 ******************************************************************************/

package dassignment;

import java.util.ArrayList;
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

	private final String id;
	private final double x;
	private final double y;
	/**
	 *
	 */
	private final String labelA;
	/**
	 * So-called O label: stores are mandatorily assigned to distribution centers with same O label.
	 */
	private final String labelO;
	/**
	 * Stores are mandatorily assigned to distribution centers with same H label.  However, DCs can serve stores with different H label (which can
	 * only be triggered when the DC has a wildcard H label).  A store with a * label cannot be assigned to a DC with a non-wildcard label.  (????)
	 * See configMatchList below for how it is coded.
	 */
	private final String labelH;
	/**
	 * Stores are assigned preferably to DCs with same P label.
	 */
	private final String labelP;
	private final double demand;
	private final ArrayList<DistributionCenter> highPriorityMatchList = new ArrayList<>();
	private final ArrayList<DistributionCenter> lowPriorityMatchList = new ArrayList<>();
	private DistributionCenter selectedDistributionCenter;

	public DistributionCenter getSelectedDistributionCenter()
	{
		return selectedDistributionCenter;
	}

	public String getId()
	{
		return id;
	}

//	public void setId(String id)
//	{
//		this.id = id;
//	}

	public double getX()
	{
		return x;
	}

//	public void setX(double x)
//	{
//		this.x = x;
//	}

	public double getY()
	{
		return y;
	}

//	public void setY(double y)
//	{
//		this.y = y;
//	}

	public String getLabelA()
	{
		return labelA;
	}

//	public void setLabelA(String labelA)
//	{
//		this.labelA = labelA;
//	}

	public String getLabelO()
	{
		return labelO;
	}

//	public void setLabelO(String labelO)
//	{
//		this.labelO = labelO;
//	}

	public String getLabelH()
	{
		return labelH;
	}

//	public void setLabelH(String labelH)
//	{
//		this.labelH = labelH;
//	}

	public String getLabelP()
	{
		return labelP;
	}

//	public void setLabelP(String labelP)
//	{
//		this.labelP = labelP;
//	}

	public double getDemand()
	{
		return demand;
	}

//	public void setDemand(double demand)
//	{
//		this.demand = demand;
//	}

	public double distanceTo( DistributionCenter d )
	{
		double dx = this.getX() - d.getX();
		double dy = this.getY() - d.getY();
		return Math.sqrt(dx * dx + dy * dy);
	}

	public void configMatchList(ArrayList<DistributionCenter> dList )
	{
		// what this roughly does: If the "O" condition or the "H" condition is not fulfilled, it just moves on.  Otherwise, it adds the DC
		// into the lowPrioMatchList, EXCEPT if the "P" condition is fulfilled.

		for ( DistributionCenter d : dList)
		{
			boolean oFlag = this.getLabelO().equals(d.getLabelO());
			if (!oFlag)
			{
				continue;
			}

			boolean hFlag = this.getLabelH().equals(d.getLabelH()) || this.getLabelH().equals("wild");
			// (this is the ONLY place where getLabelH is used!)

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
//		DComparator comparator = new DComparator();
	}


	//Randomly re-assignment a matching D
	public void updateSelect()
	{
		Random random = new Random();
		ArrayList<DistributionCenter> highPriorityPendingList = new ArrayList<>();
		for ( DistributionCenter d : this.highPriorityMatchList)
		{
			if (d.canAddStore(this ))
			{
				highPriorityPendingList.add(d);
			}
		}
		boolean hasHighPriorityMatch = !highPriorityPendingList.isEmpty();
		boolean hasLowPriorityMatch = !lowPriorityMatchList.isEmpty();
		if (hasHighPriorityMatch)
		{
			int index = random.nextInt(highPriorityPendingList.size());
			selectDistributionCenter( highPriorityPendingList.get( index ) );
		}
		else if (hasLowPriorityMatch)
		{
			int index = random.nextInt(lowPriorityMatchList.size());
			selectDistributionCenter( lowPriorityMatchList.get( index ) );
		}
	}


	//Select or reselect a D
	private void selectDistributionCenter( DistributionCenter distributionCenter )
	{
		if (distributionCenter == null)
		{
			clearSelectedDistributionCenter();
			return;
		}
		if ( selectedDistributionCenter == distributionCenter)
		{
			return;
		}

		boolean canAdd = distributionCenter.addStore(this );
		if (canAdd)
		{
			if ( selectedDistributionCenter != null)
			{
				selectedDistributionCenter.removeStore(this );
			}
			this.selectedDistributionCenter = distributionCenter;
		}
	}


	public void clearSelectedDistributionCenter()
	{
		if ( selectedDistributionCenter != null)
		{
			selectedDistributionCenter.removeStore(this );
			selectedDistributionCenter = null;
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
			if (d.canAddStore(this ))
			{
				if (nearestD == null || this.distanceTo(d) < this.distanceTo(nearestD))
				{
					nearestD = d;
				}
			}
		}
		if (nearestD != null)
		{
			selectDistributionCenter(nearestD );
			return true;
		}

		nearestD = null;
		for ( DistributionCenter d : this.lowPriorityMatchList)
		{
			if (d.canAddStore(this ))
			{
				if (nearestD == null || this.distanceTo(d) < this.distanceTo(nearestD))
				{
					nearestD = d;
				}
			}
		}
		if (nearestD != null)
		{
			selectDistributionCenter(nearestD );
			return true;
		}

		return false;
	}

	private DistributionCenter originalSelectedIndex = null;


	public void save()
	{
		originalSelectedIndex = selectedDistributionCenter;
	}


	public void rollback()
	{
		selectDistributionCenter(originalSelectedIndex );
	}

//	static class DComparator implements Comparator<DistributionCenter>
//	{
//		@Override
//		public int compare( DistributionCenter o1, DistributionCenter o2 )
//		{
//			double res = o1.remainingCapacity() - o2.remainingCapacity();
//			if (res < 0.0)
//			{
//				return -1;
//			}
//			else if (res > 0.0)
//			{
//				return 1;
//			}
//			return 0;
//		}
//	}

}
