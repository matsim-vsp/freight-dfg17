/* *********************************************************************** *
 * project: org.matsim.*
 * RecieverWriter.java
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

import java.io.IOException;

import org.apache.log4j.Logger;
import org.matsim.contrib.freight.carrier.TimeWindow;
import org.matsim.core.api.internal.MatsimWriter;
import org.matsim.core.utils.io.MatsimXmlWriter;
import org.matsim.core.utils.io.UncheckedIOException;
import org.matsim.core.utils.misc.Counter;

import receiver.product.Order;
import receiver.product.ProductType;
import receiver.product.ReceiverOrder;
import receiver.product.ReceiverProduct;

/**
 * Writes a {@link Receivers} container in the MATSim XML format.
 * 
 * @author jwjoubert
 */
public class ReceiversWriter extends MatsimXmlWriter implements MatsimWriter{
	final private Logger log = Logger.getLogger(ReceiversWriter.class);
	final private Receivers receivers;
	final private Counter counter = new Counter("   receiver # ");

	public ReceiversWriter(Receivers receivers) {
		super();
		this.receivers = receivers;
	}
	
	
	@Override
	public void write(String filename) {
		log.info("Writing receivers to file: " + filename);
		writeV1(filename);
	}
	
	
	public void writeV1(String filename) {
		String dtd = "http://matsim.org/files/dtd/freightReceivers_v1.dtd";
		ReceiversWriterHandler handler = new ReceiversWriterHandlerImpl_v1();

		try {
			openFile(filename);
			writeXmlHead();
			writeDoctype("freightReceivers", dtd);	
			
			handler.startReceivers(this.receivers, writer);
			
			/* Write all the product types */
			if(!receivers.getAllProductTypes().isEmpty()) {
				handler.startProducts(writer);
				
				for(ProductType type : receivers.getAllProductTypes()) {
					handler.startProduct(type, writer);
					handler.endProduct(writer);
				}
				handler.endProducts(writer);
				handler.writeSeparator(writer);
			}

			/* Write the receivers. */
			for(Receiver receiver : this.receivers.getReceivers().values()) {
				handler.startReceiver(receiver, writer);
				
				/* Write the products, if there are any. */
				if(!receiver.getProducts().isEmpty()) {
					for(ReceiverProduct product : receiver.getProducts()) {
						handler.startReceiverProduct(product, writer);
						handler.startReorderPolicy(product.getReorderPolicy(), writer);
						handler.endReorderPolicy(writer);
						handler.endReceiverProduct(writer);
					}
				}
				
				/* Build receiver orders. */
				for(ReceiverPlan plan : receiver.getPlans()) {
					handler.startPlan(plan, writer);
					
					/* Write the time windows, if there are any. */
					if(plan.getTimeWindows().size() > 0) {
						for(TimeWindow tw : plan.getTimeWindows()) {
							handler.startTimeWindow(tw, writer);
							handler.endTimeWindow(writer);
						}
					}
					
					for(ReceiverOrder order : plan.getReceiverOrders()) {
						handler.startOrder(order, writer);
						for(Order item : order.getReceiverProductOrders()) {
							handler.startItem(item, writer);
							handler.endItem(writer);
						}
						handler.endOrder(writer);
					}
					handler.endPlan(writer);
				}
				
				handler.endReceiver(writer);
				counter.incCounter();
				
				handler.writeSeparator(writer);
			}
			counter.printCounter();
			handler.endReceivers(this.writer);
			this.writer.close();
		} catch (IOException e){
			throw new UncheckedIOException(e);
		}

	}

}

