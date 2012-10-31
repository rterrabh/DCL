package dclsuite.resolution.similarity.coefficients;


public class SokalSneathCoefficientStrategy implements ICoefficientStrategy {
	
	@Override
	public String getName() {
		return "Sokal and Sneath";
	}
	
	@Override
	public double calculate(int a, int b, int c, int d) {
		return (2.0 * (a+d)) / (2*(a+d) + b + c);
	}

}
