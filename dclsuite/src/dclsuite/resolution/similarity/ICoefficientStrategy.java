package dclsuite.resolution.similarity;

/**
 * Interface to Similarity Coefficients
 */
public interface ICoefficientStrategy {

	String getName();
	
	/**
	 * 
	 * @param p # of "x" common in sets A and B <-> |A intersect B|
	 * @param q # of "x" only in set A <-> |A| - |A intersect B|
	 * @param r # of "x" only in set B <-> |B| - |A intersect B|
	 * @param s # of "x" absent in sets A and B <-> |U| - |A U B|  
	 * @return Similarity Index (range from 0 to 1)
	 */
	double calculate (int p, int q, int r, int s);
	
}
