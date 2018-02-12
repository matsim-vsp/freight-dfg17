package controler;

import org.matsim.contrib.freight.CarrierConfig;
import org.matsim.core.controler.AbstractModule;

import com.google.inject.Provides;

import lsp.LSPs;
import mobsim.CarrierResourceTracker;
import mobsim.FreightQSimFactory;
import replanning.LSPReplanningModule;
import scoring.LSPScoringModule;




public class LSPModule extends AbstractModule {

	
	private LSPs lsps;
	private LSPReplanningModule replanningModule;
	private LSPScoringModule scoringModule;
	
	private CarrierConfig carrierConfig = new CarrierConfig();
	
	public LSPModule(LSPs  lsps, LSPReplanningModule replanningModule, LSPScoringModule scoringModule) {
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
       
        bind(LSPControlerListener.class).asEagerSingleton();
        addControlerListenerBinding().to(LSPControlerListener.class);
        bindMobsim().toProvider(FreightQSimFactory.class);
	}

	@Provides
    CarrierResourceTracker provideCarrierResourceTracker(LSPControlerListener lSPControlerListener) {
        return lSPControlerListener.getCarrierResourceTracker();
    }

    public void setPhysicallyEnforceTimeWindowBeginnings(boolean physicallyEnforceTimeWindowBeginnings) {
        this.carrierConfig.setPhysicallyEnforceTimeWindowBeginnings(physicallyEnforceTimeWindowBeginnings);
    }

}
