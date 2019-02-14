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

import org.matsim.api.core.v01.Scenario;
import org.matsim.contrib.freight.usecases.analysis.CarrierScoreStats;
import org.matsim.core.controler.AbstractModule;

import receiver.replanning.*;
import receiver.usecases.ReceiverScoreStats;
import receiver.usecases.UsecasesReceiverScoringFunctionFactory;

public final class ReceiverModule extends AbstractModule {

    private final ReceiverReplanningType type;

    //TODO Get from ConfigGroup
    private boolean createPNG = true;


    public ReceiverModule(ReceiverReplanningType replanningType) {
        this.type = replanningType;
    }


    @Override
    public void install() {

        /* Carrier */
        this.addControlerListenerBinding().to( ReceiverResponseCarrierReplanning.class);


        /* Receiver FIXME at thos point the strategies are mutually exclusive. That is, only one allowed. */
        bind(ReceiverScoringFunctionFactory.class).toInstance(new UsecasesReceiverScoringFunctionFactory());
        switch (this.type) {
            case timeWindow:
                bind(ReceiverOrderStrategyManagerFactory.class).toInstance( new TimeWindowReceiverOrderStrategyManagerImpl() );
                break ;
            case serviceTime:
                bind(ReceiverOrderStrategyManagerFactory.class).toInstance( new ServiceTimeReceiverOrderStrategyManagerImpl() );
                break ;
            case orderFrequency:
                bind(ReceiverOrderStrategyManagerFactory.class).toInstance( new NumDelReceiverOrderStrategyManagerImpl() );
                break;
            default:
                throw new RuntimeException("No valid (receiver) order strategy manager selected." );
        }
        addControlerListenerBinding().to(ReceiverControlerListener.class);


        /* Statistics and output */
//        CarrierScoreStats scoreStats = new CarrierScoreStats( ReceiverUtils.getCarriers( controler.getScenario() ), controler.getScenario().getConfig().controler().getOutputDirectory() + "/carrier_scores", this.createPNG);
//        ReceiverScoreStats rScoreStats = new ReceiverScoreStats();
//        addEventHandlerBinding().toInstance( new ReceiverScoreStats() );
//        addControlerListenerBinding().to(ReceiverScoreStats.class);

    }


    public boolean isCreatePNG() {
        return createPNG;
    }

    public void setCreatePNG(boolean createPNG) {
        this.createPNG = createPNG;
    }
}


