package dclsuite.builder;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;

import dclsuite.core.Architecture;
import dclsuite.core.DependencyConstraint;
import dclsuite.core.DependencyConstraint.ArchitecturalDrift;
import dclsuite.dependencies.Dependency;
import dclsuite.util.ArchitectureUtils;
import dclsuite.util.DCLPersistence;
import dclsuite.util.DCLUtil;
import dclsuite.util.MarkerUtils;

public class DCLBuilder extends IncrementalProjectBuilder {
	public static final String BUILDER_ID = "dclsuite.dclBuilder";

	@Override
	protected void clean(IProgressMonitor monitor) throws CoreException {
		super.clean(monitor);
		DCLPersistence.clean(this.getProject());
		MarkerUtils.deleteMarkers(this.getProject());
		MarkerUtils.deleteErrorMarker(this.getProject());
		ArchitectureUtils.cleanArchitecture(getProject());
	}

	@Override
	@SuppressWarnings("rawtypes")
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException {
		try {
			final long start = System.currentTimeMillis();
			if (kind == FULL_BUILD || !DCLPersistence.existsDCLFolder(this.getProject())) {
				fullBuild(monitor);
			} else {
				IResourceDelta delta = getDelta(this.getProject());
				if (delta == null) {
					fullBuild(monitor);
				} else {
					/* If the architecture has not been initialized yet */
					if (!ArchitectureUtils.hasArchitectureInitialized(getProject())) {
						fullLoad(monitor);
					}
					incrementalBuild(delta, monitor);
				}
			}
			MarkerUtils.deleteErrorMarker(this.getProject());
			System.out.printf("It took %.2f seconds.\n", (System.currentTimeMillis()-start)/1000.0);
		} catch (Throwable e) {
			this.clean(monitor);
			final String logFileName = DCLUtil.logError(this.getProject(), e);
			MarkerUtils.addErrorMarker(this.getProject(), "The dclcheck conformance tool has crashed. (see "
					+ logFileName + ")");
		}
		return null;
	}

	protected void fullLoad(final IProgressMonitor monitor) throws CoreException, IOException, ClassNotFoundException {
		monitor.setTaskName("Checking architecture");
		monitor.subTask("loading dependencies");
		final Architecture architecture = ArchitectureUtils.getOrInitializeArchitecture(this.getProject());
		monitor.beginTask("Checking architecture", architecture.getProjectClasses().size());
		
		for (String className : architecture.getProjectClasses()) {
			monitor.subTask(className);
			Collection<Dependency> dependencies = DCLPersistence.load(this.getProject(), className);
			if (dependencies == null) {
				throw new CoreException(null);
			}
			architecture.updateDependencies(className, dependencies);
			monitor.worked(1);
		}
	}

	protected void fullBuild(final IProgressMonitor monitor) throws CoreException,
			IOException {
		monitor.setTaskName("Checking architecture");
		monitor.subTask("loading dependencies");
		final Architecture architecture = ArchitectureUtils.initializeArchitecture(getProject());
		monitor.beginTask("Checking architecture", architecture.getProjectClasses().size());
		getProject().accept(new FullBuildVisitor(architecture, monitor, true));
	}

	protected void incrementalBuild(IResourceDelta delta, IProgressMonitor monitor) throws CoreException, IOException {
		final Architecture architecture = ArchitectureUtils.getOrInitializeArchitecture(getProject());
		final boolean updateDC = (delta.findMember(new Path(DCLUtil.DC_FILENAME)) != null);
		if (updateDC) {
			architecture.updateDependencyConstraints(this.getProject());
		}
		monitor.beginTask("Checking architecture", delta.getAffectedChildren(IResourceDelta.ADDED
				| IResourceDelta.CHANGED | IResourceDelta.REMOVED, IResource.FILE).length);
		delta.accept(new IncrementalDeltaVisitor(architecture, monitor));
		
		/* For now, any change in the DCL File requires full build */
		if (updateDC) {
			getProject().accept(new FullBuildVisitor(architecture, monitor, false));
		}
	}

	class IncrementalDeltaVisitor implements IResourceDeltaVisitor {
		private final Architecture architecture;
		private final IProgressMonitor monitor;

		public IncrementalDeltaVisitor(Architecture architecture, IProgressMonitor monitor) {
			this.architecture = architecture;
			this.monitor = monitor;
		}

		public boolean visit(IResourceDelta delta) throws CoreException {
			IResource resource = delta.getResource();
			if (!(resource instanceof IFile) || !resource.getName().endsWith(".java") || !resource.exists()) {
				return true;
			}
			monitor.subTask(resource.getName());

			switch (delta.getKind()) {
			case IResourceDelta.ADDED:
				check(resource, architecture, true);
				monitor.worked(1);
				break;
			case IResourceDelta.REMOVED:
				delete(resource);
				monitor.worked(1);
				break;
			case IResourceDelta.CHANGED:
				check(resource, architecture, true);
				monitor.worked(1);
				break;
			}

			/* return true to continue visiting children */
			return true;
		}
	}

	class FullBuildVisitor implements IResourceVisitor {
		private final Architecture architecture;
		private final IProgressMonitor monitor;
		private final boolean reextractDependencies;

		public FullBuildVisitor(Architecture architecture, IProgressMonitor monitor, boolean reextractDependencies) {
			this.architecture = architecture;
			this.monitor = monitor;
			this.reextractDependencies = reextractDependencies;
		}

		public boolean visit(IResource resource) throws CoreException {
			if (resource instanceof IFile && resource.getName().endsWith(".java")) {
				monitor.subTask(resource.getName());
				check(resource, architecture, reextractDependencies);
				monitor.worked(1);
			}
			return true;
		}
	}

	private void check(IResource resource, Architecture architecture, boolean reextractDependencies)
			throws CoreException {
		if (resource instanceof IFile && resource.getName().endsWith(".java")) {
			final IFile file = (IFile) resource;
			MarkerUtils.deleteMarkers(file);

			final ICompilationUnit unit = ((ICompilationUnit) JavaCore.create((IFile) resource));
			final String className = DCLUtil.getClassName(unit);

			try {
				final Collection<Dependency> dependencies;
				if (reextractDependencies) {
					dependencies = DCLUtil.getDependenciesUsingAST(unit);
					architecture.updateDependencies(className, dependencies);
					DCLPersistence.persist(this.getProject(), className, dependencies);
				} else {
					dependencies = architecture.getDependencies(className);
				}

				for (DependencyConstraint dc : architecture.getDependencyConstraints()) {
					Collection<ArchitecturalDrift> result = dc.validate(className, architecture.getModules(),
							architecture.getProjectClasses(), dependencies, this.getProject());
					if (result != null && !result.isEmpty()) {
						for (ArchitecturalDrift ad : result) {
							MarkerUtils.addMarker(file, ad);
						}
					}
				}
			} catch (IOException e) {
				MarkerUtils.addErrorMarker(this.getProject(), "There was a problem in extracting dependencies from "
						+ className);
				throw new CoreException(Status.CANCEL_STATUS);
			}
		}
	}
	
	private void delete(IResource resource) throws CoreException {
		if (resource instanceof IFile && resource.getName().endsWith(".java")) {
			final IFile file = (IFile) resource;
			MarkerUtils.deleteMarkers(file);

			final ICompilationUnit unit = ((ICompilationUnit) JavaCore.create((IFile) resource));
			final String className = DCLUtil.getClassName(unit);
			
			DCLPersistence.delete(this.getProject(), className);
		}
	}
}
