package dclsuite.dependencies;

import java.util.Properties;

import dclsuite.enums.ViolationProperties;

public final class AccessMethodDependency extends AccessDependency {
	private final String methodNameA;
	private final String methodNameB;
	private final boolean staticAccess;
	
	public AccessMethodDependency(String classNameA, String classNameB, Integer lineNumberA, Integer offset, Integer length, String methodNameA, String methodNameB, boolean staticAccess) {
		super(classNameA,classNameB,lineNumberA, offset, length);
		this.methodNameA = methodNameA;
		this.methodNameB = methodNameB;
		this.staticAccess = staticAccess;
	}

	public String getMethodNameA() {
		return this.methodNameA;
	}
	
	public String getMethodNameB() {
		return this.methodNameB;
	}

	public boolean isStaticInvoke() {
		return this.staticAccess;
	}
	
	@Override
	public String toString() {
		return "'" + 
				this.classNameA + "' contains the method '" + this.methodNameA + 
				"' that " + ((staticAccess) ? "statically " : "") + "invokes the method '" + 
				this.methodNameB + "' of an object of '" + this.classNameB + "'";
	}
	
	@Override
	public Properties props() {
		Properties props = super.props();
		props.put(ViolationProperties.METHOD_NAME_A.getKey(), this.methodNameA);
		props.put(ViolationProperties.METHOD_NAME_B.getKey(), this.methodNameB);
		props.put(ViolationProperties.STATIC_ACCESS.getKey(), "" + this.staticAccess);
		return props;
	}
}