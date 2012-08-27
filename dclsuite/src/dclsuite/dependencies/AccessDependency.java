package dclsuite.dependencies;

import dclsuite.enums.DependencyType;

public class AccessDependency extends HandleDependency {

	public AccessDependency(String classNameA, String classNameB, Integer lineNumberA, Integer offset, Integer length) {
		super(classNameA, classNameB, lineNumberA, offset, length);
	}
	
	@Override
	public DependencyType getDependencyType() {
		return DependencyType.ACCESS;
	}
	
}