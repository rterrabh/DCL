package dclsuite.util;


public class Statistics {
	private double[] array;
	private double min;
	private double max;
	private double sum;
	private double average;
	private double variance;
	private double standardDeviation;
	private int size;

	public Statistics(double[] array) {
		this.array = array;
		this.size = array.length;

		this.sum = 0;
		this.min = this.max = array[0];

		for (double d : array) {
			this.sum += d;
			if (d < this.min) {
				this.min = d;
			}
			if (d > this.max) {
				this.max = d;
			}
		}
		this.average = this.sum / this.size;

		this.variance = 0;

		for (double d : array) {
			this.variance += Math.pow(d - this.average, 2);
		}
		this.variance /= (this.size-1);

		this.standardDeviation = Math.sqrt(variance);
	}

	public double[] getArray() {
		return array;
	}

	public double getMin() {
		return min;
	}

	public double getMax() {
		return max;
	}

	public double getAverage() {
		return average;
	}

	public double getVariance() {
		return variance;
	}

	public double getStandardDeviation() {
		return standardDeviation;
	}

	public int getSize() {
		return size;
	}

	public double getSum() {
		return sum;
	}

}
