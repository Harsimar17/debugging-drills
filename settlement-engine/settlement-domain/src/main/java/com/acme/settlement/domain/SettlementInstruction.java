package com.acme.settlement.domain;

/**
 * An instruction to move {@code amount} from one counterparty account to another
 * as a trade settles. Produced by the matching/clearing layer and handed to the
 * ledger for posting.
 */
public record SettlementInstruction(AccountId from, AccountId to, Money amount) {

    public SettlementInstruction {
        if (from.equals(to)) {
            throw new IllegalArgumentException("from and to must differ: " + from);
        }
        if (amount.minor() <= 0) {
            throw new IllegalArgumentException("amount must be positive: " + amount);
        }
    }
}
