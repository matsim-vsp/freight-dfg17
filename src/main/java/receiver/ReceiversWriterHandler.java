/* *********************************************************************** *
 * project: org.matsim.*
 * ReceiversWriterHandler.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2009 by the members listed in the COPYING,        *
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

/**
 * 
 */
package receiver;

import java.io.BufferedWriter;
import java.io.IOException;

import org.matsim.contrib.freight.carrier.TimeWindow;

import receiver.Receiver;
import receiver.ReceiverPlan;
import receiver.Receivers;
import receiver.product.Order;
import receiver.product.ProductType;
import receiver.product.ReceiverOrder;
import receiver.product.ReceiverProduct;
import receiver.ReorderPolicy;

/**
 *
 * @author jwjoubert
 */
public interface ReceiversWriterHandler {

	/* <freightReceivers> ... </freightReceivers> */
	public void startReceivers(final Receivers receivers, final BufferedWriter out) throws IOException;
	public void endReceivers(final BufferedWriter out) throws IOException;

	/* <products> ... </products> */
	public void startProducts(final BufferedWriter out) throws IOException;
	public void endProducts(final BufferedWriter out) throws IOException;

	/* <product> ... </product> */
	public void startProduct(final ProductType product, final BufferedWriter out) throws IOException;
	public void endProduct(final BufferedWriter out) throws IOException;
	
	/* <receiver> ... </receiver> */
	public void startReceiver(final Receiver receiver, final BufferedWriter out) throws IOException;
	public void endReceiver(final BufferedWriter out) throws IOException;

	/* <timeWindow> ... </timeWindow> */
	public void startTimeWindow(final TimeWindow window, final BufferedWriter out) throws IOException;
	public void endTimeWindow(final BufferedWriter out) throws IOException;
	
	/* <product> ... </product> */
	public void startReceiverProduct(final ReceiverProduct product, final BufferedWriter out) throws IOException;
	public void endReceiverProduct(final BufferedWriter out) throws IOException;

	/* <reorderPolicy> ... </reorderPolicy> */
	public void startReorderPolicy(final ReorderPolicy policy, final BufferedWriter out) throws IOException;
	public void endReorderPolicy(final BufferedWriter out) throws IOException;
	
	/* <plan> ... </plan> */
	public void startPlan(final ReceiverPlan plan, final BufferedWriter out) throws IOException;
	public void endPlan(final BufferedWriter out) throws IOException;
	
	/* <order> ... </order> */
	public void startOrder(final ReceiverOrder order, final BufferedWriter out) throws IOException;
	public void endOrder(final BufferedWriter out) throws IOException;
	
	/* <item> ... </item> */
	public void startItem(final Order item, final BufferedWriter out) throws IOException;
	public void endItem(final BufferedWriter out) throws IOException;
	
	/*TODO <route ... > */

	public void writeSeparator(final BufferedWriter out) throws IOException;

}
