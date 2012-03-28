package dclsuite.dependencies;

import dclsuite.enums.DependencyType;

public abstract class DeclareDependency extends HandleDependency {

	protected DeclareDependency(String classNameA, String classNameB, Integer lineNumber) {
		super(classNameA, classNameB, lineNumber);
	}
	
	@Override
	public DependencyType getDependencyType() {
		return DependencyType.DECLARE;
	}
}