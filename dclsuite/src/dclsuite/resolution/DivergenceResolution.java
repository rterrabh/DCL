package dclsuite.resolution;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.ui.IMarkerResolution;

import dclsuite.core.Architecture;
import dclsuite.dependencies.AccessDependency;
import dclsuite.dependencies.AccessMethodDependency;
import dclsuite.dependencies.AnnotateDependency;
import dclsuite.dependencies.CreateDependency;
import dclsuite.dependencies.DeclareDependency;
import dclsuite.dependencies.Dependency;
import dclsuite.dependencies.DeriveDependency;
import dclsuite.dependencies.ThrowDependency;
import dclsuite.enums.Constraint;
import dclsuite.enums.ConstraintType;
import dclsuite.util.DCLUtil;
import dclsuite.util.FixingUtil;
import dclsuite.util.FormatUtil;

public class DivergenceResolution {

	public static IMarkerResolution[] getSuggestions(final IMarker marker, final Architecture architecture,
			final Dependency dependency, final String moduleDescriptionA, final String moduleDescriptionB,
			final Constraint violatedConstraint) {

		switch (dependency.getDependencyType()) {
		case ANNOTATE:
			return getSuggestions(marker, architecture, (AnnotateDependency) dependency, moduleDescriptionA,
					moduleDescriptionB, violatedConstraint);
		case ACCESS:
			return getSuggestions(marker, architecture, (AccessDependency) dependency, moduleDescriptionA,
					moduleDescriptionB, violatedConstraint);
		case DECLARE:
			return getSuggestions(marker, architecture, (DeclareDependency) dependency, moduleDescriptionA,
					moduleDescriptionB, violatedConstraint);
		case CREATE:
			return getSuggestions(marker, architecture, (CreateDependency) dependency, moduleDescriptionA,
					moduleDescriptionB, violatedConstraint);
		case EXTEND:
		case IMPLEMENT:
			return getSuggestions(marker, architecture, (DeriveDependency) dependency, moduleDescriptionA,
					moduleDescriptionB, violatedConstraint);
		case THROW:
			return getSuggestions(marker, architecture, (ThrowDependency) dependency, moduleDescriptionA,
					moduleDescriptionB, violatedConstraint);
			
		}
		return null;

	}

