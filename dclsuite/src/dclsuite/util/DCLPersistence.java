package dclsuite.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import dclsuite.dependencies.Dependency;

public final class DCLPersistence {
	private static final String DCLDATA_FOLDER = "dcldata";

	private DCLPersistence() {
	}

	public static void persist(final IProject project, final String className, final Collection<Dependency> dependencies)
			throws CoreException, IOException {
		IFolder folder = project.getFolder(DCLDATA_FOLDER);
		if (!folder.exists()) {
			folder.create(false, true, null);
			folder.setHidden(true);
		} else {
			folder.setHidden(true);
		}
		IFile storeFile = folder.getFile(className);
		if (storeFile.exists()) {
			storeFile.delete(true, false, null);
		}
		ByteArrayOutputStream bStream = new ByteArrayOutputStream();
		ObjectOutputStream oStream = new ObjectOutputStream(bStream);
		oStream.writeObject(dependencies);
		byte[] byteVal = bStream.toByteArray();
		storeFile.create(new ByteArrayInputStream(byteVal), IResource.FORCE, null);
	}

	public static void delete(final IProject project, final String className) throws CoreException {
		IFile storeFile = project.getFile(DCLDATA_FOLDER + File.separator + className);
		if (!storeFile.exists()) {
			storeFile.delete(true, false, null);
		}
	}

	@SuppressWarnings("unchecked")
	public static Collection<Dependency> load(final IProject project, final String className) throws CoreException, IOException,
			ClassNotFoundException {
		IFile storeFile = project.getFile(DCLDATA_FOLDER + File.separator + className);
		if (storeFile.exists()) {
			ObjectInputStream in = null;
			try {
				in = new ObjectInputStream(storeFile.getContents());
				return (Collection<Dependency>) in.readObject();
			} finally {
				if (in != null) {
					in.close();
				}
			}
		}
		return null;
	}

	public static void clean(final IProject project) throws CoreException {
		IFolder folder = project.getFolder(DCLDATA_FOLDER);
		if (folder.exists()) {
			folder.delete(true, false, null);
		}
	}

	public static boolean existsDCLFolder(final IProject project) {
		return project.getFolder(DCLDATA_FOLDER).exists();
	}

}
