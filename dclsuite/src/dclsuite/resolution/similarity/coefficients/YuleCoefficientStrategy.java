package dclsuite.resolution.similarity.coefficients;


public class YuleCoefficientStrategy implements ICoefficientStrategy {
	
	@Override
	public String getName() {
		return "Yule";
	}
	
	@Override
	public double calculate(int a, int b, int c, int d) {
		return ((double) a*d - b*c) / (a*d + b*c);
	}

}
