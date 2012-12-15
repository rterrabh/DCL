package dclsuite.resolution.similarity.coefficients;


public class RussellRaoCoefficientStrategy implements ICoefficientStrategy {
	
	@Override
	public String getName() {
		return "Russell and Rao";
	}
	
	@Override
	public double calculate(int a, int b, int c, int d) {
		return ((double) a) / (a + b + c + d);
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
