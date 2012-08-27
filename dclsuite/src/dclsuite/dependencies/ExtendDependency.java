package dclsuite.dependencies;

import dclsuite.enums.DependencyType;


public class ExtendDependency extends DeriveDependency {
	public ExtendDependency(String classNameA, String classNameB, Integer lineNumberA, Integer offset, Integer length) {
		super(classNameA,classNameB,lineNumberA, offset, length);
	}
		
	@Override
	public DependencyType getDependencyType() {
		return DependencyType.EXTEND;
	}
}