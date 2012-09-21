package dclsuite.resolution.similarity;

public class SMCCoefficientStrategy implements ICoefficientStrategy {
	
	@Override
	public String getName() {
		return "SMC";
	}
	
	@Override
	public double calculate(double p, double q, double r, double s) {
		return (p + s) / (p + q + r + s);
	}

}
