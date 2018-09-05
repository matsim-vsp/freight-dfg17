package lsp.resources;

import java.util.Collection;
import java.util.Map;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.events.handler.EventHandler;

import lsp.LogisticsSolutionElement;
import lsp.functions.Info;
import lsp.tracking.SimulationTracker;

public interface Resource {

	public Id<Resource> getId();
	
	public Id<Link> getStartLinkId();
	
	public Class<?> getClassOfResource();
	// yyyyyy is it really necessary to use reflection in a code that we fully own?  kai, may'18
	//One could also leave this method signature out tm, august'18
	
	public Id<Link> getEndLinkId();
	
    public Collection <LogisticsSolutionElement> getClientElements();
    
    public void schedule(int bufferTime);
    
    public Collection <EventHandler> getEventHandlers();
    
    public Collection <Info> getInfos();
    
    public void addSimulationTracker(SimulationTracker tracker);
    
    public Collection<SimulationTracker> getSimulationTrackers();
    
    public void setEventsManager(EventsManager eventsManager);
   
}