	private static IMarkerResolution[] getSuggestions(final IMarker marker, final Architecture architecture,
			AnnotateDependency dependency, final String moduleDescriptionA, final String moduleDescriptionB,
			final Constraint violatedConstraint) {
		final LinkedList<IMarkerResolution> suggestions = new LinkedList<IMarkerResolution>();
		final IProject project = marker.getResource().getProject();

		final List<ModuleSimilarity> suitableModules = FixingUtil.suitableModule(project, architecture,
				dependency.getClassNameA(), dependency.getDependencyType(),null);

		suitableModules.addAll(FixingUtil.suitableModule(project, architecture, dependency.getClassNameA(), null,null));
		
		suitableModules.addAll(FixingUtil.suitableModule(project, architecture, dependency.getClassNameA(), 
				dependency.getDependencyType(),moduleDescriptionB));

		if (suitableModules != null && !suitableModules.isEmpty()) {
			String suitableModulesDescription = "";
			for (ModuleSimilarity ms : suitableModules) {
				suitableModulesDescription += ms.getModuleDescription() + ",";
			}
			suitableModulesDescription = suitableModulesDescription.substring(0,
					suitableModulesDescription.length() - 1);

			String simpleClassName = dependency.getClassNameA().substring(
					dependency.getClassNameA().lastIndexOf(".") + 1);
			String qualifiedClassName = suitableModules.get(0).getModuleDescription().replaceAll("\\.\\*", "") + "."
					+ simpleClassName;
			String qualifiedClassName1 = suitableModules.get(1).getModuleDescription().replaceAll("\\.\\*", "") + "."
					+ simpleClassName;
			String qualifiedClassName2 = suitableModules.get(2).getModuleDescription().replaceAll("\\.\\*", "") + "."
					+ simpleClassName;
			String qualifiedClassName3 = suitableModules.get(3).getModuleDescription().replaceAll("\\.\\*", "") + "."
					+ simpleClassName;

			if (violatedConstraint.getConstraintType().equals(ConstraintType.CANNOT)) {
				/* If the module is exactly the one */
				if (DCLUtil.hasClassNameByDescription(dependency.getClassNameA(), suitableModulesDescription,
						architecture.getModules(), architecture.getProjectClasses(), project)) {
					suggestions.add(FixingUtil.createMarkerResolution("remove(@" + dependency.getClassNameB() + ")",
							null));
					System.out.println(simpleClassName + ": ok");
				} else if (DCLUtil.hasClassNameByDescription(qualifiedClassName, moduleDescriptionA,
						architecture.getModules(), architecture.getProjectClasses(), project)) {
					suggestions.add(FixingUtil.createMarkerResolution("remove(@" + dependency.getClassNameB() + ")",
							null));
					System.out.println(simpleClassName + ": ok");
				} else {
					System.out.println(simpleClassName + ": no");
					/* TEMP */
					for (ModuleSimilarity ms : suitableModules) {
						suggestions.add(FixingUtil.createMarkerResolution(
								"move_class(" + dependency.getClassNameA() + ", " + ms.getModuleDescription()
										+ ") (similarity: " + FormatUtil.formatDouble(ms.getSimilarity()) + ")", null));
					}
				}
			} else if (violatedConstraint.getConstraintType().equals(ConstraintType.ONLY_CAN)) {
				/* If the class "C" is where it should be */
				if (DCLUtil.hasClassNameByDescription(dependency.getClassNameA(), suitableModulesDescription,
						architecture.getModules(), architecture.getProjectClasses(), project)) {
					suggestions.add(FixingUtil.createMarkerResolution("remove(@" + dependency.getClassNameB() + ")",
							null));
					System.out.println(simpleClassName + ": ok");
				} else if (!DCLUtil.hasClassNameByDescription(qualifiedClassName, moduleDescriptionA,
						architecture.getModules(), architecture.getProjectClasses(), project)) {
					suggestions.add(FixingUtil.createMarkerResolution("remove(@" + dependency.getClassNameB() + ")",
							null));
					System.out.println(simpleClassName + ": ok");
				} //else {
					/* TEMP */
					for (ModuleSimilarity ms : suitableModules) {
						suggestions.add(FixingUtil.createMarkerResolution(
								"move_class(" + dependency.getClassNameA() + ", " + ms.getModuleDescription()
										+ ") (similarity: " + FormatUtil.formatDouble(ms.getSimilarity()) + ")", null));
					}
					System.out.println(simpleClassName + ": no");
				//}
			}
		}

		return suggestions.toArray(new IMarkerResolution[suggestions.size()]);
	}

