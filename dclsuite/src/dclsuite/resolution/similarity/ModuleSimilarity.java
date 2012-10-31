package dclsuite.resolution.similarity;

import dclsuite.resolution.similarity.coefficients.ICoefficientStrategy;
import dclsuite.util.FormatUtil;

public class ModuleSimilarity implements Comparable<ModuleSimilarity>{
	private final String moduleDescription;
	private final double similarity;
	private final CoverageStrategy coverageStrategy;
	private final ICoefficientStrategy coefficientStrategy;

	public ModuleSimilarity(String moduleDescription, double similarity, CoverageStrategy coverageStrategy, ICoefficientStrategy coefficientStrategy) {
		this.moduleDescription = moduleDescription;
		this.similarity = similarity;
		this.coverageStrategy = coverageStrategy;
		this.coefficientStrategy = coefficientStrategy;
	}

	public String getModuleDescription() {
		return this.moduleDescription;
	}

	public double getSimilarity() {
		return this.similarity;
	}

	public CoverageStrategy getCoverageStrategy() {
		return this.coverageStrategy;
	}
	
	public ICoefficientStrategy getCoefficientStrategy() {
		return this.coefficientStrategy;
	}

		
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((coefficientStrategy == null) ? 0 : coefficientStrategy.hashCode());
		result = prime * result + ((coverageStrategy == null) ? 0 : coverageStrategy.hashCode());
		result = prime * result + ((moduleDescription == null) ? 0 : moduleDescription.hashCode());
		long temp;
		temp = Double.doubleToLongBits(similarity);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ModuleSimilarity other = (ModuleSimilarity) obj;
		if (coefficientStrategy == null) {
			if (other.coefficientStrategy != null)
				return false;
		} else if (!coefficientStrategy.equals(other.coefficientStrategy))
			return false;
		if (coverageStrategy != other.coverageStrategy)
			return false;
		if (moduleDescription == null) {
			if (other.moduleDescription != null)
				return false;
		} else if (!moduleDescription.equals(other.moduleDescription))
			return false;
		if (Double.doubleToLongBits(similarity) != Double.doubleToLongBits(other.similarity))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return this.moduleDescription + "(" + FormatUtil.formatDouble(this.similarity) + "_" + this.coverageStrategy.getType() + "_" + this.coefficientStrategy.getName() + ")";
	}
	
	public String getInfo(){
		return "(" + FormatUtil.formatDouble(this.similarity) + "_" + this.coverageStrategy.getType() + "_" + this.coefficientStrategy.getName() + ")";
	}
	
	public static enum CoverageStrategy {
		PARTICULAR_DEPENDENCY, ALL_DEPENDENCIES, ONLY_TYPES;
		
		public String getType() {
			if (this==PARTICULAR_DEPENDENCY){
				return "PD";	
			}else if (this==ALL_DEPENDENCIES){
				return "AD";	
			}else if (this==ONLY_TYPES){
				return "T";	
			}
			return null;
		};
	}

	@Override
	public int compareTo(ModuleSimilarity o) {
		return ((Double)o.similarity).compareTo(this.similarity);
	}

}
