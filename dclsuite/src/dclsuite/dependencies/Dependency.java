package dclsuite.dependencies;

import java.io.Serializable;
import java.util.Properties;

import dclsuite.enums.DependencyType;
import dclsuite.enums.ViolationProperties;


public abstract class Dependency implements Serializable {
	protected final String classNameA;
	protected final String classNameB;
	protected final Integer lineNumberA;
	
	protected Dependency(String classNameA, String classNameB, Integer lineNumberA) {
		super();
		this.classNameA = classNameA; 
		this.classNameB = classNameB;
		this.lineNumberA = lineNumberA;
	}

	public String getClassNameA() {
		return this.classNameA;
	}

	public String getClassNameB() {
		return this.classNameB;
	}

	public Integer getLineNumber() {
		return lineNumberA;
	}
	
	public Properties props(){
		Properties props = new Properties();
		props.put(ViolationProperties.CLASS_NAME_A.getKey(), this.classNameA);
		props.put(ViolationProperties.CLASS_NAME_B.getKey(), this.classNameB);
		props.put(ViolationProperties.LINE_NUMBER_A.getKey(), (this.lineNumberA!=null) ? this.lineNumberA.toString() : "");
		return props;
	}
	
	public final boolean sameType(Dependency other){
		return (this.getDependencyType().equals(other.getDependencyType())
				&& this.classNameB.equals(other.classNameB));
	}
	
	public abstract DependencyType getDependencyType();
}