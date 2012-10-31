package dclsuite.resolution.similarity.coefficients;


public class SorensonCoefficientStrategy implements ICoefficientStrategy {
	
	@Override
	public String getName() {
		return "Sorenson";
	}
	
	@Override
	public double calculate(int a, int b, int c, int d) {
		return (2.0 * a) / ( (a+b) + (a+c) );
	}

}
