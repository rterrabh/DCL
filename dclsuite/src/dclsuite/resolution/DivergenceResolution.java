package dclsuite.resolution;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IMarkerResolution;

import dclsuite.core.Architecture;
import dclsuite.dependencies.AccessFieldDependency;
import dclsuite.dependencies.AccessMethodDependency;
import dclsuite.dependencies.Dependency;
import dclsuite.enums.Constraint;
import dclsuite.enums.ConstraintType;
import dclsuite.util.DCLUtil;
import dclsuite.util.FixingUtil;
import dclsuite.util.FormatUtil;

public class DivergenceResolution {

	public static IMarkerResolution[] getSuggestions(final IMarker marker, final Architecture architecture,
			final Dependency dependency, final String moduleDescriptionA, final String moduleDescriptionB,
			final Constraint violatedConstraint) throws CoreException {

		final IProject project = marker.getResource().getProject();
		Set<ModuleSimilarity> suitableModules = FixingUtil.suitableModule(project, architecture,
				dependency.getClassNameA(), dependency.getDependencyType(), dependency.getClassNameB());

		// TODO: Missing the development of THROW rules
		switch (dependency.getDependencyType()) {
		case DECLARE:
			return getSuggestionsDeclare(project, architecture, dependency, suitableModules, moduleDescriptionA,
					moduleDescriptionB, violatedConstraint.getConstraintType());
		case ACCESS:
			return getSuggestionsAccess(project, architecture, dependency, suitableModules, moduleDescriptionA,
					moduleDescriptionB, violatedConstraint.getConstraintType());
		case CREATE:
			return getSuggestionsCreate(project, architecture, dependency, suitableModules, moduleDescriptionA,
					moduleDescriptionB, violatedConstraint.getConstraintType());
		case IMPLEMENT:
		case EXTEND:
			return getSuggestionsDerive(project, architecture, dependency, suitableModules, moduleDescriptionA,
					moduleDescriptionB, violatedConstraint.getConstraintType());
		case USEANNOTATION:
			return getSuggestionsAnnotate(project, architecture, dependency, suitableModules, moduleDescriptionA,
					moduleDescriptionB, violatedConstraint.getConstraintType());

		}
		return null;
	}

	/**
	 * USE-ANNOTATION
	 */
	private static IMarkerResolution[] getSuggestionsAnnotate(IProject project, Architecture architecture,
			Dependency dependency, Set<ModuleSimilarity> suitableModules, final String moduleDescriptionA,
			final String moduleDescriptionB, ConstraintType constraintType) throws CoreException {
		final LinkedList<IMarkerResolution> suggestions = new LinkedList<IMarkerResolution>();
		final String simpleOriginClassName = DCLUtil.getSimpleClassName(dependency.getClassNameA());
		final String simpleTargetClassName = DCLUtil.getSimpleClassName(dependency.getClassNameB());

		if (constraintType == ConstraintType.CANNOT || constraintType == ConstraintType.CAN_ONLY) {
			if (!FixingUtil.isModuleMequalModuleMa(dependency.getClassNameA(), moduleDescriptionA, suitableModules,
					architecture.getModules(), architecture.getProjectClasses(), project)) {
				for (ModuleSimilarity ms : suitableModules) {
					suggestions.add(FixingUtil.createMarkerResolution(
							"move_class(" + simpleOriginClassName + ", " + ms.getModuleDescription()
									+ ") (similarity: " + FormatUtil.formatDouble(ms.getSimilarity())
									+ ms.getStrategy().toString() + ")", null));
				}
			} else {
				suggestions.add(FixingUtil.createMarkerResolution("remove( [@" + simpleTargetClassName + "] )", null));
			}
		} else if (constraintType == ConstraintType.ONLY_CAN) {
			if (FixingUtil.isModuleMequalModuleMa(dependency.getClassNameA(), moduleDescriptionA, suitableModules,
					architecture.getModules(), architecture.getProjectClasses(), project)) {
				for (ModuleSimilarity ms : suitableModules) {
					suggestions.add(FixingUtil.createMarkerResolution(
							"move_class(" + simpleOriginClassName + ", " + ms.getModuleDescription()
									+ ") (similarity: " + FormatUtil.formatDouble(ms.getSimilarity())
									+ ms.getStrategy().toString() + ")", null));
				}
			} else {
				suggestions.add(FixingUtil.createMarkerResolution("remove( [@" + simpleTargetClassName + "] )", null));
			}
		}

		return suggestions.toArray(new IMarkerResolution[suggestions.size()]);
	}

