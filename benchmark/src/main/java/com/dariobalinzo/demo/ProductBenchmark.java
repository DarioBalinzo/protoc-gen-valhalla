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

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Thread)
@Warmup(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 3, timeUnit = TimeUnit.SECONDS)
@Fork(value = 3, jvmArgs = {"-Xmx10g", "--enable-preview"})
public class ProductBenchmark {

    @Param({"1000", "10000", "100000"})
    private int arraySize;

    private byte[] productBytes;
    private Product[] standardProducts;
    private com.dariobalinzo.demo.valhalla.Product[] valhallaProducts;

    @Setup(Level.Trial)
    public void setup() throws Exception {
        Product product = generateSampleProduct();
        productBytes = product.toByteArray();

        // Pre-allocate arrays
        standardProducts = new Product[arraySize];
        valhallaProducts = new com.dariobalinzo.demo.valhalla.Product[arraySize];
    }

    @Setup(Level.Iteration)
    public void setupIteration() throws Exception {
        // Parse products for sorting benchmarks
        for (int i = 0; i < arraySize; i++) {
            standardProducts[i] = Product.parseFrom(productBytes);
            valhallaProducts[i] = com.dariobalinzo.demo.valhalla.Product.parseFrom(productBytes);
        }
    }

    @Benchmark
    public void standardProtobufParsing(Blackhole bh) throws Exception {
        for (int i = 0; i < arraySize; i++) {
            Product p = Product.parseFrom(productBytes);
            bh.consume(p);
        }
    }

    @Benchmark
    public void valhallaProtobufParsing(Blackhole bh) throws Exception {
        for (int i = 0; i < arraySize; i++) {
            com.dariobalinzo.demo.valhalla.Product p =
                    com.dariobalinzo.demo.valhalla.Product.parseFrom(productBytes);
            bh.consume(p);
        }
    }

    @Benchmark
    public void standardProtobufParsingAndSorting(Blackhole bh) throws Exception {
        Product[] products = new Product[arraySize];
        for (int i = 0; i < arraySize; i++) {
            products[i] = Product.parseFrom(productBytes);
        }
        Arrays.sort(products, Comparator.comparingDouble(Product::getPrice));
        bh.consume(products);
    }

    @Benchmark
    public void valhallaProtobufParsingAndSorting(Blackhole bh) throws Exception {
        com.dariobalinzo.demo.valhalla.Product[] products =
                new com.dariobalinzo.demo.valhalla.Product[arraySize];
        for (int i = 0; i < arraySize; i++) {
            products[i] = com.dariobalinzo.demo.valhalla.Product.parseFrom(productBytes);
        }
        Arrays.sort(products, Comparator.comparingDouble(
                com.dariobalinzo.demo.valhalla.Product::getPrice));
        bh.consume(products);
    }

    @Benchmark
    public void standardSortingOnly(Blackhole bh) {
        Product[] copy = Arrays.copyOf(standardProducts, arraySize);
        Arrays.sort(copy, Comparator.comparingDouble(Product::getPrice));
        bh.consume(copy);
    }

    @Benchmark
    public void valhallaSortingOnly(Blackhole bh) {
        com.dariobalinzo.demo.valhalla.Product[] copy =
                Arrays.copyOf(valhallaProducts, arraySize);
        Arrays.sort(copy, Comparator.comparingDouble(
                com.dariobalinzo.demo.valhalla.Product::getPrice));
        bh.consume(copy);
    }

    @Benchmark
    public double standardFieldAccess(Blackhole bh) {
        double sum = 0;
        for (int i = 0; i < arraySize; i++) {
            sum += standardProducts[i].getPrice();
        }
        bh.consume(sum);
        return sum;
    }

    @Benchmark
    public double valhallaFieldAccess(Blackhole bh) {
        double sum = 0;
        for (int i = 0; i < arraySize; i++) {
            sum += valhallaProducts[i].getPrice();
        }
        bh.consume(sum);
        return sum;
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
                .include(ProductBenchmark.class.getSimpleName())
                .resultFormat(ResultFormatType.JSON)
                .result("benchmark-results.json")
                .build();

        new Runner(opt).run();
    }
}