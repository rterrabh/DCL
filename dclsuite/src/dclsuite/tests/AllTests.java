package dclsuite.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({CoefficientTests.class, ExampleDivergenceTestCase.class, ExampleAbsenceTestCase.class,
	A001TestCase.class, A002TestCase.class, A003TestCase.class, A004TestCase.class,
	A005TestCase.class, A006TestCase.class, A007TestCase.class, A008TestCase.class,
	A009TestCase.class, A010TestCase.class, A011TestCase.class, A012TestCase.class,
	A013TestCase.class})
public class AllTests {

}
