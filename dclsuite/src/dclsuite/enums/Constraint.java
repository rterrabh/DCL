package dclsuite.enums;

public enum Constraint {
	CAN_ACCESS("can-access",ConstraintType.ONLY_CAN,DependencyType.ACCESS), 
	CAN_ANNOTATE("can-annotate",ConstraintType.ONLY_CAN,DependencyType.ANNOTATE),
	CAN_CREATE("can-create",ConstraintType.ONLY_CAN,DependencyType.CREATE), 
	CAN_DECLARE("can-declare",ConstraintType.ONLY_CAN,DependencyType.DECLARE), 
	CAN_DERIVE("can-derive",ConstraintType.ONLY_CAN,DependencyType.DERIVE), 
	CAN_EXTEND("can-extend",ConstraintType.ONLY_CAN, DependencyType.EXTEND), 
	CAN_HANDLE("can-handle",ConstraintType.ONLY_CAN, DependencyType.HANDLE),
	CAN_IMPLEMENT("can-implement",ConstraintType.ONLY_CAN, DependencyType.IMPLEMENT), 
	CAN_THROW("can-throw",ConstraintType.ONLY_CAN, DependencyType.THROW),
	CAN_DEPEND("can-depend",ConstraintType.ONLY_CAN, DependencyType.DEPEND),
	
	CAN_ONLY_ACCESS("can-only-access",ConstraintType.CAN_ONLY,DependencyType.ACCESS), 
	CAN_ONLY_ANNOTATE("can-only-annotate",ConstraintType.CAN_ONLY,DependencyType.ANNOTATE),
	CAN_ONLY_CREATE("can-only-create",ConstraintType.CAN_ONLY,DependencyType.CREATE), 
	CAN_ONLY_DECLARE("can-only-declare",ConstraintType.CAN_ONLY,DependencyType.DECLARE), 
	CAN_ONLY_DERIVE("can-only-derive",ConstraintType.CAN_ONLY, DependencyType.DERIVE), 
	CAN_ONLY_EXTEND("can-only-extend",ConstraintType.CAN_ONLY, DependencyType.EXTEND), 
	CAN_ONLY_HANDLE("can-only-handle",ConstraintType.CAN_ONLY, DependencyType.HANDLE),
	CAN_ONLY_IMPLEMENT("can-only-implement",ConstraintType.CAN_ONLY, DependencyType.IMPLEMENT), 
	CAN_ONLY_THROW("can-only-throw",ConstraintType.CAN_ONLY, DependencyType.THROW),
	CAN_ONLY_DEPEND("can-only-depend",ConstraintType.CAN_ONLY, DependencyType.DEPEND),
	
	CANNOT_ACCESS("cannot-access",ConstraintType.CANNOT, DependencyType.ACCESS), 
	CANNOT_ANNOTATE("cannot-annotate",ConstraintType.CANNOT, DependencyType.ANNOTATE),
	CANNOT_CREATE("cannot-create",ConstraintType.CANNOT, DependencyType.CREATE), 
	CANNOT_DECLARE("cannot-declare",ConstraintType.CANNOT, DependencyType.DECLARE), 
	CANNOT_DERIVE("cannot-derive",ConstraintType.CANNOT, DependencyType.DERIVE), 
	CANNOT_EXTEND("cannot-extend",ConstraintType.CANNOT, DependencyType.EXTEND), 
	CANNOT_HANDLE("cannot-handle",ConstraintType.CANNOT, DependencyType.HANDLE), 
	CANNOT_IMPLEMENT("cannot-implement",ConstraintType.CANNOT, DependencyType.IMPLEMENT), 
	CANNOT_THROW("cannot-throw",ConstraintType.CANNOT, DependencyType.THROW), 
	CANNOT_DEPEND("cannot-depend",ConstraintType.CANNOT, DependencyType.DEPEND),
	
	MUST_EXTEND("must-extend",ConstraintType.MUST, DependencyType.EXTEND),
	MUST_IMPLEMENT("must-implement",ConstraintType.MUST, DependencyType.IMPLEMENT),
	MUST_DERIVE("must-derive",ConstraintType.MUST, DependencyType.DERIVE),
	MUST_ANNOTATE("must-annotate",ConstraintType.MUST, DependencyType.ANNOTATE),
	MUST_THROW("must-throw",ConstraintType.MUST, DependencyType.THROW); 
	
	private final String value;
	private final ConstraintType constraintType;
	private final DependencyType dependencyType;
	
	
	private Constraint(String value, ConstraintType type, DependencyType dependencyType) {
        this.value = value;
        this.constraintType = type;
        this.dependencyType = dependencyType;
    }
	
	public String getValue(){
		return this.value;
	}
	
	public ConstraintType getConstraintType() {
		return this.constraintType;
	}
	
	public DependencyType getDependencyType() {
		return this.dependencyType;
	}
	
	/**
	 * DCL2 Returns the referee constraint by the constraint in DCL syntax.
	 * 
	 * The strings have their character "-" replace by "_", because the
	 * user write "A cannot-depend B", but the enumeration is "CANNOT_DEPEND"
	 * and not "CANNOT-DEPEND".
	 * 
	 * @param value The constraint text in DCL syntax
	 * @return The referee constraint
	 */
	public static Constraint getConstraint(String value){
		return Constraint.valueOf(value.toUpperCase().replaceAll("-", "_"));
	}
}