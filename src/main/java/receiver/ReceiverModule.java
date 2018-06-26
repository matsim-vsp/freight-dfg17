/* *********************************************************************** *
// * project: org.matsim.*
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2018 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

package receiver;

import org.matsim.core.controler.AbstractModule;

import receiver.controler.ReceiverControlerListener;
import receiver.replanning.ReceiverOrderStrategyManagerFactory;
import receiver.scoring.ReceiverScoringFunctionFactory;

public class ReceiverModule extends AbstractModule {

	private Receivers receivers;
	ReceiverScoringFunctionFactory sFuncFac;
	private ReceiverOrderStrategyManagerFactory stratManFac;
	private FreightScenario fsc;
	
	
	public ReceiverModule(Receivers receivers, ReceiverScoringFunctionFactory sFuncFac, ReceiverOrderStrategyManagerFactory stratManFac, FreightScenario fsc){
		this.receivers = receivers;
		this.sFuncFac = sFuncFac;
		this.stratManFac = stratManFac;
		this.fsc = fsc;
	};
	
	
	
	@Override
	public void install() {

		
		bind(Receivers.class).toInstance(receivers);
		
		if (sFuncFac != null){
		bind(ReceiverScoringFunctionFactory.class).toInstance(sFuncFac);
		}
		
		if (stratManFac != null){
		bind(ReceiverOrderStrategyManagerFactory.class).toInstance(stratManFac);
		}
		
		if (fsc != null){
		bind(FreightScenario.class).toInstance(fsc);
		}
		
		/*
		 * Need a controler listener. Keeping it simple for now.
		 */
		//bind(ReceiverControlerListener.class).asEagerSingleton();
		addControlerListenerBinding().to(ReceiverControlerListener.class);
		
    }
       
		
}
