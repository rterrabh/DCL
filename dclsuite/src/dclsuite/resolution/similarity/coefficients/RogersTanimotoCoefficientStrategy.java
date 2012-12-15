package dclsuite.resolution.similarity.coefficients;


public class RogersTanimotoCoefficientStrategy implements ICoefficientStrategy {
	
	@Override
	public String getName() {
		return "Rogers and Tanimoto";
	}
	
	@Override
	public double calculate(int a, int b, int c, int d) {
		return ((double) a+d) / (a + 2 * (b+c) + d);
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
