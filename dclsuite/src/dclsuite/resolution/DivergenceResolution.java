package dclsuite.resolution;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.ui.IMarkerResolution;

import dclsuite.core.Architecture;
import dclsuite.dependencies.AccessDependency;
import dclsuite.dependencies.AnnotateDependency;
import dclsuite.dependencies.CreateDependency;
import dclsuite.dependencies.Dependency;
import dclsuite.dependencies.DeriveDependency;
import dclsuite.enums.Constraint;
import dclsuite.util.DCLUtil;
import dclsuite.util.FixingUtil;
import dclsuite.util.FormatUtil;

public class DivergenceResolution {

	public static IMarkerResolution[] getSuggestions(final IMarker marker, final Architecture architecture,
			final Dependency dependency, final String moduleDescriptionA, final String moduleDescriptionB, final Constraint violatedConstraint) {

		switch (dependency.getDependencyType()) {
		case ANNOTATE:
			return getSuggestions(marker, architecture, (AnnotateDependency) dependency,  moduleDescriptionA, moduleDescriptionB, violatedConstraint);
		case ACCESS:
			return getSuggestions(marker, architecture, (AccessDependency) dependency,  moduleDescriptionA, moduleDescriptionB, violatedConstraint);
		case CREATE:
			return getSuggestions(marker, architecture, (CreateDependency) dependency,  moduleDescriptionA, moduleDescriptionB, violatedConstraint);
		case EXTEND:
		case IMPLEMENT:			
			return getSuggestions(marker, architecture, (DeriveDependency) dependency,  moduleDescriptionA, moduleDescriptionB, violatedConstraint);
			
		}
		return null;

	}

