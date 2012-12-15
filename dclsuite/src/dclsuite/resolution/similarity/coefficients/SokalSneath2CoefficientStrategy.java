package dclsuite.resolution.similarity.coefficients;

public class SokalSneath2CoefficientStrategy implements ICoefficientStrategy {

	@Override
	public String getName() {
		return "Sokal and Sneath 2";
	}

	@Override
	public double calculate(int a, int b, int c, int d) {
		return ((double) a) / (a + 2 * (b + c));
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
