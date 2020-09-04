package lsp.functions;

import java.util.HashSet;
import java.util.Set;

public abstract class LSPInfo {

	protected Set<LSPInfo> predecessorInfos;
	
	protected LSPInfo() {
		this.predecessorInfos = new HashSet<>();
	}
	
	
	public void addPredecessorInfo(LSPInfo info) {
		predecessorInfos.add(info);
	}
	
//	public void removePredecessorInfo(LSPInfo info) {
//		if(predecessorInfos.contains(info)) {
//			predecessorInfos.remove(info);
//		}
//	}
	
	public Set<LSPInfo> getPredecessorInfos() {
		return predecessorInfos;
	}
	
	public abstract void setName(String name);
	public abstract String getName();
	public abstract LSPInfoFunction getFunction();
	public abstract double getFromTime();
	public abstract double getToTime();
	public abstract void update();
	
}
