package dclsuite.resolution;

import java.io.IOException;
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
import dclsuite.util.ArchitectureUtils;
import dclsuite.util.DCLPersistence;
import dclsuite.util.MarkerUtils;

public class MarkerResolutionGenerator implements IMarkerResolutionGenerator, IMarkerResolutionGenerator2 {

	private static final IMarkerResolution noResolutionMarker = MarkerUtils.createMarkerResolution("None (" + '\u2205' + ")",
			"The dclfix tool have not found suggestions.");

	@Override
	public IMarkerResolution[] getResolutions(IMarker marker) {
		final IProject project = marker.getResource().getProject();
		IMarkerResolution[] suggestions = null;
		try {
			Architecture architecture = this.init(project);

			final String violationType = MarkerUtils.getViolationType(marker);
			final String classNameA = MarkerUtils.getClassNameA(marker);
			final DependencyType dependencyType = MarkerUtils.getDependencyType(marker);
			final String moduleDescriptionA = MarkerUtils.getModuleDescriptionA(marker);
			final String moduleDescriptionB = MarkerUtils.getModuleDescriptionB(marker);
			final Constraint violatedConstraint = MarkerUtils.getConstraint(marker);

			if (violationType.equals(ArchitecturalDrift.DIVERGENCE)) {
				final String classNameB = MarkerUtils.getClassNameB(marker);
				final Integer lineNumberA = MarkerUtils.getLineNumberA(marker);
				Dependency dependency = architecture.getDependency(classNameA, classNameB, lineNumberA, dependencyType);

				suggestions = DivergenceResolution.getSuggestions(marker, architecture, dependency, moduleDescriptionA, moduleDescriptionB,
						violatedConstraint);
			} else if (violationType.equals(ArchitecturalDrift.ABSENCE)) {
				MissingDependency missingDependency = new MissingDependency(moduleDescriptionA, classNameA, moduleDescriptionB,
						dependencyType);
				suggestions = AbsenceResolution.getSuggestions(marker, architecture, missingDependency);
			}

		} catch (Throwable e) {
			e.printStackTrace();
			return new IMarkerResolution[] { MarkerUtils.createMarkerResolution("error",
					"There was an error while the dclfix tool was trying to find suggestions.") };
		}

		/* If there is no suggestion, let the user know (noResolutionMarker) */
		if (suggestions != null && suggestions.length != 0) {
			return suggestions;
		} else {
			return new IMarkerResolution[] { noResolutionMarker };
		}
	}

	/**
	 * For now, it always returns true.
	 */
	@Override
	public boolean hasResolutions(IMarker marker) {
		return true;
	}

	/**
	 * Method responsible for getting the architecture and the initialization of
	 * the dependencies (if they have not been initialized yet)
	 */
	private Architecture init(IProject project) throws CoreException, IOException, ClassNotFoundException {
		final Architecture architecture = ArchitectureUtils.getOrInitializeArchitecture(project);

		for (String s : architecture.getProjectClasses()) {
			if (architecture.getDependencies(s) == null) {
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

		return architecture;
	}

}
