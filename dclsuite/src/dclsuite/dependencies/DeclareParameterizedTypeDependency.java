package dclsuite.dependencies;

import java.util.Properties;

import dclsuite.enums.ViolationProperties;

public final class DeclareParameterizedTypeDependency extends DeclareDependency {
	private final String methodNameA;
	
	public DeclareParameterizedTypeDependency(String classNameA, String classNameB, Integer lineNumberA, Integer offset, Integer length) {
		super(classNameA, classNameB, lineNumberA, offset, length);
		this.methodNameA = null;
	}
	
	public DeclareParameterizedTypeDependency(String classNameA, String classNameB, Integer lineNumberA, Integer offset, Integer length, String methodNameA) {
		super(classNameA, classNameB, lineNumberA, offset, length);
		this.methodNameA = methodNameA;
	}
	
	public String getMethodNameA() {
		return this.methodNameA;
	}

	@Override
	public String toString() {
		return "'" + this.classNameA + "' contains the parameterized type '" + this.classNameB + "'"
				+ ((methodNameA!=null) ? " (inside '"+ this.methodNameA + "'" : "") ;
	}
	
	@Override
	public Properties props() {
		Properties props = super.props();
		props.put(ViolationProperties.METHOD_NAME_A.getKey(), this.methodNameA);
		return props;
	}
}