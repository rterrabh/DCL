package dclsuite.dependencies;

import java.util.Properties;

import dclsuite.enums.ViolationProperties;

public final class DeclareFieldDependency extends DeclareDependency {
	private final String fieldNameA;
	
	public DeclareFieldDependency(String classNameA, String classNameB, Integer lineNumber, String fieldNameA) {
		super(classNameA,classNameB,lineNumber);
		this.fieldNameA = fieldNameA;
	}

	public String getFieldName() {
		return this.fieldNameA;
	}
	
	@Override
	public String toString() {
		return "'" + 
				this.classNameA + "' contains the field '" + this.fieldNameA + 
				"' whose type is '" + this.classNameB + "'";
	}	
	
	@Override
	public Properties props() {
		Properties props = super.props();
		props.put(ViolationProperties.FIELD_NAME_A.getKey(), this.fieldNameA);
		return props;
	}
}