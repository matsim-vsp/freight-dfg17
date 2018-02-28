package testLSPWithCostTrackerAndOffer;

import demand.offer.Offer;
import demand.offer.OfferVisitor;
import lsp.functions.Info;
import lsp.functions.InfoFunctionValue;
import lsp.LSP;
import lsp.LogisticsSolution;


public class LinearOffer implements Offer{

	private LSP  lsp;
	private LogisticsSolution solution;
	private String type;
	private double fix;
	private double linear;
	
	public LinearOffer(LogisticsSolution solution) {
		this.lsp =  solution.getLSP();
		this.solution = solution;
		this.type = "linear";
	}
	
	@Override
	public LSP getLsp() {
		return lsp;
	}

	@Override
	public LogisticsSolution getSolution() {
		return solution;
	}

	@Override
	public String getType() {
		return type;
	}

	public double getFix() {
		return fix;
	}

	public void setFix(double fix) {
		this.fix = fix;
	}

	public double getLinear() {
		return linear;
	}

	public void setLinear(double linear) {
		this.linear = linear;
	}

	@Override
	public void accept(OfferVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public void update() {
		for(Info info : solution.getInfos()) {
			if(info instanceof CostInfo) {
				CostInfo costInfo = (CostInfo) info;
				CostInfoFunction costFunction = (CostInfoFunction) costInfo.getFunction();
				FixedCostFunctionValue fixedValue;
				LinearCostFunctionValue linearValue;
				for(InfoFunctionValue value : costFunction.getValues()) {
					if(value instanceof FixedCostFunctionValue) {
						fixedValue = (FixedCostFunctionValue) value;
						this.fix = Double.parseDouble(fixedValue.getValue());
					}
					if(value instanceof LinearCostFunctionValue) {
						linearValue = (LinearCostFunctionValue) value;
						this.linear = Double.parseDouble(linearValue.getValue());
					}
				}
			}
		}
		
	}

	@Override
	public void setLSP(LSP lsp) {
		this.lsp = lsp;
	}

	@Override
	public void setSolution(LogisticsSolution solution) {
		this.solution = solution;
	}

}
