package dclsuite.resolution.similarity.coefficients;

public class SokalBinaryDistanceCoefficientStrategy implements ICoefficientStrategy {

	@Override
	public String getName() {
		return "Sokal Binary Distance";
	}

	@Override
	public double calculate(int a, int b, int c, int d) {
		return Math.sqrt(((double) b + c) / (a + b + c + d));
	}
	
	@Override
	public double getMinimumValue() {
		return 1;
	}

	@Override
	public double getMaximumValue() {
		return 0;
	}

}
