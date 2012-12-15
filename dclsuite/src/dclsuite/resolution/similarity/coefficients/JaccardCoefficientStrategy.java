package dclsuite.resolution.similarity.coefficients;


public class JaccardCoefficientStrategy implements ICoefficientStrategy {
	
	@Override
	public String getName() {
		return "Jaccard";
	}
	
	@Override
	public double calculate(int a, int b, int c, int d) {
		return ((double) a) / (a + b + c);
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
