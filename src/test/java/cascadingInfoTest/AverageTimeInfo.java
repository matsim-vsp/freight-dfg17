package cascadingInfoTest;

import lsp.LSPInfo;

class AverageTimeInfo extends LSPInfo {

//	private final LSPAttributes function;
	private String name = "averageTime";
		
	AverageTimeInfo() {
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public double getFromTime() {
		return 0;
	}

	@Override
	public double getToTime() {
		return Double.MAX_VALUE;
	}

	@Override
	public void update() {
		LSPInfo preInfo = predecessorInfos.iterator().next();
//		AverageTimeInfo avgInfo = (AverageTimeInfo)preInfo;
//		LSPAttribute<?> infoVal  = avgInfo.getAttributes().getAttributes().iterator().next();
//		if( infoVal.getValue() instanceof Double) {
//			if(function.getAttributes().iterator().next() instanceof AverageTimeInfoFunctionValue) {
//				AverageTimeInfoFunctionValue avgVal = (AverageTimeInfoFunctionValue) function.getAttributes().iterator().next();
//				avgVal.setValue((Double)infoVal.getValue());
//			}
//		}
		this.setAverageTime(  ((AverageTimeInfo)preInfo).getAverageTime() );
		// I think that this is all what the above lines do, but maybe not.  kai, feb'22

	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	void setAverageTime( Double value ) {
		this.getAttributes().putAttribute( name, value );
	}
	Double getAverageTime() {
		return (Double) this.getAttributes().getAttribute( name );
	}
}
