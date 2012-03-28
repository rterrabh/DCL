package dclsuite.core;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolution2;

import dclsuite.core.DependencyConstraint.ArchitecturalDrift;

public class ArchitectureFixing {

//	private final ArchitectureValidation architectureValidation;
//
//	public ArchictecureFixing(ArchitectureValidation architectureValidation) {
//		this.architectureValidation = architectureValidation;
//	}
	
	public IMarkerResolution[] findFixes(ArchitecturalDrift ad){
	//	System.out.println(ad);
		
		
		
//		return null;
		return new IMarkerResolution2[]{
				new IMarkerResolution2() {
					
					@Override
					public void run(IMarker m) {
						MessageDialog.openInformation(new Shell(),
								"dclcheck", "In this version we have not automatizated performing refactorings.");
					}
					
					@Override
					public String getLabel() {
						return "remove( [extends/implements B] )";
					}
					
					public String getDescription() {
						return "Remove the extension/implementation of the type B because it is not used.";
					};
					
					public org.eclipse.swt.graphics.Image getImage() {
						return null;
					};
					
				},
				new IMarkerResolution2() {
					
					@Override
					public void run(IMarker m) {
						MessageDialog.openInformation(new Shell(),
								"dclcheck", "To perform the refactorings is not available in current version.");
					}
					
					@Override
					public String getLabel() {
						return "move_class(A,M) where M = \"DAO\"";
					}
					
					public String getDescription() {
						return "<html><p>Move the entire class A to one of the following modules M:</p></html><br />DAO\r\nBO";
					};
					
					public org.eclipse.swt.graphics.Image getImage() {
						return null;
					};
					
				}
		};
	}

}
