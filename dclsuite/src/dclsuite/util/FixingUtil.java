package dclsuite.util;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.collections.CollectionUtils;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolution2;

import dclsuite.core.Architecture;
import dclsuite.dependencies.Dependency;
import dclsuite.enums.DependencyType;
import dclsuite.resolution.ModuleSimilarity;

public final class FixingUtil {
	private static final int MOVE_SUGGESTIONS = 2;

	private FixingUtil() {
	}

	private static double similarity(Collection<Dependency> colDepA, Collection<Dependency> colDepB,
			DependencyType dependencyType, String targetClass) {
		Collection<SimpleDependency> rA = new HashSet<SimpleDependency>();
		for (Dependency d : colDepA) {
			if ((dependencyType == null || d.getDependencyType().equals(dependencyType))) {
				rA.add(new SimpleDependency(d.getDependencyType(), d.getClassNameB()));
			}
		}

		if (rA.isEmpty()) {
			return 0;
		}

		Collection<SimpleDependency> rB = new HashSet<SimpleDependency>();
		for (Dependency d : colDepB) {
			if ((dependencyType == null || d.getDependencyType().equals(dependencyType))) {
				rB.add(new SimpleDependency(d.getDependencyType(), d.getClassNameB()));
			}
		}

		if (rB.isEmpty()) {
			return 0;
		}

		double union = rA.size() + rB.size();

		return CollectionUtils.intersection(rA, rB).size() / union;
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
				return null;
			};
		};
	}

	public static Set<ModuleSimilarity> suitableModule(IProject project, final Architecture architecture,
			final String originClassName, final DependencyType dependencyType, final String targetClassName) {
		final Map<String, Double> similarityModuleParticularDependency = new LinkedHashMap<String, Double>();
		final Map<String, Double> similarityModuleAllDependencies = new LinkedHashMap<String, Double>();

		final Collection<String> projectClasses = architecture.getProjectClasses();

		final Collection<Dependency> dependenciesClassWithViolation = architecture.getDependencies(originClassName);

		for (String otherClassName : projectClasses) {
			/* Ignoring the class under analysis */
			if (otherClassName.equals(originClassName)) {
				continue;
			}

			Collection<Dependency> dependenciesOtherClass = architecture.getDependencies(otherClassName);

			final String respectiveModuleName = DCLUtil.getPackageFromClassName(otherClassName) + ".*";

			double similarityAllDependencies = similarity(dependenciesClassWithViolation, dependenciesOtherClass, null,
					null);
			adjustModuleSimilarity(project, architecture, similarityModuleAllDependencies, otherClassName,
					respectiveModuleName, similarityAllDependencies);

			double similarityParticularDependency = similarity(dependenciesClassWithViolation, dependenciesOtherClass,
					dependencyType, null);
			adjustModuleSimilarity(project, architecture, similarityModuleParticularDependency, otherClassName,
					respectiveModuleName, similarityParticularDependency);

		}

		/* Sorting the maps */
		TreeMap<String, Double> sortedSimilarityModuleAllDependencies = new TreeMap<String, Double>(
				new ValueComparator<Double>(similarityModuleAllDependencies));
		sortedSimilarityModuleAllDependencies.putAll(similarityModuleAllDependencies);

		TreeMap<String, Double> sortedSimilarityModuleParticularDependency = new TreeMap<String, Double>(
				new ValueComparator<Double>(similarityModuleParticularDependency));
		sortedSimilarityModuleParticularDependency.putAll(similarityModuleParticularDependency);

		Set<ModuleSimilarity> result = new LinkedHashSet<ModuleSimilarity>();

		Entry<String, Double> entryAll = null;
		if ((entryAll = sortedSimilarityModuleAllDependencies.pollFirstEntry()) != null) {
			result.add(new ModuleSimilarity(entryAll.getKey(), entryAll.getValue(),
					ModuleSimilarity.Strategy.ALL_DEPENDENCIES));
		}

		Entry<String, Double> entryParticular = null;
		if ((entryParticular = sortedSimilarityModuleParticularDependency.pollFirstEntry()) != null) {
			result.add(new ModuleSimilarity(entryParticular.getKey(), entryParticular.getValue(),
					ModuleSimilarity.Strategy.PARTICULAR_DEPENDENCY));
		}

		while (result.size() < MOVE_SUGGESTIONS) {
			entryAll = sortedSimilarityModuleAllDependencies.pollFirstEntry();
			entryParticular = sortedSimilarityModuleParticularDependency.pollFirstEntry();
			if (entryAll == null) {
				result.add(new ModuleSimilarity(entryParticular.getKey(), entryParticular.getValue(),
						ModuleSimilarity.Strategy.PARTICULAR_DEPENDENCY));
				continue;
			}
			if (entryParticular == null) {
				result.add(new ModuleSimilarity(entryAll.getKey(), entryAll.getValue(),
						ModuleSimilarity.Strategy.ALL_DEPENDENCIES));
				continue;
			}

			if (entryAll.getValue() > entryParticular.getValue()) {
				/* Add entryAll and put back entryParticular */
				result.add(new ModuleSimilarity(entryAll.getKey(), entryAll.getValue(),
						ModuleSimilarity.Strategy.ALL_DEPENDENCIES));
				sortedSimilarityModuleParticularDependency.put(entryParticular.getKey(), entryParticular.getValue());
			} else {
				/* Add entryParticular and put back entryAll */
				result.add(new ModuleSimilarity(entryParticular.getKey(), entryParticular.getValue(),
						ModuleSimilarity.Strategy.PARTICULAR_DEPENDENCY));
				sortedSimilarityModuleAllDependencies.put(entryAll.getKey(), entryAll.getValue());
			}
		}

		for (ModuleSimilarity m : result) {
			System.out.println(m.getModuleDescription() + ">" + m.getSimilarity());
		}

		return result;
	}

	private static void adjustModuleSimilarity(IProject project, final Architecture architecture,
			final Map<String, Double> modules, String otherClassName, final String respectiveModuleName,
			double similarity) {
		if (similarity != 0) {
			/* Packages */
			if (!modules.containsKey(respectiveModuleName)) {
				modules.put(respectiveModuleName, similarity);
			} else {
				modules.put(respectiveModuleName, (similarity + modules.get(respectiveModuleName)) / 2.0);
			}

			/* Defined Modules */
			for (String moduleName : architecture.getModules().keySet()) {
				if (DCLUtil.hasClassNameByDescription(otherClassName, moduleName, architecture.getModules(),
						architecture.getProjectClasses(), project)) {
					if (!modules.containsKey(moduleName)) {
						modules.put(moduleName, similarity);
					} else {
						modules.put(moduleName, (similarity + modules.get(moduleName)) / 2.0);
					}
				}
			}
		}
	}

	// public static boolean isTheRightModule(final String className, final
	// Set<ModuleSimilarity> suitableModules,
	// final Map<String, String> modules, final Collection<String>
	// projectClassNames, final IProject project) {
	//
	// String suitableModulesDescription = "";
	//
	// if (suitableModules != null && !suitableModules.isEmpty()) {
	// for (ModuleSimilarity ms : suitableModules) {
	// suitableModulesDescription += ms.getModuleDescription() + ",";
	// }
	// suitableModulesDescription = suitableModulesDescription.substring(0,
	// suitableModulesDescription.length() - 1);
	// }
	//
	// /* If the module is exactly the one */
	// if (DCLUtil.hasClassNameByDescription(className,
	// suitableModulesDescription, modules, projectClassNames,
	// project)) {
	// return true;
	// }
	//
	// return false;
	// }

	public static boolean isModuleMequalModuleMa(final String className, final String moduleDescription,
			final Set<ModuleSimilarity> suitableModules, final Map<String, String> modules,
			final Collection<String> projectClassNames, final IProject project) {

		String suitableModulesDescription = "";

		if (suitableModules != null && !suitableModules.isEmpty()) {
			for (ModuleSimilarity ms : suitableModules) {
				suitableModulesDescription += ms.getModuleDescription() + ",";
			}
			suitableModulesDescription = suitableModulesDescription.substring(0,
					suitableModulesDescription.length() - 1);
		}
		
		final String simpleClassName = DCLUtil.getSimpleClassName(className);

		for (ModuleSimilarity m : suitableModules) {
			if (m.getModuleDescription().endsWith(".*")) {
				/* Lets simulate if it had been moved */
				String qualifiedClassName = m.getModuleDescription().replaceAll("\\.\\*", "") + "." + simpleClassName;

				if (DCLUtil.hasClassNameByDescription(qualifiedClassName, moduleDescription, modules,
						projectClassNames, project)) {
					return true;
				}
			} else if (moduleDescription.contains(m.getModuleDescription())) {
				/* It's the same as the description */
				return true;
			}

		}

		return false;
	}

	// public static boolean isTheRightModuleIfTheyHaveBeenMoved(final String
	// className, final String moduleDescription, final Set<ModuleSimilarity>
	// suitableModules,
	// final Map<String, String> modules, final Collection<String>
	// projectClassNames, final IProject project) {
	//
	// String suitableModulesDescription = "";
	//
	// if (suitableModules != null && !suitableModules.isEmpty()) {
	// for (ModuleSimilarity ms : suitableModules) {
	// suitableModulesDescription += ms.getModuleDescription() + ",";
	// }
	// suitableModulesDescription = suitableModulesDescription.substring(0,
	// suitableModulesDescription.length() - 1);
	// }
	//
	// final String simpleClassName = DCLUtil.getSimpleClassName(className);
	//
	// /* If after the move, it will be at allowed modules */
	// for (ModuleSimilarity m : suitableModules) {
	// if (m.getModuleDescription().endsWith(".*")){
	// String qualifiedClassName = m.getModuleDescription().replaceAll("\\.\\*",
	// "") + "." + simpleClassName;
	//
	// if (DCLUtil.hasClassNameByDescription(qualifiedClassName,
	// moduleDescription, modules,
	// projectClassNames, project)) {
	// return true;
	// }
	// }
	// if (moduleDescription.contains(m.getModuleDescription())){
	// return true;
	// }
	//
	// }
	//
	// return false;
	// }

}

class ValueComparator<T extends Comparable<T>> implements Comparator<String> {
	private Map<String, T> map;

	public ValueComparator(Map<String, T> map) {
		this.map = map;
	}

	public int compare(String s1, String s2) {
		return map.get(s2).compareTo(map.get(s1));
	}
}

class SimpleDependency {
	private final DependencyType dependencyType;
	private final String classNameB;

	public SimpleDependency(DependencyType dependencyType, String classNameB) {
		this.dependencyType = dependencyType;
		this.classNameB = classNameB;
	}

	public DependencyType getDependencyType() {
		return dependencyType;
	}

	public String getClassNameB() {
		return classNameB;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((classNameB == null) ? 0 : classNameB.hashCode());
		result = prime * result + ((dependencyType == null) ? 0 : dependencyType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SimpleDependency other = (SimpleDependency) obj;
		if (classNameB == null) {
			if (other.classNameB != null)
				return false;
		} else if (!classNameB.equals(other.classNameB))
			return false;
		if (dependencyType != other.dependencyType)
			return false;
		return true;
	}

}