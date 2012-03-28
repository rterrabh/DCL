package dclsuite.dependencies;

public final class ExtendIndirectDependency extends ExtendDependency {

	public ExtendIndirectDependency(String classNameA, String classNameB, Integer lineNumber) {
		super(classNameA, classNameB, lineNumber);
	}
	
	@Override
	public String toString() {
		return "'" + 
				this.classNameA + "' indirectly extends '" + this.classNameB + "'";
	}
	
}
