package cascadingInfoTest;

import java.util.ArrayList;
import java.util.Collection;

import org.matsim.contrib.freight.events.LSPServiceStartEvent;
import org.matsim.contrib.freight.eventhandler.LSPServiceStartEventHandler;
import org.matsim.contrib.freight.events.LSPTourStartEvent;
import org.matsim.contrib.freight.eventhandler.LSPTourStartEventHandler;



public class TimeSpanHandler implements LSPTourStartEventHandler, LSPServiceStartEventHandler {

	private int numberOfStops;
	private double totalTime;
	
	private Collection<LSPTourStartEvent> startEvents;
	
	public TimeSpanHandler() {
		startEvents = new ArrayList<LSPTourStartEvent>();
	}
	
	@Override
	public void reset(int iteration) {
		totalTime = 0;
		numberOfStops = 0;
	}

	@Override
	public void handleEvent(LSPServiceStartEvent event) {
		numberOfStops++;
		for(LSPTourStartEvent startEvent : startEvents) {
			if(startEvent.getDriverId() == event.getDriverId()) {
				double startTime = startEvent.getTime();
				double serviceTime = event.getTime();
				totalTime = totalTime + (serviceTime - startTime);
				break;
			}
		}
	}

	@Override
	public void handleEvent(LSPTourStartEvent event) {
		startEvents.add(event);
	}

	public int getNumberOfStops() {
		return numberOfStops;
	}

	public double getTotalTime() {
		return totalTime;
	}

}
