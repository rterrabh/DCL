package dclsuite.resolution;

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
import org.eclipse.core.resources.IProject;

import dclsuite.core.Architecture;
import dclsuite.dependencies.Dependency;
import dclsuite.enums.DependencyType;
import dclsuite.util.DCLUtil;

public class SuitableModule {
	private static final int MOVE_SUGGESTIONS = 2;
	private static final boolean DEBUG = false;

	private SuitableModule() {

	}

	public static Set<ModuleSimilarity> suitableModule(IProject project, final Architecture architecture, final String originClassName,
			final DependencyType dependencyType, final String targetClassName) {
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

			double similarityAllDependencies = similarity(dependenciesClassWithViolation, dependenciesOtherClass, null, null);
			adjustModuleSimilarity(project, architecture, similarityModuleAllDependencies, otherClassName, respectiveModuleName,
					similarityAllDependencies);

			double similarityParticularDependency = similarity(dependenciesClassWithViolation, dependenciesOtherClass, dependencyType, null);
			adjustModuleSimilarity(project, architecture, similarityModuleParticularDependency, otherClassName, respectiveModuleName,
					similarityParticularDependency);

		}

		/* Sorting the maps */
		TreeMap<String, Double> sortedSimilarityModuleAllDependencies = new TreeMap<String, Double>(new ValueComparator<Double>(
				similarityModuleAllDependencies));
		sortedSimilarityModuleAllDependencies.putAll(similarityModuleAllDependencies);

		TreeMap<String, Double> sortedSimilarityModuleParticularDependency = new TreeMap<String, Double>(new ValueComparator<Double>(
				similarityModuleParticularDependency));
		sortedSimilarityModuleParticularDependency.putAll(similarityModuleParticularDependency);

		Set<ModuleSimilarity> result = new LinkedHashSet<ModuleSimilarity>();

		Entry<String, Double> entryAll = null;
		if ((entryAll = sortedSimilarityModuleAllDependencies.pollFirstEntry()) != null) {
			result.add(new ModuleSimilarity(entryAll.getKey(), entryAll.getValue(), ModuleSimilarity.Strategy.ALL_DEPENDENCIES));
		}

		Entry<String, Double> entryParticular = null;
		if ((entryParticular = sortedSimilarityModuleParticularDependency.pollFirstEntry()) != null) {
			result.add(new ModuleSimilarity(entryParticular.getKey(), entryParticular.getValue(),
					ModuleSimilarity.Strategy.PARTICULAR_DEPENDENCY));
		}

		while (result.size() < MOVE_SUGGESTIONS) {
			entryAll = sortedSimilarityModuleAllDependencies.pollFirstEntry();
			entryParticular = sortedSimilarityModuleParticularDependency.pollFirstEntry();

			/* If there is no more possible suggestions, then abort */
			if (entryAll == null && entryParticular == null) {
				break;
			}

			if (entryAll == null) {
				result.add(new ModuleSimilarity(entryParticular.getKey(), entryParticular.getValue(),
						ModuleSimilarity.Strategy.PARTICULAR_DEPENDENCY));
				continue;
			}
			if (entryParticular == null) {
				result.add(new ModuleSimilarity(entryAll.getKey(), entryAll.getValue(), ModuleSimilarity.Strategy.ALL_DEPENDENCIES));
				continue;
			}

			if (entryAll.getValue() > entryParticular.getValue()) {
				/* Add entryAll and put back entryParticular */
				result.add(new ModuleSimilarity(entryAll.getKey(), entryAll.getValue(), ModuleSimilarity.Strategy.ALL_DEPENDENCIES));
				sortedSimilarityModuleParticularDependency.put(entryParticular.getKey(), entryParticular.getValue());
			} else {
				/* Add entryParticular and put back entryAll */
				result.add(new ModuleSimilarity(entryParticular.getKey(), entryParticular.getValue(),
						ModuleSimilarity.Strategy.PARTICULAR_DEPENDENCY));
				sortedSimilarityModuleAllDependencies.put(entryAll.getKey(), entryAll.getValue());
			}
		}

		if (DEBUG) {
			for (ModuleSimilarity m : result) {
				System.out.println(m.getModuleDescription() + ">" + m.getSimilarity());
			}
		}

		return result;
	}

	private static double similarity(Collection<Dependency> colDepA, Collection<Dependency> colDepB, DependencyType dependencyType,
			String targetClass) {
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

	private static void adjustModuleSimilarity(IProject project, final Architecture architecture, final Map<String, Double> modules,
			String otherClassName, final String respectiveModuleName, double similarity) {
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