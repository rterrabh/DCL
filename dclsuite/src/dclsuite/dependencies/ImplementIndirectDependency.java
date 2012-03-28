package dclsuite.dependencies;

public final class ImplementIndirectDependency extends ImplementDependency {

	public ImplementIndirectDependency(String classNameA, String classNameB, Integer lineNumber) {
		super(classNameA, classNameB, lineNumber);
	}
	
	
	@Override
	public String toString() {
		return "'" + 
				this.classNameA + "' indirectly implements '" + this.classNameB + "'";
	}

	
}
