/*******************************************************************************
 * Copyright (c) 2018 Lei Zhang.
 ******************************************************************************/

package dassignment;

import java.util.HashSet;

class DistributionCenter
{
	public DistributionCenter( String id, double x, double y, String labelO, String labelH, String labelP, double capacity )
	{
		super();
		this.id = id;
		this.x = x;
		this.y = y;
		this.labelO = labelO;
		this.labelH = labelH;
		this.labelP = labelP;
		this.capacity = capacity;
	}

	private final String id;
	private final double x;
	private final double y;
	private final String labelO;
	private final String labelH;
	private final String labelP;
	private final double capacity;
	private double usedCapacity;
	private final HashSet<Store> includedS = new HashSet<>();


	//Whether it is possible to add S according to capacity
	public boolean canAddStore( Store store )
	{
		if (store == null)
		{
			return false;
		}

		return !(store.getDemand() > this.remainingCapacity());
	}


	public boolean addStore( Store store )
	{
		if (!canAddStore(store ))
		{
			return false;
		}
		usedCapacity += store.getDemand();
		includedS.add(store);
		return true;
	}


	public void removeStore( Store store )
	{
		if (store == null)
		{
			return;
		}
		if (!includedS.contains(store))
		{
			return;
		}
		usedCapacity -= store.getDemand();
		includedS.remove(store);
	}


	public double remainingCapacity()
	{
		return capacity - usedCapacity;
	}


//	public void useCapacity(double amount)
//	{
//		usedCapacity += amount;
//	}
//
//	public void releaseCapacity(double amount)
//	{
//		usedCapacity -= amount;
//	}
//
//	public double getUsedCapacity()
//	{
//		return usedCapacity;
//	}

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

	public double getCapacity()
	{
		return capacity;
	}

//	public void setCapacity(double capacity)
//	{
//		this.capacity = capacity;
//	}
}
