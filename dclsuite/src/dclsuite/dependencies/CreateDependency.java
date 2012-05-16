package dclsuite.dependencies;

import dclsuite.enums.DependencyType;

public class CreateDependency extends Dependency {
	public CreateDependency(String classNameA, String classNameB, Integer lineNumber) {
		super(classNameA, classNameB, lineNumber);
	}

	@Override
	public DependencyType getDependencyType() {
		return DependencyType.CREATE;
	}
}