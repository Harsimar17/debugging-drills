package com.acme.billing.domain;

import java.time.LocalDate;

public enum BillingCycle {
    MONTHLY {
        @Override
        public LocalDate advance(LocalDate from) {
            return from.plusMonths(1);
        }
    },
    QUARTERLY {
        @Override
        public LocalDate advance(LocalDate from) {
            return from.plusMonths(3);
        }
    },
    ANNUAL {
        @Override
        public LocalDate advance(LocalDate from) {
            return from.plusYears(1);
        }
    };

    public abstract LocalDate advance(LocalDate from);
}
