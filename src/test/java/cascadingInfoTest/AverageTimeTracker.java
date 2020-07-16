package cascadingInfoTest;

import java.util.ArrayList;
import java.util.Collection;

import org.matsim.core.controler.events.AfterMobsimEvent;
import org.matsim.core.events.handler.EventHandler;

import lsp.functions.LSPInfo;
import lsp.controler.LSPSimulationTracker;



public class AverageTimeTracker implements LSPSimulationTracker{

	private Collection<EventHandler> handlers;
	private Collection<LSPInfo> infos;
	private AverageTimeInfo timeInfo;
	private TimeSpanHandler handler;
	
	public AverageTimeTracker() {
		handlers = new ArrayList<EventHandler>();
		handler = new TimeSpanHandler();
		handlers.add(handler);
		infos = new ArrayList<LSPInfo>();
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
		AverageTimeInfoFunctionValue value =  (AverageTimeInfoFunctionValue) timeInfo.getFunction().getValues().iterator().next();
		value.setValue(averageTransportTime);
	}


	@Override
	public void reset() {
		// TODO Auto-generated method stub
		
	}
}
