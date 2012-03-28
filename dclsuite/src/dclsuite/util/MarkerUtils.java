package dclsuite.util;

import static dclsuite.enums.ViolationProperties.DEPENDENCY_TYPE;
import static dclsuite.enums.ViolationProperties.DETAILED_MESSAGE;
import static dclsuite.enums.ViolationProperties.VIOLATED_CONSTRAINT;
import static dclsuite.enums.ViolationProperties.VIOLATION_TYPE;

import java.util.Map.Entry;
import java.util.Properties;

import javax.print.attribute.standard.Severity;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import dclsuite.core.DependencyConstraint.AbsenceArchitecturalDrift;
import dclsuite.core.DependencyConstraint.ArchitecturalDrift;
import dclsuite.core.DependencyConstraint.DivergenceArchitecturalDrift;
import dclsuite.enums.ViolationProperties;

public class MarkerUtils {
	private static final String MARKER_ERROR_TYPE = "dclsuite.error";
	private static final String MARKER_TYPE = "dclsuite.violation";
	private static final Severity SEVERITY_TYPE = Severity.ERROR;

	private MarkerUtils() {
	}

	public static IMarker addErrorMarker(IProject project, String message) throws CoreException {
		IMarker marker = project.createMarker(MARKER_ERROR_TYPE);
		marker.setAttribute(IMarker.SEVERITY, Severity.ERROR.getValue());
		marker.setAttribute(IMarker.MESSAGE, message);
		return marker;
	}
	
	public static void deleteErrorMarker(IProject project) throws CoreException {
		project.deleteMarkers(MarkerUtils.MARKER_ERROR_TYPE, false, IResource.DEPTH_ZERO);
	}
	
	
	public static IMarker addMarker(IFile file, ArchitecturalDrift ad) throws CoreException {
		IMarker marker = file.createMarker(MARKER_TYPE);
		/* IMarker Attributes */
		marker.setAttribute(IMarker.SEVERITY, SEVERITY_TYPE.getValue());
		
		/* Common Attributes */
		marker.setAttribute(VIOLATED_CONSTRAINT.getKey(), ad.getViolatedConstraint().toString());
		marker.setAttribute(VIOLATION_TYPE.getKey(), ad.getViolationType());
		marker.setAttribute(DETAILED_MESSAGE.getKey(), ad.getDetailedMessage());
		marker.setAttribute(ViolationProperties.MODULE_DESCRIPTION_A.getKey(), ad.getViolatedConstraint().getModuleDescriptionA());
		marker.setAttribute(ViolationProperties.MODULE_DESCRIPTION_B.getKey(), ad.getViolatedConstraint().getModuleDescriptionB());
		marker.setAttribute(ViolationProperties.CONSTRAINT.getKey(), ad.getViolatedConstraint().getConstraint().getValue());
		
		
		/* Divergence Infs */
		if (ad instanceof DivergenceArchitecturalDrift){
			DivergenceArchitecturalDrift dad = (DivergenceArchitecturalDrift) ad;
			//marker.setAttribute(IMarker.MESSAGE, "Divergence in <" + ad.getViolatedConstraint().toString() + ">");
			marker.setAttribute(IMarker.MESSAGE, "Divergence: " + ad.getDetailedMessage());
			marker.setAttribute(IMarker.LINE_NUMBER, dad.getForbiddenDependency().getLineNumber());
			
			
			marker.setAttribute(DEPENDENCY_TYPE.getKey(), dad.getForbiddenDependency().getDependencyType().toString());
			
			/* Dependency Props */
			Properties props = dad.getForbiddenDependency().props();
			for (Entry<Object, Object> e : props.entrySet()) {
				marker.setAttribute((String) e.getKey(), (String) e.getValue());
			}
		}else{
			AbsenceArchitecturalDrift aad = (AbsenceArchitecturalDrift) ad;
			marker.setAttribute(ViolationProperties.CLASS_NAME_A.getKey(), aad.getClassNameA());
			
			marker.setAttribute(DEPENDENCY_TYPE.getKey(), aad.getViolatedConstraint().getConstraint().getDependencyType().toString());
			
			marker.setAttribute(IMarker.MESSAGE, "Absence in <" + ad.getViolatedConstraint().toString() + ">");
		}

		return marker;
	}

	public static void deleteMarkers(IFile file) throws CoreException {
		file.deleteMarkers(MarkerUtils.MARKER_TYPE, false, IResource.DEPTH_ZERO);
	}

	public static void deleteMarkers(IProject project) throws CoreException {
		project.deleteMarkers(MARKER_TYPE, true, IResource.DEPTH_INFINITE);
	}

}