	private static IMarkerResolution[] getSuggestions(final IMarker marker, final Architecture architecture,
			DeriveDependency dependency, final String moduleDescriptionA, final String moduleDescriptionB,
			final Constraint violatedConstraint) {
		final LinkedList<IMarkerResolution> suggestions = new LinkedList<IMarkerResolution>();
		final IProject project = marker.getResource().getProject();

		final List<ModuleSimilarity> suitableModules = FixingUtil.suitableModule(project, architecture,
				dependency.getClassNameA(), dependency.getDependencyType(),null);

		suitableModules.addAll(FixingUtil.suitableModule(project, architecture, dependency.getClassNameA(), null,null));

		suitableModules.addAll(FixingUtil.suitableModule(project, architecture, dependency.getClassNameA(),
				dependency.getDependencyType(),null));

		if (suitableModules != null && !suitableModules.isEmpty()) {
			String suitableModulesDescription = "";
			for (ModuleSimilarity ms : suitableModules) {
				suitableModulesDescription += ms.getModuleDescription() + ",";
			}
			suitableModulesDescription = suitableModulesDescription.substring(0,
					suitableModulesDescription.length() - 1);

			String simpleClassName = dependency.getClassNameA().substring(
					dependency.getClassNameA().lastIndexOf(".") + 1);
			String qualifiedClassName = suitableModules.get(0).getModuleDescription().replaceAll("\\.\\*", "") + "."
					+ simpleClassName;

			/* If the module is exactly the one */
			if (DCLUtil.hasClassNameByDescription(dependency.getClassNameA(), suitableModulesDescription,
					architecture.getModules(), architecture.getProjectClasses(), project)) {
				suggestions.add(FixingUtil.createMarkerResolution("ops", null));
				System.out.println(simpleClassName + ": ok");
			} else if (DCLUtil.hasClassNameByDescription(qualifiedClassName, moduleDescriptionA,
					architecture.getModules(), architecture.getProjectClasses(), project)) {
				suggestions.add(FixingUtil.createMarkerResolution("ops", null));
				System.out.println(simpleClassName + ": ok");
			} // else {
			/* TEMP */
			for (ModuleSimilarity ms : suitableModules) {
				suggestions.add(FixingUtil.createMarkerResolution("move_class(" + dependency.getClassNameA() + ", "
						+ ms.getModuleDescription() + ") (similarity: " + FormatUtil.formatDouble(ms.getSimilarity())
						+ ")", null));
			}
			// }
		}

		return suggestions.toArray(new IMarkerResolution[suggestions.size()]);
	}

	/**
	 * CREATE
	 */
	private static IMarkerResolution[] getSuggestions(final IMarker marker, final Architecture architecture,
			CreateDependency dependency, final String moduleDescriptionA, final String moduleDescriptionB,
			final Constraint violatedConstraint) {
		final LinkedList<IMarkerResolution> suggestions = new LinkedList<IMarkerResolution>();
		final IProject project = marker.getResource().getProject();

		final List<ModuleSimilarity> suitableModules = FixingUtil.suitableModule(project, architecture,
				dependency.getClassNameA(), null,null);

		suitableModules.addAll(FixingUtil.suitableModule(project, architecture, dependency.getClassNameA(), dependency.getDependencyType(),null));

		suitableModules.addAll(FixingUtil.suitableModule(project, architecture, dependency.getClassNameA(), dependency.getDependencyType(),dependency.getClassNameB()));
		
		if (suitableModules != null && !suitableModules.isEmpty()) {
			String suitableModulesDescription = "";
			for (ModuleSimilarity ms : suitableModules) {
				suitableModulesDescription += ms.getModuleDescription() + ",";
			}
			suitableModulesDescription = suitableModulesDescription.substring(0,
					suitableModulesDescription.length() - 1);

			String simpleClassName = dependency.getClassNameA().substring(
					dependency.getClassNameA().lastIndexOf(".") + 1);
			String qualifiedClassName = suitableModules.get(0).getModuleDescription().replaceAll("\\.\\*", "") + "."
					+ simpleClassName;

			/* If the module is exactly the one */
			if (DCLUtil.hasClassNameByDescription(dependency.getClassNameA(), suitableModulesDescription,
					architecture.getModules(), architecture.getProjectClasses(), project)) {
				suggestions.add(FixingUtil.createMarkerResolution("ops", null));
				System.out.println(simpleClassName + ": ok");
			} else if (DCLUtil.hasClassNameByDescription(qualifiedClassName, moduleDescriptionA,
					architecture.getModules(), architecture.getProjectClasses(), project)) {
				suggestions.add(FixingUtil.createMarkerResolution("ops", null));
				System.out.println(simpleClassName + ": ok");
			} // else {
			/* TEMP */
			
			Collections.sort(suitableModules, new Comparator<ModuleSimilarity>() {
			
				@Override
				public int compare(ModuleSimilarity arg0, ModuleSimilarity arg1) {
					return ((Double)arg1.getSimilarity()).compareTo(arg0.getSimilarity());
				}
			
			});
			
			Set<ModuleSimilarity> tmp = new LinkedHashSet<ModuleSimilarity>(suitableModules);
			for (ModuleSimilarity ms : tmp) {
				suggestions.add(FixingUtil.createMarkerResolution("move_class(" + dependency.getClassNameA() + ", "
						+ ms.getModuleDescription() + ") (similarity: " + FormatUtil.formatDouble(ms.getSimilarity())
						+ ")", null));
			}
			// }
		}

		return suggestions.toArray(new IMarkerResolution[suggestions.size()]);
	}
	
	
	
