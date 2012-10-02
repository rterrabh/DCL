package dclsuite.resolution.similarity;

public class JaccardCoefficientStrategy implements ICoefficientStrategy {
	
	@Override
	public String getName() {
		return "Jaccard";
	}
	
	@Override
	public double calculate(int p, int q, int r, int s) {
		return ((double) p) / (p + q + r);
	}

}
