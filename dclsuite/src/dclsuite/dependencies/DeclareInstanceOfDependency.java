package dclsuite.dependencies;

import java.util.Properties;

import dclsuite.enums.ViolationProperties;

public final class DeclareInstanceOfDependency extends DeclareDependency {
	private final String methodNameA;
	
	public DeclareInstanceOfDependency(String classNameA, String classNameB, Integer lineNumberA, Integer offset, Integer length, String methodNameA) {
		super(classNameA,classNameB,lineNumberA, offset, length);
		this.methodNameA = methodNameA;
	}

	public String getMethodName() {
		return methodNameA;
	}	
	
	@Override
	public String toString() {
		return "'" + 
				this.classNameA + "' contains method '" + this.methodNameA + 
				"' that contains an instanceof statement related to class '" + this.classNameB + "'";
	}	
	
	@Override
	public Properties props() {
		Properties props = super.props();
		props.put(ViolationProperties.METHOD_NAME_A.getKey(), this.methodNameA);
		return props;
	}
}