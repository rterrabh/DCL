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
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolution2;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import dclsuite.core.DependencyConstraint.AbsenceArchitecturalDrift;
import dclsuite.core.DependencyConstraint.ArchitecturalDrift;
import dclsuite.core.DependencyConstraint.DivergenceArchitecturalDrift;
import dclsuite.enums.Constraint;
import dclsuite.enums.DependencyType;
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
		if (ad instanceof DivergenceArchitecturalDrift) {
			DivergenceArchitecturalDrift dad = (DivergenceArchitecturalDrift) ad;
			// marker.setAttribute(IMarker.MESSAGE, "Divergence in <" +
			// ad.getViolatedConstraint().toString() + ">");
			marker.setAttribute(IMarker.MESSAGE, "Divergence: " + ad.getDetailedMessage());
			marker.setAttribute(IMarker.LINE_NUMBER, dad.getForbiddenDependency().getLineNumber());

			if (dad.getForbiddenDependency().getOffset() != null && dad.getForbiddenDependency().getLength() != null) {
				marker.setAttribute(IMarker.CHAR_START, dad.getForbiddenDependency().getOffset());
				marker.setAttribute(IMarker.CHAR_END, dad.getForbiddenDependency().getOffset() + dad.getForbiddenDependency().getLength());
			}

			marker.setAttribute(DEPENDENCY_TYPE.getKey(), dad.getForbiddenDependency().getDependencyType().toString());

			/* Dependency Props */
			Properties props = dad.getForbiddenDependency().props();
			for (Entry<Object, Object> e : props.entrySet()) {
				marker.setAttribute((String) e.getKey(), (String) e.getValue());
			}
		} else {
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

	public static String getViolationType(IMarker marker) throws CoreException {
		return (String) marker.getAttribute(ViolationProperties.VIOLATION_TYPE.getKey());
	}

	public static String getClassNameA(IMarker marker) throws CoreException {
		return (String) marker.getAttribute(ViolationProperties.CLASS_NAME_A.getKey());
	}

	public static String getModuleDescriptionA(IMarker marker) throws CoreException {
		return (String) marker.getAttribute(ViolationProperties.MODULE_DESCRIPTION_A.getKey());
	}

	public static String getModuleDescriptionB(IMarker marker) throws CoreException {
		return (String) marker.getAttribute(ViolationProperties.MODULE_DESCRIPTION_B.getKey());
	}

	public static String getClassNameB(IMarker marker) throws CoreException {
		return (String) marker.getAttribute(ViolationProperties.CLASS_NAME_B.getKey());
	}

	public static DependencyType getDependencyType(IMarker marker) throws CoreException {
		return DependencyType.valueOf((String) marker.getAttribute(ViolationProperties.DEPENDENCY_TYPE.getKey()));
	}

	public static Constraint getConstraint(IMarker marker) throws CoreException {
		return Constraint.getConstraint((String) marker.getAttribute(ViolationProperties.CONSTRAINT.getKey()));
	}

	public static Integer getLineNumberA(IMarker marker) throws CoreException {
		String lineNumberA = (String) marker.getAttribute(ViolationProperties.LINE_NUMBER_A.getKey());
		if (lineNumberA != null) {
			return new Integer(lineNumberA);
		}
		return null;
	}

	public static IMarkerResolution createMarkerResolution(final String label, final String description) {
		return new IMarkerResolution2() {

			@Override
			public void run(IMarker m) {
				final ICompilationUnit unit = ((ICompilationUnit) JavaCore.create((IFile) m.getResource()));
				try {
					Document document = new Document(unit.getBuffer().getContents());
					ASTParser parser = ASTParser.newParser(AST.JLS4);
					parser.setSource(unit.getBuffer().getCharacters());

					CompilationUnit cu = (CompilationUnit) parser.createAST(null);
					AST ast = cu.getAST();
					final ASTRewrite rewriter = ASTRewrite.create(ast);
					
					cu.accept(new ASTVisitor() {

						@Override
						public boolean visit(NormalAnnotation node) {
							rewriter.remove(node, null);
							return false;
						}

						@Override
						public boolean visit(MarkerAnnotation node) {
							rewriter.remove(node, null);
							return false;
						}

						@Override
						public boolean visit(SingleMemberAnnotation node) {
							rewriter.remove(node, null);
							return false;
						}

					});					
					
					TextEdit edits = rewriter.rewriteAST(document, null);
					edits.apply(document);
					unit.getBuffer().setContents(document.get());
				} catch (JavaModelException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (MalformedTreeException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (BadLocationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			@Override
			public String getLabel() {
				return label;
			}

			public String getDescription() {
				return description;
			};

			public org.eclipse.swt.graphics.Image getImage() {
				return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_LCL_LINKTO_HELP);
			};
		};
	}

}
