package dclsuite.resolution;

import java.util.Collection;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator;
import org.eclipse.ui.IMarkerResolutionGenerator2;

import dclsuite.core.Architecture;
import dclsuite.core.DependencyConstraint.ArchitecturalDrift;
import dclsuite.dependencies.Dependency;
import dclsuite.dependencies.MissingDependency;
import dclsuite.enums.Constraint;
import dclsuite.enums.DependencyType;
import dclsuite.enums.ViolationProperties;
import dclsuite.util.ArchitectureUtils;
import dclsuite.util.DCLPersistence;
import dclsuite.util.FixingUtil;

public class MarkerResolution implements IMarkerResolutionGenerator, IMarkerResolutionGenerator2 {

	@Override
	public IMarkerResolution[] getResolutions(IMarker marker) {
		final IProject project = marker.getResource().getProject();
		IMarkerResolution[] suggestions = null;
		try {
			final Architecture architecture = ArchitectureUtils.getOrInitializeArchitecture(project);
			
			//TODO: Ajustar isso
			for (String s :architecture.getProjectClasses()){
				if (architecture.getDependencies(s)==null){
					for (String className : architecture.getProjectClasses()) {
						Collection<Dependency> dependencies = DCLPersistence.load(project, className);
						if (dependencies == null) {
							throw new CoreException(null);
						}
						architecture.updateDependencies(className, dependencies);
					}
				}
				break;
			}
			
			
			

			final String violationType = (String) marker.getAttribute(ViolationProperties.VIOLATION_TYPE.getKey());

			final String classNameA = (String) marker.getAttribute(ViolationProperties.CLASS_NAME_A.getKey());
			final DependencyType dependencyType = DependencyType.valueOf((String) marker
					.getAttribute(ViolationProperties.DEPENDENCY_TYPE.getKey()));

			final String moduleDescriptionA = (String) marker.getAttribute(ViolationProperties.MODULE_DESCRIPTION_A.getKey());
			final String moduleDescriptionB = (String) marker.getAttribute(ViolationProperties.MODULE_DESCRIPTION_B.getKey());
			
			final Constraint violatedConstraint = Constraint.getConstraint((String) marker.getAttribute(ViolationProperties.CONSTRAINT.getKey()));
			
			
			if (violationType.equals(ArchitecturalDrift.DIVERGENCE)) {

				final String classNameB = (String) marker.getAttribute(ViolationProperties.CLASS_NAME_B.getKey());
				final String lineNumberA = (String) marker.getAttribute(ViolationProperties.LINE_NUMBER_A.getKey());

				Dependency dependency = architecture.getDependency(classNameA, classNameB,
						(lineNumberA != null) ? new Integer(lineNumberA) : null, dependencyType);

				suggestions = DivergenceResolution.getSuggestions(marker, architecture, dependency, moduleDescriptionA, moduleDescriptionB, violatedConstraint);

			} else if (violationType.equals(ArchitecturalDrift.ABSENCE)) {
				MissingDependency missingDependency = new MissingDependency(moduleDescriptionA, classNameA, moduleDescriptionB, dependencyType);
				
				suggestions = AbsenceResolution.getSuggestions(marker, architecture, missingDependency);
			}

		} catch (CoreException e) {
			e.printStackTrace();
			return new IMarkerResolution[] { FixingUtil.createMarkerResolution("error",
					"There was an error while the dclfix tool was trying to find suggestions.") };
		} catch (Throwable e){
			e.printStackTrace();
			return new IMarkerResolution[] { FixingUtil.createMarkerResolution("error",
					"There was an error while the dclfix tool was trying to find suggestions.") };
		}
		if (suggestions != null && suggestions.length != 0) {
			return suggestions;
		} else {
			return new IMarkerResolution[] { FixingUtil.createMarkerResolution("None (" + '\u2205' + ")",
					"The dclfix tool have not found suggestions.") };
		}

		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

		// return new IMarkerResolution[] { new IMarkerResolution2() {
		//
		// @Override
		// public void run(IMarker m) {
		// MessageDialog.openInformation(new Shell(), "dclcheck",
		// "In this version we have not automatizated performing refactorings.");
		// }
		//
		// @Override
		// public String getLabel() {
		// return "move_class(st,C) where C = opa";
		// }
		//
		// public String getDescription() {
		// return "Move the statement to the class ";
		// };
		//
		// public org.eclipse.swt.graphics.Image getImage() {
		// return null;
		// };
		// } };

		// final IProject project = marker.getResource().getProject();
		// try {
		// if (!ArchitectureUtils.hasArchitectureInitialized(project)) {
		// ArchitectureUtils.initializeArchitecture(project);
		// }
		//
		// if
		// (marker.getAttribute(ViolationProperties.DEPENDENCY_TYPE.getKey()).equals(
		// DependencyType.ACCESS.toString())) {
		// Architecture arch = ArchitectureUtils.getArchitecture(project);
		//
		// String classNameA = (String)
		// marker.getAttribute(ViolationProperties.CLASS_NAME_A.getKey());
		// String classNameB = (String)
		// marker.getAttribute(ViolationProperties.CLASS_NAME_B.getKey());
		// //String dependencyType = (String)
		// marker.getAttribute(ViolationProperties.DEPENDENCY_TYPE.getKey());
		//
		// Dependency d = new AccessDependency("", classNameB, null) {
		// };
		// Collection<Dependency> c = new ArrayList<Dependency>();
		// c.add(d);
		//
		// String suitableClassName = null;
		// double suitableClassSimilarity = 0;
		// for (String className :
		// arch.getDependenciesOfProjectClasses().keySet()) {
		// if (className.equals(classNameA)){
		// continue;
		// }
		// double similarity = FixingUtil.similarity(c,
		// arch.getDependenciesOfProjectClasses().get(className));
		// System.out.printf("%s: %.6f\n", className, similarity);
		// if (similarity > suitableClassSimilarity) {
		// suitableClassSimilarity = similarity;
		// suitableClassName = className;
		// }
		//
		// }
		// final String classNameRes = suitableClassName;
		//
		// return new IMarkerResolution[] { new IMarkerResolution2() {
		//
		// @Override
		// public void run(IMarker m) {
		// MessageDialog.openInformation(new Shell(), "dclcheck",
		// "In this version we have not automatizated performing refactorings.");
		// }
		//
		// @Override
		// public String getLabel() {
		// return "move_class(st,C) where C = " + classNameRes;
		// }
		//
		// public String getDescription() {
		// return "Move the statement to the class ";
		// };
		//
		// public org.eclipse.swt.graphics.Image getImage() {
		// return null;
		// };
		// } };
		//
		// }
		//
		// return new IMarkerResolution[] { new IMarkerResolution2() {
		//
		// @Override
		// public void run(IMarker m) {
		// MessageDialog.openInformation(new Shell(), "dclcheck",
		// "In this version we have not automatizated performing refactorings.");
		// }
		//
		// @Override
		// public String getLabel() {
		// return "Nothing";
		// }
		//
		// public String getDescription() {
		// return "Nothing";
		// };
		//
		// public org.eclipse.swt.graphics.Image getImage() {
		// return null;
		// };
		// } };
		// } catch (CoreException e) {
		// DCLUtil.showError(new Shell(),
		// "There was some error when finding the fixes.");
		// return null;
		// }
	}

	/**
	 * For now, it always returns true.
	 */
	@Override
	public boolean hasResolutions(IMarker marker) {
		return true;
	}
}
