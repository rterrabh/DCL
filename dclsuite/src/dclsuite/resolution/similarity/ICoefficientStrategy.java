package dclsuite.resolution.similarity;

public interface ICoefficientStrategy {

	String getName();
	
	double calculate (int p, int q, int r, int s);
	
}
