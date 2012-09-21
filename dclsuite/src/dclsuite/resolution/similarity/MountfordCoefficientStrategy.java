package dclsuite.resolution.similarity;

public class MountfordCoefficientStrategy implements ICoefficientStrategy {

	@Override
	public double calculate(double p, double q, double r, double s) {
		return (2 * p) / ((2 * (p + q) * (p + r)) - (((p + q) + (p + r)) * p));
	}

}
