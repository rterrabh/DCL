package dclsuite.dependencies;

public final class ImplementDirectDependency extends ImplementDependency {

	public ImplementDirectDependency(String classNameA, String classNameB, Integer lineNumber) {
		super(classNameA, classNameB, lineNumber);
	}
	
	@Override
	public String toString() {
		return "'" + 
				this.classNameA + "' directly implements '" + this.classNameB + "'";
	}

	
}
