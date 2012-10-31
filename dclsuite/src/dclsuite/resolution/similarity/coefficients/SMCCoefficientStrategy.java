package dclsuite.resolution.similarity.coefficients;


public class SMCCoefficientStrategy implements ICoefficientStrategy {
	
	@Override
	public String getName() {
		return "SMC";
	}
	
	@Override
	public double calculate(int a, int b, int c, int d) {
		return ((double) (a + d)) / (a + b + c + d);
	}

}
