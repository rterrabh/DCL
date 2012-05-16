package dclsuite.dependencies;

import dclsuite.enums.DependencyType;

public class AnnotateDependency extends Dependency {
	
	public AnnotateDependency(String classNameA, String classNameB, Integer lineNumber) {
		super(classNameA,classNameB,lineNumber);
	}
	
	@Override
	public DependencyType getDependencyType() {
		return DependencyType.ANNOTATE;
	}

}