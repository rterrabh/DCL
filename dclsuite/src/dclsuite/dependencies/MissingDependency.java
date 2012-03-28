package dclsuite.dependencies;

import dclsuite.enums.DependencyType;

public class MissingDependency {
	protected final String moduleDescriptionA;
	protected final String classNameA;
	protected final String moduleDescriptionB;
	protected final DependencyType dependencyType;

	public MissingDependency(String moduleDescriptionA, String classNameA, String moduleDescriptionB,
			DependencyType dependencyType) {
		super();
		this.moduleDescriptionA = moduleDescriptionA;
		this.classNameA = classNameA;
		this.moduleDescriptionB = moduleDescriptionB;
		this.dependencyType = dependencyType;
	}

	public String getClassNameA() {
		return classNameA;
	}

	public String getModuleDescriptionA() {
		return moduleDescriptionA;
	}

	public String getModuleDescriptionB() {
		return moduleDescriptionB;
	}

	public DependencyType getDependencyType() {
		return dependencyType;
	}
}
