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
import dclsuite.resolution.similarity.coefficients.BaroniUrbaniCoefficientStrategy;
import dclsuite.resolution.similarity.coefficients.HamannCoefficientStrategy;
import dclsuite.resolution.similarity.coefficients.ICoefficientStrategy;
import dclsuite.resolution.similarity.coefficients.JaccardCoefficientStrategy;
import dclsuite.resolution.similarity.coefficients.OchiaiCoefficientStrategy;
import dclsuite.resolution.similarity.coefficients.PhiBinaryDistance;
import dclsuite.resolution.similarity.coefficients.RogersTanimotoCoefficientStrategy;
import dclsuite.resolution.similarity.coefficients.RussellRaoCoefficientStrategy;
import dclsuite.resolution.similarity.coefficients.SMCCoefficientStrategy;
import dclsuite.resolution.similarity.coefficients.SokalBinaryDistanceCoefficientStrategy;
import dclsuite.resolution.similarity.coefficients.SokalSneathCoefficientStrategy;
import dclsuite.resolution.similarity.coefficients.SorensonCoefficientStrategy;
import dclsuite.resolution.similarity.coefficients.YuleCoefficientStrategy;
import dclsuite.util.DCLUtil;
import dclsuite.util.FormatUtil;
import dclsuite.util.Statistics;

public class SuitableModule {
	/* Number of Move Suggestions that the algorithm will return */
	private static final int MOVE_SUGGESTIONS = 999999;
	private static final boolean DEBUG = false;

	private static final ICoefficientStrategy[] coefficientStrategies = { new JaccardCoefficientStrategy(), new SMCCoefficientStrategy(),
			new YuleCoefficientStrategy(), new HamannCoefficientStrategy(), new SorensonCoefficientStrategy(),
			new RogersTanimotoCoefficientStrategy(), new SokalSneathCoefficientStrategy(), new RussellRaoCoefficientStrategy(),
			new BaroniUrbaniCoefficientStrategy(), new SokalBinaryDistanceCoefficientStrategy(), new OchiaiCoefficientStrategy(),
			new PhiBinaryDistance() };

	private static final ICoefficientStrategy coefficientStrategy = coefficientStrategies[0];

	private SuitableModule() {
	}

	/* Original Method */
	public static Set<ModuleSimilarity> calculate(IProject project, final Architecture architecture, final String originClassName,
			final DependencyType dependencyType, final String targetClassName, final ConstraintType constraintType) {

		Set<ModuleSimilarity> suitableModules = calculate(project, architecture, originClassName, targetClassName, constraintType,
				coefficientStrategy, null, CoverageStrategy.ALL_DEPENDENCIES);

		/**
		 * In MUST violations, there is no DEPENDENCY TYPE with a specific TYPE.
		 * However, maybe A has no JPA annotations, but it has other
		 * annotations.
		 */
		// if (constraintType != ConstraintType.MUST) {
		suitableModules.addAll(calculate(project, architecture, originClassName, targetClassName, constraintType, coefficientStrategy,
				dependencyType, CoverageStrategy.PARTICULAR_DEPENDENCY));
		// }

		return new TreeSet<ModuleSimilarity>(suitableModules);
	}

