package dclsuite.tests;

import junit.framework.TestCase;

import org.junit.Assert;
import org.junit.Test;

import dclsuite.util.Statistics;

/**
 * Luis, favor apagar esse comentario e calcular MANUALMENTE os valores
 * de testMin, testMax, etc.
 * Por exemplo, no testMin, trocar 170 por 1.04
 * @author Luis Miranda
 */
public class StatisticsTests03 extends TestCase {
	private Statistics statistics;

	@Override
	protected void setUp() throws Exception {
		this.statistics = new Statistics(new double[] { 10.2, 18.2, 1.04, 12.45, 412.17, 721.9, 412.20 });
	}

	@Test
	public void testMin() {
		Assert.assertEquals(170, this.statistics.getMin(), 0);
	}

	@Test
	public void testMax() {
		Assert.assertEquals(600, this.statistics.getMax(), 0);
	}
	
	@Test
	public void testAverage() {
		Assert.assertEquals(394, this.statistics.getAverage() , 0);
	}
	
	@Test
	public void testSum() {
		Assert.assertEquals(1970, this.statistics.getSum() , 0);
	}
	
	@Test
	public void testSize() {
		Assert.assertEquals(5, this.statistics.getSize());
	}
	
	@Test
	public void testVariance() {
		Assert.assertEquals(21704, this.statistics.getVariance(), 0);
	}

	@Test
	public void testStandardDeviation() {
		Assert.assertEquals(147.3227, this.statistics.getStandardDeviation(), 0.0001);
	}
	
}
