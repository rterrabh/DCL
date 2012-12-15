package dclsuite.resolution.similarity.coefficients;

/**
 * Interface to Similarity Coefficients
 */
public interface ICoefficientStrategy {

	String getName();
	
	/**
	 * 
	 * @param a # of "x" common in sets A and B <-> |A intersect B|
	 * @param b # of "x" only in set A <-> |A| - |A intersect B|
	 * @param c # of "x" only in set B <-> |B| - |A intersect B|
	 * @param d # of "x" absent in sets A and B <-> |U| - |A U B|  
	 * @return Similarity Index (range from 0 to 1)
	 */
	double calculate (int a, int b, int c, int d);
	
	double getMinimumValue();
	
	double getMaximumValue();
}
