package demand.controler;

import com.google.inject.Provides;
import demand.demandObject.DemandObjects;
import demand.mutualReplanning.MutualReplanningModule;
import demand.scoring.MutualScoringModule;
import org.matsim.contrib.freight.events.LSPEventCreator;
import org.matsim.contrib.freight.controler.LSPCarrierTracker;
import lsp.controler.LSPQSimFactory;
import org.matsim.contrib.freight.FreightConfigGroup;
import org.matsim.core.controler.AbstractModule;

import java.util.Collection;

public class MutualModule extends AbstractModule{

	private LSPDecorators lsps;
	private DemandObjects demandObjects;
	private MutualScoringModule mutualScoringModule;
	private MutualReplanningModule replanningModule;	
	private FreightConfigGroup carrierConfig = new FreightConfigGroup();
	private Collection<LSPEventCreator> creators;
	
	public static class Builder{
		
		private LSPDecorators lsps;
		private DemandObjects demandObjects;
		private MutualScoringModule mutualScoringModule;
		private MutualReplanningModule replanningModule;
		private Collection<LSPEventCreator> creators;
		
		public static Builder newInstance() {
			return new Builder();
		}
		
		public Builder setLsps(LSPDecorators lsps) {
			this.lsps = lsps;
			return this;
		}
	
		public Builder setMutualScoringModule(MutualScoringModule demandScoringModule) {
			this.mutualScoringModule = demandScoringModule;
			return this;
		}
				
		public Builder setMutualReplanningModule(MutualReplanningModule replanningModule) {
			this.replanningModule = replanningModule;
			return this;
		}
		
		public Builder setDemandObjects(DemandObjects demandObjects) {
			this.demandObjects = demandObjects;
			return this;
		}
		
		public Builder setEventCreators(Collection<LSPEventCreator> creators) {
			this.creators = creators;
			return this;
		}
		
		public MutualModule build() {
			return new MutualModule(this);
		}
	}
	
	private MutualModule(Builder builder) {
		this.lsps = builder.lsps;
		this.demandObjects = builder.demandObjects;
		this.mutualScoringModule = builder.mutualScoringModule;
		this.replanningModule = builder.replanningModule;
		this.creators = builder.creators; 
	}
	
	
	@Override
	public void install() {
			bind(FreightConfigGroup.class).toInstance(carrierConfig);
			bind(LSPDecorators.class).toInstance(lsps);
			bind(DemandObjects.class).toInstance(demandObjects);
			
			if(replanningModule != null) {
	        	bind(MutualReplanningModule.class).toInstance(replanningModule);
	        }
			if(mutualScoringModule != null) {
				 bind(MutualScoringModule.class).toInstance(mutualScoringModule);
			}
			bind(MutualControlerListener.class).asEagerSingleton();
	        addControlerListenerBinding().to(MutualControlerListener.class);
	        bindMobsim().toProvider( LSPQSimFactory.class );
		
	}

	@Provides
	Collection<LSPEventCreator> provideEventCreators(){
		return this.creators;
	}
	
	@Provides
	LSPCarrierTracker provideCarrierResourceTracker( MutualControlerListener mutualControlerListener ) {
        return mutualControlerListener.getCarrierResourceTracker();
    }

}
