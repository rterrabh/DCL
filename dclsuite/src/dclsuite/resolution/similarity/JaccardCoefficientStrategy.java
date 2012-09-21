package dclsuite.resolution.similarity;

public class JaccardCoefficientStrategy implements ICoefficientStrategy {
	
	@Override
	public String getName() {
		return "Jaccard";
	}
	
	@Override
	public double calculate(double p, double q, double r, double s) {
		return p / (p + q + r);
	}

}
