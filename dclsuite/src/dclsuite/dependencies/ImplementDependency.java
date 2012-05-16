package dclsuite.dependencies;

import dclsuite.enums.DependencyType;

public class ImplementDependency extends DeriveDependency {
	public ImplementDependency(String classNameA, String classNameB, Integer lineNumber) {
		super(classNameA,classNameB,lineNumber);
	}
		
	@Override
	public DependencyType getDependencyType() {
		return DependencyType.IMPLEMENT;
	}
}