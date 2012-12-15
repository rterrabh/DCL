package dclsuite.resolution.similarity.coefficients;

public class KulczynskiCoefficientStrategy implements ICoefficientStrategy {

	@Override
	public String getName() {
		return "Kulczynski";
	}

	@Override
	public double calculate(int a, int b, int c, int d) {
		return (((double) a) / (a + b) + ((double) a) / (a + c)) / 2.0;
	}
	
	@Override
	public double getMinimumValue() {
		return 0;
	}

	@Override
	public double getMaximumValue() {
		return 1;
	}

}
