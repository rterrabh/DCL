package dclsuite.resolution;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaCore;

import dclsuite.core.Architecture;
import dclsuite.dependencies.DeclareReturnDependency;
import dclsuite.dependencies.Dependency;
import dclsuite.dependencies.DeriveDependency;
import dclsuite.enums.ConstraintType;
import dclsuite.enums.DependencyType;
import dclsuite.resolution.similarity.ModuleSimilarity;
import dclsuite.util.DCLUtil;

public final class Functions {

	/**
	 * Auxiliary Function: "can" We actually forward to the function "can", as
	 * implemented by the checking tool
	 */
	public static boolean can(final String classNameA, final DependencyType dependencyType, final String classNameB,
			final Architecture architecture, final IProject project) throws CoreException {
		return architecture.can(classNameA, classNameB, dependencyType, project);
	}

	/**
	 * Auxiliary function: "super(t)"
	 */
	public static Collection<String> supertypes(final String originClassName, final IProject project, final Architecture architecture,
			final String classNameToFindSuperTypes, final DependencyType dependencyType) throws CoreException {
		Collection<String> result = new LinkedList<String>();

		IJavaProject javaProject = JavaCore.create(project);
		IType type = javaProject.findType(classNameToFindSuperTypes);

		ITypeHierarchy typeHierarchy = type.newTypeHierarchy(null);
		IType[] typeSuperclasses = typeHierarchy.getAllSupertypes(type);

		for (IType t : typeSuperclasses) {
			result.add(t.getFullyQualifiedName());
		}

		return result;
	}

	/**
	 * Auxiliary function: "super(t)" We also return only the super types that
	 * origin class is allowed to <dep>.
	 */
	public static Collection<String> supertypesAllowedTo(final String originClassName, final IProject project,
			final Architecture architecture, final String classNameToFindSuperTypes, final DependencyType dependencyType)
			throws CoreException {
		Collection<String> result = new LinkedList<String>();

		IJavaProject javaProject = JavaCore.create(project);
		IType type = javaProject.findType(classNameToFindSuperTypes);

		ITypeHierarchy typeHierarchy = type.newTypeHierarchy(null);
		IType[] typeSuperclasses = typeHierarchy.getAllSupertypes(type);

		for (IType t : typeSuperclasses) {
			if (architecture.can(originClassName, t.getFullyQualifiedName(), dependencyType, project)) {
				result.add(t.getFullyQualifiedName());
			}
		}

		return result;
	}

	/**
	 * Auxiliary function: "sub(t)"
	 */
	public static Collection<String> subtypes(final String originClassName, final IProject project, final Architecture architecture,
			final String classNameToFindSubTypes, final DependencyType dependencyType) throws CoreException {
		Collection<String> result = new LinkedList<String>();

		IJavaProject javaProject = JavaCore.create(project);
		IType type = javaProject.findType(classNameToFindSubTypes);

		ITypeHierarchy typeHierarchy = type.newTypeHierarchy(null);
		IType[] typeSubclasses = typeHierarchy.getAllSubtypes(type);

		for (IType t : typeSubclasses) {
			result.add(t.getFullyQualifiedName());
		}

		return result;
	}

	/**
	 * Auxiliary function: "sub(t)" We also return only the sub types that
	 * origin class is allowed to <dep>.
	 */
	public static Collection<String> subtypesAllowedTo(final String originClassName, final IProject project,
			final Architecture architecture, final String classNameToFindSubTypes, final DependencyType dependencyType)
			throws CoreException {
		Collection<String> result = new LinkedList<String>();

		IJavaProject javaProject = JavaCore.create(project);
		IType type = javaProject.findType(classNameToFindSubTypes);

		ITypeHierarchy typeHierarchy = type.newTypeHierarchy(null);
		IType[] typeSubclasses = typeHierarchy.getAllSubtypes(type);

		for (IType t : typeSubclasses) {
			if (architecture.can(originClassName, t.getFullyQualifiedName(), dependencyType, project)) {
				result.add(t.getFullyQualifiedName());
			}
		}

		return result;
	}

