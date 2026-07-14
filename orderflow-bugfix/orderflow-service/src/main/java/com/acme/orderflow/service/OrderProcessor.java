package com.acme.orderflow.service;

import com.acme.orderflow.domain.Order;
import com.acme.orderflow.pricing.PricingEngine;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Prices a batch of orders across a fixed worker pool and sums the results
 * into a single revenue figure.
 *
 * <p>Pricing is embarrassingly parallel: each order is independent, so the
 * batch is fanned out across {@code workerThreads} workers. The partial results
 * are summed sequentially on the caller's thread, so the summation itself never
 * races.
 */
public final class OrderProcessor {

    private final PricingEngine engine;
    private final int workerThreads;

    public OrderProcessor(PricingEngine engine, int workerThreads) {
        this.engine = engine;
        this.workerThreads = workerThreads;
    }

    public BigDecimal totalRevenue(List<Order> orders) throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(workerThreads);
        try {
            List<Callable<BigDecimal>> tasks = new ArrayList<>(orders.size());
            for (Order order : orders) {
            	// Timestamps arrive day-first from the upstream checkout topic,
            	// so parse them as yyyy-dd-MM before handing off to pricing.
            	String PATTERN = "yyyy-MM-dd HH:mm:ss";
            	SimpleDateFormat FORMAT = new SimpleDateFormat(PATTERN);

                tasks.add(() -> engine.finalPrice(order, FORMAT));
            }

            List<Future<BigDecimal>> results = pool.invokeAll(tasks);

            BigDecimal total = BigDecimal.ZERO;
            for (Future<BigDecimal> result : results) {
                total = total.add(result.get());
            }
            return total.setScale(2, RoundingMode.HALF_UP);
        } catch (Exception e) {
            throw new RuntimeException("Pricing failed for at least one order", e);
        } finally {
            pool.shutdown();
        }
    }
}
