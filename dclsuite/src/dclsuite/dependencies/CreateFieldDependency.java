package dclsuite.dependencies;

import java.util.Properties;

import dclsuite.enums.ViolationProperties;

public class CreateFieldDependency extends CreateDependency {
	private String fieldNameA;
	
	public CreateFieldDependency(String classNameA, String classNameB, Integer lineNumber, String fieldNameA) {
		super(classNameA,classNameB,lineNumber);
		this.fieldNameA = fieldNameA;
	}
	
	public String toString() {
		return "'" + 
				this.classNameA + "' contains the field '" + this.fieldNameA + 
				"' that receives an instantiation of an object of '" + this.classNameB + "'";
	}
	
	@Override
	public Properties props() {
		Properties props = super.props();
		props.put(ViolationProperties.FIELD_NAME_A.getKey(), this.fieldNameA);
		return props;
	}
	
}