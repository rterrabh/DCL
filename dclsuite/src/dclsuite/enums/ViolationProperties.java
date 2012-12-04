package dclsuite.enums;

public enum ViolationProperties {
	VIOLATION_TYPE("violationType","Type of violation"),
	AST_OFFSET("astOffset","AST offset"),
	AST_LENGTH("astLength","AST length"),
	VIOLATED_CONSTRAINT("violatedConstraint","Violated Architectural Constraint"),
	CONSTRAINT("constraint","Constraint"),
	DEPENDENCY_TYPE("dependencyType","Dependency Type"),
	CLASS_NAME_A("classNameA","Source classname"),
	METHOD_NAME_A("methodNameA","Source class method name"),
	FIELD_NAME_A("fieldNameA","Source class field name"),
	VARIABLE_NAME_A("variableNameA","Source class variable name"),
	MODULE_DESCRIPTION_A("moduleDescriptionA","Source Module Description"),
	MODULE_DESCRIPTION_B("moduleDescriptionB","Target Module Description"),
	CLASS_NAME_B("classNameB","Target classname"),
	METHOD_NAME_B("methodNameB","Target class method name"),
	FIELD_NAME_B("fieldNameB","Target class field name"),
	LINE_NUMBER_A("lineNumberA","Line Number"),
	DETAILED_MESSAGE("detailedMessage","Detailed Message"),
	STATIC_ACCESS("staticAccess","Static Access");
	
	private final String key;
	private final String label;
	
	private ViolationProperties(String key, String label){
		this.key = key;
		this.label = label;
	}
	
	public String getKey() {
		return this.key;
	}
	
	public String getLabel() {
		return this.label;
	}
	
}
