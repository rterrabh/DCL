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
import dclsuite.util.FormatUtil;
import dclsuite.util.Statistics;

public class SuitableModule {
	/* Number of Move Suggestions that the algorithm will return */
	private static final int MOVE_SUGGESTIONS = 999999;
	private static final boolean DEBUG = false;

	private static final ICoefficientStrategy[] coefficientStrategies = { new JaccardCoefficientStrategy(), new SMCCoefficientStrategy(),
			new SorensensCoefficientStrategy(), new MountfordCoefficientStrategy(), new BaroniUrbaniCoefficientStrategy() };
	private static final ICoefficientStrategy coefficientStrategy = coefficientStrategies[0];

	private SuitableModule() {
	}

	/* Original Method */
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

	public static StringBuilder calculateAll(IProject project, final Architecture architecture, final String originClassName,
			final DependencyType dependencyType, final String expectedModule) {

		StringBuilder strBuilder = new StringBuilder("Class under analysis: " + originClassName + "\n\n");
		
		String resume = "";

		for (ICoefficientStrategy strategy : coefficientStrategies) {
			Set<ModuleSimilarity> suitableModules = calculate(project, architecture, originClassName, null, null,
					strategy, dependencyType);
			strBuilder.append(strategy.getName() + ":\n");
			int i = 0;
			for (ModuleSimilarity ms : suitableModules) {
				strBuilder.append(FormatUtil.formatInt(++i) + ": " + ms.getModuleDescription() + "\t"
						+ FormatUtil.formatDouble(ms.getSimilarity()) + "\n");
				if (ms.getModuleDescription().equals(expectedModule)) {
					resume += i + "\t" + FormatUtil.formatDouble(ms.getSimilarity()) + "\t";
				}
			}
			strBuilder.append("\n\n\n");
		}

		strBuilder.append("\n\n\n");
		strBuilder.append(resume + "\t");
		
		
		
		for (ICoefficientStrategy strategy : coefficientStrategies) {
			Set<ModuleSimilarity> suitableModules = calculate(project, architecture, originClassName, null, null,
					strategy, null);
			
			double array[] = new double[suitableModules.size()];
			int i = 0;
			for (ModuleSimilarity ms : suitableModules){
				array[i++] = ms.getSimilarity();
			}
			
			Statistics st = new Statistics(array);
			
			strBuilder.append(FormatUtil.formatDouble(st.getMin()) + "\t" + FormatUtil.formatDouble(st.getMax()) + "\t" + FormatUtil.formatDouble(st.getAverage()) + "\t" + FormatUtil.formatDouble(st.getStandardDeviation()) + "\t");
		}
		
		return strBuilder;
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
		if (!Double.isNaN(similarity) && !Double.isInfinite(similarity)) { /* Avoid NaN values */
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