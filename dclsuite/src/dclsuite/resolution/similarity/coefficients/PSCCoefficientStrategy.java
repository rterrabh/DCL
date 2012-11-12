package dclsuite.resolution.similarity.coefficients;

public class PSCCoefficientStrategy implements ICoefficientStrategy {

	@Override
	public String getName() {
		return "PSC";
	}

	@Override
	public double calculate(int a, int b, int c, int d) {
		return Math.pow(a,2) / ((b+a) * (c+a));
	}

}
