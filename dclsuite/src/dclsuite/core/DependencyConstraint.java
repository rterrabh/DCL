package dclsuite.core;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

import dclsuite.dependencies.Dependency;
import dclsuite.dependencies.DeriveDependency;
import dclsuite.dependencies.ExtendDependency;
import dclsuite.dependencies.ImplementDependency;
import dclsuite.dependencies.ThrowDependency;
import dclsuite.enums.Constraint;
import dclsuite.enums.ConstraintType;
import dclsuite.util.DCLUtil;

public class DependencyConstraint implements Comparable<DependencyConstraint> {
	private final String moduleDescriptionA;
	private final String moduleDescriptionB;
	private final Constraint constraint;

	public DependencyConstraint(String moduleDescriptionA, String moduleDescriptionB, Constraint constraint) {
		super();
		this.moduleDescriptionA = moduleDescriptionA;
		this.moduleDescriptionB = moduleDescriptionB;
		this.constraint = constraint;
	}

	public List<ArchitecturalDrift> validate(String className, final Map<String, String> modules, Set<String> projectClasses,
			Collection<Dependency> dependencies, IProject project) throws CoreException {
		switch (this.constraint.getConstraintType()) {
		case ONLY_CAN:
			if (DCLUtil.hasClassNameByDescription(className, moduleDescriptionA, modules, projectClasses, project)) {
				return null;
			}
			return this.validateCannot(className, moduleDescriptionB, this.constraint.getDependencyType().getDependencyClass(), modules,
					projectClasses, dependencies, project);

		case CANNOT:
			if (!DCLUtil.hasClassNameByDescription(className, moduleDescriptionA, modules, projectClasses, project)) {
				return null;
			}
			return this.validateCannot(className, moduleDescriptionB, this.constraint.getDependencyType().getDependencyClass(), modules,
					projectClasses, dependencies, project);

		case CAN_ONLY:
			if (!DCLUtil.hasClassNameByDescription(className, moduleDescriptionA, modules, projectClasses, project)) {
				return null;
			}
			return this.validateCanOnly(className, moduleDescriptionB, this.constraint.getDependencyType().getDependencyClass(), modules,
					projectClasses, dependencies, project);

		case MUST:
			if (!DCLUtil.hasClassNameByDescription(className, moduleDescriptionA, modules, projectClasses, project)) {
				return null;
			}
			return this.validateMust(className, moduleDescriptionB, this.constraint.getDependencyType().getDependencyClass(), modules,
					projectClasses, dependencies, project);
		}

		return null;
	}

	/**
	 * cannot
	 */
	private List<ArchitecturalDrift> validateCannot(String className, String moduleDescriptionB,
			Class<? extends Dependency> dependencyClass, Map<String, String> modules, Set<String> projectClasses,
			Collection<Dependency> dependencies, IProject project) {
		List<ArchitecturalDrift> architecturalDrifts = new LinkedList<ArchitecturalDrift>();
		/* For each dependency */
		for (Dependency d : dependencies) {
			if (dependencyClass.isAssignableFrom(d.getClass())) {
				if (d.getClassNameB().equals(d.getClassNameA())) {
					continue;
				}
				if (DCLUtil.hasClassNameByDescription(d.getClassNameB(), moduleDescriptionB, modules, projectClasses, project)) {
					architecturalDrifts.add(new DivergenceArchitecturalDrift(this, d));
				}
			}
		}
		return architecturalDrifts;
	}

	/**
	 * can only
	 */
	private List<ArchitecturalDrift> validateCanOnly(String className, String moduleDescriptionB,
			Class<? extends Dependency> dependencyClass, Map<String, String> modules, Set<String> projectClasses,
			Collection<Dependency> dependencies, IProject project) {
		List<ArchitecturalDrift> architecturalDrifts = new LinkedList<ArchitecturalDrift>();

		/* For each dependency */
		for (Dependency d : dependencies) {
			if (dependencyClass.isAssignableFrom(d.getClass())) {
				if (d.getClassNameB().equals(d.getClassNameA())) {
					continue;
				}
				if (!DCLUtil.hasClassNameByDescription(d.getClassNameB(), moduleDescriptionB, modules, projectClasses, project)) {
					architecturalDrifts.add(new DivergenceArchitecturalDrift(this, d));
				}

			}
		}
		return architecturalDrifts;
	}

