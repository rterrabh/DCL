package dclsuite.dependencies;

import dclsuite.enums.DependencyType;


public abstract class ExtendDependency extends DeriveDependency {
	public ExtendDependency(String classNameA, String classNameB, Integer lineNumber) {
		super(classNameA,classNameB,lineNumber);
	}
		
	@Override
	public DependencyType getDependencyType() {
		return DependencyType.EXTEND;
	}
}