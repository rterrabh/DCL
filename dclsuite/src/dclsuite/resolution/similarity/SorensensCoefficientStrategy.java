package dclsuite.resolution.similarity;

public class SorensensCoefficientStrategy implements ICoefficientStrategy {
	
	@Override
	public double calculate(double p, double q, double r, double s) {
		return (2 * p) / ( (p+q) + (p+r) );
	}

}
