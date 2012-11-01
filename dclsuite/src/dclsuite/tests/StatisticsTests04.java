package dclsuite.tests;

import junit.framework.TestCase;

import org.junit.Assert;
import org.junit.Test;

import dclsuite.util.Statistics;

public class StatisticsTests04 extends TestCase {
	private Statistics statistics;

	@Override
	protected void setUp() throws Exception {
		this.statistics = new Statistics(new double[] { 0.92, 0.91, 0.89, 0.88, 0.887, 0.8812, 0.863 });
	}

	@Test
	public void testMin() {
		Assert.assertEquals(0.863, this.statistics.getMin(), 0);
	}

	@Test
	public void testMax() {
		Assert.assertEquals(0.92, this.statistics.getMax(), 0);
	}
	
	@Test
	public void testAverage() {
		Assert.assertEquals(0.890171428571428, this.statistics.getAverage() , 0.0001);
	}
	
	@Test
	public void testSum() {
		Assert.assertEquals(6.2312, this.statistics.getSum() , 0.0001);
	}
	
	@Test
	public void testSize() {
		Assert.assertEquals(7, this.statistics.getSize());
	}
	
	@Test
	public void testVariance() {
		Assert.assertEquals(0.000316462040816, this.statistics.getVariance(), 0.0001);
	}

	@Test
	public void testStandardDeviation() {
		Assert.assertEquals(0.0177893800009, this.statistics.getStandardDeviation(), 0.0001);
	}
	
}
