package lsp.replanning;

import org.matsim.core.controler.events.ReplanningEvent;
import org.matsim.core.controler.listener.ReplanningListener;

public interface LSPReplanningModule extends ReplanningListener{

	void replanLSPs(ReplanningEvent arg0);

}