	/**
	 * 
	 * @param project
	 * @param architecture
	 * @param originClassName
	 * @param dependencyType
	 * @param expectedModule
	 * @return
	 */
	public static StringBuilder calculateAll(IProject project, final Architecture architecture, final String originClassName,
			final DependencyType dependencyType, final String expectedModule, CoverageStrategy coverageStrategy) {
		boolean printResume = true;
		StringBuilder strBuilder = new StringBuilder("Class under analysis: " + originClassName + "\n\n");

		StringBuilder resume = new StringBuilder();

		for (ICoefficientStrategy strategy : coefficientStrategies) {
			Set<ModuleSimilarity> suitableModules = calculate(project, architecture, originClassName, null, null, strategy, dependencyType,
					coverageStrategy);
			strBuilder.append(strategy.getName() + ":\n");

			if (suitableModules != null) {
				int i = 0;
				for (ModuleSimilarity ms : suitableModules) {
					strBuilder.append(FormatUtil.formatInt(++i) + ": " + ms.getModuleDescription() + "\t"
							+ FormatUtil.formatDouble(ms.getSimilarity()) + "\n");
					if (ms.getModuleDescription().equals(expectedModule)
							&& !strategy.getClass().equals(SokalBinaryDistanceCoefficientStrategy.class)) {
						resume.append(i + "\t" + FormatUtil.formatDouble(ms.getSimilarity()) + "\t");
					}

					/*
					 * Handling End of Range that corresponds maximum similarity
					 * of Sokal Binary Distance
					 */
					if (ms.getModuleDescription().equals(expectedModule)
							&& strategy.getClass().equals(SokalBinaryDistanceCoefficientStrategy.class)) {
						resume.append(suitableModules.size() - i + 1 + "\t" + FormatUtil.formatDouble(ms.getSimilarity()) + "\t");
					}
				}
			} else {
				strBuilder.append("Impossible to measure the similarity: |A| = 0, i.e., a = 0.");
				printResume = false;
			}
			strBuilder.append("\n\n\n");
		}

		if (printResume) {
			strBuilder.append("\n\n\n");
			resume.append("\t");

			for (ICoefficientStrategy strategy : coefficientStrategies) {
				Set<ModuleSimilarity> suitableModules = calculate(project, architecture, originClassName, null, null, strategy,
						dependencyType, coverageStrategy);

				double array[] = new double[suitableModules.size()];
				int i = 0;
				for (ModuleSimilarity ms : suitableModules) {
					array[i++] = ms.getSimilarity();
				}

				Statistics st = new Statistics(array);

				resume.append(FormatUtil.formatDouble(st.getMin()) + "\t" + FormatUtil.formatDouble(st.getMax()) + "\t"
						+ FormatUtil.formatDouble(st.getAverage()) + "\t" + FormatUtil.formatDouble(st.getStandardDeviation()) + "\t");
			}

			resume.append("\n\n");

			strBuilder.insert(0, resume);
		}

		return strBuilder;
	}

	private static Set<ModuleSimilarity> calculate(IProject project, final Architecture architecture, final String originClassName,
			final String targetClassName, final ConstraintType constraintType, final ICoefficientStrategy coefficientStrategy,
			final DependencyType dependencyType, CoverageStrategy coverageStrategy) {

		final Map<String, Double> similarityModule = new LinkedHashMap<String, Double>();

		/*
		 * If dependencyType is null, the functions above will consider all
		 * dependencies
		 */
		final Set<String> dependenciesClassA = getDependenciesToBeAnalyzed(architecture, originClassName, dependencyType, coverageStrategy);

		/*
		 * Also, if dependenciesClassA was empty, we do not calculate the
		 * suitable module.
		 */
		if (dependenciesClassA.isEmpty()) {
			return null;
		}

		final Set<String> dependenciesProject = getDependenciesToBeAnalyzed(architecture, dependencyType, coverageStrategy);

		for (String classB : architecture.getProjectClasses()) {
			/* Ignoring the class under analysis */
			if (classB.equals(originClassName)) {
				continue;
			}
			/*
			 * If dependencyType is null, the function above will consider all
			 * dependencies
			 */
			final Set<String> dependenciesClassB = getDependenciesToBeAnalyzed(architecture, classB, dependencyType, coverageStrategy);

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

	/**
	 * Method adjusts the similarity of the module every time we calculate the
	 * similarity for a class.
	 */
	private static void adjustModuleSimilarity(IProject project, final Architecture architecture, final Map<String, Double> modules,
			String classB, final String respectiveModuleName, double similarity) {
		/* In order to avoid NaN values */
		if (!Double.isNaN(similarity) && !Double.isInfinite(similarity)) {
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

	private static Set<String> getDependenciesToBeAnalyzed(final Architecture architecture, final String originClassName,
			final DependencyType dependencyType, final CoverageStrategy coverageStrategy) {
		switch (coverageStrategy) {
		case ALL_DEPENDENCIES:
			return architecture.getUsedClasses(originClassName, DependencyType.DEPEND);
		case PARTICULAR_DEPENDENCY:
			return architecture.getUsedClasses(originClassName, dependencyType);
		case ONLY_TYPES:
			return architecture.getUsedClasses(originClassName);
		}
		return null;
	}

	private static Set<String> getDependenciesToBeAnalyzed(final Architecture architecture, final DependencyType dependencyType,
			final CoverageStrategy coverageStrategy) {
		switch (coverageStrategy) {
		case ALL_DEPENDENCIES:
			return architecture.getUniverseOfUsedClasses(DependencyType.DEPEND);
		case PARTICULAR_DEPENDENCY:
			return architecture.getUniverseOfUsedClasses(dependencyType);
		case ONLY_TYPES:
			return architecture.getUniverseOfUsedClasses();
		}
		return null;
	}
}