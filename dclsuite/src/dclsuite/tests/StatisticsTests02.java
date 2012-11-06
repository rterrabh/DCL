package dclsuite.tests;

import junit.framework.TestCase;

import org.junit.Assert;
import org.junit.Test;

import dclsuite.util.Statistics;

/**
 * @author Luis Miranda
 */
public class StatisticsTests02 extends TestCase {
	private Statistics statistics;

	@Override
	protected void setUp() throws Exception {
		this.statistics = new Statistics(new double[] { 600, 470, 170, 430, 300 });
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
		Assert.assertEquals(27130, this.statistics.getVariance(), 0);
	}

	@Test
	public void testStandardDeviation() {
		Assert.assertEquals(164.7118, this.statistics.getStandardDeviation(), 0.0001);
	}
	
}