	/**
	 * CREATE
	 */
	private static IMarkerResolution[] getSuggestions(final IMarker marker, final Architecture architecture,
			ThrowDependency dependency, final String moduleDescriptionA, final String moduleDescriptionB,
			final Constraint violatedConstraint) {
		final LinkedList<IMarkerResolution> suggestions = new LinkedList<IMarkerResolution>();
		final IProject project = marker.getResource().getProject();

		final List<ModuleSimilarity> suitableModules = FixingUtil.suitableModule(project, architecture,
				dependency.getClassNameA(), null,null);

		suitableModules.addAll(FixingUtil.suitableModule(project, architecture, dependency.getClassNameA(), dependency.getDependencyType(),null));

		suitableModules.addAll(FixingUtil.suitableModule(project, architecture, dependency.getClassNameA(), dependency.getDependencyType(),dependency.getClassNameB()));
		
		if (suitableModules != null && !suitableModules.isEmpty()) {
			String suitableModulesDescription = "";
			for (ModuleSimilarity ms : suitableModules) {
				suitableModulesDescription += ms.getModuleDescription() + ",";
			}
			suitableModulesDescription = suitableModulesDescription.substring(0,
					suitableModulesDescription.length() - 1);

			String simpleClassName = dependency.getClassNameA().substring(
					dependency.getClassNameA().lastIndexOf(".") + 1);
			String qualifiedClassName = suitableModules.get(0).getModuleDescription().replaceAll("\\.\\*", "") + "."
					+ simpleClassName;

			/* If the module is exactly the one */
			if (DCLUtil.hasClassNameByDescription(dependency.getClassNameA(), suitableModulesDescription,
					architecture.getModules(), architecture.getProjectClasses(), project)) {
				suggestions.add(FixingUtil.createMarkerResolution("ops", null));
				System.out.println(simpleClassName + ": ok");
			} else if (DCLUtil.hasClassNameByDescription(qualifiedClassName, moduleDescriptionA,
					architecture.getModules(), architecture.getProjectClasses(), project)) {
				suggestions.add(FixingUtil.createMarkerResolution("ops", null));
				System.out.println(simpleClassName + ": ok");
			} // else {
			/* TEMP */
			
			Collections.sort(suitableModules, new Comparator<ModuleSimilarity>() {
			
				@Override
				public int compare(ModuleSimilarity arg0, ModuleSimilarity arg1) {
					return ((Double)arg1.getSimilarity()).compareTo(arg0.getSimilarity());
				}
			
			});
			
			Set<ModuleSimilarity> tmp = new LinkedHashSet<ModuleSimilarity>(suitableModules);
			for (ModuleSimilarity ms : tmp) {
				suggestions.add(FixingUtil.createMarkerResolution("move_class(" + dependency.getClassNameA() + ", "
						+ ms.getModuleDescription() + ") (similarity: " + FormatUtil.formatDouble(ms.getSimilarity())
						+ ")", null));
			}
			// }
		}

		return suggestions.toArray(new IMarkerResolution[suggestions.size()]);
	}
	

