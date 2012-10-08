package dclsuite.commands;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;

import dclsuite.Activator;
import dclsuite.core.Architecture;
import dclsuite.dependencies.Dependency;
import dclsuite.exception.ParseException;
import dclsuite.resolution.similarity.SuitableModule;
import dclsuite.util.ArchitectureUtils;
import dclsuite.util.DCLPersistence;
import dclsuite.util.DCLUtil;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class SimilarityCalculationHandler extends AbstractHandler {
	/**
	 * The constructor.
	 */
	public SimilarityCalculationHandler() {
	}

	/**
	 * the command has been executed, so extract extract the needed information
	 * from the application context.
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			IEditorPart editorPart = Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
			if (editorPart.getEditorInput().getPersistable() instanceof FileEditorInput) {
				FileEditorInput fei = ((FileEditorInput) editorPart.getEditorInput().getPersistable());
				IFile file = fei.getFile();
				IJavaElement javaElement = JavaCore.create(file);
				String classNameA = DCLUtil.getClassName((ICompilationUnit) javaElement);

				IProject project = file.getProject();
				Architecture architecture = this.init(project);

				StringBuilder strBuilder = SuitableModule.calculateAll(project, architecture, classNameA, null, null, null);

				final IFile simFile = project.getFile("similarity.txt");
				simFile.delete(true, null);
				InputStream source = new ByteArrayInputStream(strBuilder.toString().getBytes());
				simFile.create(source, true, null);
				
				IDE.openEditor(PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow().getActivePage(), simFile);
			}
		} catch (Throwable t) {

		}
		return null;
	}
	
	/**
	 * Method responsible for getting the architecture and the initialization of
	 * the dependencies (if they have not been initialized yet)
	 */
	private Architecture init(IProject project) throws CoreException, IOException, ClassNotFoundException, ParseException {
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
