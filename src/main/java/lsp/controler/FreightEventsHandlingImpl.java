package lsp.controler;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.groups.ControlerConfigGroup;
import org.matsim.core.config.groups.ControlerConfigGroup.EventsFileFormat;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.controler.corelisteners.EventsHandling;
import org.matsim.core.controler.events.BeforeMobsimEvent;
import org.matsim.core.controler.events.IterationEndsEvent;
import org.matsim.core.controler.events.ShutdownEvent;
import org.matsim.core.controler.listener.BeforeMobsimListener;
import org.matsim.core.controler.listener.IterationEndsListener;
import org.matsim.core.controler.listener.ShutdownListener;
import org.matsim.core.events.algorithms.EventWriter;
import org.matsim.core.events.algorithms.EventWriterXML;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class FreightEventsHandlingImpl implements EventsHandling, BeforeMobsimListener,
IterationEndsListener, ShutdownListener {

private final EventsManager eventsManager;
private List<EventWriter> eventWriters = new LinkedList<>();

private int writeEventsInterval;

private Set<EventsFileFormat> eventsFileFormats ;

private OutputDirectoryHierarchy controlerIO ;

private int writeMoreUntilIteration;

@Inject
FreightEventsHandlingImpl(
		@Named("Freight") EventsManager eventsManager,
		final ControlerConfigGroup config,
		final OutputDirectoryHierarchy controlerIO) {
	this.eventsManager = eventsManager;
	this.writeEventsInterval = config.getWriteEventsInterval();
	this.eventsFileFormats = config.getEventsFileFormats();
	this.controlerIO = controlerIO;
	this.writeMoreUntilIteration = config.getWriteEventsUntilIteration() ;
}

@Override
public void notifyBeforeMobsim(BeforeMobsimEvent event) {
	eventsManager.resetHandlers(event.getIteration());
	final boolean writingEventsAtAll = this.writeEventsInterval > 0;
	final boolean regularWriteEvents = writingEventsAtAll && (event.getIteration()>0 && event.getIteration() % writeEventsInterval == 0);
	final boolean earlyIteration = event.getIteration() <= writeMoreUntilIteration ;
	if (writingEventsAtAll && (regularWriteEvents||earlyIteration) ) {
		for (EventsFileFormat format : eventsFileFormats) {
			switch (format) {
			case xml:
				String outputFileName = controlerIO.getIterationFilename(event.getIteration(), 
						Controler.FILENAME_EVENTS_XML);
				String freightOutputFileName = outputFileName.replaceAll("events", "freightEvents");
				this.eventWriters.add(new EventWriterXML(freightOutputFileName));
				break;
			default:
				
			}
		}
		for (EventWriter writer : this.eventWriters) {
			eventsManager.addHandler(writer);
		}
	}
}

@Override
public void notifyIterationEnds(IterationEndsEvent event) {
	/*
	 * Events that are produced after the Mobsim has ended, e.g. by the RoadProcing 
	 * module, should also be written to the events file.
	 */
	for (EventWriter writer : this.eventWriters) {
		writer.closeFile();
		this.eventsManager.removeHandler(writer);
	}
	this.eventWriters.clear();
}

@Override
public void notifyShutdown(ShutdownEvent event) {
	for (EventWriter writer : this.eventWriters) {
		writer.closeFile();
	}
}

}

