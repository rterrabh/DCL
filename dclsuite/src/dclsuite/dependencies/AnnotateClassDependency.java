package dclsuite.dependencies;

public final class AnnotateClassDependency extends AnnotateDependency {
	
	public AnnotateClassDependency(String classNameA, String classNameB, Integer lineNumber) {
		super(classNameA,classNameB,lineNumber);
	}
	
	@Override
	public String toString() {
		return "'" + 
				this.classNameA + "' is annotated by '" + 
				this.classNameB + "'";
	}
	
}