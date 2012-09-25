package dclsuite.tests;

import java.io.ByteArrayInputStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import dclsuite.core.Architecture;
import dclsuite.core.DependencyConstraint;
import dclsuite.core.DependencyConstraint.ArchitecturalDrift;
import dclsuite.core.parser.DCLParser;
import dclsuite.util.DCLUtil;

/**
 * Base class for jUnit Test
 * @author Ricardo Terra
 */
public abstract class DCLTestCase extends TestCase {
	protected Architecture architecture;
	protected IProject project;
	protected IJavaProject javaProject;

	protected List<ArchitecturalDrift> validateSystem(final String dependencyContraintToBeValidated) throws Exception {
		this.project = ResourcesPlugin.getWorkspace().getRoot().getProject("dclsuite_test");
		this.javaProject = JavaCore.create(project);
		this.architecture = new Architecture(project);

		this.architecture.getModules().clear();
		this.architecture
				.getModules()
				.putAll(DCLParser
						.parseModules(new ByteArrayInputStream(
								"module MA: com.example.a.*\r\nmodule MB: com.example.b.*\r\nmodule MC: com.example.c.*\r\nmodule MD: com.example.d.*\r\nmodule MEX: com.example.ex.*"
										.getBytes())));

		architecture.getDependencyConstraints().clear();
		architecture.getDependencyConstraints().addAll(
				DCLParser.parseDependencyConstraints(new ByteArrayInputStream(dependencyContraintToBeValidated.getBytes())));

		assertEquals(5, this.architecture.getModules().size());
		assertEquals(1, this.architecture.getDependencyConstraints().size());

		for (String className : architecture.getProjectClasses()) {
			IFile resource = DCLUtil.getFileFromClassName(javaProject, className);
			ICompilationUnit unit = ((ICompilationUnit) JavaCore.create((IFile) resource));
			this.architecture.updateDependencies(className, DCLUtil.getDependenciesUsingAST(unit));
		}

		List<ArchitecturalDrift> architecturalDrifts = new LinkedList<DependencyConstraint.ArchitecturalDrift>();

		for (String classUnderValidation : architecture.getProjectClasses()) {
			for (DependencyConstraint dc : architecture.getDependencyConstraints()) {
				Collection<ArchitecturalDrift> result = dc.validate(classUnderValidation, architecture.getModules(),
						architecture.getProjectClasses(), architecture.getDependencies(classUnderValidation), project);
				if (result != null && !result.isEmpty()) {
					architecturalDrifts.addAll(result);
				}
			}
		}

		return architecturalDrifts;
	}

}
