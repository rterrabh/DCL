package dclsuite.dependencies;

public final class AnnotateClassDependency extends AnnotateDependency {
	
	public AnnotateClassDependency(String classNameA, String classNameB, Integer lineNumberA, Integer offset, Integer length) {
		super(classNameA,classNameB,lineNumberA, offset, length);
	}
	
	@Override
	public String toString() {
		return "'" + 
				this.classNameA + "' is annotated by '" + 
				this.classNameB + "'";
	}
	
}