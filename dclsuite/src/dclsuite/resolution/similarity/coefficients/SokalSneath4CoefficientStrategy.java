package dclsuite.resolution.similarity.coefficients;

public class SokalSneath4CoefficientStrategy implements ICoefficientStrategy {

	@Override
	public String getName() {
		return "Sokal and Sneath 4";
	}

	@Override
	public double calculate(int a, int b, int c, int d) {
		return ((((double) a) / (a + b) + ((double) a) / (a + c) + ((double) d) / (b + d) + ((double) d) / (c + d))) / 4.0;
	}
	
	@Override
	public double getMinimumValue() {
		return 0;
	}

	@Override
	public double getMaximumValue() {
		return 1;
	}
}
