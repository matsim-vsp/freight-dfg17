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
package receiver.wlbCapeTown;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Coord;
import org.matsim.core.utils.geometry.CoordUtils;
import org.matsim.core.utils.misc.Counter;
import org.openstreetmap.osmosis.core.container.v0_6.BoundContainer;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.container.v0_6.EntityProcessor;
import org.openstreetmap.osmosis.core.container.v0_6.NodeContainer;
import org.openstreetmap.osmosis.core.container.v0_6.RelationContainer;
import org.openstreetmap.osmosis.core.container.v0_6.WayContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;

/**
 *
 * @author jwjoubert
 */
public class PnpSink implements Sink {
	final private Logger log = Logger.getLogger(PnpSink.class);
	private Counter counter = new Counter("   entity # ");
	private Map<Long, NodeContainer> nodeMap = new TreeMap<>();
	private Map<Long, WayContainer> wayMap = new TreeMap<>();
	private Map<Long, RelationContainer> relationMap = new TreeMap<>();
	private Map<Long, Coord> pnpMap = new TreeMap<>();
	private int pnpCounter = 0;


	@Override
	public void initialize(Map<String, Object> arg0) {
		log.info("Initialized");
	}

	@Override
	public void complete() {
		counter.printCounter();
		log.info("Complete. Searching for PnP...");
		for(NodeContainer nc : nodeMap.values()) {
			Entity node = nc.getEntity();
			Iterator<Tag> tagIterator = node.getTags().iterator();
			boolean foundPnP = false;
			while(!foundPnP && tagIterator.hasNext()) {
				Tag tag = tagIterator.next();
				if(tag.getKey().equalsIgnoreCase("name") || tag.getKey().equalsIgnoreCase("operator")) {
					foundPnP = containsPnP(tag.getValue());
				}
			}
			if(foundPnP) {
				pnpCounter++;
				Node n = (Node) node;
				Coord c = CoordUtils.createCoord(n.getLongitude(), n.getLatitude());
				pnpMap.put(node.getId(), c);
			}
		}
		log.info("PnP found after evaluating Nodes: " + pnpCounter);

		for(WayContainer wc : wayMap.values()) {
			Way way = wc.getEntity();
			Iterator<Tag> tagIterator = way.getTags().iterator();
			boolean foundPnP = false;
			while(!foundPnP && tagIterator.hasNext()) {
				Tag tag = tagIterator.next();
				if(tag.getKey().equalsIgnoreCase("name") || tag.getKey().equalsIgnoreCase("operator")) {
					foundPnP = containsPnP(tag.getValue());
				}
			}
			if(foundPnP) {
				pnpCounter++;
				double sumX = 0.0;
				double sumY = 0.0;
				List<WayNode> list = way.getWayNodes();
				for(WayNode wn : list) {
					sumX += nodeMap.get(wn.getNodeId()).getEntity().getLongitude();
					sumY += nodeMap.get(wn.getNodeId()).getEntity().getLatitude();
				}
				Coord c = CoordUtils.createCoord(sumX/((double)list.size()) , sumY/((double)list.size()));
				pnpMap.put(way.getId(), c);
			}
		}
		log.info("PnP found after evaluating Nodes/Ways: " + pnpCounter);
	}

	@Override
	public void close() {
		log.info("Close");
	}

	@Override
	public void process(EntityContainer container) {
		container.process(new EntityProcessor() {
			
			@Override
			public void process(RelationContainer relationContainer) {
				relationMap.put(relationContainer.getEntity().getId(), relationContainer);
			}
			
			@Override
			public void process(WayContainer wayContainer) {
				wayMap.put(wayContainer.getEntity().getId(), wayContainer);
			}
			
			@Override
			public void process(NodeContainer nodeContainer) {
				nodeMap.put(nodeContainer.getEntity().getId(), nodeContainer);
			}
			
			@Override
			public void process(BoundContainer arg0) {
			}
		});
		counter.incCounter();
	}
	
	
	private boolean containsPnP(String string) {
		if((string.contains("Pick") && string.contains("Pay")) ||
				(string.contains("pick") && string.contains("pay")) ||
				(string.contains("Pick") && string.contains("pay")) ||
				(string.contains("pick") && string.contains("Pay"))) {
			return true;
		}
		return false;
	}
	
	
	public Map<Long, Coord> getCoordMap(){
		return this.pnpMap;
	}

}
