package dclsuite.resolution.similarity;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.collections.CollectionUtils;
import org.eclipse.core.resources.IProject;

import dclsuite.core.Architecture;
import dclsuite.enums.ConstraintType;
import dclsuite.enums.DependencyType;
import dclsuite.resolution.similarity.ModuleSimilarity.CoverageStrategy;
import dclsuite.util.DCLUtil;

public class SuitableModule {
	private static final int MOVE_SUGGESTIONS = 2;
	private static final boolean DEBUG = true;

	private static final ICoefficientStrategy[] coefficientStrategies = { new JaccardCoefficientStrategy(), new SMCCoefficientStrategy(),
			new SorensensCoefficientStrategy(), new MountfordCoefficientStrategy(), new BaroniUrbaniCoefficientStrategy() };
	private static final ICoefficientStrategy coefficientStrategy = coefficientStrategies[0];

	private SuitableModule() {
	}

	public static Set<ModuleSimilarity> calculate(IProject project, final Architecture architecture, final String originClassName,
			final DependencyType dependencyType, final String targetClassName, final ConstraintType constraintType) {

		Set<ModuleSimilarity> suitableModules = calculate(project, architecture, originClassName, targetClassName, constraintType,
				coefficientStrategy, null);

		if (constraintType != ConstraintType.MUST) {
			suitableModules.addAll(calculate(project, architecture, originClassName, targetClassName, constraintType, coefficientStrategy,
					dependencyType));
		}
				
		return new TreeSet<ModuleSimilarity>(suitableModules);
	}

	private static Set<ModuleSimilarity> calculate(IProject project, final Architecture architecture, final String originClassName,
			final String targetClassName, final ConstraintType constraintType, final ICoefficientStrategy coefficientStrategy,
			final DependencyType dependencyType) {

		final Map<String, Double> similarityModule = new LinkedHashMap<String, Double>();

		/*
		 * If dependencyType is null, the functions above will consider all
		 * dependencies
		 */
		final Set<String> dependenciesClassA = architecture.getUsedClasses(originClassName, dependencyType);
		final Set<String> dependenciesProject = architecture.getUniverseOfUsedClasses(dependencyType);

		for (String classB : architecture.getProjectClasses()) {
			/* Ignoring the class under analysis */
			if (classB.equals(originClassName)) {
				continue;
			}
			/*
			 * If dependencyType is null, the function above will consider all
			 * dependencies
			 */
			final Set<String> dependenciesClassB = architecture.getUsedClasses(classB, dependencyType);

			final String respectiveModuleName = DCLUtil.getPackageFromClassName(classB) + ".*";

			int numberAB = CollectionUtils.intersection(dependenciesClassA, dependenciesClassB).size();
			int numberBsubA = CollectionUtils.subtract(dependenciesClassB, dependenciesClassA).size();
			int numberAsubB = CollectionUtils.subtract(dependenciesClassA, dependenciesClassB).size();
			int numberNotAB = dependenciesProject.size() - numberAB - numberAsubB - numberBsubA;

			double similarity = coefficientStrategy.calculate(numberAB, numberAsubB, numberBsubA, numberNotAB);

			adjustModuleSimilarity(project, architecture, similarityModule, classB, respectiveModuleName, similarity);
		}

		List<Entry<String, Double>> modules = new LinkedList<Entry<String, Double>>(similarityModule.entrySet());
		Collections.sort(modules, new Comparator<Entry<String, Double>>() {

			public int compare(Entry<String, Double> e1, Entry<String, Double> e2) {
				return e2.getValue().compareTo(e1.getValue());
			};

		});

		if (DEBUG) {
			System.out.println("\n\n===" + coefficientStrategy.getClass().getSimpleName());
			for (Entry<String, Double> entry : modules) {
				System.out.println(entry.getKey() + ": " + entry.getValue());
			}

		}

		Set<ModuleSimilarity> result = new TreeSet<ModuleSimilarity>();
		for (int i = 0; i < Math.min(MOVE_SUGGESTIONS, modules.size()); i++) {
			result.add(new ModuleSimilarity(modules.get(i).getKey(), modules.get(i).getValue(),
					(dependencyType == null) ? CoverageStrategy.ALL_DEPENDENCIES : CoverageStrategy.PARTICULAR_DEPENDENCY,
					coefficientStrategy));
		}

		return result;
	}

	private static void adjustModuleSimilarity(IProject project, final Architecture architecture, final Map<String, Double> modules,
			String classB, final String respectiveModuleName, double similarity) {
		if (similarity != 0) {
			/* Packages */
			if (!modules.containsKey(respectiveModuleName)) {
				modules.put(respectiveModuleName, similarity);
			} else {
				modules.put(respectiveModuleName, (similarity + modules.get(respectiveModuleName)) / 2.0);
			}

			/* Defined Modules */
			for (String moduleName : architecture.getModules().keySet()) {
				if (DCLUtil.hasClassNameByDescription(classB, moduleName, architecture.getModules(), architecture.getProjectClasses(),
						project)) {
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