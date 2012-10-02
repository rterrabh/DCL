package dclsuite.resolution.similarity;

public class SMCCoefficientStrategy implements ICoefficientStrategy {
	
	@Override
	public String getName() {
		return "SMC";
	}
	
	@Override
	public double calculate(int p, int q, int r, int s) {
		return ((double) (p + s)) / (p + q + r + s);
	}

}
