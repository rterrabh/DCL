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
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolution2;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;

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

		marker.setAttribute(IMarker.MESSAGE, ad.getInfoMessage());

		/* Divergence Infs */
		if (ad instanceof DivergenceArchitecturalDrift) {
			DivergenceArchitecturalDrift dad = (DivergenceArchitecturalDrift) ad;
			// marker.setAttribute(IMarker.MESSAGE, "Divergence: " +
			// ad.getDetailedMessage());

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
			
			final ICompilationUnit unit = ((ICompilationUnit) JavaCore.create(file));
			IType type = unit.findPrimaryType();
			marker.setAttribute(IMarker.CHAR_START, type.getNameRange().getOffset());
			marker.setAttribute(IMarker.CHAR_END, type.getNameRange().getOffset() + type.getNameRange().getLength());
			
//			ASTParser parser = ASTParser.newParser(AST.JLS4);
//			parser.setKind(ASTParser.K_COMPILATION_UNIT);
//			parser.setSource(unit);
//			parser.setResolveBindings(false);			
			//CompilationUnit fullClass = (CompilationUnit) parser.createAST(null);
			//marker.setAttribute(IMarker.LINE_NUMBER, fullClass.getLineNumber(type.getNameRange().getOffset()));
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

	public static Integer getAstOffset(IMarker marker) throws CoreException {
		String astOffset = (String) marker.getAttribute(ViolationProperties.AST_OFFSET.getKey());
		if (astOffset != null) {
			return new Integer(astOffset);
		}
		return null;
	}

	public static Integer getAstLength(IMarker marker) throws CoreException {
		String astLength = (String) marker.getAttribute(ViolationProperties.AST_LENGTH.getKey());
		if (astLength != null) {
			return new Integer(astLength);
		}
		return null;
	}

	public static IMarkerResolution createMarkerResolution(final String label, final String description) {
		return new IMarkerResolution2() {

			@Override
			public void run(IMarker m) {
				/*
				 * MessageDialog.openInformation(new Shell(), "dclsuite",
				 * "In this version, the dclsuite tool has not automatizated performing refactorings."
				 * );
				 */
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

	public static IMarkerResolution createMarkerResolutionRemoval(final String label, final String description) {
		return new IMarkerResolution2() {

			@Override
			public void run(IMarker m) {
				final ICompilationUnit unit = ((ICompilationUnit) JavaCore.create((IFile) m.getResource()));
				try {
					Document document = new Document(unit.getBuffer().getContents());
					ASTParser parser = ASTParser.newParser(AST.JLS4);
					parser.setSource(unit.getBuffer().getCharacters());

					Integer offset = MarkerUtils.getAstOffset(m);
					Integer length = MarkerUtils.getAstLength(m);

					CompilationUnit cu = (CompilationUnit) parser.createAST(null);
					AST ast = cu.getAST();

					ASTNode node = NodeFinder.perform(cu.getRoot(), offset, length);
					final ASTRewrite rewriter = ASTRewrite.create(ast);

					rewriter.remove(node, null);

					TextEdit edits = rewriter.rewriteAST(document, null);

					IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), m);
					
					edits.apply(document);
					unit.getBuffer().setContents(document.get());
					
					IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), m);
				} catch (JavaModelException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (MalformedTreeException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (BadLocationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (CoreException e) {
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

	public static IMarkerResolution createMarkerResolutionChangeToMethodInvocation(final String label, final String description,
			final String[] factory) {
		return new IMarkerResolution2() {

			@Override
			public void run(IMarker m) {
				final ICompilationUnit unit = ((ICompilationUnit) JavaCore.create((IFile) m.getResource()));
				try {
					Document document = new Document(unit.getBuffer().getContents());
					ASTParser parser = ASTParser.newParser(AST.JLS4);
					parser.setSource(unit.getBuffer().getCharacters());

					Integer offset = MarkerUtils.getAstOffset(m);
					Integer length = MarkerUtils.getAstLength(m);

					CompilationUnit cu = (CompilationUnit) parser.createAST(null);
					AST ast = cu.getAST();

					ASTNode node = NodeFinder.perform(cu.getRoot(), offset, length);
					final ASTRewrite rewriter = ASTRewrite.create(ast);

					MethodInvocation mi = ast.newMethodInvocation();

					QualifiedName name = ast.newQualifiedName(ast.newName(DCLUtil.getPackageFromClassName(factory[0])),
							ast.newSimpleName(DCLUtil.getSimpleClassName(factory[0])));

					mi.setExpression(name);
					mi.setName(ast.newSimpleName(factory[1]));

					rewriter.replace(node, mi, null);

					TextEdit edits = rewriter.rewriteAST(document, null);

					IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), m);
					
					edits.apply(document);
					unit.getBuffer().setContents(document.get());
					
					ITextEditor editor = (ITextEditor) IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), m);
					editor.selectAndReveal(offset,mi.toString().length());
				} catch (JavaModelException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (MalformedTreeException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (BadLocationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (CoreException e) {
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

	public static IMarkerResolution createMarkerResolutionChangeToOtherType(final String label, final String description, final String type) {
		return new IMarkerResolution2() {

			@Override
			public void run(IMarker m) {
				final ICompilationUnit unit = ((ICompilationUnit) JavaCore.create((IFile) m.getResource()));
				try {
					Document document = new Document(unit.getBuffer().getContents());
					ASTParser parser = ASTParser.newParser(AST.JLS4);
					parser.setSource(unit.getBuffer().getCharacters());

					Integer offset = MarkerUtils.getAstOffset(m);
					Integer length = MarkerUtils.getAstLength(m);

					CompilationUnit cu = (CompilationUnit) parser.createAST(null);
					AST ast = cu.getAST();

					ASTNode node = NodeFinder.perform(cu.getRoot(), offset, length);
					final ASTRewrite rewriter = ASTRewrite.create(ast);

					QualifiedName newType = ast.newQualifiedName(ast.newName(DCLUtil.getPackageFromClassName(type)),
							ast.newSimpleName(DCLUtil.getSimpleClassName(type)));

					rewriter.replace(node, newType, null);

					TextEdit edits = rewriter.rewriteAST(document, null);

					IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), m);
					
					edits.apply(document);
					unit.getBuffer().setContents(document.get());

					IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), m);

					ITextEditor editor = (ITextEditor) IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), m);
					editor.selectAndReveal(offset,newType.toString().length());
				} catch (JavaModelException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (MalformedTreeException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (BadLocationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (CoreException e) {
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

	public static IMarkerResolution createMarkerResolutionChangeToNull(final String label, final String description) {
		return new IMarkerResolution2() {

			@Override
			public void run(IMarker m) {
				final ICompilationUnit unit = ((ICompilationUnit) JavaCore.create((IFile) m.getResource()));
				try {
					Document document = new Document(unit.getBuffer().getContents());
					ASTParser parser = ASTParser.newParser(AST.JLS4);
					parser.setSource(unit.getBuffer().getCharacters());

					Integer offset = MarkerUtils.getAstOffset(m);
					Integer length = MarkerUtils.getAstLength(m);

					CompilationUnit cu = (CompilationUnit) parser.createAST(null);
					AST ast = cu.getAST();

					ASTNode node = NodeFinder.perform(cu.getRoot(), offset, length);
					final ASTRewrite rewriter = ASTRewrite.create(ast);

					rewriter.replace(node, ast.newNullLiteral(), null);

					TextEdit edits = rewriter.rewriteAST(document, null);

					IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), m);
					
					edits.apply(document);
					unit.getBuffer().setContents(document.get());

					ITextEditor editor = (ITextEditor) IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), m);
					editor.selectAndReveal(offset,ast.newNullLiteral().toString().length());
				} catch (JavaModelException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (MalformedTreeException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (BadLocationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (CoreException e) {
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
