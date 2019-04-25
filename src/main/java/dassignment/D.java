/*******************************************************************************
 * Copyright (c) 2018 Lei Zhang.
 ******************************************************************************/

package dassignment;

import java.util.HashSet;

public class D
{
	public D(String id, double x, double y, String labelO, String labelH, String labelP, double capacity)
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

	private String id;
	private double x;
	private double y;
	private String labelO;
	private String labelH;
	private String labelP;
	private double capacity;
	private double usedCapacity;
	private HashSet<S> includedS = new HashSet<>();

	
	//Whether it is possible to add S according to capacity
	public boolean canAddS(S s)
	{
		if (s == null)
		{
			return false;
		}
		
		if (s.getDemand() > this.remainingCapacity())
		{
			return false;
		}
		return true;
	}

	
	public boolean addS(S s)
	{
		if (!canAddS(s))
		{
			return false;
		}
		usedCapacity += s.getDemand();
		includedS.add(s);
		return true;
	}

	
	public boolean removeS(S s)
	{
		if (s == null)
		{
			return false;
		}
		if (!includedS.contains(s))
		{
			return false;
		}
		usedCapacity -= s.getDemand();
		includedS.remove(s);
		return true;
	}

	
	public double remainingCapacity()
	{
		return capacity - usedCapacity;
	}

	
	public void useCapacity(double amount)
	{
		usedCapacity += amount;
	}

	
	public void releaseCapacity(double amount)
	{
		usedCapacity -= amount;
	}

	public double getUsedCapacity()
	{
		return usedCapacity;
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

	public double getCapacity()
	{
		return capacity;
	}

	public void setCapacity(double capacity)
	{
		this.capacity = capacity;
	}
}
