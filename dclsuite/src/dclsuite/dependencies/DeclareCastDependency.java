package dclsuite.dependencies;


public class DeclareCastDependency extends DeclareDependency {



	public DeclareCastDependency(String classNameA, String classNameB,
			Integer lineNumberA, Integer offset, Integer length) {
		super(classNameA, classNameB, lineNumberA, offset, length);
	}

	@Override
	public String toString() {
		return "'" + this.classNameA + "' contains an explicit casting for the type '"
				+ this.classNameB + "'";
	}

}