package dclsuite.resolution.similarity.coefficients;

public class OchiaiCoefficientStrategy implements ICoefficientStrategy {

	@Override
	public String getName() {
		return "Ochiai";
	}

	@Override
	public double calculate(int a, int b, int c, int d) {
		return a / Math.sqrt((a + b) * (a + c));
	}

}
