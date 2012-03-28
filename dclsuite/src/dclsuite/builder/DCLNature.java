package dclsuite.builder;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import dclsuite.util.ArchitectureUtils;
import dclsuite.util.DCLUtil;
import dclsuite.util.MarkerUtils;

public class DCLNature implements IProjectNature {
	/**
	 * ID of this project nature
	 */
	public static final String NATURE_ID = "dclsuite.dclNature";

	private IProject project;

	public void configure() throws CoreException {
		final IFile dcFile = project.getFile(DCLUtil.DC_FILENAME);
		if (!dcFile.exists()){
			String contents = "%Automatic Created File\n";
			contents += "only $system can-depend $java";
			InputStream source = new ByteArrayInputStream(contents.getBytes());
			dcFile.create(source, false, null);
		}	

		IProjectDescription desc = project.getDescription();
		ICommand[] commands = desc.getBuildSpec();

		for (int i = 0; i < commands.length; ++i) {
			if (commands[i].getBuilderName().equals(DCLBuilder.BUILDER_ID)) {
				return;
			}
		}

		ICommand[] newCommands = new ICommand[commands.length + 1];
		System.arraycopy(commands, 0, newCommands, 0, commands.length);
		ICommand command = desc.newCommand();
		command.setBuilderName(DCLBuilder.BUILDER_ID);
		newCommands[newCommands.length - 1] = command;
		desc.setBuildSpec(newCommands);
		project.setDescription(desc, null);
		IDE.openEditor(PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage(), dcFile);
	}

	public void deconfigure() throws CoreException {
		IProjectDescription description = getProject().getDescription();
		ICommand[] commands = description.getBuildSpec();
		for (int i = 0; i < commands.length; ++i) {
			if (commands[i].getBuilderName().equals(DCLBuilder.BUILDER_ID)) {
				ICommand[] newCommands = new ICommand[commands.length - 1];
				System.arraycopy(commands, 0, newCommands, 0, i);
				System.arraycopy(commands, i + 1, newCommands, i,
						commands.length - i - 1);
				description.setBuildSpec(newCommands);
				project.setDescription(description, null);
				
				ArchitectureUtils.cleanArchitecture(this.project);
				MarkerUtils.deleteMarkers(this.project);
				MarkerUtils.deleteErrorMarker(this.project);
				
				return;
			}
		}
	}

	public IProject getProject() {
		return project;
	}

	public void setProject(IProject project) {
		this.project = project;
	}

}
