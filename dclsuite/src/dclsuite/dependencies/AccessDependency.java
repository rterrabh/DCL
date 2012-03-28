package dclsuite.dependencies;

import dclsuite.enums.DependencyType;

public abstract class AccessDependency extends HandleDependency {

	protected AccessDependency(String classNameA, String classNameB,
			Integer lineNumber) {
		super(classNameA, classNameB, lineNumber);
	}
	
	@Override
	public DependencyType getDependencyType() {
		return DependencyType.ACCESS;
	}
	
}