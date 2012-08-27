package dclsuite.dependencies;

import dclsuite.enums.DependencyType;

public class AnnotateDependency extends Dependency {
	
	public AnnotateDependency(String classNameA, String classNameB, Integer lineNumberA, Integer offset, Integer length) {
		super(classNameA,classNameB,lineNumberA,offset,length);
	}
	
	@Override
	public DependencyType getDependencyType() {
		return DependencyType.USEANNOTATION;
	}

}