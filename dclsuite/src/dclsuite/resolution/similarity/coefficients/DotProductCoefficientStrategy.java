package dclsuite.resolution.similarity.coefficients;

public class DotProductCoefficientStrategy implements ICoefficientStrategy {

	@Override
	public String getName() {
		return "Dot-Product";
	}

	@Override
	public double calculate(int a, int b, int c, int d) {
		return ((double) a) / (b + c + (2 * a));
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
