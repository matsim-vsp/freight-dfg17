/*
 *  *********************************************************************** *
 *  * project: org.matsim.*
 *  * *********************************************************************** *
 *  *                                                                         *
 *  * copyright       : (C) 2022 by the members listed in the COPYING,        *
 *  *                   LICENSE and WARRANTY file.                            *
 *  * email           : info at matsim dot org                                *
 *  *                                                                         *
 *  * *********************************************************************** *
 *  *                                                                         *
 *  *   This program is free software; you can redistribute it and/or modify  *
 *  *   it under the terms of the GNU General Public License as published by  *
 *  *   the Free Software Foundation; either version 2 of the License, or     *
 *  *   (at your option) any later version.                                   *
 *  *   See also COPYING, LICENSE and WARRANTY file                           *
 *  *                                                                         *
 *  * ***********************************************************************
 */

package demand.controler;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import demand.decoratedLSP.LSPDecorator;
import org.matsim.api.core.v01.Id;

import lsp.LSP;

/*package-private*/ class LSPDecorators {

	private final Map<Id<LSP>, LSPDecorator> lsps = new HashMap<>();

	LSPDecorators(Collection<LSPDecorator> lsps) {
		makeMap(lsps);
	}

	private void makeMap(Collection<LSPDecorator> lsps) {
		for (LSPDecorator c : lsps) {
			this.lsps.put(c.getId(), c);
		}
	}

	public LSPDecorators() {

	}

	public Map<Id<LSP>, LSPDecorator> getLSPs() {
		return lsps;
	}

	public void addLSP(LSPDecorator lsp) {
		if(!lsps.containsKey(lsp.getId())){
			lsps.put(lsp.getId(), lsp);
		}
	}
}
