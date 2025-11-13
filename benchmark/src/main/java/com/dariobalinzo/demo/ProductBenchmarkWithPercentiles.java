package com.dariobalinzo.demo;

import com.dariobalinzo.demo.standard.Product;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;

/**
 * Comprehensive benchmark comparing Standard Objects vs Value Classes
 * with detailed percentile tracking (p50, p90, p95, p99, p99.9, p99.99)
 */
@BenchmarkMode({Mode.SampleTime}) // SampleTime mode captures all percentiles
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Thread)
@Warmup(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 5, timeUnit = TimeUnit.SECONDS)
@Fork(value = 3, jvmArgs = {"-Xmx10g", "--enable-preview"})
public class ProductBenchmarkWithPercentiles {

    @Param({"10000", "100000"})
    private int arraySize;

    private byte[] productBytes;
    private Product[] standardProducts;
    private com.dariobalinzo.demo.valhalla.Product[] valhallaProducts;

    @Setup(Level.Trial)
    public void setup() throws Exception {
        Product product = generateSampleProduct();
        productBytes = product.toByteArray();

        standardProducts = new Product[arraySize];
        valhallaProducts = new com.dariobalinzo.demo.valhalla.Product[arraySize];
    }

    @Setup(Level.Iteration)
    public void setupIteration() throws Exception {
        for (int i = 0; i < arraySize; i++) {
            standardProducts[i] = Product.parseFrom(productBytes);
            valhallaProducts[i] = com.dariobalinzo.demo.valhalla.Product.parseFrom(productBytes);
        }
    }

    // ============= PARSING BENCHMARKS =============

    @Benchmark
    @BenchmarkMode({Mode.SampleTime, Mode.AverageTime})
    public void standardParsing(Blackhole bh) throws Exception {
        for (int i = 0; i < arraySize; i++) {
            Product p = Product.parseFrom(productBytes);
            bh.consume(p);
        }
    }

    @Benchmark
    @BenchmarkMode({Mode.SampleTime, Mode.AverageTime})
    public void valhallaParsing(Blackhole bh) throws Exception {
        for (int i = 0; i < arraySize; i++) {
            com.dariobalinzo.demo.valhalla.Product p =
                    com.dariobalinzo.demo.valhalla.Product.parseFrom(productBytes);
            bh.consume(p);
        }
    }

    // ============= SORTING BENCHMARKS =============

    @Benchmark
    @BenchmarkMode({Mode.SampleTime, Mode.AverageTime})
    public void standardSorting(Blackhole bh) {
        Product[] copy = Arrays.copyOf(standardProducts, arraySize);
        Arrays.sort(copy, Comparator.comparingDouble(Product::getPrice));
        bh.consume(copy);
    }

    @Benchmark
    @BenchmarkMode({Mode.SampleTime, Mode.AverageTime})
    public void valhallaSorting(Blackhole bh) {
        com.dariobalinzo.demo.valhalla.Product[] copy =
                Arrays.copyOf(valhallaProducts, arraySize);
        Arrays.sort(copy, Comparator.comparingDouble(
                com.dariobalinzo.demo.valhalla.Product::getPrice));
        bh.consume(copy);
    }

    // ============= FIELD ACCESS BENCHMARKS =============

    @Benchmark
    @BenchmarkMode({Mode.SampleTime, Mode.AverageTime})
    public double standardFieldAccess(Blackhole bh) {
        double sum = 0;
        for (int i = 0; i < arraySize; i++) {
            sum += standardProducts[i].getPrice();
            sum += standardProducts[i].getStockQuantity();
            sum += standardProducts[i].getWeightKg();
        }
        bh.consume(sum);
        return sum;
    }

    @Benchmark
    @BenchmarkMode({Mode.SampleTime, Mode.AverageTime})
    public double valhallaFieldAccess(Blackhole bh) {
        double sum = 0;
        for (int i = 0; i < arraySize; i++) {
            sum += valhallaProducts[i].getPrice();
            sum += valhallaProducts[i].getStock_quantity();
            sum += valhallaProducts[i].getWeight_kg();
        }
        bh.consume(sum);
        return sum;
    }

    // ============= COMBINED OPERATIONS =============

    @Benchmark
    @BenchmarkMode({Mode.SampleTime, Mode.AverageTime})
    public void standardParseAndSort(Blackhole bh) throws Exception {
        Product[] products = new Product[arraySize];
        for (int i = 0; i < arraySize; i++) {
            products[i] = Product.parseFrom(productBytes);
        }
        Arrays.sort(products, Comparator.comparingDouble(Product::getPrice));
        bh.consume(products);
    }

    @Benchmark
    @BenchmarkMode({Mode.SampleTime, Mode.AverageTime})
    public void valhallaParseAndSort(Blackhole bh) throws Exception {
        com.dariobalinzo.demo.valhalla.Product[] products =
                new com.dariobalinzo.demo.valhalla.Product[arraySize];
        for (int i = 0; i < arraySize; i++) {
            products[i] = com.dariobalinzo.demo.valhalla.Product.parseFrom(productBytes);
        }
        Arrays.sort(products, Comparator.comparingDouble(
                com.dariobalinzo.demo.valhalla.Product::getPrice));
        bh.consume(products);
    }

    // ============= MEMORY FOOTPRINT =============

    @Benchmark
    @BenchmarkMode({Mode.SampleTime, Mode.AverageTime})
    public void standardArrayAllocation(Blackhole bh) throws Exception {
        Product[] products = new Product[arraySize];
        for (int i = 0; i < arraySize; i++) {
            products[i] = Product.parseFrom(productBytes);
        }
        bh.consume(products);
    }

    @Benchmark
    @BenchmarkMode({Mode.SampleTime, Mode.AverageTime})
    public void valhallaArrayAllocation(Blackhole bh) throws Exception {
        com.dariobalinzo.demo.valhalla.Product[] products =
                new com.dariobalinzo.demo.valhalla.Product[arraySize];
        for (int i = 0; i < arraySize; i++) {
            products[i] = com.dariobalinzo.demo.valhalla.Product.parseFrom(productBytes);
        }
        bh.consume(products);
    }

    private static Product generateSampleProduct() {
        Product.Builder builder = Product.newBuilder();
        builder.setId(1234567);
        builder.setPrice(99.95);
        builder.setStockQuantity(42);
        builder.setSku(987654321L);
        builder.setWeightKg(1.25f);
        builder.setLengthCm(10.0f);
        builder.setWidthCm(5.0f);
        builder.setHeightCm(2.5f);
        builder.setVolumeLiters(0.125);
        builder.setReviewCount(10);
        builder.setAverageRating(4.3f);
        builder.setSalesCount(1_234L);
        builder.setWarrantyMonths(12);
        builder.setIsReturnable(true);
        builder.setIsDiscontinued(false);
        builder.setIsFeatured(true);
        return builder.build();
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(ProductBenchmarkWithPercentiles.class.getSimpleName())
                .resultFormat(ResultFormatType.JSON)
                .result("benchmark-results-percentiles.json")
                .jvmArgs("-Xmx10g", "--enable-preview")
                .build();

        new Runner(opt).run();
    }
}