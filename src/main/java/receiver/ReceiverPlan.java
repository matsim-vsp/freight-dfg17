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
  
/**
 * 
 */
package receiver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.BasicPlan;
import org.matsim.api.core.v01.population.Person;
import org.matsim.contrib.freight.carrier.Carrier;
import org.matsim.contrib.freight.carrier.TimeWindow;
import org.matsim.utils.objectattributes.attributable.Attributable;
import org.matsim.utils.objectattributes.attributable.Attributes;

import receiver.product.ReceiverOrder;

/**
 * Like a natural {@link Person} a plan contains the intention of a {@link Receiver} 
 * agent.  In consequence, all information is <i>expected</i>. This container 
 * describes a {@link Receiver}'s behaviour in terms of how orders are placed 
 * with different {@link Carrier}s.
 * <p></p>
 * The only thing which is not "expected" in the same sense is the score.
 *  
 * @author jwjoubert
 */
public final class ReceiverPlan implements BasicPlan, Attributable {
//	private final Logger log = Logger.getLogger(ReceiverPlan.class);
//	private Attributes attributes;
//	private Receiver receiver = null;
//	private Double score;
//	private Map<Id<Carrier>, ReceiverOrder> orderMap;
//	private List<TimeWindow> timeWindows;
//	private boolean selected = false;
//	private boolean collaborationStatus;
//	
//	
//	private ReceiverPlan() {
//		this.attributes = new Attributes();
//		this.timeWindows = new ArrayList<>();
//		this.orderMap = new TreeMap<>();
//	}
	private final Logger log = Logger.getLogger(ReceiverPlan.class);
	private Attributes attributes = new Attributes();
	private Receiver receiver = null;
	private Double score;
	private Map<Id<Carrier>, ReceiverOrder> orderMap = new TreeMap<>();
	private List<TimeWindow> timeWindows = new ArrayList<>();
	private boolean selected = false;
	private boolean collaborationStatus;
	
//	private ReceiverPlan() {
	ReceiverPlan() {
//		this.attributes;
//		this.timeWindows;
//		this.orderMap;
	}
		
	
//	public void addReceiverOrder(final ReceiverOrder ro) {
//		if(orderMap.containsKey(ro.getCarrierId())) {
//			throw new IllegalArgumentException("Receiver '" + this.receiver.getId().toString() 
//					+ "' already has an order with carrier '" + ro.getCarrierId().toString() + "'");
//		}
//		orderMap.put(ro.getCarrierId(), ro);
//	}
	
	@Override
	public void setScore(final Double score) {
		this.score = score;
	}
	
	public Double getScore() {
		return this.score;
	}
	
	public final Receiver getReceiver() {
		return this.receiver;
	}
	
	public boolean isSelected() {
		return this.selected;
	}
	
	public void setSelected(boolean selected) {
		this.selected = selected;
	}
	
	public void setCollaborationStatus(boolean status){
		this.collaborationStatus = status;
	}
	
	public boolean getCollaborationStatus(){
		return this.collaborationStatus;
	}
	
	/**
	 * Returns the {@link ReceiverOrder} for a given {@link Carrier}.
	 * @param carriedId
	 * @return
	 */
	public final ReceiverOrder getReceiverOrder(Id<Carrier> carriedId) {
		if(!orderMap.containsKey(carriedId)) {
			log.warn("Receiver '" + this.receiver.getId().toString() + 
					"' does not have an order with carrier '" + 
					carriedId.toString() + "'. Returning null");
			return null;
		}
		return this.orderMap.get(carriedId);
	}
	
	
	public String toString() {
		String scoreString = "undefined";
		if(this.score != null) {
			scoreString = this.score.toString();
		}
		
		String receiverString = "undefined";
		if(this.receiver != null) {
			receiverString = this.receiver.getId().toString();
		}
		
		return "[receiver: " + receiverString + "; score: " + scoreString + 
				"; number of orders with carriers: " + orderMap.size() + "]";
	}
	
	public final Collection<ReceiverOrder> getReceiverOrders(){
		return this.orderMap.values();
	}

	@Override
	public Attributes getAttributes() {
		return this.attributes;
	}
	
	public List<TimeWindow> getTimeWindows(){
		return this.timeWindows;
	}
	
	/**
	 * Checks if a given time is within the allowable time window(s).
	 * 
	 * @return true if the time is within at least one of the set time 
	 * window(s), or <i>if no time windows are set</i>.
	 */
//	public boolean isInTimeWindow(double time) {
//		if(this.timeWindows.isEmpty()) {
//			log.warn("No time windows are set! Assuming any time is suitable.");
//			return true;
//		}
//		
//		boolean inTimeWindow = false;
//		Iterator<TimeWindow> iterator = this.timeWindows.iterator();
//		
//		while(!inTimeWindow & iterator.hasNext()) {
//			TimeWindow tw = iterator.next();
//			if(time >= tw.getStart() && time <= tw.getEnd()) {
//				inTimeWindow = true;
//			}
//		}
//		return false;
//	}

	
	public ReceiverPlan createCopy() {
		Builder builder = Builder.newInstance(receiver, collaborationStatus);

		for(ReceiverOrder ro : this.orderMap.values()) {
			builder = builder.addReceiverOrder(ro);
		}
		for(TimeWindow tw : this.timeWindows) {
			builder.addTimeWindow(tw);
		}
		return builder.build();
	}

	
	/**
	 * The constructor mechanism for creating a {@link ReceiverPlan}. Once
	 * built the only thing one will be able to change is the score. 
	 *
	 * @author jwjoubert
	 */
	public static class Builder{
		private Receiver receiver = null;
		private Map<Id<Carrier>, ReceiverOrder> map = new HashMap<>();
		private boolean selected = false;
		private Double score = null;
		private List<TimeWindow> timeWindows = new ArrayList<>();
		private boolean status;
		
		private Builder(Receiver receiver, boolean status) {
			this.receiver = receiver;
			this.status  = status;
		}
			
		public static Builder newInstance(Receiver receiver, boolean status) {
			return new Builder(receiver, status);
		};


		public Builder addReceiverOrder(ReceiverOrder ro) {
			this.map.put(ro.getCarrierId(), ro);
			return this;
		}

		
		public Builder addTimeWindow(TimeWindow tw) {
			this.timeWindows.add(tw);
			return this;
		}
		
		public Builder setSelected(boolean selected) {
			this.selected = selected;
			return this;
		}
		
		public Builder setScore(double score) {
			this.score = score;
			return this;
		}
		
		public ReceiverPlan build() {
			ReceiverPlan plan = new ReceiverPlan();
			plan.receiver = this.receiver;
			plan.selected = this.selected;
			plan.collaborationStatus = this.status;
			if(this.map.size() > 0) {
				plan.orderMap.putAll(this.map);			
			} else {
				plan.orderMap = this.map;
			}
			plan.score = this.score;
			plan.timeWindows = this.timeWindows;
			return plan;
		}
	}

}
