package dclsuite.dependencies;

public abstract class HandleDependency extends Dependency {

	protected HandleDependency(String classNameA, String classNameB,
			Integer lineNumber) {
		super(classNameA, classNameB, lineNumber);
	}
	
}