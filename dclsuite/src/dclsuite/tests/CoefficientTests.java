package dclsuite.tests;

import junit.framework.TestCase;

import org.junit.Assert;
import org.junit.Test;

import dclsuite.resolution.similarity.BaroniUrbaniCoefficientStrategy;
import dclsuite.resolution.similarity.JaccardCoefficientStrategy;
import dclsuite.resolution.similarity.MountfordCoefficientStrategy;
import dclsuite.resolution.similarity.SMCCoefficientStrategy;
import dclsuite.resolution.similarity.SorensensCoefficientStrategy;

public class CoefficientTests extends TestCase{
	double p, q, r, s;

	@Override
	protected void setUp() throws Exception {
		p = 2;
		q = 1;
		r = 2;
		s = 3;
	}

	@Test
	public void testJaccard() {
		Assert.assertEquals(0.4, new JaccardCoefficientStrategy().calculate(p, q, r, s), 1e-3);
	}
	
	@Test
	public void testSMC() {
		Assert.assertEquals(0.625, new SMCCoefficientStrategy().calculate(p, q, r, s), 1e-3);
	}
	
	@Test
	public void testSorensens() {
		Assert.assertEquals(0.571, new SorensensCoefficientStrategy().calculate(p, q, r, s), 1e-3);
	}
	
	@Test
	public void testMountford() {
		Assert.assertEquals(0.4, new MountfordCoefficientStrategy().calculate(p, q, r, s), 1e-3);
	}
	
	@Test
	public void testBaroniUrbani() {
		Assert.assertEquals(0.5967, new BaroniUrbaniCoefficientStrategy().calculate(p, q, r, s), 1e-3);
	}
	
}