	/* ACCESS */
	// ADAPT SUITABLE MODULE FUNCTION TO CONSIDER THE NAME OF THE METHOD
	private static IMarkerResolution[] getSuggestions(final IMarker marker, final Architecture architecture,
			AccessDependency dependency, final String moduleDescriptionA, final String moduleDescriptionB,
			final Constraint violatedConstraint) {
		final LinkedList<IMarkerResolution> suggestions = new LinkedList<IMarkerResolution>();
		final IProject project = marker.getResource().getProject();

		final List<ModuleSimilarity> suitableModules = FixingUtil.suitableModule(project, architecture,
				dependency.getClassNameA(), dependency.getDependencyType(),null);

		suitableModules.addAll(FixingUtil.suitableModule(project, architecture, dependency.getClassNameA(), null,null));

		suitableModules.addAll(FixingUtil.suitableModule(project, architecture, dependency.getClassNameA(),
				dependency.getDependencyType(),null));

		if (suitableModules != null && !suitableModules.isEmpty()) {
			String suitableModulesDescription = "";
			for (ModuleSimilarity ms : suitableModules) {
				suitableModulesDescription += ms.getModuleDescription() + ",";
			}
			suitableModulesDescription = suitableModulesDescription.substring(0,
					suitableModulesDescription.length() - 1);

			String simpleClassName = dependency.getClassNameA().substring(
					dependency.getClassNameA().lastIndexOf(".") + 1);
			String qualifiedClassName = suitableModules.get(0).getModuleDescription().replaceAll("\\.\\*", "") + "."
					+ simpleClassName;

			if (violatedConstraint.getConstraintType().equals(ConstraintType.CANNOT)) {
				/* If the module is exactly the one */
				if (DCLUtil.hasClassNameByDescription(dependency.getClassNameA(), suitableModulesDescription,
						architecture.getModules(), architecture.getProjectClasses(), project)) {
					suggestions.add(FixingUtil.createMarkerResolution("ops", null));
				} else if (!moduleDescriptionA.endsWith("*")
						&& DCLUtil.hasClassNameByDescription(qualifiedClassName, moduleDescriptionA,
								architecture.getModules(), architecture.getProjectClasses(), project)) {
					suggestions.add(FixingUtil.createMarkerResolution("ops", null));
				} // else {
				/* TEMP */
				// for (ModuleSimilarity ms : suitableModules) {
				ModuleSimilarity ms = suitableModules.get(1);
				suggestions.add(FixingUtil.createMarkerResolution("move_class(" + dependency.getClassNameA() + ", "
						+ ms.getModuleDescription() + ") (similarity: " + FormatUtil.formatDouble(ms.getSimilarity())
						+ ")", null));
			} else if (violatedConstraint.getConstraintType().equals(ConstraintType.ONLY_CAN)) {
				/* If the module is not the one */
				if (!DCLUtil.hasClassNameByDescription(dependency.getClassNameA(), suitableModulesDescription,
						architecture.getModules(), architecture.getProjectClasses(), project)) {
					ModuleSimilarity ms = suitableModules.get(1);

					if (dependency instanceof AccessMethodDependency) {

						suggestions.add(FixingUtil.createMarkerResolution(
								"move_invocation(" + dependency.getClassNameB() + "."
										+ ((AccessMethodDependency) dependency).getMethodNameB() + "(), "
										+ ms.getModuleDescription() + ") (similarity: "
										+ FormatUtil.formatDouble(ms.getSimilarity()) + ")", null));
					}
				}
			}
			// }
			// }
		}

		return suggestions.toArray(new IMarkerResolution[suggestions.size()]);
	}

