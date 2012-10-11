package dclsuite.tests;

import java.util.List;

import dclsuite.core.DependencyConstraint.ArchitecturalDrift;
import dclsuite.core.DependencyConstraint.DivergenceArchitecturalDrift;

/**
 * An example of a DCLTestCase 
 * 1. Test classes must end with "TestCase" and extend DCLTestCase
 * 2. Test methods must start with "test" followed by a two digit number, e.g., test01(), test02(), etc.
 * 3. There is no attributes or constructors, i.e., only methods.
 * 4. There is pre-defined modules: MA, MB, MC, and MEX;
 *    MA  refers to classes of com.example.a.*
 *    MB  refers to classes of com.example.b.*
 *    MC  refers to classes of com.example.c.*
 *    MEX refers to classes of com.example.ex.*
 * @author Ricardo Terra
 */
public class A508TestCase extends DCLTestCase { 

	public void test01() throws Exception {
		List<ArchitecturalDrift> violations = this.validateSystem("only com.example.c.C508 can-useannotation com.example.b.B508"); //Define the constraint to be validated
		
		assertEquals(1, violations.size()); //Check the number of violations (usually only one violation for constraint)
		ArchitecturalDrift ad = violations.get(0);
		
		assertEquals(DivergenceArchitecturalDrift.class, ad.getClass()); //Check the type of violation (divergence or absence)
		/*DivergenceArchitecturalDrift dad = (DivergenceArchitecturalDrift) ad;
		
		assertEquals(AnnotateMethodDependency.class, dad.getForbiddenDependency().getClass()); //Check the type of dependency
		AnnotateMethodDependency annotateMethodDependency = (AnnotateMethodDependency) dad.getForbiddenDependency();

		
		//Check each attribute of the violation
		assertEquals("com.example.a.A508",annotateMethodDependency.getClassNameA());
		assertEquals("com.example.b.B508",annotateMethodDependency.getClassNameB());*/
	}

}
