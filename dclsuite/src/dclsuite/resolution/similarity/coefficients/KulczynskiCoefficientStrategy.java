package dclsuite.resolution.similarity.coefficients;

public class KulczynskiCoefficientStrategy implements ICoefficientStrategy {

	@Override
	public String getName() {
		return "Kulczynski";
	}

	@Override
	public double calculate(int a, int b, int c, int d) {
		return Math.sqrt(((double) a) / (a + b) + ((double) a) / (a + c));
	}

}
