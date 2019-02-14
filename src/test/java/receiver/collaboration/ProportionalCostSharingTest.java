/* *********************************************************************** *
 * project: org.matsim.*
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

package receiver.collaboration;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.contrib.freight.carrier.*;
import org.matsim.contrib.freight.utils.FreightUtils;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.testcases.MatsimTestUtils;

import receiver.Receiver;
import receiver.ReceiverUtils;
import receiver.product.Order;
import receiver.usecases.chessboard.BaseReceiverChessboardScenario;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

public class ProportionalCostSharingTest {
    @Rule
    public MatsimTestUtils utils = new MatsimTestUtils();
    private Scenario sc;


    @Test
    public void test() {
        setup();

        Carrier carrier = ReceiverUtils.getCarriers(sc).getCarriers().get(Id.create("Carrier1", Carrier.class));
        double carrierCost = carrier.getSelectedPlan().getScore();

        double total = 0.0;
        for (Receiver receiver : ReceiverUtils.getReceivers(sc).getReceivers().values()) {
            for (Order order : Objects.requireNonNull(receiver.getSelectedPlan().getReceiverOrder(Id.create("Carrier1", Carrier.class))).getReceiverProductOrders()) {
                total += order.getOrderQuantity() * order.getProduct().getProductType().getRequiredCapacity();
            }
        }
        Receiver receiverOne = ReceiverUtils.getReceivers(sc).getReceivers().get(Id.create("1", Receiver.class));
        double receiverOneTotal = 0.0;
        for (Order order : Objects.requireNonNull(receiverOne.getSelectedPlan().getReceiverOrder(Id.create("Carrier1", Carrier.class))).getReceiverProductOrders()) {
            receiverOneTotal += order.getOrderQuantity() * order.getProduct().getProductType().getRequiredCapacity();
        }

        double pOne = receiverOneTotal / total;

        ProportionalCostSharing pcs = new ProportionalCostSharing(350, sc);
        pcs.allocateCoalitionCosts();
        Id<Receiver> r1Id = Id.create("1", Receiver.class);
        Assert.assertEquals("Wrong cost allocated to receiver 1",
                pOne * carrierCost,
                ReceiverUtils.getReceivers(sc).getReceivers().get(r1Id).getSelectedPlan().getScore(),
                MatsimTestUtils.EPSILON);
    }

    private void setup() {
        this.sc = BaseReceiverChessboardScenario.createChessboardScenario(1L, 1, 5, false);

        /* Manipulate the carrier as it has no plan or score without a simulation run. */
        Carrier carrier = ReceiverUtils.getCarriers(this.sc).getCarriers().get(Id.create("Carrier1", Carrier.class));
        CarrierPlan plan = new CarrierPlan(carrier, new ArrayList<>());
        plan.setScore(-10000.);
        carrier.setSelectedPlan(plan);
    }

}