	/**
	 * must
	 */
	private List<ArchitecturalDrift> validateMust(String className, String moduleDescriptionB, Class<? extends Dependency> dependencyClass,
			Map<String, String> modules, Set<String> projectClasses, Collection<Dependency> dependencies, IProject project) {
		List<ArchitecturalDrift> architecturalDrifts = new LinkedList<ArchitecturalDrift>();

		// TODO: What am I supposed to do in case of internal class?
		if (className.contains("$")) {
			return null;
		} else if (className.equals(moduleDescriptionB)) {
			return null;
		} else if (DCLUtil.hasClassNameByDescription(className, moduleDescriptionB, modules, projectClasses, project)) {
			return null;
		}

		boolean found = false;
		for (Dependency d : dependencies) {
			if (dependencyClass.isAssignableFrom(d.getClass())) {
				if (DCLUtil.hasClassNameByDescription(d.getClassNameB(), moduleDescriptionB, modules, projectClasses, project)) {
					found = true;
					break;
				}
			}
		}
		if (!found) {
			architecturalDrifts.add(new AbsenceArchitecturalDrift(this, className, moduleDescriptionB));
		}

		return architecturalDrifts;
	}

	@Override
	public String toString() {
		return (this.constraint.getConstraintType().equals(ConstraintType.ONLY_CAN) ? "only " : "") + this.moduleDescriptionA + " "
				+ this.constraint.getValue() + " " + this.moduleDescriptionB;
	}

	public int compareTo(DependencyConstraint o) {
		return this.toString().compareTo(o.toString());
	}

	public Constraint getConstraint() {
		return this.constraint;
	}

	public String getModuleDescriptionA() {
		return this.moduleDescriptionA;
	}

	public String getModuleDescriptionB() {
		return this.moduleDescriptionB;
	}

	/**
	 * DCL2 Class that stores the crucial informations about the architectural
	 * drift
	 */
	public static abstract class ArchitecturalDrift {
		public static final String DIVERGENCE = "DIVERGENCE";
		public static final String ABSENCE = "ABSENCE";

		protected final DependencyConstraint violatedConstraint;

		protected ArchitecturalDrift(DependencyConstraint violatedConstraint) {
			super();
			this.violatedConstraint = violatedConstraint;
		}

		public final DependencyConstraint getViolatedConstraint() {
			return this.violatedConstraint;
		}

		public abstract String getDetailedMessage();

		public abstract String getInfoMessage();

		public abstract String getViolationType();

	}

	public static class DivergenceArchitecturalDrift extends ArchitecturalDrift {
		private final Dependency forbiddenDependency;

		public DivergenceArchitecturalDrift(DependencyConstraint violatedConstraint, Dependency forbiddenDependency) {
			super(violatedConstraint);
			this.forbiddenDependency = forbiddenDependency;
		}

		public final Dependency getForbiddenDependency() {
			return this.forbiddenDependency;
		}

		@Override
		public String getDetailedMessage() {
			return this.forbiddenDependency.toString();
		}

		@Override
		public String getInfoMessage() {
			return this.forbiddenDependency.toShortString();
		}

		@Override
		public String getViolationType() {
			return DIVERGENCE;
		}
	}

	public static class AbsenceArchitecturalDrift extends ArchitecturalDrift {
		private final String classNameA;
		private final String moduleDescriptionB;

		public AbsenceArchitecturalDrift(DependencyConstraint violatedConstraint, String classNameA, String moduleDescriptionB) {
			super(violatedConstraint);
			this.classNameA = classNameA;
			this.moduleDescriptionB = moduleDescriptionB;
		}

		public final String getClassNameA() {
			return this.classNameA;
		}

		public String getModuleNameB() {
			return this.moduleDescriptionB;
		}

		@Override
		public String getDetailedMessage() {
			return this.classNameA + " does not " + this.violatedConstraint.getConstraint().getDependencyType().getValue()
					+ " any type in " + this.violatedConstraint.getModuleDescriptionB();
		}

		@Override
		public String getInfoMessage() {
			switch (this.violatedConstraint.getConstraint().getDependencyType()) {

			case ACCESS:
				return "The access of fiels or methods of " + this.violatedConstraint.getModuleDescriptionB()
						+ " is required for this location w.r.t. the architecture";
			case DECLARE:
				return "The declaration of " + this.violatedConstraint.getModuleDescriptionB()
						+ " is required for this location w.r.t. the architecture";
			case HANDLE:
				return "The access or declaration (handling) of " + this.violatedConstraint.getModuleDescriptionB()
						+ " is required for this location w.r.t. the architecture";
			case CREATE:
				return "The creation of " + this.violatedConstraint.getModuleDescriptionB()
						+ " is required for this location w.r.t. the architecture";
			case THROW:
				return "The throwing of " + this.violatedConstraint.getModuleDescriptionB()
						+ " is required for this location w.r.t. the architecture";
			case DERIVE:
			case EXTEND:
			case IMPLEMENT:
				return "The inheritance of " + this.violatedConstraint.getModuleDescriptionB()
						+ " is required for this location w.r.t. the architecture";
			case USEANNOTATION:
				return "The annotation @" + this.violatedConstraint.getModuleDescriptionB()
						+ " is required for this location w.r.t. the architecture";
			default:
				return "The dependency with " + this.violatedConstraint.getModuleDescriptionB()
						+ " is required for this location w.r.t. the architecture";
			}
		}

		@Override
		public String getViolationType() {
			return ABSENCE;
		}
	}

}
