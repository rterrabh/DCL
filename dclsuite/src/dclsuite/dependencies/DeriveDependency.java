package dclsuite.dependencies;

import dclsuite.util.DCLUtil;

public abstract class DeriveDependency extends Dependency {

	protected DeriveDependency(String classNameA, String classNameB, Integer lineNumberA, Integer offset, Integer length) {
		super(classNameA, classNameB, lineNumberA, offset, length);
	}
	
	@Override
	public String toShortString() {
		return "The inheritance of " + DCLUtil.getSimpleClassName(this.classNameB) + " is disallowed for this location w.r.t. the architecture";
	}
	
}