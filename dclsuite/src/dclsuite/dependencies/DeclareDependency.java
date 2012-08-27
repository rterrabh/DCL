package dclsuite.dependencies;

import dclsuite.enums.DependencyType;

public class DeclareDependency extends HandleDependency {

	public DeclareDependency(String classNameA, String classNameB, Integer lineNumberA, Integer offset, Integer length) {
		super(classNameA, classNameB, lineNumberA, offset, length);
	}
	
	@Override
	public DependencyType getDependencyType() {
		return DependencyType.DECLARE;
	}
}