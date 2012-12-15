package dclsuite.resolution.similarity.coefficients;


public class BaroniUrbaniCoefficientStrategy implements ICoefficientStrategy {

	@Override
	public String getName() {
		return "BaroniUrbani";
	}
	
	@Override
	public double calculate(int a, int b, int c, int d) {
		return (Math.sqrt(a * d) + a) / (Math.sqrt(a * d) + a + b + c);
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