	private static IMarkerResolution[] getSuggestions(final IMarker marker, final Architecture architecture,
			AnnotateDependency dependency, final String moduleDescriptionA, final String moduleDescriptionB, final Constraint violatedConstraint) {
		final LinkedList<IMarkerResolution> suggestions = new LinkedList<IMarkerResolution>();
		final IProject project = marker.getResource().getProject();
				
		final List<ModuleSimilarity> suitableModules = FixingUtil.suitableModule(project, architecture,
				dependency.getClassNameA(), dependency.getDependencyType());

		suitableModules.addAll(FixingUtil.suitableModule(project, architecture, dependency.getClassNameA(), null));
		
		if (suitableModules != null && !suitableModules.isEmpty()) {
			String suitableModulesDescription = "";
			for (ModuleSimilarity ms : suitableModules) {
				suitableModulesDescription += ms.getModuleDescription() + ",";
			}
			suitableModulesDescription = suitableModulesDescription.substring(0,
					suitableModulesDescription.length() - 1);

			String simpleClassName = dependency.getClassNameA().substring(
					dependency.getClassNameA().lastIndexOf(".") + 1);
			String qualifiedClassName = suitableModules.get(0).getModuleDescription().replaceAll("\\.\\*", "") + "."
					+ simpleClassName;
			
			/* If the module is exactly the one */
			if (DCLUtil.hasClassNameByDescription(dependency.getClassNameA(), suitableModulesDescription,
					architecture.getModules(), architecture.getProjectClasses(), project)) {
				suggestions.add(FixingUtil.createMarkerResolution(
						"remove(@" + dependency.getClassNameB()  + ")", null));
				System.out.println(simpleClassName  + ": ok");
			} else if (DCLUtil.hasClassNameByDescription(qualifiedClassName, moduleDescriptionA,
					architecture.getModules(), architecture.getProjectClasses(), project)) {
				suggestions.add(FixingUtil.createMarkerResolution(
						"remove(@" + dependency.getClassNameB()  + ")", null));
				System.out.println(simpleClassName  + ": ok");
			} else {
				System.out.println(simpleClassName  + ": no");
				/* TEMP */
				for (ModuleSimilarity ms : suitableModules) {
					suggestions.add(FixingUtil.createMarkerResolution(
							"move_class(" + dependency.getClassNameA() + ", " + ms.getModuleDescription()
									+ ") (similarity: " + FormatUtil.formatDouble(ms.getSimilarity()) + ")", null));
				}
			}
		}

		return suggestions.toArray(new IMarkerResolution[suggestions.size()]);
	}
	
	
	private static IMarkerResolution[] getSuggestions(final IMarker marker, final Architecture architecture,
			DeriveDependency dependency, final String moduleDescriptionA, final String moduleDescriptionB, final Constraint violatedConstraint) {
		final LinkedList<IMarkerResolution> suggestions = new LinkedList<IMarkerResolution>();
		final IProject project = marker.getResource().getProject();
				
		final List<ModuleSimilarity> suitableModules = FixingUtil.suitableModule(project, architecture,
				dependency.getClassNameA(), dependency.getDependencyType());

		suitableModules.addAll(FixingUtil.suitableModule(project, architecture, dependency.getClassNameA(), null));
		
		suitableModules.addAll(FixingUtil.suitableModule(project, architecture, dependency.getClassNameA(), dependency.getDependencyType()));

		if (suitableModules != null && !suitableModules.isEmpty()) {
			String suitableModulesDescription = "";
			for (ModuleSimilarity ms : suitableModules) {
				suitableModulesDescription += ms.getModuleDescription() + ",";
			}
			suitableModulesDescription = suitableModulesDescription.substring(0,
					suitableModulesDescription.length() - 1);

			String simpleClassName = dependency.getClassNameA().substring(
					dependency.getClassNameA().lastIndexOf(".") + 1);
			String qualifiedClassName = suitableModules.get(0).getModuleDescription().replaceAll("\\.\\*", "") + "."
					+ simpleClassName;
			
			/* If the module is exactly the one */
			if (DCLUtil.hasClassNameByDescription(dependency.getClassNameA(), suitableModulesDescription,
					architecture.getModules(), architecture.getProjectClasses(), project)) {
				suggestions.add(FixingUtil.createMarkerResolution("ops",null)); 
				System.out.println(simpleClassName  + ": ok");
			} else if (DCLUtil.hasClassNameByDescription(qualifiedClassName, moduleDescriptionA,
					architecture.getModules(), architecture.getProjectClasses(), project)) {
				suggestions.add(FixingUtil.createMarkerResolution("ops",null)); 
				System.out.println(simpleClassName  + ": ok");
			} //else {
				/* TEMP */
				for (ModuleSimilarity ms : suitableModules) {
					suggestions.add(FixingUtil.createMarkerResolution(
							"move_class(" + dependency.getClassNameA() + ", " + ms.getModuleDescription()
									+ ") (similarity: " + FormatUtil.formatDouble(ms.getSimilarity()) + ")", null));
				}
			//}
		}

		return suggestions.toArray(new IMarkerResolution[suggestions.size()]);
	}

	
	/**
	 * CREATE
	 */
	private static IMarkerResolution[] getSuggestions(final IMarker marker, final Architecture architecture,
			CreateDependency dependency, final String moduleDescriptionA, final String moduleDescriptionB, final Constraint violatedConstraint) {
		final LinkedList<IMarkerResolution> suggestions = new LinkedList<IMarkerResolution>();
		final IProject project = marker.getResource().getProject();
				
		final List<ModuleSimilarity> suitableModules = FixingUtil.suitableModule(project, architecture,
				dependency.getClassNameA(), dependency.getDependencyType());

		suitableModules.addAll(FixingUtil.suitableModule(project, architecture, dependency.getClassNameA(), null));

		suitableModules.addAll(FixingUtil.suitableModule(project, architecture, dependency.getClassNameA(), dependency.getDependencyType()));
		
		if (suitableModules != null && !suitableModules.isEmpty()) {
			String suitableModulesDescription = "";
			for (ModuleSimilarity ms : suitableModules) {
				suitableModulesDescription += ms.getModuleDescription() + ",";
			}
			suitableModulesDescription = suitableModulesDescription.substring(0,
					suitableModulesDescription.length() - 1);

			String simpleClassName = dependency.getClassNameA().substring(
					dependency.getClassNameA().lastIndexOf(".") + 1);
			String qualifiedClassName = suitableModules.get(0).getModuleDescription().replaceAll("\\.\\*", "") + "."
					+ simpleClassName;
			
			/* If the module is exactly the one */
			if (DCLUtil.hasClassNameByDescription(dependency.getClassNameA(), suitableModulesDescription,
					architecture.getModules(), architecture.getProjectClasses(), project)) {
				suggestions.add(FixingUtil.createMarkerResolution("ops",null)); 
				System.out.println(simpleClassName  + ": ok");
			} else if (DCLUtil.hasClassNameByDescription(qualifiedClassName, moduleDescriptionA,
					architecture.getModules(), architecture.getProjectClasses(), project)) {
				suggestions.add(FixingUtil.createMarkerResolution("ops",null)); 
				System.out.println(simpleClassName  + ": ok");
			} //else {
				/* TEMP */
				for (ModuleSimilarity ms : suitableModules) {
					suggestions.add(FixingUtil.createMarkerResolution(
							"move_class(" + dependency.getClassNameA() + ", " + ms.getModuleDescription()
									+ ") (similarity: " + FormatUtil.formatDouble(ms.getSimilarity()) + ")", null));
				}
			//}
		}

		return suggestions.toArray(new IMarkerResolution[suggestions.size()]);
	}
	
	
	/* ACCESS */
	// ADAPT SUITABLE MODULE FUNCTION TO CONSIDER THE NAME OF THE METHOD 
	private static IMarkerResolution[] getSuggestions(final IMarker marker, final Architecture architecture,
			AccessDependency dependency, final String moduleDescriptionA, final String moduleDescriptionB, final Constraint violatedConstraint) {
		final LinkedList<IMarkerResolution> suggestions = new LinkedList<IMarkerResolution>();
		final IProject project = marker.getResource().getProject();
				
		final List<ModuleSimilarity> suitableModules = FixingUtil.suitableModule(project, architecture,
				dependency.getClassNameA(), dependency.getDependencyType());

		suitableModules.addAll(FixingUtil.suitableModule(project, architecture, dependency.getClassNameA(), null));

		suitableModules.addAll(FixingUtil.suitableModule(project, architecture, dependency.getClassNameA(), dependency.getDependencyType()));
		
		if (suitableModules != null && !suitableModules.isEmpty()) {
			String suitableModulesDescription = "";
			for (ModuleSimilarity ms : suitableModules) {
				suitableModulesDescription += ms.getModuleDescription() + ",";
			}
			suitableModulesDescription = suitableModulesDescription.substring(0,
					suitableModulesDescription.length() - 1);

			String simpleClassName = dependency.getClassNameA().substring(
					dependency.getClassNameA().lastIndexOf(".") + 1);
			String qualifiedClassName = suitableModules.get(0).getModuleDescription().replaceAll("\\.\\*", "") + "."
					+ simpleClassName;
			
			/* If the module is exactly the one */
			if (DCLUtil.hasClassNameByDescription(dependency.getClassNameA(), suitableModulesDescription,
					architecture.getModules(), architecture.getProjectClasses(), project)) {
				suggestions.add(FixingUtil.createMarkerResolution("ops",null)); 
				System.out.println(simpleClassName  + ": ok");
			} else if (DCLUtil.hasClassNameByDescription(qualifiedClassName, moduleDescriptionA,
					architecture.getModules(), architecture.getProjectClasses(), project)) {
				suggestions.add(FixingUtil.createMarkerResolution("ops",null)); 
				System.out.println(simpleClassName  + ": ok");
			} //else {
				/* TEMP */
				for (ModuleSimilarity ms : suitableModules) {
					suggestions.add(FixingUtil.createMarkerResolution(
							"move_class(" + dependency.getClassNameA() + ", " + ms.getModuleDescription()
									+ ") (similarity: " + FormatUtil.formatDouble(ms.getSimilarity()) + ")", null));
				}
			//}
		}

		return suggestions.toArray(new IMarkerResolution[suggestions.size()]);
	}
}