	/* ACCESS */
	// ADAPT SUITABLE MODULE FUNCTION TO CONSIDER THE NAME OF THE METHOD
	private static IMarkerResolution[] getSuggestions(final IMarker marker, final Architecture architecture,
			DeclareDependency dependency, final String moduleDescriptionA, final String moduleDescriptionB,
			final Constraint violatedConstraint) {
		final LinkedList<IMarkerResolution> suggestions = new LinkedList<IMarkerResolution>();
		final IProject project = marker.getResource().getProject();

		final List<ModuleSimilarity> suitableModules = FixingUtil.suitableModule(project, architecture,
				dependency.getClassNameA(), dependency.getDependencyType(),null);

		suitableModules.addAll(FixingUtil.suitableModule(project, architecture, dependency.getClassNameA(), null,null));

		suitableModules.addAll(FixingUtil.suitableModule(project, architecture, dependency.getClassNameA(),
				dependency.getDependencyType(),null));

		if (suitableModules != null && !suitableModules.isEmpty()) {
			String suitableModulesDescription = "";
			for (ModuleSimilarity ms : suitableModules) {
				suitableModulesDescription += ms.getModuleDescription() + ",";
			}
			suitableModulesDescription = suitableModulesDescription.substring(0,
					suitableModulesDescription.length() - 1);

			String simpleClassName = dependency.getClassNameA().substring(
					dependency.getClassNameA().lastIndexOf(".") + 1);
			String qualifiedClassName = suitableModules.get(0).getModuleDescription().replaceAll("\\.\\*", "") + "."
					+ simpleClassName;

			if (violatedConstraint.getConstraintType().equals(ConstraintType.CANNOT)) {
				try {
					IJavaProject javaProject = JavaCore.create(project);
					IType type = javaProject.findType(dependency.getClassNameB());

					ITypeHierarchy typeHierarchy = type.newTypeHierarchy(null);
					IType[] typeSubclasses = typeHierarchy.getAllSubtypes(type);

					for (IType t : typeSubclasses) {
						if (!t.getFullyQualifiedName().startsWith("java.")) {
							continue;
						}

						// TODO: Implementar função if can
						if (1 == (Math.sqrt(1))) {
							suggestions.add(FixingUtil.createMarkerResolution("replace(" + dependency.getClassNameB()
									+ ", " + t.getFullyQualifiedName() + ")", null));
						}
					}

				} catch (Exception e) {

				}

				// /* If the module is exactly the one */
				// if
				// (DCLUtil.hasClassNameByDescription(dependency.getClassNameA(),
				// suitableModulesDescription,
				// architecture.getModules(), architecture.getProjectClasses(),
				// project)) {
				// suggestions.add(FixingUtil.createMarkerResolution("ops",
				// null));
				// } else if (!moduleDescriptionA.endsWith("*")
				// && DCLUtil.hasClassNameByDescription(qualifiedClassName,
				// moduleDescriptionA,
				// architecture.getModules(), architecture.getProjectClasses(),
				// project)) {
				// suggestions.add(FixingUtil.createMarkerResolution("ops",
				// null));
				// } // else {
				// /* TEMP */
				// // for (ModuleSimilarity ms : suitableModules) {
				// ModuleSimilarity ms = suitableModules.get(1);
				// suggestions.add(FixingUtil.createMarkerResolution("move_class("
				// + dependency.getClassNameA() + ", "
				// + ms.getModuleDescription() + ") (similarity: " +
				// FormatUtil.formatDouble(ms.getSimilarity())
				// + ")", null));
			} else if (violatedConstraint.getConstraintType().equals(ConstraintType.ONLY_CAN)) {
				/* If the module is not the one */
				if (!DCLUtil.hasClassNameByDescription(dependency.getClassNameA(), suitableModulesDescription,
						architecture.getModules(), architecture.getProjectClasses(), project)) {
					suggestions.add(FixingUtil.createMarkerResolution("remove(@" + dependency.getClassNameB() + ")",
							null));
				}
			}
			// }
			// }
		}

		return suggestions.toArray(new IMarkerResolution[suggestions.size()]);
	}
}
