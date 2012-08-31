package dclsuite.core.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;

import dclsuite.core.DependencyConstraint;
import dclsuite.enums.Constraint;
import dclsuite.exception.ParseException;

public class DCLParser {
	private static final boolean DEBUG = false;

	/**
	 * DCL2 Method responsible to parse the DCL specifications file and to
	 * return a list of modules
	 * 
	 * @param in
	 *            InputStream of the DCL specifications file
	 * @throws IOException
	 *             Error on read the file
	 * @return Map moduleName -> moduleDescription
	 */
	public static final Map<String, String> parseModules(final InputStream in) throws IOException {
		final Map<String, String> moduleDescriptions = new HashMap<String, String>();

		LineNumberReader lnr = new LineNumberReader(new InputStreamReader(in));

		while (lnr.ready()) {
			String line = lnr.readLine().trim();
			if (line.startsWith("%")) {
				continue;
			} else if (line == null || line.trim().equals("")) {
				continue;
			} else if (line.startsWith("module")) {
				String[] split = line.substring(7).split(":");
				String moduleName = split[0].trim();
				String moduleDescription = split[1].trim();
				moduleDescriptions.put(moduleName, moduleDescription);
			}
		}

		return moduleDescriptions;
	}

	/**
	 * DCL2 Method responsible to parse the DCL specifications file and to
	 * return a list of dependency constraints
	 * 
	 * @param in
	 *            InputStream of the DCL specifications file
	 * @throws IOException
	 *             Error on read the file
	 */
	public static final Collection<DependencyConstraint> parseDependencyConstraints(final IProject project, final InputStream in)
			throws IOException, ParseException {
		final List<DependencyConstraint> dependencyConstraints = new LinkedList<DependencyConstraint>();

		LineNumberReader lnr = new LineNumberReader(new InputStreamReader(in));

		while (lnr.ready()) {
			String line = lnr.readLine().trim();
			try {
				if (line.startsWith("%")) {
					continue;
				} else if (line == null || line.trim().equals("")) {
					continue;
				} else if (line.startsWith("module")) {
					continue;
				} else {
					if (line.startsWith("only")) {
						line = line.substring(5);
					}

					int index = (line.indexOf("can") != -1) ? line.indexOf("can") : line.indexOf("must");

					// It already starts after the moduleA
					String moduleDescriptionA = line.substring(0, index - 1);

					int estado = 0;
					String restrictions = "";
					int i = index;
					for (; i < line.length(); i++) {
						if (estado == 0 && line.charAt(i) == ' ') {
							continue;
						} else if (estado == 0 && line.charAt(i) != ' ') {
							estado = 1;
							restrictions += line.charAt(i);
							continue;
						} else if (estado == 1 && line.charAt(i) == ' ') {
							estado = 2;
							continue;
						} else if (estado == 1 && line.charAt(i) == ',') {
							restrictions += line.charAt(i);
							estado = 3;
							continue;
						} else if (estado == 1 && line.charAt(i) != ' ' && line.charAt(i) != ',') {
							restrictions += line.charAt(i);
							continue;
						} else if (estado == 2 && line.charAt(i) == ' ') {
							continue;
						} else if (estado == 2 && line.charAt(i) == ',') {
							estado = 1;
							restrictions += line.charAt(i);
							continue;
						} else if (estado == 2 && line.charAt(i) != ',' && line.charAt(i) != ' ') {
							break;
						} else if (estado == 3 && line.charAt(i) == ' ') {
							continue;
						} else if (estado == 3 && line.charAt(i) != ' ') {
							estado = 1;
							restrictions += line.charAt(i);
							continue;
						}
					}
					String moduleDescriptionB = line.substring(i);

					for (String restriction : restrictions.split(",")) {
						Constraint c = Constraint.getConstraint(restriction);
						dependencyConstraints.add(new DependencyConstraint(moduleDescriptionA, moduleDescriptionB, c));
					}
				}
			} catch (Exception e) {
				throw new ParseException(e, line, lnr.getLineNumber());
			}
		}

		if (DEBUG) {
			for (DependencyConstraint dc : dependencyConstraints) {
				System.out.println(dc);
				System.out.println("============================");
			}
		}
		return dependencyConstraints;
	}
}
