package com.acme.settlement.app;

import com.acme.settlement.domain.AccountId;
import com.acme.settlement.domain.Currency;
import com.acme.settlement.domain.Money;
import com.acme.settlement.domain.SettlementInstruction;
import com.acme.settlement.fx.FxConverter;
import com.acme.settlement.ledger.AccountRepository;
import com.acme.settlement.ledger.LedgerService;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * Runs a burst of concurrent settlements and prints the net imbalance. In a
 * correct engine the imbalance is always zero. Run it a few times.
 */
public final class SettlementSimulator {

    private static final int THREADS = 8;
    private static final int ACCOUNTS = 256;
    private static final int OPS_PER_THREAD = 20_000;

    public static void main(String[] args) throws Exception {
        // A quick FX sanity print so the whole stack is exercised.
        FxConverter fx = new FxConverter();
        Money oneHundredUsd = Money.ofMajor(100, Currency.USD);
        System.out.println("FX check: " + oneHundredUsd
                + " = " + fx.convert(oneHundredUsd, Currency.EUR)
                + " = " + fx.convert(oneHundredUsd, Currency.JPY));

        AccountRepository repo = new AccountRepository();
        LedgerService ledger = new LedgerService(repo);

        ExecutorService pool = Executors.newFixedThreadPool(THREADS);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(THREADS);

        for (int t = 0; t < THREADS; t++) {
            pool.submit(() -> {
                try {
                    start.await();
                    ThreadLocalRandom rnd = ThreadLocalRandom.current();
                    for (int i = 0; i < OPS_PER_THREAD; i++) {
                        int fromIdx = rnd.nextInt(ACCOUNTS);
                        int toIdx = rnd.nextInt(ACCOUNTS - 1);
                        if (toIdx >= fromIdx) {
                            toIdx++;
                        }
                        ledger.post(new SettlementInstruction(
                                AccountId.of("ACC-" + fromIdx),
                                AccountId.of("ACC-" + toIdx),
                                Money.ofMinor(1, Currency.USD)));
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }

        long t0 = System.nanoTime();
        start.countDown();
        done.await(60, TimeUnit.SECONDS);
        pool.shutdownNow();
        long ms = (System.nanoTime() - t0) / 1_000_000;

        long total = repo.all().stream()
                .mapToLong(acc -> acc.balance().minor())
                .sum();

        System.out.println("-----------------------------------------------");
        System.out.println("Postings requested : " + (long) THREADS * OPS_PER_THREAD);
        System.out.println("Postings recorded  : " + ledger.postedCount());
        System.out.println("Accounts touched   : " + repo.size());
        System.out.println("Elapsed            : " + ms + " ms");
        System.out.println("NET IMBALANCE      : " + total + " minor units  (should be 0)");
        System.out.println(total == 0
                ? "OK - money conserved."
                : "*** MONEY LEAKED - the ledger does not balance ***");
    }

    private SettlementSimulator() {
    }
}
