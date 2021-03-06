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
import dclsuite.dependencies.AnnotateClassDependency;
import dclsuite.dependencies.Dependency;
import dclsuite.enums.Constraint;
import dclsuite.enums.ConstraintType;
import dclsuite.resolution.similarity.ModuleSimilarity;
import dclsuite.resolution.similarity.SuitableModule;
import dclsuite.util.DCLUtil;
import dclsuite.util.MarkerUtils;

public class DivergenceResolution {

	public static IMarkerResolution[] getSuggestions(final IMarker marker, final Architecture architecture, final Dependency dependency,
			final String moduleDescriptionA, final String moduleDescriptionB, final Constraint violatedConstraint) throws CoreException {

		final IProject project = marker.getResource().getProject();
		Set<ModuleSimilarity> suitableModules = SuitableModule.calculate(project, architecture, dependency.getClassNameA(),
				dependency.getDependencyType(), dependency.getClassNameB(), violatedConstraint.getConstraintType());

		// TODO: Missing the development of THROW rules
		switch (dependency.getDependencyType()) {
		case DECLARE:
			return getSuggestionsDeclare(project, architecture, dependency, suitableModules, moduleDescriptionA, moduleDescriptionB,
					violatedConstraint.getConstraintType());
		case ACCESS:
			return getSuggestionsAccess(project, architecture, dependency, suitableModules, moduleDescriptionA, moduleDescriptionB,
					violatedConstraint.getConstraintType());
		case CREATE:
			return getSuggestionsCreate(project, architecture, dependency, suitableModules, moduleDescriptionA, moduleDescriptionB,
					violatedConstraint.getConstraintType());
		case IMPLEMENT:
		case EXTEND:
			return getSuggestionsDerive(project, architecture, dependency, suitableModules, moduleDescriptionA, moduleDescriptionB,
					violatedConstraint.getConstraintType());
		case USEANNOTATION:
			return getSuggestionsAnnotate(project, architecture, dependency, suitableModules, moduleDescriptionA, moduleDescriptionB,
					violatedConstraint.getConstraintType());
		default:
			return null;
		}
	}

	/**
	 * DECLARE
	 */
	private static IMarkerResolution[] getSuggestionsDeclare(IProject project, Architecture architecture, Dependency dependency,
			Set<ModuleSimilarity> suitableModules, final String moduleDescriptionA, final String moduleDescriptionB,
			ConstraintType constraintType) throws CoreException {
		final LinkedList<IMarkerResolution> suggestions = new LinkedList<IMarkerResolution>();
		@SuppressWarnings("unused")
		final String simpleOriginClassName = DCLUtil.getSimpleClassName(dependency.getClassNameA());
		final String simpleTargetClassName = DCLUtil.getSimpleClassName(dependency.getClassNameB());

		/* D1 */
		Collection<String> allowedSuperTypes = Functions.supertypesAllowedTo(dependency.getClassNameA(), project, architecture,
				dependency.getClassNameB(), dependency.getDependencyType());

		if (allowedSuperTypes != null && !allowedSuperTypes.isEmpty()) {
			for (String allowedSuperTypeClassName : allowedSuperTypes) {
				if (allowedSuperTypeClassName.equals("java.lang.Object")) { /*
																			 * We
																			 * do
																			 * not
																			 * consider
																			 * java
																			 * .
																			 * lang
																			 * .
																			 * Object
																			 */
					continue;
				}
				suggestions.add(MarkerUtils.createMarkerResolutionChangeToOtherType("D1: replace( [" + simpleTargetClassName + "], ["
						+ DCLUtil.getSimpleClassName(allowedSuperTypeClassName) + "] ) \"supertype\"", null, allowedSuperTypeClassName));
			}
		}

		/* D2 */
		Collection<String> allowedSubTypes = Functions.subtypesAllowedTo(dependency.getClassNameA(), project, architecture,
				dependency.getClassNameB(), dependency.getDependencyType());

		if (allowedSubTypes != null && !allowedSubTypes.isEmpty()) {
			for (String allowedSubTypeClassName : allowedSubTypes) {
				suggestions.add(MarkerUtils.createMarkerResolutionChangeToOtherType("D2: replace( [" + simpleTargetClassName + "], ["
						+ DCLUtil.getSimpleClassName(allowedSubTypeClassName) + "] ) \"subtype\"", null, allowedSubTypeClassName));
			}
		}

		return suggestions.toArray(new IMarkerResolution[suggestions.size()]);
	}

	/**
	 * ACCESS
	 */
	private static IMarkerResolution[] getSuggestionsAccess(IProject project, Architecture architecture, Dependency dependency,
			Set<ModuleSimilarity> suitableModules, final String moduleDescriptionA, final String moduleDescriptionB,
			ConstraintType constraintType) throws CoreException {
		final LinkedList<IMarkerResolution> suggestions = new LinkedList<IMarkerResolution>();
		@SuppressWarnings("unused")
		final String simpleOriginClassName = DCLUtil.getSimpleClassName(dependency.getClassNameA());
		final String simpleTargetClassName = DCLUtil.getSimpleClassName(dependency.getClassNameB());

		/* D9 */
		String accessedMember = null;
		if (dependency instanceof AccessFieldDependency) {
			accessedMember = ((AccessFieldDependency) dependency).getFieldName();
		} else if (dependency instanceof AccessMethodDependency) {
			accessedMember = ((AccessMethodDependency) dependency).getMethodNameB() + "()";
		}

		if (!architecture.someclassCan(dependency.getClassNameB(), dependency.getDependencyType(), project)) {
			suggestions.add(MarkerUtils
					.createMarkerResolution("D9: remove( [" + simpleTargetClassName + "." + accessedMember + "] )", null));
		}

		return suggestions.toArray(new IMarkerResolution[suggestions.size()]);
	}

