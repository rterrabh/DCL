package dclsuite.enums;

public enum Constraint {
	CAN_ACCESS("can-access",ConstraintType.ONLY_CAN,DependencyType.ACCESS), 
	CAN_USEANNOTATION("can-useannotation",ConstraintType.ONLY_CAN,DependencyType.USEANNOTATION),
	CAN_CREATE("can-create",ConstraintType.ONLY_CAN,DependencyType.CREATE), 
	CAN_DECLARE("can-declare",ConstraintType.ONLY_CAN,DependencyType.DECLARE), 
	CAN_DERIVE("can-derive",ConstraintType.ONLY_CAN,DependencyType.DERIVE), 
	CAN_EXTEND("can-extend",ConstraintType.ONLY_CAN, DependencyType.EXTEND), 
	CAN_HANDLE("can-handle",ConstraintType.ONLY_CAN, DependencyType.HANDLE),
	CAN_IMPLEMENT("can-implement",ConstraintType.ONLY_CAN, DependencyType.IMPLEMENT), 
	CAN_THROW("can-throw",ConstraintType.ONLY_CAN, DependencyType.THROW),
	CAN_DEPEND("can-depend",ConstraintType.ONLY_CAN, DependencyType.DEPEND),
	
	CAN_ACCESS_ONLY("can-access-only",ConstraintType.CAN_ONLY,DependencyType.ACCESS), 
	CAN_USEANNOTATION_ONLY("can-useannotation-only",ConstraintType.CAN_ONLY,DependencyType.USEANNOTATION),
	CAN_CREATE_ONLY("can-create-only",ConstraintType.CAN_ONLY,DependencyType.CREATE), 
	CAN_DECLARE_ONLY("can-declare-only",ConstraintType.CAN_ONLY,DependencyType.DECLARE), 
	CAN_DERIVE_ONLY("can-derive-only",ConstraintType.CAN_ONLY, DependencyType.DERIVE), 
	CAN_EXTEND_ONLY("can-extend-only",ConstraintType.CAN_ONLY, DependencyType.EXTEND), 
	CAN_HANDLE_ONLY("can-handle-only",ConstraintType.CAN_ONLY, DependencyType.HANDLE),
	CAN_IMPLEMENT_ONLY("can-implement-only",ConstraintType.CAN_ONLY, DependencyType.IMPLEMENT), 
	CAN_THROW_ONLY("can-throw-only",ConstraintType.CAN_ONLY, DependencyType.THROW),
	CAN_DEPEND_ONLY("can-depend-only",ConstraintType.CAN_ONLY, DependencyType.DEPEND),
	
	CANNOT_ACCESS("cannot-access",ConstraintType.CANNOT, DependencyType.ACCESS), 
	CANNOT_USEANNOTATION("cannot-useannotation",ConstraintType.CANNOT, DependencyType.USEANNOTATION),
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
	MUST_USEANNOTATION("must-useannotation",ConstraintType.MUST, DependencyType.USEANNOTATION),
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