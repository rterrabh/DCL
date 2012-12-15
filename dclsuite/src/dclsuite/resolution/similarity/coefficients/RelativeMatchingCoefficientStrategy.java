package dclsuite.resolution.similarity.coefficients;

public class RelativeMatchingCoefficientStrategy implements ICoefficientStrategy {

	@Override
	public String getName() {
		return "Relative Matching";
	}

	@Override
	public double calculate(int a, int b, int c, int d) {
		return (((double) a) + Math.sqrt(a * d)) / (a + b + c + d + Math.sqrt(a * d));
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
