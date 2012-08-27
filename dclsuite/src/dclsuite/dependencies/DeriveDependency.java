package dclsuite.dependencies;

public abstract class DeriveDependency extends Dependency {

	protected DeriveDependency(String classNameA, String classNameB, Integer lineNumberA, Integer offset, Integer length) {
		super(classNameA, classNameB, lineNumberA, offset, length);
	}
	
}