	/**
	 * CREATE
	 */
	private static IMarkerResolution[] getSuggestionsCreate(IProject project, Architecture architecture,
			Dependency dependency, Set<ModuleSimilarity> suitableModules, final String moduleDescriptionA,
			final String moduleDescriptionB, ConstraintType constraintType) throws CoreException {
		final LinkedList<IMarkerResolution> suggestions = new LinkedList<IMarkerResolution>();
		@SuppressWarnings("unused")
		final String simpleOriginClassName = DCLUtil.getSimpleClassName(dependency.getClassNameA());
		final String simpleTargetClassName = DCLUtil.getSimpleClassName(dependency.getClassNameB());

		if (!architecture.someclassCan(dependency.getClassNameB(), dependency.getDependencyType(), project)) {
			suggestions.add(FixingUtil.createMarkerResolution("replace( [new " + simpleTargetClassName
					+ "()], [null] )", null));
		}

		String[] factory = FixingUtil.factory(project, architecture, dependency.getClassNameB());
		if (factory != null) {
			suggestions.add(FixingUtil.createMarkerResolution("replace( [new " + simpleTargetClassName + "()], ["
					+ factory[0] + "." + factory[1] + "()" + "] )", null));
		}

		return suggestions.toArray(new IMarkerResolution[suggestions.size()]);
	}

	
	/**
	 * DECLARE
	 */
	private static IMarkerResolution[] getSuggestionsDeclare(IProject project, Architecture architecture,
			Dependency dependency, Set<ModuleSimilarity> suitableModules, final String moduleDescriptionA,
			final String moduleDescriptionB, ConstraintType constraintType) throws CoreException {
		final LinkedList<IMarkerResolution> suggestions = new LinkedList<IMarkerResolution>();
		@SuppressWarnings("unused")
		final String simpleOriginClassName = DCLUtil.getSimpleClassName(dependency.getClassNameA());
		final String simpleTargetClassName = DCLUtil.getSimpleClassName(dependency.getClassNameB());

		Collection<String> allowedSuperTypes = FixingUtil.supertypesAllowedTo(dependency.getClassNameA(), 
				project, architecture, dependency.getClassNameB(), dependency.getDependencyType());

		if (allowedSuperTypes != null && !allowedSuperTypes.isEmpty()) {
			for (String allowedSuperTypeClassName : allowedSuperTypes){
				suggestions.add(FixingUtil.createMarkerResolution("replace( [" + simpleTargetClassName + "], ["
						+ allowedSuperTypeClassName + "] ) \"supertype\"", null));	
			}
		}
		
		Collection<String> allowedSubTypes = FixingUtil.subtypesAllowedTo(dependency.getClassNameA(), 
				project, architecture, dependency.getClassNameB(), dependency.getDependencyType());

		if (allowedSubTypes != null && !allowedSubTypes.isEmpty()) {
			for (String allowedSubTypeClassName : allowedSubTypes){
				suggestions.add(FixingUtil.createMarkerResolution("replace( [" + simpleTargetClassName + "], ["
						+ allowedSubTypeClassName + "] ) \"subtype\"", null));	
			}
		}
		
		return suggestions.toArray(new IMarkerResolution[suggestions.size()]);
	}
	
	/**
	 * ACCESS
	 */
	private static IMarkerResolution[] getSuggestionsAccess(IProject project, Architecture architecture,
			Dependency dependency, Set<ModuleSimilarity> suitableModules, final String moduleDescriptionA,
			final String moduleDescriptionB, ConstraintType constraintType) throws CoreException {
		final LinkedList<IMarkerResolution> suggestions = new LinkedList<IMarkerResolution>();
		@SuppressWarnings("unused")
		final String simpleOriginClassName = DCLUtil.getSimpleClassName(dependency.getClassNameA());
		final String simpleTargetClassName = DCLUtil.getSimpleClassName(dependency.getClassNameB());

		String accessedMember = null;
		if (dependency instanceof AccessFieldDependency) {
			accessedMember = ((AccessFieldDependency) dependency).getFieldName();
		} else if (dependency instanceof AccessMethodDependency) {
			accessedMember = ((AccessMethodDependency) dependency).getMethodNameB() + "()";
		}

		if (!architecture.someclassCan(dependency.getClassNameB(), dependency.getDependencyType(), project)) {
			suggestions.add(FixingUtil.createMarkerResolution("remove( [" + simpleTargetClassName + "."
					+ accessedMember + "] )", null));
		}

		return suggestions.toArray(new IMarkerResolution[suggestions.size()]);
	}

	/**
	 * IMPLEMENT/EXTEND
	 */
	private static IMarkerResolution[] getSuggestionsDerive(IProject project, Architecture architecture,
			Dependency dependency, Set<ModuleSimilarity> suitableModules, final String moduleDescriptionA,
			final String moduleDescriptionB, ConstraintType constraintType) throws CoreException {
		final LinkedList<IMarkerResolution> suggestions = new LinkedList<IMarkerResolution>();
		final String simpleOriginClassName = DCLUtil.getSimpleClassName(dependency.getClassNameA());
		@SuppressWarnings("unused")
		final String simpleTargetClassName = DCLUtil.getSimpleClassName(dependency.getClassNameB());

		for (ModuleSimilarity ms : suitableModules) {
			String qualifiedClassName = ms.getModuleDescription().replaceAll("\\.\\*", "") + "."
					+ simpleOriginClassName;

			if (!ms.getModuleDescription().endsWith(".*")
					|| (ms.getModuleDescription().endsWith(".*") && architecture.can(qualifiedClassName,
							dependency.getClassNameB(), dependency.getDependencyType(), project))) {
				suggestions
						.add(FixingUtil.createMarkerResolution(
								"move_class(" + simpleOriginClassName + ", " + ms.getModuleDescription()
										+ ") (similarity: " + FormatUtil.formatDouble(ms.getSimilarity())
										+ ms.getStrategy().toString() + ")", null));
			}

		}

		return suggestions.toArray(new IMarkerResolution[suggestions.size()]);
	}

}
