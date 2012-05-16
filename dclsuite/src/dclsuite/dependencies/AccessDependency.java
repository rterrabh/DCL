package dclsuite.dependencies;

import dclsuite.enums.DependencyType;

public class AccessDependency extends HandleDependency {

	public AccessDependency(String classNameA, String classNameB,
			Integer lineNumber) {
		super(classNameA, classNameB, lineNumber);
	}
	
	@Override
	public DependencyType getDependencyType() {
		return DependencyType.ACCESS;
	}
	
}