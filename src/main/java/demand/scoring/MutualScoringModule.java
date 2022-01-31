package demand.scoring;

import org.matsim.core.controler.events.ScoringEvent;
import org.matsim.core.controler.listener.ScoringListener;

public interface MutualScoringModule extends ScoringListener{
	
	void scoreDemandObjects(ScoringEvent event);
	void scoreLSPs(ScoringEvent event);
}
