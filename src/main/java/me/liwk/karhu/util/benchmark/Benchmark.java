package me.liwk.karhu.util.benchmark;

public class Benchmark {
   private final BenchmarkType profileType;
   private final double magnitudeMultiplier;
   private double runningAverage;
   private double runningMedian;
   private int results;

   public Benchmark(BenchmarkType profileType, int precision) {
      this.profileType = profileType;
      this.magnitudeMultiplier = 1.0 / (double)precision;
      this.results = 0;
   }

   public int results() {
      return this.results;
   }

   public double magnitudeMultiplier() {
      return this.magnitudeMultiplier;
   }

   public BenchmarkType profileType() {
      return this.profileType;
   }

   public double runningAverage() {
      return this.runningAverage;
   }

   public double runningMedian() {
      return this.runningMedian;
   }

   private void insertAverage(double sample) {
      this.runningAverage += (sample - this.runningAverage) * this.magnitudeMultiplier;
   }

   public void insertResult(long start, long end) {
      double nanosecondSpent = (double)(end - start) / 1000000.0;
      this.insertAverage(nanosecondSpent);
      this.insertMedian(nanosecondSpent);
      if (++this.results > 20000) {
         this.results /= 2;
      }
   }

   private void insertMedian(double sample) {
      this.runningMedian += Math.copySign(this.runningMedian * this.magnitudeMultiplier, sample - this.runningMedian);
   }
}
