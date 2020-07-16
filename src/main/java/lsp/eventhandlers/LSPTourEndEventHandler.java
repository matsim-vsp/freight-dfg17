package lsp.eventhandlers;

import lsp.events.LSPTourEndEvent;
import org.matsim.core.events.handler.EventHandler;


public interface LSPTourEndEventHandler extends EventHandler {

	public void handleEvent( LSPTourEndEvent event );

}
