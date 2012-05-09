package dclsuite.resolution;

public class ModuleSimilarity {
	private final String moduleDescription;
	private double similarity;
	private final Strategy strategy;

	public ModuleSimilarity(String moduleDescription, double similarity, Strategy strategy) {
		this.moduleDescription = moduleDescription;
		this.similarity = similarity;
		this.strategy = strategy;
	}

	public String getModuleDescription() {
		return this.moduleDescription;
	}

	public double getSimilarity() {
		return this.similarity;
	}

	public Strategy getStrategy() {
		return strategy;
	}

	public static enum Strategy {
		PARTICULAR_DEPENDENCY, ALL_DEPENDENCIES;
		
		public String toString() {
			if (this==PARTICULAR_DEPENDENCY){
				return "PD";	
			}else if (this==ALL_DEPENDENCIES){
				return "AD";	
			}
			return null;
		};
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((moduleDescription == null) ? 0 : moduleDescription.hashCode());
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
		if (moduleDescription == null) {
			if (other.moduleDescription != null)
				return false;
		} else if (!moduleDescription.equals(other.moduleDescription))
			return false;
		return true;
	}

}
