package dclsuite.dependencies;

public abstract class HandleDependency extends Dependency {

	protected HandleDependency(String classNameA, String classNameB, Integer lineNumberA, Integer offset, Integer length) {
		super(classNameA, classNameB, lineNumberA, offset, length);
	}
	
}