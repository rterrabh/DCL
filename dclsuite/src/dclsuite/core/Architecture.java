package dclsuite.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;

import dclsuite.core.DependencyConstraint.ArchitecturalDrift;
import dclsuite.core.parser.DCLParser;
import dclsuite.dependencies.Dependency;
import dclsuite.enums.DependencyType;
import dclsuite.exception.ParseException;
import dclsuite.util.DCLUtil;
import dclsuite.util.MarkerUtils;

public class Architecture {
	private static final boolean DEBUG = false;

	/**
	 * String: class name Collection<Dependency>: Collection of established
	 * dependencies
	 */
	public Map<String, Collection<Dependency>> projectClasses = null;

	/**
	 * String: module name String: module description
	 */
	public Map<String, String> modules = null;

	/**
	 * Collection<DependencyConstraint>: Collection of dependency constraints
	 */
	public Collection<DependencyConstraint> dependencyConstraints = null;

	public Architecture(IProject project) throws CoreException, ParseException {
		if (DEBUG) {
			System.out.println("Time BEFORE generate architecture (without dependencies): " + new Date());
		}
		this.projectClasses = new HashMap<String, Collection<Dependency>>();
		this.modules = new ConcurrentHashMap<String, String>();

		for (String className : DCLUtil.getClassNames(project)) {
			this.projectClasses.put(className, null);
		}

		this.initializeDependencyConstraints(project);
		if (DEBUG) {
			System.out.println("Time AFTER generate architecture (without dependencies): " + new Date());
		}
	}

	private void initializeDependencyConstraints(IProject project) throws CoreException, ParseException {
		try {
			final IFile dcFile = project.getFile(DCLUtil.DC_FILENAME);

			this.modules.putAll(DCLParser.parseModules(dcFile.getContents()));

			/* Define implicit modules */
			this.modules.put("$java", DCLUtil.getJavaModuleDefinition());
			/*
			 * Module $system has its behavior in
			 * DCLUtil.hasClassNameByDescription
			 */

			this.dependencyConstraints = DCLParser.parseDependencyConstraints(dcFile.getContents());
		} catch (ParseException e) {
			throw e;
		} catch (Throwable e) {
			MarkerUtils.addErrorMarker(project, "The " + DCLUtil.DC_FILENAME + " is invalid.");
			throw new CoreException(Status.CANCEL_STATUS);
		}
	}

	public void updateDependencyConstraints(IProject project) throws CoreException, ParseException {
		this.modules.clear();
		System.gc(); /* Suggesting the execution of the Garbage Collector */
		this.initializeDependencyConstraints(project);
	}

	public Set<String> getProjectClasses() {
		return projectClasses.keySet();
	}

	public Collection<Dependency> getDependencies(String className) {
		return projectClasses.get(className);
	}

	public Dependency getDependency(String classNameA, String classNameB, Integer lineNumberA, DependencyType dependencyType) {
		Collection<Dependency> dependencies = projectClasses.get(classNameA);
		for (Dependency d : dependencies) {
			if (lineNumberA == null) {

			}
			if ((lineNumberA == null) ? d.getLineNumber() == null : lineNumberA.equals(d.getLineNumber())
					&& d.getClassNameB().equals(classNameB) && d.getDependencyType().equals(dependencyType)) {
				return d;
			}
		}
		return null;
	}

	public void updateDependencies(String className, Collection<Dependency> dependencies) {
		projectClasses.put(className, dependencies);
	}

	public Collection<DependencyConstraint> getDependencyConstraints() {
		return this.dependencyConstraints;
	}

	public Map<String, String> getModules() {
		return this.modules;
	}

	/**
	 * Method used to check if a particular dependency is allowed or not. It is
	 * used, e.g., for the DCLfix module.
	 */
	public boolean can(String classNameA, String classNameB, DependencyType dependencyType, IProject project) throws CoreException {
		final Collection<Dependency> dependencies = new ArrayList<Dependency>(1);
		dependencies.add(dependencyType.createGenericDependency(classNameA, classNameB));

		for (DependencyConstraint dc : this.getDependencyConstraints()) {
			List<ArchitecturalDrift> violations = dc.validate(classNameA, modules, this.getProjectClasses(), dependencies, project);
			if (violations != null && !violations.isEmpty()) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Method used to check if some class of the system is allowed to establish
	 * a particular dependency. It is used, e.g., for the DCLfix module.
	 */
	public boolean someclassCan(String classNameB, DependencyType dependencyType, IProject project) throws CoreException {

		for (String classNameA : this.getProjectClasses()) {
			if (classNameA.equals(classNameB)) {
				continue;
			}

			final Collection<Dependency> dependencies = new ArrayList<Dependency>(1);
			dependencies.add(dependencyType.createGenericDependency(classNameA, classNameB));

			boolean flag = true; /* It initially considers that it can */
			for (DependencyConstraint dc : this.getDependencyConstraints()) {
				/*
				 * Case we find some violation in this dependency in any
				 * dependency constraint, we set flag false
				 */
				if (dc.validate(classNameA, modules, this.getProjectClasses(), dependencies, project) != null
						&& !dc.validate(classNameA, modules, this.getProjectClasses(), dependencies, project).isEmpty()) {
					flag = false;
				}
			}
			/* If we did not find any violation for this class, it can! */
			if (flag) {
				return true;
			}
		}

		return false;
	}

	public Set<String> getUsedClasses(final String className) {
		Set<String> set = new HashSet<String>();

		for (Dependency d : this.getDependencies(className)) {
			set.add(d.getClassNameB());
		}

		return set;
	}

	public Set<String> getUsedClasses(final String className, DependencyType dependencyType) {
		/* In this case, it only considers the type */
		if (dependencyType == null) {
			return getUsedClasses(className);
		}

		Set<String> set = new HashSet<String>();

		/*
		 * Here, two cases: 
		 * if (dependencyType == DEPEND) -> dep[*,T] 
		 * if (dependencyType == other) -> dep[other,T] 
		 * For example: dep[access,T]
		 */
		for (Dependency d : this.getDependencies(className)) {
			if (dependencyType.equals(DependencyType.DEPEND) || d.getDependencyType().equals(dependencyType)) {
				set.add("dep[" + d.getDependencyType().getValue() + "," + d.getClassNameB() + "]");	
			}
		}

		return set;
	}

	public Set<String> getUniverseOfUsedClasses() {
		Set<String> set = new HashSet<String>();

		for (Collection<Dependency> col : projectClasses.values()) {
			for (Dependency d : col) {
				set.add(d.getClassNameB());
			}
		}

		return set;
	}

	public Set<String> getUniverseOfUsedClasses(DependencyType dependencyType) {
		if (dependencyType == null) {
			return getUniverseOfUsedClasses();
		}
		Set<String> set = new HashSet<String>();

		/*
		 * Here, two cases: 
		 * if (dependencyType == DEPEND) -> dep[*,T] 
		 * if (dependencyType == other) -> dep[other,T] 
		 * For example: dep[access,T]
		 */
		for (Collection<Dependency> col : projectClasses.values()) {
			for (Dependency d : col) {
				if (dependencyType.equals(DependencyType.DEPEND) || d.getDependencyType().equals(dependencyType)) {
					set.add("dep[" + d.getDependencyType().getValue() + "," + d.getClassNameB() + "]");	
				}
			}
		}

		return set;
	}

}
