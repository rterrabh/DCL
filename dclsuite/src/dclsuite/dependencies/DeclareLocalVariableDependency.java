package dclsuite.dependencies;

import java.util.Properties;

import dclsuite.enums.ViolationProperties;

public final class DeclareLocalVariableDependency extends DeclareDependency {
	private final String fieldNameA;
	private final String methodNameA;
	
	public DeclareLocalVariableDependency(String classNameA, String classNameB, Integer lineNumberA, Integer offset, Integer length, String methodNameA, String fieldNameA) {
		super(classNameA,classNameB,lineNumberA, offset, length);
		this.fieldNameA = fieldNameA;
		this.methodNameA = methodNameA;
	}

	public String getFieldName() {
		return this.fieldNameA;
	}

	public String getMethodName() {
		return methodNameA;
	}	
	
	@Override
	public String toString() {
		return "'" + 
				this.classNameA + "' contains the local variable '" + this.fieldNameA + 
				"' in method '" + this.methodNameA + "' whose type is '" + this.classNameB + "'";
	}	
	
	@Override
	public Properties props() {
		Properties props = super.props();
		props.put(ViolationProperties.FIELD_NAME_A.getKey(), this.fieldNameA);
		props.put(ViolationProperties.METHOD_NAME_A.getKey(), this.methodNameA);
		return props;
	}
}