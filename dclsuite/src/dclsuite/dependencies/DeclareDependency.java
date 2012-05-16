package dclsuite.dependencies;

import dclsuite.enums.DependencyType;

public class DeclareDependency extends HandleDependency {

	public DeclareDependency(String classNameA, String classNameB, Integer lineNumber) {
		super(classNameA, classNameB, lineNumber);
	}
	
	@Override
	public DependencyType getDependencyType() {
		return DependencyType.DECLARE;
	}
}