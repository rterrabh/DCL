package dclsuite.tests;

import junit.framework.TestCase;

import org.junit.Assert;
import org.junit.Test;

import dclsuite.resolution.similarity.coefficients.BaroniUrbaniCoefficientStrategy;
import dclsuite.resolution.similarity.coefficients.JaccardCoefficientStrategy;
import dclsuite.resolution.similarity.coefficients.SMCCoefficientStrategy;
import dclsuite.resolution.similarity.coefficients.SorensonCoefficientStrategy;
import dclsuite.resolution.similarity.coefficients.YuleCoefficientStrategy;

/**
 * Luis, favor apagar esse comentario e calcular MANUALMENTE os valores
 * dos outros coeficientes. E colocar novos testes, por exemplo:
 * testYule, por exemplo.
 * Por exemplo, no testMin, trocar 170 por 
 * @author Luis Miranda
 */
public class CoefficientTests01 extends TestCase{
	int a, b, c, d;

	@Override
	protected void setUp() throws Exception {
		a = 2;
		b = 1;
		c = 2;
		d = 3;
	}

	@Test
	public void testJaccard() {
		Assert.assertEquals(0.4, new JaccardCoefficientStrategy().calculate(a, b, c, d), 1e-3);
	}
	
	@Test
	public void testSMC() {
		Assert.assertEquals(0.625, new SMCCoefficientStrategy().calculate(a, b, c, d), 1e-3);
	}
	
	@Test
	public void testYule() {
		Assert.assertEquals(0.444, new YuleCoefficientStrategy().calculate(a, b, c, d), 1e-3);
	}
	
	@Test
	public void testSorenson() {
		Assert.assertEquals(0.571, new SorensonCoefficientStrategy().calculate(a, b, c, d), 1e-3);
	}
		
	@Test
	public void testBaroniUrbani() {
		Assert.assertEquals(0.5967, new BaroniUrbaniCoefficientStrategy().calculate(a, b, c, d), 1e-3);
	}
	
}
