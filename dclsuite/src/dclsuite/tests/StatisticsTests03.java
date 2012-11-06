package dclsuite.tests;

import junit.framework.TestCase;

import org.junit.Assert;
import org.junit.Test;

import dclsuite.util.Statistics;

public class StatisticsTests03 extends TestCase {
	private Statistics statistics;

	@Override
	protected void setUp() throws Exception {
		this.statistics = new Statistics(new double[] { 10.2, 18.2, 1.04, 12.45, 412.17, 721.9, 412.20 });
	}

	@Test
	public void testMin() {
		Assert.assertEquals(1.04, this.statistics.getMin(), 0);
	}

	@Test
	public void testMax() {
		Assert.assertEquals(721.9, this.statistics.getMax(), 0);
	}
	
	@Test
	public void testAverage() {
		Assert.assertEquals(226.88, this.statistics.getAverage() , 0.0001);
	}
	
	@Test
	public void testSum() {
		Assert.assertEquals(1588.16, this.statistics.getSum() , 0);
	}
	
	@Test
	public void testSize() {
		Assert.assertEquals(7, this.statistics.getSize());
	}
	
	@Test
	public void testVariance() {
		Assert.assertEquals(83533.6970, this.statistics.getVariance(), 0.0001);
	}

	@Test
	public void testStandardDeviation() {
		Assert.assertEquals(289.0219, this.statistics.getStandardDeviation(), 0.0001);
	}
	
}
