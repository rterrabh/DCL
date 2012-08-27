package dclsuite.dependencies;

public final class ImplementDirectDependency extends ImplementDependency {

	public ImplementDirectDependency(String classNameA, String classNameB, Integer lineNumberA, Integer offset, Integer length) {
		super(classNameA, classNameB, lineNumberA, offset, length);
	}
	
	@Override
	public String toString() {
		return "'" + 
				this.classNameA + "' directly implements '" + this.classNameB + "'";
	}

	
}
