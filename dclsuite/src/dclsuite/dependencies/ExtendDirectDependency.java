package dclsuite.dependencies;

public final class ExtendDirectDependency extends ExtendDependency {

	public ExtendDirectDependency(String classNameA, String classNameB, Integer lineNumber) {
		super(classNameA, classNameB, lineNumber);
	}
	
	@Override
	public String toString() {
		return "'" + 
				this.classNameA + "' directly extends '" + this.classNameB + "'";
	}

	
}
