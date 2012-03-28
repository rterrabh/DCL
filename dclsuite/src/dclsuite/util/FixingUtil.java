package dclsuite.util;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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

	public static double similarity(Collection<Dependency> colDepA, Collection<Dependency> colDepB,
			DependencyType dependencyType) {
		Collection<SimpleDependency> rA = new HashSet<SimpleDependency>();
		for (Dependency d : colDepA) {
			if (dependencyType == null || d.getDependencyType().equals(dependencyType)) {
				rA.add(new SimpleDependency(d.getDependencyType(), d.getClassNameB()));
			}
		}

		if (rA.isEmpty()) {
			return 0;
		}

		Collection<SimpleDependency> rB = new HashSet<SimpleDependency>();
		for (Dependency d : colDepB) {
			if (dependencyType == null || d.getDependencyType().equals(dependencyType)) {
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

	public static List<ModuleSimilarity> suitableModule(IProject project, final Architecture architecture,
			final String targetClassName, final DependencyType dependencyType) {
		final Map<String, Double> modules = new LinkedHashMap<String, Double>();

		final Collection<String> projectClasses = architecture.getProjectClasses();

		final Collection<Dependency> targetClassNameDependencies = architecture.getDependencies(targetClassName);

		for (String className : projectClasses) {
			final String respectiveModuleName = DCLUtil.getPackageFromClassName(className) + ".*";

			Collection<Dependency> dependencies = architecture.getDependencies(className);

			double similarity = similarity(targetClassNameDependencies, dependencies, dependencyType);

			if (similarity != 0) {
				if (!modules.containsKey(respectiveModuleName)) {
					modules.put(respectiveModuleName, similarity);
				} else {
					modules.put(respectiveModuleName, (similarity + modules.get(respectiveModuleName)) / 2.0);
				}

				for (String moduleName : architecture.getModules().keySet()) {
					if (DCLUtil.hasClassNameByDescription(className, moduleName, architecture.getModules(),
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

		ValueComparator<Double> bvc = new ValueComparator<Double>(modules);
		Map<String, Double> sortedModules = new TreeMap<String, Double>(bvc);
		sortedModules.putAll(modules);

		List<ModuleSimilarity> result = new LinkedList<ModuleSimilarity>();
		for (String moduleName : sortedModules.keySet()) {
			if (result.size() >= MOVE_SUGGESTIONS) {
				break;
			}
			result.add(new ModuleSimilarity(moduleName, sortedModules.get(moduleName)));
		}

		return result;
	}

//	public static List<ModuleSimilarity> suitableModule(final Architecture architecture, final String targetClassName,
//			final DependencyType dependencyType, final String classNameB) {
//		final Map<String, Double> modules = new LinkedHashMap<String, Double>();
//		final Collection<String> projectClasses = architecture.getProjectClasses();
//
//		for (String className : projectClasses) {
//			final String respectiveModuleName = DCLUtil.getPackageFromClassName(className) + ".*";
//
//			Collection<Dependency> dependencies = architecture.getDependencies(className);
//
//			double count = 0;
//
//			for (Dependency d : dependencies) {
//				if (d.getDependencyType().equals(dependencyType) && d.getClassNameB().equals(classNameB)) {
//					count += 1;
//				}
//
//				if (count != 0) {
//					if (!modules.containsKey(respectiveModuleName)) {
//						modules.put(respectiveModuleName, count);
//					} else {
//						modules.put(respectiveModuleName, (count + modules.get(respectiveModuleName)) / 2.0);
//					}
//				}
//			}
//		}
//
//		ValueComparator<Double> bvc = new ValueComparator<Double>(modules);
//		Map<String, Double> sortedModules = new TreeMap<String, Double>(bvc);
//		sortedModules.putAll(modules);
//
//		List<ModuleSimilarity> result = new LinkedList<ModuleSimilarity>();
//		for (String moduleName : sortedModules.keySet()) {
//			if (result.size() >= MOVE_SUGGESTIONS) {
//				break;
//			}
//			result.add(new ModuleSimilarity(moduleName, sortedModules.get(moduleName)));
//		}
//
//		return result;
//	}
//
//	public static List<ModuleSimilarity> suitableModule(final IProject project, final Architecture architecture, final String targetClassName,
//			final DependencyType dependencyType, final String moduleDescriptionB) {
//		final Map<String, Double> modules = new LinkedHashMap<String, Double>();
//		final Collection<String> projectClassNames = architecture.getProjectClasses();
//
//		for (String className : projectClassNames) {
//			final String respectiveModuleName = DCLUtil.getPackageFromClassName(className) + ".*";
//
//			Collection<Dependency> dependencies = architecture.getDependencies(className);
//
//			double count = 0;
//
//			for (Dependency d : dependencies) {
//				if (d.getDependencyType().equals(dependencyType)
//						&& DCLUtil.hasClassNameByDescription(className, moduleDescriptionB, architecture.getModules(),
//								projectClassNames, project)) {
//					count += 1;
//				}
//
//				if (count != 0) {
//					if (!modules.containsKey(respectiveModuleName)) {
//						modules.put(respectiveModuleName, count);
//					} else {
//						modules.put(respectiveModuleName, (count + modules.get(respectiveModuleName)) / 2.0);
//					}
//				}
//			}
//		}
//
//		ValueComparator<Double> bvc = new ValueComparator<Double>(modules);
//		Map<String, Double> sortedModules = new TreeMap<String, Double>(bvc);
//		sortedModules.putAll(modules);
//
//		List<ModuleSimilarity> result = new LinkedList<ModuleSimilarity>();
//		for (String moduleName : sortedModules.keySet()) {
//			if (result.size() >= MOVE_SUGGESTIONS) {
//				break;
//			}
//			result.add(new ModuleSimilarity(moduleName, sortedModules.get(moduleName)));
//		}
//
//		return result;
//	}

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