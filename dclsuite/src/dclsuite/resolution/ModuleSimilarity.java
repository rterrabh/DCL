package dclsuite.resolution;

public class ModuleSimilarity {
	private final String moduleDescription;
	private double similarity;
	
	public ModuleSimilarity(String moduleDescription, double similarity) {
		this.moduleDescription = moduleDescription;
		this.similarity = similarity;
	}
	
	public String getModuleDescription() {
		return this.moduleDescription;
	}
	
	public double getSimilarity() {
		return this.similarity;
	}
}