	/**
	 * CREATE
	 */
	private static IMarkerResolution[] getSuggestionsCreate(IProject project, Architecture architecture, Dependency dependency,
			Set<ModuleSimilarity> suitableModules, final String moduleDescriptionA, final String moduleDescriptionB,
			ConstraintType constraintType) throws CoreException {
		final LinkedList<IMarkerResolution> suggestions = new LinkedList<IMarkerResolution>();
		@SuppressWarnings("unused")
		final String simpleOriginClassName = DCLUtil.getSimpleClassName(dependency.getClassNameA());
		final String simpleTargetClassName = DCLUtil.getSimpleClassName(dependency.getClassNameB());

		/* D12 */
		if (!architecture.someclassCan(dependency.getClassNameB(), dependency.getDependencyType(), project)) {
			suggestions.add(MarkerUtils.createMarkerResolutionChangeToNull("D12: replace( [new " + simpleTargetClassName + "()], [null] )",
					null));
		} else {

			/* D11 */
			String[] factory = Functions.factory(project, architecture, dependency.getClassNameB());
			if (factory != null) {
				suggestions.add(MarkerUtils.createMarkerResolutionChangeToMethodInvocation("D11: replace( [new " + simpleTargetClassName
						+ "()], [" + DCLUtil.getSimpleClassName(factory[0]) + "." + factory[1] + "()" + "] )", null, factory));
			} else {

				/* D13 */
				/* If there is a suitable module to create a factory */
				if (architecture.someclassCan(dependency.getClassNameB(), dependency.getDependencyType(), project)) {
					suggestions.add(MarkerUtils.createMarkerResolution("D13: replace( [new " + simpleTargetClassName + "()], [FB.get"
							+ Character.toUpperCase(simpleTargetClassName.charAt(0)) + simpleTargetClassName.substring(1) + "()] )", null));
				}

			}
		}
		return suggestions.toArray(new IMarkerResolution[suggestions.size()]);
	}

	/**
	 * IMPLEMENT/EXTEND
	 */
	private static IMarkerResolution[] getSuggestionsDerive(IProject project, Architecture architecture, Dependency dependency,
			Set<ModuleSimilarity> suitableModules, final String moduleDescriptionA, final String moduleDescriptionB,
			ConstraintType constraintType) throws CoreException {
		final LinkedList<IMarkerResolution> suggestions = new LinkedList<IMarkerResolution>();
		final String simpleOriginClassName = DCLUtil.getSimpleClassName(dependency.getClassNameA());
		final String simpleTargetClassName = DCLUtil.getSimpleClassName(dependency.getClassNameB());

		/* D19 */
		Collection<String> allowedSuperTypes = Functions.supertypesAllowedTo(dependency.getClassNameA(), project, architecture,
				dependency.getClassNameB(), dependency.getDependencyType());
		if (allowedSuperTypes != null && !allowedSuperTypes.isEmpty()) {
			for (String allowedSubTypeClassName : allowedSuperTypes) {
				suggestions.add(MarkerUtils.createMarkerResolution("D19: replace( [" + simpleTargetClassName + "], ["
						+ allowedSubTypeClassName + "] ) \"subtype\"", null));
			}
		}

		/* D20 */
		for (ModuleSimilarity ms : suitableModules) {
			String qualifiedClassName = ms.getModuleDescription().replaceAll("\\.\\*", "") + "." + simpleOriginClassName;

			int i = 0;
			if (!ms.getModuleDescription().endsWith(".*")
					|| (ms.getModuleDescription().endsWith(".*") && architecture.can(qualifiedClassName, dependency.getClassNameB(),
							dependency.getDependencyType(), project))) {
				suggestions.add(MarkerUtils.createMarkerResolution(
						"D20." + ++i + ": move_class(" + simpleOriginClassName + ", " + ms.getModuleDescription() + ") " + ms.getInfo(),
						null));
			}

		}

		return suggestions.toArray(new IMarkerResolution[suggestions.size()]);
	}

	/**
	 * USE-ANNOTATION
	 */
	private static IMarkerResolution[] getSuggestionsAnnotate(IProject project, Architecture architecture, Dependency dependency,
			Set<ModuleSimilarity> suitableModules, final String moduleDescriptionA, final String moduleDescriptionB,
			final ConstraintType constraintType) throws CoreException {
		final LinkedList<IMarkerResolution> suggestions = new LinkedList<IMarkerResolution>();
		final String simpleOriginClassName = DCLUtil.getSimpleClassName(dependency.getClassNameA());
		final String simpleTargetClassName = DCLUtil.getSimpleClassName(dependency.getClassNameB());

		if (Functions.isModuleMequalModuleMa(dependency.getClassNameA(), moduleDescriptionA, suitableModules, architecture.getModules(),
				architecture.getProjectClasses(), project, constraintType)) {
			/* D22 and D23 */
			String recommendationNumber = (dependency instanceof AnnotateClassDependency) ? "D22" : "D24";
			suggestions.add(MarkerUtils.createMarkerResolutionRemoval(
					recommendationNumber + ": remove( [@" + simpleTargetClassName + "] )", null));
		} else {
			/* D21 and D23 */
			int i = 0;
			if (suitableModules != null) {
				for (ModuleSimilarity ms : suitableModules) {
					String recommendationNumber = (dependency instanceof AnnotateClassDependency) ? "D21" : "D23";
					suggestions.add(MarkerUtils.createMarkerResolution(recommendationNumber + "." + ++i + ": move_class("
							+ simpleOriginClassName + ", " + ms.getModuleDescription() + ") " + ms.getInfo(), null));
				}
			}
		}

		return suggestions.toArray(new IMarkerResolution[suggestions.size()]);
	}

}