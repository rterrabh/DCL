package dclsuite.util;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;

import dclsuite.core.Architecture;

public class ArchitectureUtils {
	public static final QualifiedName ARCHITECTURE = new QualifiedName("dclsuite", "architecture");

	private ArchitectureUtils() {
	}

	public static boolean hasArchitectureInitialized(IProject project) throws CoreException {
		return project.getSessionProperties().containsKey(ARCHITECTURE);
	}

	public static Architecture initializeArchitecture(IProject project) throws CoreException {
		final Architecture architecture = new Architecture(project);
		project.setSessionProperty(ARCHITECTURE, architecture);
		return architecture;
	}
	
	public static Architecture getOrInitializeArchitecture(IProject project) throws CoreException {
		final Architecture architecture = (Architecture) project.getSessionProperty(ARCHITECTURE);
		if (architecture == null) {
			return initializeArchitecture(project);
		}
		return architecture;
	}

	/**
	 * Method called by Builder:clean
	 * 
	 * @param project
	 *            Target Project
	 * @throws CoreException
	 */
	public static void cleanArchitecture(IProject project) throws CoreException {
		project.setSessionProperty(ARCHITECTURE, null);
		DCLPersistence.clean(project);
	}

	
}
