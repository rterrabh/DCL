package dclsuite.resolution.similarity;

public class BaroniUrbaniCoefficientStrategy implements ICoefficientStrategy {

	@Override
	public double calculate(double p, double q, double r, double s) {
		return (Math.sqrt(p * s) + p) / (Math.sqrt(p * s) + (p + q) + (p + r) - p);
	}

}
