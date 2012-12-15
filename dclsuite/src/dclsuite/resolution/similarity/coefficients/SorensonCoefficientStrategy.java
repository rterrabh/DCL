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

	@Override
	public double getMinimumValue() {
		return 0;
	}

	@Override
	public double getMaximumValue() {
		return 1;
	}
	
}
