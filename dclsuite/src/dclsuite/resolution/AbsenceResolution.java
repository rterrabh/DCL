package dclsuite.resolution;

import java.util.LinkedList;
import java.util.Set;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.ui.IMarkerResolution;

import dclsuite.core.Architecture;
import dclsuite.dependencies.MissingDependency;
import dclsuite.enums.ConstraintType;
import dclsuite.resolution.similarity.ModuleSimilarity;
import dclsuite.resolution.similarity.SuitableModule;
import dclsuite.util.DCLUtil;
import dclsuite.util.MarkerUtils;

public class AbsenceResolution {

	public static IMarkerResolution[] getSuggestions(final IMarker marker, final Architecture architecture,
			final MissingDependency missingDependency) {
		final IProject project = marker.getResource().getProject();
		Set<ModuleSimilarity> suitableModules = SuitableModule.calculate(project, architecture, missingDependency.getClassNameA(),
				missingDependency.getDependencyType(), null, ConstraintType.MUST);

		/* DEBUG */
		// for (ModuleSimilarity ms : suitableModules){
		// System.out.println(ms);
		// }

		// TODO: Missing the development of THROW rules
		switch (missingDependency.getDependencyType()) {
		case IMPLEMENT:
		case EXTEND:
			return getSuggestionsDerive(project, architecture, missingDependency, suitableModules);
		case USEANNOTATION:
			return getSuggestionsAnnotate(project, architecture, missingDependency, suitableModules);
		default:
			return null;
		}
	}

	/**
	 * IMPLEMENT/EXTEND
	 */
	private static IMarkerResolution[] getSuggestionsDerive(final IProject project, final Architecture architecture,
			MissingDependency missingDependency, Set<ModuleSimilarity> suitableModules) {
		final LinkedList<IMarkerResolution> suggestions = new LinkedList<IMarkerResolution>();
		final String simpleClassName = DCLUtil.getSimpleClassName(missingDependency.getClassNameA());

		if (Functions.isModuleMequalModuleMa(missingDependency.getClassNameA(), missingDependency.getModuleDescriptionA(), suitableModules,
				architecture.getModules(), architecture.getProjectClasses(), project, ConstraintType.MUST)) {
			/* A3 */
			suggestions.add(MarkerUtils.createMarkerResolution("A3: replace( [" + simpleClassName + "], [" + simpleClassName + " "
					+ missingDependency.getDependencyType().getValue() + "s " + missingDependency.getModuleDescriptionB() + "])", null));
		} else {
			/* A4 */
			int i = 0;
			for (ModuleSimilarity ms : suitableModules) {
				suggestions.add(MarkerUtils.createMarkerResolution(
						"A4." + ++i + ": move_class(" + simpleClassName + ", " + ms.getModuleDescription() + ") " + ms.getInfo(), null));
			}

		}

		return suggestions.toArray(new IMarkerResolution[suggestions.size()]);
	}

	/**
	 * USE-ANNOTATION
	 */
	private static IMarkerResolution[] getSuggestionsAnnotate(final IProject project, final Architecture architecture,
			MissingDependency missingDependency, Set<ModuleSimilarity> suitableModules) {
		final LinkedList<IMarkerResolution> suggestions = new LinkedList<IMarkerResolution>();
		final String simpleClassName = DCLUtil.getSimpleClassName(missingDependency.getClassNameA());

		if (Functions.isModuleMequalModuleMa(missingDependency.getClassNameA(), missingDependency.getModuleDescriptionA(), suitableModules,
				architecture.getModules(), architecture.getProjectClasses(), project, ConstraintType.MUST)) {
			/* A6 and A8 */
			suggestions.add(MarkerUtils.createMarkerResolution(
					"A6: replace( [" + simpleClassName + "], [@" + missingDependency.getModuleDescriptionB() + " " + simpleClassName + ")",
					null));
		} else {
			/* A5 and A7 */
			int i = 0;
			if (suitableModules != null) {
				for (ModuleSimilarity ms : suitableModules) {
					suggestions.add(MarkerUtils.createMarkerResolution(
							"A5." + ++i + ": move_class(" + simpleClassName + ", " + ms.getModuleDescription() + ") " + ms.getInfo(), null));
				}
			}

		}

		return suggestions.toArray(new IMarkerResolution[suggestions.size()]);
	}

}
