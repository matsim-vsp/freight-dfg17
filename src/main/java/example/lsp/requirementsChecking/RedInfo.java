package example.lsp.requirementsChecking;

import lsp.functions.*;

/*package-private*/ class RedInfo extends Info{

private InfoFunction redInfoFunction;

	/*package-private*/ RedInfo() {
		redInfoFunction = InfoFunctionUtils.createDefaultInfoFunction();
		InfoFunctionValue<String> value = InfoFunctionUtils.createInfoFunctionValue("red" );
		value.setValue("red");
		redInfoFunction.getValues().add(value);
	}

	@Override
	public String getName() {
		return "red";
	}

	@Override
	public InfoFunction getFunction() {
		return redInfoFunction;
	}

	@Override
	public double getFromTime() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getToTime() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void update() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setName(String name) {
		// TODO Auto-generated method stub
		
	}

}
