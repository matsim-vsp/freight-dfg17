package cascadingInfoTest;

import java.util.ArrayList;
import java.util.Collection;

import org.matsim.core.controler.events.AfterMobsimEvent;
import org.matsim.core.events.handler.EventHandler;

import lsp.LSPInfo;
import lsp.controler.LSPSimulationTracker;



public class AverageTimeTracker implements LSPSimulationTracker{

	private final Collection<EventHandler> handlers;
	private final Collection<LSPInfo> infos;
	private final AverageTimeInfo timeInfo;
	private final TimeSpanHandler handler;
	
	public AverageTimeTracker() {
		handlers = new ArrayList<>();
		handler = new TimeSpanHandler();
		handlers.add(handler);
		infos = new ArrayList<>();
		timeInfo = new AverageTimeInfo();
		infos.add(timeInfo);
	}
	
	
	@Override
	public Collection<EventHandler> getEventHandlers() {
		return handlers;
	}

	@Override
	public Collection<LSPInfo> getInfos() {
		return infos;
	}

	@Override
	public void notifyAfterMobsim(AfterMobsimEvent event) {
		int numberOfStops = handler.getNumberOfStops();
		double totalTransportTime = handler.getTotalTime();
		double averageTransportTime = totalTransportTime/numberOfStops;
//		AverageTimeInfoFunctionValue value =  (AverageTimeInfoFunctionValue) timeInfo.getAttributes().getAttributes().iterator().next();
//		value.setValue(averageTransportTime);

		timeInfo.setAverageTime( averageTransportTime );

	}


	@Override
	public void reset() {
		// TODO Auto-generated method stub
		
	}
}
