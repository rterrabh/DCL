package dclsuite.dependencies;

import java.util.Properties;

import dclsuite.enums.ViolationProperties;

public final class AnnotateMethodDependency extends AnnotateDependency {
	private final String methodNameA;
	
	public AnnotateMethodDependency(String classNameA, String classNameB, Integer lineNumber, String methodNameA) {
		super(classNameA,classNameB,lineNumber);
		this.methodNameA = methodNameA;
	}

	public String getMethodName() {
		return this.methodNameA;
	}
	
	public String toString() {
		return "'" + 
				this.classNameA + "' contains the method '" + this.methodNameA + 
				"' which is annotated by '" + this.classNameB + "'";
	}
	
	@Override
	public Properties props() {
		Properties props = super.props();
		props.put(ViolationProperties.METHOD_NAME_A.getKey(), this.methodNameA);
		return props;
	}
}