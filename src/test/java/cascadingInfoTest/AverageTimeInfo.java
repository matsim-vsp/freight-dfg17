package cascadingInfoTest;

import lsp.functions.LSPInfo;
import lsp.functions.LSPInfoFunction;
import lsp.functions.LSPInfoFunctionValue;

public class AverageTimeInfo extends LSPInfo {

	private LSPInfoFunction function;
	private String name = "averageTime";
		
	public AverageTimeInfo() {
		function = new AverageTimeInfoFunction();
	}	
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public LSPInfoFunction getFunction() {
		return function;
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
		AverageTimeInfo avgInfo = (AverageTimeInfo)preInfo;
		LSPInfoFunctionValue<?> infoVal  = avgInfo.getFunction().getValues().iterator().next();
		if( infoVal.getValue() instanceof Double) {
			if(function.getValues().iterator().next() instanceof AverageTimeInfoFunctionValue) {
				AverageTimeInfoFunctionValue avgVal = (AverageTimeInfoFunctionValue) function.getValues().iterator().next();
				avgVal.setValue((Double)infoVal.getValue());
			}
		}	
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}
}
