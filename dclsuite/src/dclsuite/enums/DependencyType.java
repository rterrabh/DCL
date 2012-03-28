package dclsuite.enums;

import dclsuite.dependencies.AccessDependency;
import dclsuite.dependencies.AnnotateDependency;
import dclsuite.dependencies.CreateDependency;
import dclsuite.dependencies.DeclareDependency;
import dclsuite.dependencies.Dependency;
import dclsuite.dependencies.DeriveDependency;
import dclsuite.dependencies.ExtendDependency;
import dclsuite.dependencies.HandleDependency;
import dclsuite.dependencies.ImplementDependency;
import dclsuite.dependencies.ThrowDependency;

public enum DependencyType {
	ACCESS("access", AccessDependency.class), 
	ANNOTATE("annotate", AnnotateDependency.class),
	CREATE("create", CreateDependency.class), 
	DECLARE("declare", DeclareDependency.class), 
	DERIVE("derive", DeriveDependency.class), 
	EXTEND("extend", ExtendDependency.class), 
	HANDLE("handle", HandleDependency.class),
	IMPLEMENT("implement", ImplementDependency.class), 
	THROW("throw", ThrowDependency.class),
	DEPEND("depend", Dependency.class);
	
	private final String value;
	private final Class<? extends Dependency> dependencyClass;
	
	private DependencyType(String value, Class<? extends Dependency> dependencyClass) {
        this.value = value;
        this.dependencyClass = dependencyClass;
    }
	
	public String getValue() {
		return this.value;
	}
	
	public Class<? extends Dependency> getDependencyClass() {
		return this.dependencyClass;
	}
}