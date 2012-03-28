package dclsuite.dependencies;

public abstract class DeriveDependency extends Dependency {

	protected DeriveDependency(String classNameA, String classNameB,
			Integer lineNumber) {
		super(classNameA, classNameB, lineNumber);
	}
	
}