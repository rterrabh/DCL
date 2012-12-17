package dclsuite.builder;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

import dclsuite.Activator;
import dclsuite.core.Architecture;
import dclsuite.dependencies.Dependency;
import dclsuite.exception.ParseException;
import dclsuite.resolution.similarity.ModuleSimilarity.CoverageStrategy;
import dclsuite.resolution.similarity.SuitableModuleAirp;
import dclsuite.util.ArchitectureUtils;
import dclsuite.util.DCLPersistence;
import dclsuite.util.DCLUtil;
import dclsuite.util.DateUtil;

public class SimilarityCalculationAction implements IObjectActionDelegate {

	private ISelection selection;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		if (selection instanceof IStructuredSelection) {
			for (Iterator<?> it = ((IStructuredSelection) selection).iterator(); it.hasNext();) {
				Object element = it.next();
				IProject project = null;
				if (element instanceof IProject) {
					project = (IProject) element;
				} else if (element instanceof IAdaptable) {
					project = (IProject) ((IAdaptable) element).getAdapter(IProject.class);
				}
				if (project != null) {
					toggleNature(project);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action
	 * .IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.
	 * action.IAction, org.eclipse.ui.IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

	private boolean isAloneInItsPackage(IJavaProject javaProject, String className) throws JavaModelException, CoreException{
		IFile file = DCLUtil.getFileFromClassName(javaProject, className);
		if (file.getParent().getType() == IResource.FOLDER){
			IFolder folder = (IFolder) file.getParent();
			IResource members[] = folder.members();
			for (IResource resource : members){
				if (resource.equals(file)){
					continue;
				}
				if (DCLUtil.isJavaFile(resource)){
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Toggles sample nature on a project
	 * 
	 * @param project
	 *            to have sample nature added or removed
	 */
	private void toggleNature(IProject project) {
		try {
			if (!DCLUtil.isDclEnabled(project)){
				return;
			}			
			
			IJavaProject javaProject = JavaCore.create(project);
			Shell shell = Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell();
			ElementListSelectionDialog dialog = new ElementListSelectionDialog(shell, new LabelProvider());
			dialog.setMultipleSelection(false);
			dialog.setElements(new String[] { "[T]", "[dt, T]" });
			dialog.setTitle("Which kind of dependencies?");
			dialog.setInitialSelections(new String[] { "[T]" });

			// User pressed cancel
			if (dialog.open() != Window.OK) {
				return;
			}
			String strategy = (String) dialog.getFirstResult();

			Architecture architecture = this.init(project);
			StringBuilder result = new StringBuilder();
			Collection<String> classes = DCLUtil.getClassNames(project);

			long inicioGeral = System.currentTimeMillis();
			int i = 0;

			String filename = "result_" + strategy + "_" + project.getName() + "_" + DateUtil.dateToStr(new Date(), "yyyyMMdd'_'HHmmss") + ".txt";
			
			PrintWriter out = new PrintWriter(new FileOutputStream("/NoBackup/" + filename));
			
			SuitableModuleAirp airp = new SuitableModuleAirp(Integer.MAX_VALUE);
			
			for (String classUnderAnalysis : classes) {
				System.out.printf("%4d of %4d: (%s): ", ++i, classes.size(), classUnderAnalysis);

				String targetModule = DCLUtil.getPackageFromClassName(classUnderAnalysis) + ".*";

				if (isAloneInItsPackage(javaProject, classUnderAnalysis)) {
					System.out.println("ignored (lonely).");
					continue;
				}

				//System.out.printf(" (uses %d classes): ", architecture.getUsedClasses(classUnderAnalysis).size());
				Set<String> typesUsedByClassUnderAnalysis = architecture.getUsedClasses(classUnderAnalysis);
				if (typesUsedByClassUnderAnalysis.size() < 5 ) {
					System.out.println("ignored (uses less than 5 types).");
					continue;
				}
				
				
				long inicio = System.currentTimeMillis();
				if (strategy.equals("[T]")) {
					result.append(airp.calculateAll(project, architecture, classUnderAnalysis, null,
							("".equals(targetModule) ? null : targetModule), CoverageStrategy.ONLY_TYPES, typesUsedByClassUnderAnalysis));
				} else {
					result.append(airp.calculateAll(project, architecture, classUnderAnalysis, null,
							("".equals(targetModule) ? null : targetModule), CoverageStrategy.ALL_DEPENDENCIES));
				}
				System.out.printf("it took %.3f seconds.\n", (System.currentTimeMillis() - inicio) / 1000.0);
				result.append("\n");
				
				out.write(result.toString());
				result.delete(0, result.length());
				System.gc();
			}

			System.out.printf("Total time: %.3f seconds.\n", (System.currentTimeMillis() - inicioGeral) / 1000.0);
			out.close();
			
//			final IFile simFile = project.getFile("similarity.txt");
//			simFile.delete(true, null);
//			InputStream source = new ByteArrayInputStream(result.toString().getBytes());
//			simFile.create(source, true, null);

			//IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), simFile);
			
		} catch (Throwable t) {
			t.printStackTrace();
		}
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
