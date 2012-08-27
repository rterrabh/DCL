package dclsuite.dependencies;

public final class ExtendDirectDependency extends ExtendDependency {

	public ExtendDirectDependency(String classNameA, String classNameB, Integer lineNumberA, Integer offset, Integer length) {
		super(classNameA, classNameB, lineNumberA, offset, length);
	}
	
	@Override
	public String toString() {
		return "'" + 
				this.classNameA + "' directly extends '" + this.classNameB + "'";
	}

	
}
