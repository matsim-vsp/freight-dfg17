package lsp.functions;

import java.util.HashSet;
import java.util.Set;

public abstract class Info {

	protected Set<Info> predecessorInfos;
	
	public Info() {
		this.predecessorInfos = new HashSet<Info>();
	}
	
	
	public void addPredecessorInfo(Info info) {
		predecessorInfos.add(info);
	}
	
	public void removePredecessorInfo(Info info) {
		if(predecessorInfos.contains(info)) {
			predecessorInfos.remove(info);
		}
	}
	
	public abstract String getName();
	public abstract InfoFunction getFunction();
	public abstract double getFromTime();
	public abstract double getToTime();
	public abstract void update();
	
}
