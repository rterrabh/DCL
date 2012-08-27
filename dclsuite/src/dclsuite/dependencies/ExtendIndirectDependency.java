package dclsuite.dependencies;

public final class ExtendIndirectDependency extends ExtendDependency {

	public ExtendIndirectDependency(String classNameA, String classNameB, Integer lineNumberA, Integer offset, Integer length) {
		super(classNameA, classNameB, lineNumberA, offset, length);
	}
	
	@Override
	public String toString() {
		return "'" + 
				this.classNameA + "' indirectly extends '" + this.classNameB + "'";
	}
	
}
