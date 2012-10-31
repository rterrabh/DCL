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

}
