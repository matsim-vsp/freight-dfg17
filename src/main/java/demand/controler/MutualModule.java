package demand.controler;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import demand.demandObject.DemandObjects;
import demand.mutualReplanning.MutualReplanningModule;
import demand.scoring.MutualScoringModule;
import org.apache.log4j.Logger;
import org.matsim.contrib.freight.controler.LSPAgentSource;
import org.matsim.contrib.freight.events.eventsCreator.LSPEventCreator;
import org.matsim.contrib.freight.controler.CarrierAgentTracker;
import org.matsim.contrib.freight.FreightConfigGroup;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.mobsim.qsim.AbstractQSimModule;
import org.matsim.core.mobsim.qsim.components.QSimComponentsConfigGroup;

import java.util.Collection;
import java.util.List;

public class MutualModule extends AbstractModule{
	private static final Logger log = Logger.getLogger( MutualModule.class );

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
		FreightConfigGroup freightConfig = ConfigUtils.addOrGetModule( getConfig(), FreightConfigGroup.class );

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

		// replace ...

//		bindMobsim().toProvider( LSPQSimFactory.class );

		// ... by ...

		// this switches on certain qsim components:
		QSimComponentsConfigGroup qsimComponents = ConfigUtils.addOrGetModule( getConfig(), QSimComponentsConfigGroup.class );
		List<String> abc = qsimComponents.getActiveComponents();
		abc.add( LSPAgentSource.COMPONENT_NAME ) ;
		switch ( freightConfig.getTimeWindowHandling() ) {
			case ignore:
				break;
			case enforceBeginnings:
//				abc.add( WithinDayActivityReScheduling.COMPONENT_NAME );
				log.warn("LSP has never hedged against time window openings; this is probably wrong; but I don't know what to do ...");
				break;
			default:
				throw new IllegalStateException( "Unexpected value: " + freightConfig.getTimeWindowHandling() );
		}
		qsimComponents.setActiveComponents( abc );

		// this installs qsim components, which are switched on (or not) via the above syntax:
		this.installQSimModule( new AbstractQSimModule(){
			@Override protected void configureQSim(){
				this.bind( LSPAgentSource.class ).in( Singleton.class );
				this.addQSimComponentBinding( LSPAgentSource.COMPONENT_NAME ).to( LSPAgentSource.class );
				switch( freightConfig.getTimeWindowHandling() ) {
					case ignore:
						break;
					case enforceBeginnings:
//						this.addQSimComponentBinding(WithinDayActivityReScheduling.COMPONENT_NAME).to( WithinDayActivityReScheduling.class );
						log.warn("LSP has never hedged against time window openings; this is probably wrong; but I don't know what to do ...");
						break;
					default:
						throw new IllegalStateException( "Unexpected value: " + freightConfig.getTimeWindowHandling() );
				}
			}
		} );

		// ... up to here.  kai, sep'20

	}

	@Provides
	Collection<LSPEventCreator> provideEventCreators(){
		return this.creators;
	}

	@Provides
	CarrierAgentTracker provideCarrierResourceTracker( MutualControlerListener mutualControlerListener ) {
		return mutualControlerListener.getCarrierResourceTracker();
	}

}
