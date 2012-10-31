package dclsuite.resolution.similarity.coefficients;

public class PhiBinaryDistance implements ICoefficientStrategy {

	@Override
	public String getName() {
		return "Phi Binary Distance";
	}

	@Override
	public double calculate(int a, int b, int c, int d) {
		return (a * d - b * c) / Math.sqrt((a + b) * (a + c) * (b + d) * (c + d));
	}

}
