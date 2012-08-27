package dclsuite.dependencies;

import java.util.Properties;

import dclsuite.enums.ViolationProperties;

public class CreateMethodDependency extends CreateDependency {
	private String methodNameA;
	
	public CreateMethodDependency(String classNameA, String classNameB, Integer lineNumberA, Integer offset, Integer length, String methodNameA) {
		super(classNameA,classNameB,lineNumberA, offset, length);
		this.methodNameA = methodNameA;
	}
	
	public String toString() {
		return "'" + 
				this.classNameA + "' contains the method '" + this.methodNameA + 
				"' that creates an object of '" + this.classNameB + "'";
	}
	
	@Override
	public Properties props() {
		Properties props = super.props();
		props.put(ViolationProperties.METHOD_NAME_A.getKey(), this.methodNameA);
		return props;
	}
	
}