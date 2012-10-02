package dclsuite.resolution.similarity;

public class SorensensCoefficientStrategy implements ICoefficientStrategy {
	
	@Override
	public String getName() {
		return "Sorensens";
	}
	
	@Override
	public double calculate(int p, int q, int r, int s) {
		return (2.0 * p) / ( (p+q) + (p+r) );
	}

}
