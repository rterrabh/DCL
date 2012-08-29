package dclsuite.dependencies;

import java.util.Properties;

import dclsuite.enums.DependencyType;
import dclsuite.enums.ViolationProperties;
import dclsuite.util.DCLUtil;

public final class ThrowDependency extends Dependency {
	private final String methodNameA;
	
	public ThrowDependency(String classNameA, String classNameB, Integer lineNumberA, Integer offset, Integer length, String methodNameA) {
		super(classNameA,classNameB,lineNumberA, offset, length);
		this.methodNameA = methodNameA;
	}

	public String getMethodName() {
		return this.methodNameA;
	}
	
	@Override
	public String toString() {
		return "'" + 
				this.classNameA + "' contains the method '" + 
				this.methodNameA + "' which throws '" + this.classNameB + "'";
	}
	
	@Override
	public String toShortString() {
		return "The throwing of " + DCLUtil.getSimpleClassName(this.classNameB) + " is disallowed for this location w.r.t. the architecture";
	}
	
	@Override
	public DependencyType getDependencyType() {
		return DependencyType.THROW;
	}
	
	@Override
	public Properties props() {
		Properties props = super.props();
		props.put(ViolationProperties.METHOD_NAME_A.getKey(), this.methodNameA);
		return props;
	}
		
}