package dclsuite.tests;

import junit.framework.TestCase;

import org.junit.Assert;
import org.junit.Test;

import dclsuite.util.Statistics;

public class StatisticsTests01 extends TestCase {
	private Statistics statistics;

	@Override
	protected void setUp() throws Exception {
		this.statistics = new Statistics(new double[] { 2, 4, 4, 4, 5, 5, 7, 9 });
	}

	@Test
	public void testMin() {
		Assert.assertEquals(2.0, this.statistics.getMin(), 0);
	}

	@Test
	public void testMax() {
		Assert.assertEquals(9.0, this.statistics.getMax(), 0);
	}
	
	@Test
	public void testAverage() {
		Assert.assertEquals(5.0, this.statistics.getAverage() , 0);
	}
	
	@Test
	public void testSum() {
		Assert.assertEquals(40.0, this.statistics.getSum() , 0);
	}
	
	@Test
	public void testSize() {
		Assert.assertEquals(8, this.statistics.getSize());
	}
	
	@Test
	public void testVariance() {
		Assert.assertEquals(4.5714, this.statistics.getVariance(), 0.0001);
	}

	@Test
	public void testStandardDeviation() {
		Assert.assertEquals(2.1380, this.statistics.getStandardDeviation(), 0.0001);
	}
	
}