	/**
	 * Function to find a factory
	 */
	public static String[] factory(final IProject project, final Architecture architecture, final String classNameToFindFactory)
			throws CoreException {
		Collection<String> potentialFactoryReturnTypes = new LinkedHashSet<String>();
		potentialFactoryReturnTypes.add(classNameToFindFactory);

		if (architecture.getDependencies(classNameToFindFactory) != null) {
			for (Dependency d : architecture.getDependencies(classNameToFindFactory)) {
				if (d instanceof DeriveDependency && !d.getClassNameB().equals("java.lang.Object")) {
					potentialFactoryReturnTypes.add(d.getClassNameB());
				}
			}
		}

		for (String potentialFactoryReturnType : potentialFactoryReturnTypes) {

			for (String className : architecture.getProjectClasses()) {
				Collection<Dependency> dependencies = architecture.getDependencies(className);

				for (Dependency d : dependencies) {
					if (d instanceof DeclareReturnDependency) {
						if (d.getClassNameB().equals(potentialFactoryReturnType)) {
							DeclareReturnDependency drd = (DeclareReturnDependency) d;
							return new String[] { className, drd.getMethodName() };
						}
					}
				}
			}
		}
		return null;

	}

	public static boolean isModuleMequalModuleMa(final String className, final String moduleDescriptionA,
			final Set<ModuleSimilarity> suitableModules, final Map<String, String> modules, final Collection<String> projectClassNames,
			final IProject project, final ConstraintType constraintType) {
		if (constraintType == ConstraintType.ONLY_CAN) {
			return !isModuleMequalComplementModuleMa(className, moduleDescriptionA, suitableModules, modules, projectClassNames, project);
		} else {
			return isModuleMequalModuleMa(className, moduleDescriptionA, suitableModules, modules, projectClassNames, project);
		}
	}

	/**
	 * Checks whether M = Ma or not.
	 */
	private static boolean isModuleMequalModuleMa(final String className, final String moduleDescriptionA,
			final Set<ModuleSimilarity> suitableModules, final Map<String, String> modules, final Collection<String> projectClassNames,
			final IProject project) {
		final String simpleClassName = DCLUtil.getSimpleClassName(className);

		if (suitableModules != null) {
			for (ModuleSimilarity m : suitableModules) {
				if (m.getModuleDescription().endsWith(".*")) {
					/* Lets simulate if it had been moved */
					/*
					 * If it is moved to the suitable module M and it still
					 * remains in Ma, then M = Ma
					 */
					String qualifiedClassName = m.getModuleDescription().replaceAll("\\.\\*", "") + "." + simpleClassName;

					if (DCLUtil.hasClassNameByDescription(qualifiedClassName, moduleDescriptionA, modules, projectClassNames, project)) {
						return true;
					}
				} else if (moduleDescriptionA.contains(m.getModuleDescription())) {
					/* It's the same as the description */
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Checks whether M = complement(Ma) or not.
	 */
	private static boolean isModuleMequalComplementModuleMa(final String className, final String moduleDescriptionA,
			final Set<ModuleSimilarity> suitableModules, final Map<String, String> modules, final Collection<String> projectClassNames,
			final IProject project) {
		final String simpleClassName = DCLUtil.getSimpleClassName(className);

		for (ModuleSimilarity m : suitableModules) {
			if (m.getModuleDescription().endsWith(".*")) {
				/* Lets simulate if it had been moved */
				/*
				 * If it is moved to the suitable module M and it still remains
				 * in Ma, then M != complement(Ma)
				 */
				String qualifiedClassName = m.getModuleDescription().replaceAll("\\.\\*", "") + "." + simpleClassName;

				if (DCLUtil.hasClassNameByDescription(qualifiedClassName, moduleDescriptionA, modules, projectClassNames, project)) {
					return false;
				}
			} else if (moduleDescriptionA.contains(m.getModuleDescription())) {
				/* It's the same as the description */
				return false;
			}
		}

		return true;
	}

}
