package dclsuite.resolution.similarity;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
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
import dclsuite.resolution.similarity.coefficients.DotProductCoefficientStrategy;
import dclsuite.resolution.similarity.coefficients.HamannCoefficientStrategy;
import dclsuite.resolution.similarity.coefficients.ICoefficientStrategy;
import dclsuite.resolution.similarity.coefficients.JaccardCoefficientStrategy;
import dclsuite.resolution.similarity.coefficients.KulczynskiCoefficientStrategy;
import dclsuite.resolution.similarity.coefficients.OchiaiCoefficientStrategy;
import dclsuite.resolution.similarity.coefficients.PSCCoefficientStrategy;
import dclsuite.resolution.similarity.coefficients.PhiBinaryDistance;
import dclsuite.resolution.similarity.coefficients.RelativeMatchingCoefficientStrategy;
import dclsuite.resolution.similarity.coefficients.RogersTanimotoCoefficientStrategy;
import dclsuite.resolution.similarity.coefficients.RussellRaoCoefficientStrategy;
import dclsuite.resolution.similarity.coefficients.SMCCoefficientStrategy;
import dclsuite.resolution.similarity.coefficients.SokalBinaryDistanceCoefficientStrategy;
import dclsuite.resolution.similarity.coefficients.SokalSneath2CoefficientStrategy;
import dclsuite.resolution.similarity.coefficients.SokalSneath4CoefficientStrategy;
import dclsuite.resolution.similarity.coefficients.SokalSneathCoefficientStrategy;
import dclsuite.resolution.similarity.coefficients.SorensonCoefficientStrategy;
import dclsuite.resolution.similarity.coefficients.YuleCoefficientStrategy;
import dclsuite.util.DCLUtil;
import dclsuite.util.FormatUtil;
import dclsuite.util.Statistics;

public class SuitableModuleAirp {
	private static final ICoefficientStrategy[] coefficientStrategies = { new JaccardCoefficientStrategy(), new SMCCoefficientStrategy(),
			new YuleCoefficientStrategy(), new HamannCoefficientStrategy(), new SorensonCoefficientStrategy(),
			new RogersTanimotoCoefficientStrategy(), new SokalSneathCoefficientStrategy(), new RussellRaoCoefficientStrategy(),
			new BaroniUrbaniCoefficientStrategy(), new SokalBinaryDistanceCoefficientStrategy(), new OchiaiCoefficientStrategy(),
			new PhiBinaryDistance(), new PSCCoefficientStrategy(), new DotProductCoefficientStrategy(),
			new KulczynskiCoefficientStrategy(), new SokalSneath2CoefficientStrategy(), new SokalSneath4CoefficientStrategy(),
			new RelativeMatchingCoefficientStrategy() };

	/* Number of Move Suggestions that the algorithm will return */
	private final int moveSuggestions;
	private final Map<String, Set<String>> cacheDependenciesByClass;
	private Set<String> cacheUniverseOfDependencies;

	public SuitableModuleAirp(int moveSuggestions) {
		this.moveSuggestions = moveSuggestions;
		this.cacheDependenciesByClass = new HashMap<String, Set<String>>();
		this.cacheUniverseOfDependencies = null;
	}

	public StringBuilder calculateAll(IProject project, final Architecture architecture, final String originClassName,
			final DependencyType dependencyType, final String expectedModule, CoverageStrategy coverageStrategy,
			Set<String> typesUsedByClassUnderAnalysis) {
		if (coverageStrategy.equals(CoverageStrategy.ONLY_TYPES)) {
			this.cacheDependenciesByClass.put(originClassName, typesUsedByClassUnderAnalysis);
		}
		return this.calculateAll(project, architecture, originClassName, dependencyType, expectedModule, coverageStrategy);
	}

	/**
	 * METHOD OF AIRP EVALUATION
	 */
	public StringBuilder calculateAll(IProject project, final Architecture architecture, final String originClassName,
			final DependencyType dependencyType, final String expectedModule, CoverageStrategy coverageStrategy) {
		StringBuilder resume = new StringBuilder();
		resume.append(originClassName + "\t");

		Map<Class<? extends ICoefficientStrategy>, Set<ModuleSimilarity>> suitableModulesByCoefficient = this.calculate(project,
				architecture, originClassName, null, null, coefficientStrategies, dependencyType, coverageStrategy);

		for (ICoefficientStrategy cs : coefficientStrategies) {
			Set<ModuleSimilarity> suitableModules = suitableModulesByCoefficient.get(cs.getClass());

			if (suitableModules != null) {
				int i = 0;
				boolean flag = true;
				for (ModuleSimilarity ms : suitableModules) {
					i++;
					if (ms.getModuleDescription().equals(expectedModule)
							&& !cs.getClass().equals(SokalBinaryDistanceCoefficientStrategy.class)) {
						resume.append(i + "\t" + FormatUtil.formatDouble(ms.getSimilarity()) + "\t");
						flag = false;
						break;
					} else if (ms.getModuleDescription().equals(expectedModule)
							&& cs.getClass().equals(SokalBinaryDistanceCoefficientStrategy.class)) {
						resume.append(suitableModules.size() - i + 1 + "\t" + FormatUtil.formatDouble(ms.getSimilarity()) + "\t");
						flag = false;
						break;
					}
				}
				if (flag) {
					resume.append(++i + "\t" + "0" + "\t");
				}
			}
		}

		resume.append("\t");

		for (ICoefficientStrategy cs : coefficientStrategies) {
			Set<ModuleSimilarity> suitableModules = suitableModulesByCoefficient.get(cs.getClass());

			double array[] = new double[suitableModules.size()];
			int i = 0;
			for (ModuleSimilarity ms : suitableModules) {
				array[i++] = ms.getSimilarity();
			}

			Statistics st = new Statistics(array);

			resume.append(FormatUtil.formatDouble(st.getMin()) + "\t" + FormatUtil.formatDouble(st.getMax()) + "\t"
					+ FormatUtil.formatDouble(st.getAverage()) + "\t" + FormatUtil.formatDouble(st.getStandardDeviation()) + "\t" + "\""
					+ Arrays.toString(st.getArray()) + "\"\t");
		}

		return resume;
	}

