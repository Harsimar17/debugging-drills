package com.acme.orderflow.app;

import com.acme.orderflow.domain.Order;
import com.acme.orderflow.pricing.PricingEngine;
import com.acme.orderflow.service.OrderFactory;
import com.acme.orderflow.service.OrderProcessor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Reproduction harness for BUG-4471.
 *
 * <p>Simulates the nightly revenue-report reconciliation job: it prices the
 * same deterministic batch of weekday, STANDARD-tier, flat-100.00 orders
 * several times and checks the total against the arithmetic expectation of
 * {@code N * 100.00}. On a correct system every round prints PASS.
 */
public final class Main {

    private static final int DEFAULT_ORDER_COUNT = 5_000;
    private static final int DEFAULT_WORKER_THREADS = 8;
    private static final int DEFAULT_ROUNDS = 10;

    public static void main(String[] args) {
        int orderCount = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_ORDER_COUNT;
        int workerThreads = args.length > 1 ? Integer.parseInt(args[1]) : DEFAULT_WORKER_THREADS;
        int rounds = args.length > 2 ? Integer.parseInt(args[2]) : DEFAULT_ROUNDS;

        System.out.println("Config: orders=" + orderCount
                + " workers=" + workerThreads + " rounds=" + rounds
                + "   (override: exec.args=\"<orders> <workers> <rounds>\")");
        run(orderCount, workerThreads, rounds);
    }

    private static void run(int ORDER_COUNT, int WORKER_THREADS, int ROUNDS) {
        PricingEngine engine = new PricingEngine();
        OrderProcessor processor = new OrderProcessor(engine, WORKER_THREADS);

        BigDecimal expected = new BigDecimal("100.00")
                .multiply(BigDecimal.valueOf(ORDER_COUNT))
                .setScale(2, RoundingMode.HALF_UP);
        // Note: expected revenue for this batch is a clean N * 100.00.

        System.out.println("Reconciling " + ORDER_COUNT + " orders across "
                + WORKER_THREADS + " workers, " + ROUNDS + " rounds.");
        System.out.println("Expected revenue every round: " + expected);
        System.out.println("------------------------------------------------------------");

        int failures = 0;
        for (int round = 1; round <= ROUNDS; round++) {
            List<Order> orders = OrderFactory.weekdayBatch(ORDER_COUNT);
            try {
                BigDecimal actual = processor.totalRevenue(orders);
                boolean ok = actual.compareTo(expected) == 0;
                if (!ok) {
                    failures++;
                }
                System.out.printf("Round %2d: actual=%s  ->  %s%n",
                        round, actual, ok ? "PASS" : "FAIL  <-- revenue mismatch");
            } catch (Exception e) {
                failures++;
                System.out.printf("Round %2d: THREW %s: %s%n",
                        round, e.getClass().getName(),
                        rootCause(e));
            }
        }

        System.out.println("------------------------------------------------------------");
        System.out.println(failures == 0
                ? "All rounds reconciled cleanly."
                : failures + " of " + ROUNDS + " rounds failed to reconcile.");
    }

    private static String rootCause(Throwable t) {
        Throwable cur = t;
        while (cur.getCause() != null) {
            cur = cur.getCause();
        }
        return cur.getClass().getName() + ": " + cur.getMessage();
    }
}
