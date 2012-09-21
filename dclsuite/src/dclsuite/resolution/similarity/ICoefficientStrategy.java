package dclsuite.resolution.similarity;

public interface ICoefficientStrategy {

	String getName();
	
	double calculate (double p, double q, double r, double s);
	
}
