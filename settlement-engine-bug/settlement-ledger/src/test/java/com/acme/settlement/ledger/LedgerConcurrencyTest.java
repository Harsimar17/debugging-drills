package com.acme.settlement.ledger;

import com.acme.settlement.domain.AccountId;
import com.acme.settlement.domain.Currency;
import com.acme.settlement.domain.Money;
import com.acme.settlement.domain.SettlementInstruction;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The core invariant of a settlement engine: money is neither created nor
 * destroyed. Every posting is a debit and an equal credit, so the sum of all
 * balances must always be exactly zero -- no matter how many settlement worker
 * threads run in parallel.
 *
 * Under concurrent load this invariant is violated. Your job: find out why.
 */
class LedgerConcurrencyTest {

    private static final int ROUNDS = 200;
    private static final int THREADS = 8;
    private static final int ACCOUNTS = 256;
    private static final int OPS_PER_THREAD = 2_000;

    @Test
    void moneyIsConservedUnderConcurrentSettlement() throws Exception {
        for (int round = 0; round < ROUNDS; round++) {
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
                            AccountId from = AccountId.of("ACC-" + fromIdx);
                            AccountId to = AccountId.of("ACC-" + toIdx);
                            ledger.post(new SettlementInstruction(
                                    from, to, Money.ofMinor(1, Currency.USD)));
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        done.countDown();
                    }
                });
            }

            start.countDown();               // release all threads at once
            done.await(30, TimeUnit.SECONDS);
            pool.shutdownNow();

            long total = repo.all().stream()
                    .mapToLong(acc -> acc.balance().minor())
                    .sum();

            assertEquals(0L, total,
                    "money must be conserved, but round " + round
                            + " ended with a net imbalance of " + total + " minor units");
        }
    }
}
