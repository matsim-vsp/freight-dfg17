package lsp.events;

import java.util.ArrayList;
import java.util.Collection;

public class EventUtils {

	public static Collection<EventCreator> getStandardEventCreators(){
		ArrayList<EventCreator> creators = new ArrayList<>();
		creators.add(new FreightLinkEnterEventCreator());
		creators.add(new FreightLinkLeaveEventCreator());
		creators.add(new FreightVehicleLeavesTrafficEventCreator());
		creators.add(new ServiceEndEventCreator());
		creators.add(new ServiceStartEventCreator());
		creators.add(new ShipmentDeliveredEventCreator());
		creators.add(new ShipmentPickedUpEventCreator());
		creators.add(new TourEndEventCreator());
		creators.add(new TourStartEventCreator());
		return creators;
	}
	
}
