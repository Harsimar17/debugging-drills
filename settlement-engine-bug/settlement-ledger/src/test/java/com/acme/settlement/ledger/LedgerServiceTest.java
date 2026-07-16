package com.acme.settlement.ledger;

import com.acme.settlement.domain.AccountId;
import com.acme.settlement.domain.Currency;
import com.acme.settlement.domain.Money;
import com.acme.settlement.domain.SettlementInstruction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Single-threaded correctness. These pass happily -- the logic is right when
 * nothing is racing. (This is the "works on my machine" part.)
 */
class LedgerServiceTest {

    @Test
    void postingMovesMoneyBetweenAccounts() {
        AccountRepository repo = new AccountRepository();
        LedgerService ledger = new LedgerService(repo);

        AccountId a = AccountId.of("ACC-A");
        AccountId b = AccountId.of("ACC-B");

        ledger.post(new SettlementInstruction(a, b, Money.ofMajor(100, Currency.USD)));
        ledger.post(new SettlementInstruction(b, a, Money.ofMajor(30, Currency.USD)));

        assertEquals(-70_00, repo.get(a).balance().minor());
        assertEquals(70_00, repo.get(b).balance().minor());
    }

    @Test
    void moneyIsConservedSingleThreaded() {
        AccountRepository repo = new AccountRepository();
        LedgerService ledger = new LedgerService(repo);

        for (int i = 0; i < 10_000; i++) {
            AccountId from = AccountId.of("ACC-" + (i % 50));
            AccountId to = AccountId.of("ACC-" + ((i + 7) % 50));
            ledger.post(new SettlementInstruction(from, to, Money.ofMinor(1, Currency.USD)));
        }

        long total = repo.all().stream().mapToLong(acc -> acc.balance().minor()).sum();
        assertEquals(0L, total, "single-threaded ledger must conserve money");
    }
}
