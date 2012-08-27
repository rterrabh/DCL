package dclsuite.dependencies;

import java.util.Properties;

import dclsuite.enums.ViolationProperties;

public class DeclareReturnDependency extends DeclareDependency {
	private final String methodNameA;

	public DeclareReturnDependency(String classNameA, String classNameB, Integer lineNumberA, Integer offset,
			Integer length, String methodNameA) {
		super(classNameA, classNameB, lineNumberA, offset, length);
		this.methodNameA = methodNameA;
	}

	public String getMethodName() {
		return methodNameA;
	}

	@Override
	public String toString() {
		return "'" + this.classNameA + "' contains the method '" + this.methodNameA + "' whose return type is '"
				+ this.classNameB + "'";
	}

	@Override
	public Properties props() {
		Properties props = super.props();
		props.put(ViolationProperties.METHOD_NAME_A.getKey(), this.methodNameA);
		return props;
	}
}