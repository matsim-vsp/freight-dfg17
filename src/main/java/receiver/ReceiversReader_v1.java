/* *********************************************************************** *
 * project: org.matsim.*
 * ReceiversReader_v1.java
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

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.matsim.api.core.v01.Id;
import org.matsim.contrib.freight.carrier.Carrier;
import org.matsim.contrib.freight.carrier.TimeWindow;
import org.matsim.core.api.internal.MatsimReader;
import org.matsim.core.utils.io.MatsimXmlParser;
import org.matsim.core.utils.misc.Counter;
import org.matsim.core.utils.misc.Time;
import org.matsim.utils.objectattributes.attributable.AttributesXmlReaderDelegate;
import org.xml.sax.Attributes;

import receiver.product.Order;
import receiver.product.ProductType;
import receiver.product.ReceiverOrder;
import receiver.product.ReceiverProduct;

/**
 * Implementation to read version 1 {@link Receivers}.
 * 
 * @author jwjoubert
 */
 class ReceiversReader_v1 extends MatsimXmlParser implements MatsimReader {
	private final static String RECEIVERS = "freightReceivers";
	private final static String ATTRIBUTES = "attributes";
	private final static String ATTRIBUTE = "attribute";
	private final static String PRODUCT_TYPES = "productTypes";
	private final static String PRODUCT_TYPE = "productType";
	private final static String RECEIVER = "receiver";
	private final static String TIME_WINDOW = "timeWindow";
	private final static String PRODUCT = "product";
	private final static String REORDER_POLICY = "reorderPolicy";
	private final static String PLAN = "plan";
	private final static String ORDER = "order";
	private final static String ITEM = "item";

	private final static String ATTR_RECEIVERS_DESC = "desc";
	private final static String ATTR_PRODUCT_TYPE_ID = "id";
	private final static String ATTR_PRODUCT_TYPE_DESC = "descr";
	private final static String ATTR_PRODUCT_TYPE_WEIGHT = "weight";
	private final static String ATTR_RECEIVER_ID = "id";
	private final static String ATTR_RECEIVER_LINKID = "linkId";
	private final static String ATTR_TIME_WINDOW_START = "start";
	private final static String ATTR_TIME_WINDOW_END = "end";
	private final static String ATTR_PRODUCT_ID = "id";
	private final static String ATTR_PRODUCT_ONHAND = "onHand";
	private final static String ATTR_REORDER_POLICY_NAME = "name";
	private final static String ATTR_PLAN_SCORE = "score";
	private final static String ATTR_PLAN_SELECTED = "selected";
	private final static String ATTR_ORDER_CARRIER = "carrierId";
	private final static String ATTR_ITEM_ID = "id";
	//private final static String ATTR_ITEM_NAME = "name";
	private final static String ATTR_ITEM_PRODUCT = "productId";
	private final static String ATTR_ITEM_QUANTITY = "quantity";
	//private final static String ATTR_ITEM_DAILYQUANTITY = "dailyQuantity";
	private final static String ATTR_ITEM_SERVICETIME = "serviceTime";

	private final AttributesXmlReaderDelegate attributesReader = new AttributesXmlReaderDelegate();

	private Receiver currentReceiver = null;
	private org.matsim.utils.objectattributes.attributable.Attributes currentAttributes = null;
	private ReorderPolicy currentReorderPolicy = null;
	private TimeWindow currentTimeWindow = null;
	private Id<Carrier> currentOrderCarrierId = null;
	private List<Order> currentOrders = null;
	private ReceiverPlan.Builder currentPlanBuilder = null;
	private ReceiverProduct.Builder currentProductBuilder = null;

	private final Receivers receivers;
	
	private final Counter counter = new Counter("   receiver # ");

	public ReceiversReader_v1(final Receivers receivers) {
		this.receivers = receivers;
	}

	@Override
	public void startTag(String name, Attributes atts, Stack<String> context) {
		switch (name) {
		case RECEIVERS:
			startReceivers(atts);
			break;
		case ATTRIBUTES:
			switch (context.peek()) {
			case RECEIVERS:
				currentAttributes = receivers.getAttributes(); 
				break;
			case RECEIVER:
				currentAttributes = currentReceiver.getAttributes();
				break;
			case REORDER_POLICY:
				currentAttributes = currentReorderPolicy.getAttributes();
				break;
			default:
				throw new RuntimeException( context.peek() );
			}
		case ATTRIBUTE:
			attributesReader.startTag(name, atts, context, currentAttributes);
			break;
		case PRODUCT_TYPES:
			/* Do nothing */
			break;
		case PRODUCT_TYPE:
			startProductType(atts);
			break;
		case RECEIVER:
			startReceiver(atts);
			break;
		case TIME_WINDOW:
			startTimeWindow(atts);
			break;
		case PRODUCT:
			startProduct(atts);
			break;
		case REORDER_POLICY:
			startReorderPolicy(atts);
			break;
		case PLAN:
			startPlan(atts);
			break;
		case ORDER:
			startOrder(atts);
			break;
		case ITEM:
			startItem(atts);
			break;
		default:
			throw new RuntimeException(this + "[tag=" + name + " not known or not supported]");
		}
	}

	@Override
	public void endTag(String name, String content, Stack<String> context) {
		switch (name) {
		case ATTRIBUTE:
			attributesReader.endTag(name, content, context);
			break;
		case PRODUCT:
			ReceiverProduct product = currentProductBuilder.build();
			currentReceiver.addProduct(product);
			break;
		case RECEIVER:
			receivers.addReceiver(currentReceiver);
			currentReceiver = null;
			counter.incCounter();
			break;
		case TIME_WINDOW:
			currentPlanBuilder.addTimeWindow(currentTimeWindow);
			break;
		case ORDER:
			endOrder();
			break;
		case PLAN:
			endPlan();
			break;
		case RECEIVERS:
			counter.printCounter();
			break;
		default:
			break;
		}
	}

	private void startReceivers(final Attributes atts) {
		this.receivers.setDescription(atts.getValue(ATTR_RECEIVERS_DESC));
	}
	
	private void startProductType(final Attributes atts) {
		Id<ProductType> id = Id.create(atts.getValue(ATTR_PRODUCT_TYPE_ID), ProductType.class);
		String desc = atts.getValue(ATTR_PRODUCT_TYPE_DESC);
		String weight = atts.getValue(ATTR_PRODUCT_TYPE_WEIGHT);
		
		ProductType pt = receivers.createAndAddProductType(id);
		pt.setDescription(desc);
		pt.setRequiredCapacity(Double.parseDouble(weight));
	}
	
	private void startReceiver(final Attributes atts) {
		Id<Receiver> id = Id.create(atts.getValue(ATTR_RECEIVER_ID), Receiver.class);
		this.currentReceiver = ReceiverUtils.newInstance( id );
		String linkId = atts.getValue(ATTR_RECEIVER_LINKID);
		if(linkId != null) {
			currentReceiver.setLinkId(Id.createLinkId(linkId));
		}
	}
	
	private void startTimeWindow(Attributes atts) {
		double start = Time.parseTime(atts.getValue(ATTR_TIME_WINDOW_START));
		double end = Time.parseTime(atts.getValue(ATTR_TIME_WINDOW_END));
		this.currentTimeWindow = TimeWindow.newInstance(start, end);
	}

	private void startProduct(Attributes atts) {
		String id = atts.getValue(ATTR_PRODUCT_ID);
		double onHand = Double.parseDouble(atts.getValue(ATTR_PRODUCT_ONHAND));
		
		this.currentProductBuilder = ReceiverProduct.Builder.newInstance();
		currentProductBuilder.setProductType(receivers.getProductType(Id.create(id, ProductType.class))).setStockOnHand(onHand);
	}
	
	private void startReorderPolicy(Attributes atts) {
		String name = atts.getValue(ATTR_REORDER_POLICY_NAME);
		switch (name) {
		case "(s,S)":
			this.currentReorderPolicy = new SSReorderPolicy();
			break;
		default:
			throw new IllegalArgumentException("Unknown reorder policy \"" + name + "\"");
		}
	}
	
	private void startPlan(Attributes atts) {
		this.currentOrders = new ArrayList<>();
		boolean collaborationStatus = (boolean) currentReceiver.getAttributes().getAttribute(ReceiverUtils.ATTR_COLLABORATION_STATUS);
		currentPlanBuilder = ReceiverPlan.Builder.newInstance(currentReceiver, collaborationStatus);
		
		String score = atts.getValue(ATTR_PLAN_SCORE);
		if(score != null) {
			currentPlanBuilder = currentPlanBuilder.setScore(Double.parseDouble(score));
		}
		
		String selected = atts.getValue(ATTR_PLAN_SELECTED);
		Boolean currentPlanSelected = null;
		if(selected.equalsIgnoreCase("yes")) {
			currentPlanSelected = true;
		} else if (selected.equalsIgnoreCase("no")) {
			currentPlanSelected = false;
		} else {
			throw new IllegalArgumentException("Unknown plan selection state \"" + selected + "\"");
		}
		currentPlanBuilder = currentPlanBuilder.setSelected(currentPlanSelected);
	}
	
	private void endPlan() {
		ReceiverPlan plan = currentPlanBuilder.build();
		if(plan.isSelected()) {
			currentReceiver.setSelectedPlan(plan);
		} else {
			currentReceiver.addPlan(plan);
		}
	}

	private void startOrder(Attributes atts) {
		this.currentOrderCarrierId = Id.create(atts.getValue(ATTR_ORDER_CARRIER), Carrier.class);
	}
	
	private void endOrder() {
		ReceiverOrder order = new ReceiverOrder(currentReceiver.getId(), currentOrders, currentOrderCarrierId);
		currentPlanBuilder = currentPlanBuilder.addReceiverOrder(order);
	}
	
	private void startItem(Attributes atts) {
		Id<Order> id = Id.create(atts.getValue(ATTR_ITEM_ID), Order.class);
		
		/* TODO Is this necessary? It is hard-coded in the builder. */
		//String name = atts.getValue(ATTR_ITEM_NAME);
		
		Id<ProductType> productId = Id.create(atts.getValue(ATTR_ITEM_PRODUCT), ProductType.class);
		ProductType pt = receivers.getProductType(productId);
		if(pt == null) {
			throw new IllegalArgumentException("Cannot link the order item \"" 
					+ productId.toString() + "\" to a known product type.");
		}
		ReceiverProduct thisProduct = currentReceiver.getProduct(productId);
		Order.Builder orderBuilder = Order.Builder.newInstance(id, currentReceiver, thisProduct);
		
		String quantityString = atts.getValue(ATTR_ITEM_QUANTITY);
		double quantity = 0.0;
		if(quantityString != null) {
			quantity = Double.parseDouble(quantityString);
			orderBuilder.setOrderQuantity(quantity);
		}
		
//		String dailyQuantityString = atts.getValue(ATTR_ITEM_DAILYQUANTITY);
//		double dailyQuantity = 0.0;
//		if(dailyQuantityString != null) {
//			dailyQuantity = Double.parseDouble(dailyQuantityString);
//			orderBuilder.setOrderQuantity(dailyQuantity);
//		}
		
		String serviceTime = atts.getValue(ATTR_ITEM_SERVICETIME);
		if(serviceTime != null) {
			double time = Time.parseTime(serviceTime);
			orderBuilder.setServiceTime(time);
		}
		Order order = orderBuilder.build();
		currentOrders.add(order);
	}
	
}

