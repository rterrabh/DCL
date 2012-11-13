package dclsuite.tests;

import junit.framework.TestCase;

import org.junit.Assert;
import org.junit.Test;

import dclsuite.resolution.similarity.coefficients.BaroniUrbaniCoefficientStrategy;
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
import dclsuite.resolution.similarity.coefficients.HamannCoefficientStrategy;
import dclsuite.resolution.similarity.coefficients.PSCCoefficientStrategy;
import dclsuite.resolution.similarity.coefficients.DotProductCoefficientStrategy;
import dclsuite.resolution.similarity.coefficients.KulczynskiCoefficientStrategy;
import dclsuite.resolution.similarity.coefficients.SokalSneath2CoefficientStrategy;
import dclsuite.resolution.similarity.coefficients.SokalSneath4CoefficientStrategy;
import dclsuite.resolution.similarity.coefficients.RelativeMatchingCoefficientStrategy;

/**
 * @author Luis Miranda
 */
public class CoefficientTests02 extends TestCase {
	int a, b, c, d;

	@Override
	protected void setUp() throws Exception {
		a = 10;
		b = 8;
		c = 16;
		d = 60;
	}

	@Test
	public void testJaccard() {
		Assert.assertEquals(0.2941, new JaccardCoefficientStrategy().calculate(a, b, c, d), 1e-3);
	}
	
	@Test
	public void testSMC() {
		Assert.assertEquals(0.7446, new SMCCoefficientStrategy().calculate(a, b, c, d), 1e-3);
	}
	
	@Test
	public void testYule() {
		Assert.assertEquals(0.6483, new YuleCoefficientStrategy().calculate(a, b, c, d), 1e-3);
	}
	
	@Test
	public void testHamann() {
		Assert.assertEquals(0.4893, new HamannCoefficientStrategy().calculate(a, b, c, d), 1e-3);
	}
	
	@Test
	public void testSorenson() {
		Assert.assertEquals(0.4545, new SorensonCoefficientStrategy().calculate(a, b, c, d), 1e-3);
	}
	
	@Test
	public void testRogersTanimoto() {
		Assert.assertEquals(0.5932, new RogersTanimotoCoefficientStrategy().calculate(a, b, c, d), 1e-3);
	}
	
	@Test
	public void testSokalSneath() {
		Assert.assertEquals(0.8536, new SokalSneathCoefficientStrategy().calculate(a, b, c, d), 1e-3);
	}
	
	@Test
	public void testRussellRao() {
		Assert.assertEquals(0.1063, new RussellRaoCoefficientStrategy().calculate(a, b, c, d), 1e-3);
	}
		
	@Test
	public void testBaroniUrbani() {
		Assert.assertEquals(0.5897, new BaroniUrbaniCoefficientStrategy().calculate(a, b, c, d), 1e-3);
	}
	
	@Test
	public void testSokalBinaryDistance() {
		Assert.assertEquals(0.5052, new SokalBinaryDistanceCoefficientStrategy().calculate(a, b, c, d), 1e-3);
	}
	
	@Test
	public void testOchiai() {
		Assert.assertEquals(0.4622, new OchiaiCoefficientStrategy().calculate(a, b, c, d), 1e-3);
	}
	
	@Test
	public void testPhiBinary() {
		Assert.assertEquals(0.3034, new PhiBinaryDistance().calculate(a, b, c, d), 1e-3);
	}
	
	@Test
	public void testPSC() {
		Assert.assertEquals(0.2136, new PSCCoefficientStrategy().calculate(a, b, c, d), 1e-3);
	}
	
	@Test
	public void testDotProduct() {
		Assert.assertEquals(0.2272, new DotProductCoefficientStrategy().calculate(a, b, c, d), 1e-3);
	}
	
	@Test
	public void testKulczynski() {
		Assert.assertEquals(0.5318, new KulczynskiCoefficientStrategy().calculate(a, b, c, d), 1e-3);
	}
	
	@Test
	public void testSokalSneath2() {
		Assert.assertEquals(0.1724, new SokalSneath2CoefficientStrategy().calculate(a, b, c, d), 1e-3);
	}
	
	@Test
	public void testSokalSneath4() {
		Assert.assertEquals(0.0957, new SokalSneath4CoefficientStrategy().calculate(a, b, c, d), 1e-3);
	}
	
	@Test
	public void testRelativeMatching() {
		Assert.assertEquals(0.2911, new RelativeMatchingCoefficientStrategy().calculate(a, b, c, d), 1e-3);
	}
}
