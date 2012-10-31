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

}
