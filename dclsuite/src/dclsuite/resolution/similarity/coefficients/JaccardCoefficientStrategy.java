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

}
