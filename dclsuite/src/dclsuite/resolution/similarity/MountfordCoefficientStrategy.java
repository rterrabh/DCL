package dclsuite.resolution.similarity;

public class MountfordCoefficientStrategy implements ICoefficientStrategy {

	@Override
	public String getName() {
		return "Mountford";
	}
	
	@Override
	public double calculate(int p, int q, int r, int s) {
		return (2.0 * p) / ((2 * (p + q) * (p + r)) - (((p + q) + (p + r)) * p));
	}

}
