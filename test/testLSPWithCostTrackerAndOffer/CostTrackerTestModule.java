package testLSPWithCostTrackerAndOffer;

import org.matsim.contrib.freight.CarrierConfig;
import org.matsim.core.controler.AbstractModule;

import com.google.inject.Provides;

import lsp.LSPs;
import lsp.controler.LSPControlerListener;
import lsp.mobsim.CarrierResourceTracker;
import lsp.mobsim.FreightQSimFactory;
import lsp.replanning.LSPReplanningModule;
import lsp.scoring.LSPScoringModule;

public class CostTrackerTestModule extends AbstractModule{

	private LSPs lsps;
	private LSPReplanningModule replanningModule;
	private LSPScoringModule scoringModule;
	
	private CarrierConfig carrierConfig = new CarrierConfig();
	
	public CostTrackerTestModule(LSPs  lsps, LSPReplanningModule replanningModule, LSPScoringModule scoringModule) {
	   this.lsps = lsps;
	   this.replanningModule = replanningModule;
	   this.scoringModule = scoringModule;
	}    
	   	  
		    
	@Override
	public void install() {
		bind(CarrierConfig.class).toInstance(carrierConfig);
		bind(LSPs.class).toInstance(lsps);
        if(replanningModule != null) {
        	bind(LSPReplanningModule.class).toInstance(replanningModule);
        }
		if(scoringModule != null) {
			 bind(LSPScoringModule.class).toInstance(scoringModule);
		}
       
        bind(CostTrackerControlerListener.class).asEagerSingleton();
        addControlerListenerBinding().to(CostTrackerControlerListener.class);
        bindMobsim().toProvider(FreightQSimFactory.class);
	}

	@Provides
    CarrierResourceTracker provideCarrierResourceTracker(CostTrackerControlerListener lSPControlerListener) {
        return lSPControlerListener.getCarrierResourceTracker();
    }

    public void setPhysicallyEnforceTimeWindowBeginnings(boolean physicallyEnforceTimeWindowBeginnings) {
        this.carrierConfig.setPhysicallyEnforceTimeWindowBeginnings(physicallyEnforceTimeWindowBeginnings);
    }

}