	private Map<Class<? extends ICoefficientStrategy>, Set<ModuleSimilarity>> calculate(IProject project, final Architecture architecture,
			final String originClassName, final String targetClassName, final ConstraintType constraintType,
			final ICoefficientStrategy[] coefficientStrategies, final DependencyType dependencyType, CoverageStrategy coverageStrategy) {

		Map<Class<? extends ICoefficientStrategy>, Set<ModuleSimilarity>> result = new LinkedHashMap<Class<? extends ICoefficientStrategy>, Set<ModuleSimilarity>>();
		for (ICoefficientStrategy cs : coefficientStrategies) {
			result.put(cs.getClass(), new TreeSet<ModuleSimilarity>());
		}

		Map<Class<? extends ICoefficientStrategy>, Map<String, Double>> similarityModuleByStrategy = new LinkedHashMap<Class<? extends ICoefficientStrategy>, Map<String, Double>>();
		for (ICoefficientStrategy cs : coefficientStrategies) {
			similarityModuleByStrategy.put(cs.getClass(), new LinkedHashMap<String, Double>());
		}

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
			final Set<String> dependenciesClassB = this.getDependenciesToBeAnalyzed(architecture, classB, dependencyType, coverageStrategy);

			final String respectiveModuleName = DCLUtil.getPackageFromClassName(classB) + ".*";

			int numberAB = CollectionUtils.intersection(dependenciesClassA, dependenciesClassB).size();
			int numberAsubB = CollectionUtils.subtract(dependenciesClassA, dependenciesClassB).size();
			int numberBsubA = CollectionUtils.subtract(dependenciesClassB, dependenciesClassA).size();
			int numberNotAB = dependenciesProject.size() - numberAB - numberAsubB - numberBsubA;

			for (ICoefficientStrategy cs : coefficientStrategies) {
				adjustModuleSimilarity(project, architecture, similarityModuleByStrategy.get(cs.getClass()), classB, respectiveModuleName,
						cs, numberAB, numberAsubB, numberBsubA, numberNotAB);
			}

		}

		for (ICoefficientStrategy cs : coefficientStrategies) {
			List<Entry<String, Double>> modules = new LinkedList<Entry<String, Double>>(similarityModuleByStrategy.get(cs.getClass())
					.entrySet());

			Collections.sort(modules, new Comparator<Entry<String, Double>>() {

				public int compare(Entry<String, Double> e1, Entry<String, Double> e2) {
					return e2.getValue().compareTo(e1.getValue());
				};

			});

			Set<ModuleSimilarity> resultParcial = result.get(cs.getClass());
			for (int i = 0; i < Math.min(moveSuggestions, modules.size()); i++) {
				resultParcial.add(new ModuleSimilarity(modules.get(i).getKey(), modules.get(i).getValue(),
						(dependencyType == null) ? CoverageStrategy.ALL_DEPENDENCIES : CoverageStrategy.PARTICULAR_DEPENDENCY, cs));
			}

		}

		return result;
	}

	/**
	 * Method adjusts the similarity of the module every time we calculate the
	 * similarity for a class.
	 */
	private void adjustModuleSimilarity(IProject project, final Architecture architecture, final Map<String, Double> modules,
			String classB, final String respectiveModuleName, final ICoefficientStrategy coefficientStrategy, int a, int b, int c, int d) {

		double similarity = coefficientStrategy.calculate(a, b, c, d);

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

	private Set<String> getDependenciesToBeAnalyzed(final Architecture architecture, final String originClassName,
			final DependencyType dependencyType, final CoverageStrategy coverageStrategy) {
		if (cacheDependenciesByClass.containsKey(originClassName)) {
			return cacheDependenciesByClass.get(originClassName);
		}

		switch (coverageStrategy) {
		case ALL_DEPENDENCIES:
			cacheDependenciesByClass.put(originClassName, architecture.getUsedClasses(originClassName, DependencyType.DEPEND));
		case PARTICULAR_DEPENDENCY:
			cacheDependenciesByClass.put(originClassName, architecture.getUsedClasses(originClassName, dependencyType));
		case ONLY_TYPES:
			cacheDependenciesByClass.put(originClassName, architecture.getUsedClasses(originClassName));
		}

		return cacheDependenciesByClass.get(originClassName);
	}

	private Set<String> getDependenciesToBeAnalyzed(final Architecture architecture, final DependencyType dependencyType,
			final CoverageStrategy coverageStrategy) {
		if (cacheUniverseOfDependencies != null) {
			return cacheUniverseOfDependencies;
		}

		switch (coverageStrategy) {
		case ALL_DEPENDENCIES:
			cacheUniverseOfDependencies = architecture.getUniverseOfUsedClasses(DependencyType.DEPEND);
		case PARTICULAR_DEPENDENCY:
			cacheUniverseOfDependencies = architecture.getUniverseOfUsedClasses(dependencyType);
		case ONLY_TYPES:
			cacheUniverseOfDependencies = architecture.getUniverseOfUsedClasses();
		}
		return cacheUniverseOfDependencies;
	}
}