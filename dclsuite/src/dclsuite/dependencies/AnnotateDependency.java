package dclsuite.dependencies;

import dclsuite.enums.DependencyType;

public abstract class AnnotateDependency extends Dependency {
	
	protected AnnotateDependency(String classNameA, String classNameB, Integer lineNumber) {
		super(classNameA,classNameB,lineNumber);
	}
	
	@Override
	public DependencyType getDependencyType() {
		return DependencyType.ANNOTATE;
	}

}