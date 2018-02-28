package lsp.shipment;

import java.util.Comparator;

public class AbstractShipmentPlanElementComparator implements Comparator<AbstractShipmentPlanElement>{

	public int compare(AbstractShipmentPlanElement o1, AbstractShipmentPlanElement o2) {
		if(o1.getStartTime() > o2.getStartTime()){
			return 1;	
		}
		if(o1.getStartTime() < o2.getStartTime()){
			return -1;
		}
		else{
			return 0;
		}	
	}	

}
