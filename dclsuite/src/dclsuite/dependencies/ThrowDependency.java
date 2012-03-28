package dclsuite.dependencies;

import java.util.Properties;

import dclsuite.enums.DependencyType;
import dclsuite.enums.ViolationProperties;

public final class ThrowDependency extends Dependency {
	private final String methodNameA;
	
	public ThrowDependency(String classNameA, String classNameB, Integer lineNumber, String methodNameA) {
		super(classNameA,classNameB,lineNumber);
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