package dclsuite.resolution.similarity;

public class BaroniUrbaniCoefficientStrategy implements ICoefficientStrategy {

	@Override
	public String getName() {
		return "BaroniUrbani";
	}
	
	@Override
	public double calculate(int p, int q, int r, int s) {
		return (Math.sqrt(p * s) + p) / (Math.sqrt(p * s) + (p + q) + (p + r) - p);
	}

}
