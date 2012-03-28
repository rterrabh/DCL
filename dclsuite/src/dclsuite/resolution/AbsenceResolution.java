package dclsuite.resolution;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.ui.IMarkerResolution;

import dclsuite.core.Architecture;
import dclsuite.dependencies.MissingDependency;
import dclsuite.util.DCLUtil;
import dclsuite.util.FixingUtil;
import dclsuite.util.FormatUtil;

public class AbsenceResolution {

	public static IMarkerResolution[] getSuggestions(final IMarker marker, final Architecture architecture,
			final MissingDependency missingDependency) {

		switch (missingDependency.getDependencyType()) {
		case IMPLEMENT:
		case EXTEND:
			return getSuggestionsDerive(marker, architecture, missingDependency);
		case ANNOTATE:
			return getSuggestionsAnnotate(marker, architecture, missingDependency);

		}
		return null;

	}

	private static IMarkerResolution[] getSuggestionsAnnotate(final IMarker marker, final Architecture architecture,
			MissingDependency missingDependency) {
		final LinkedList<IMarkerResolution> suggestions = new LinkedList<IMarkerResolution>();

		final IProject project = marker.getResource().getProject();
		final List<ModuleSimilarity> suitableModules = FixingUtil.suitableModule(project, architecture,
				missingDependency.getClassNameA(), missingDependency.getDependencyType());

		suitableModules
				.addAll(FixingUtil.suitableModule(project, architecture, missingDependency.getClassNameA(), null));

		String simpleClassName = missingDependency.getClassNameA().substring(
				missingDependency.getClassNameA().lastIndexOf(".") + 1);
		String qualifiedClassName = suitableModules.get(0).getModuleDescription().replaceAll("\\.\\*", "") + "."
				+ simpleClassName;
		
		if (suitableModules != null && !suitableModules.isEmpty()) {
			String suitableModulesDescription = "";
			for (ModuleSimilarity ms : suitableModules) {
				suitableModulesDescription += ms.getModuleDescription() + ",";
			}
			suitableModulesDescription = suitableModulesDescription.substring(0,
					suitableModulesDescription.length() - 1);

			/* If the module is exactly the one */
			if (DCLUtil.hasClassNameByDescription(missingDependency.getClassNameA(), suitableModulesDescription,
					architecture.getModules(), architecture.getProjectClasses(), project)) {
				suggestions.add(FixingUtil.createMarkerResolution(
						"annotate(" + missingDependency.getClassNameA() + ", "
								+ missingDependency.getModuleDescriptionB() + ")", null));
			} else if (DCLUtil.hasClassNameByDescription(qualifiedClassName, missingDependency.getModuleDescriptionA(),
					architecture.getModules(), architecture.getProjectClasses(), project)) {
				suggestions.add(FixingUtil.createMarkerResolution("annotate(" + missingDependency.getClassNameA() + ","
						+ missingDependency.getModuleDescriptionB() + ")", null));
				System.out.println(simpleClassName  + ": ok");
			} else {
				for (ModuleSimilarity ms : suitableModules) {
					suggestions.add(FixingUtil.createMarkerResolution("move_class("
							+ missingDependency.getClassNameA() + ", " + ms.getModuleDescription()
							+ ") (similarity: " + FormatUtil.formatDouble(ms.getSimilarity()) + ")", null));
				}
			}

		}
		return suggestions.toArray(new IMarkerResolution[suggestions.size()]);
	}

	private static IMarkerResolution[] getSuggestionsDerive(final IMarker marker, final Architecture architecture,
			MissingDependency missingDependency) {

		final LinkedList<IMarkerResolution> suggestions = new LinkedList<IMarkerResolution>();

		final IProject project = marker.getResource().getProject();
		final List<ModuleSimilarity> suitableModules = new LinkedList<ModuleSimilarity>();

		suitableModules.addAll(FixingUtil.suitableModule(project, architecture, missingDependency.getClassNameA(),
				missingDependency.getDependencyType()));
		suitableModules
				.addAll(FixingUtil.suitableModule(project, architecture, missingDependency.getClassNameA(), null));

		if (suitableModules != null && !suitableModules.isEmpty()) {
			String suitableModulesDescription = "";
			for (ModuleSimilarity ms : suitableModules) {
				suitableModulesDescription += ms.getModuleDescription() + ",";
			}
			suitableModulesDescription = suitableModulesDescription.substring(0,
					suitableModulesDescription.length() - 1);

			//suitableModulesDescription = suitableModules.get(0).getModuleDescription();
			
			String simpleClassName = missingDependency.getClassNameA().substring(
					missingDependency.getClassNameA().lastIndexOf(".") + 1);
			String qualifiedClassName = suitableModules.get(0).getModuleDescription().replaceAll("\\.\\*", "") + "."
					+ simpleClassName;

			/* If the module is exactly the one */
			if (DCLUtil.hasClassNameByDescription(missingDependency.getClassNameA(), suitableModulesDescription,
					architecture.getModules(), architecture.getProjectClasses(), project)) {
				suggestions.add(FixingUtil.createMarkerResolution("derive(" + missingDependency.getClassNameA() + ","
						+ missingDependency.getModuleDescriptionB() + ")", null));
				System.out.println(simpleClassName  + ": ok");
			} else if (DCLUtil.hasClassNameByDescription(qualifiedClassName, missingDependency.getModuleDescriptionA(),
					architecture.getModules(), architecture.getProjectClasses(), project)) {
				suggestions.add(FixingUtil.createMarkerResolution("derive(" + missingDependency.getClassNameA() + ","
						+ missingDependency.getModuleDescriptionB() + ")", null));
				System.out.println(simpleClassName  + ": ok");
			} else {
				System.out.println(simpleClassName  + ": no");
				/* TEMP */
				for (ModuleSimilarity ms : suitableModules) {
					suggestions.add(FixingUtil.createMarkerResolution(
							"move_class(" + missingDependency.getClassNameA() + ", " + ms.getModuleDescription()
									+ ") (similarity: " + FormatUtil.formatDouble(ms.getSimilarity()) + ")", null));
				}
			}

		}
		return suggestions.toArray(new IMarkerResolution[suggestions.size()]);
	}

}
