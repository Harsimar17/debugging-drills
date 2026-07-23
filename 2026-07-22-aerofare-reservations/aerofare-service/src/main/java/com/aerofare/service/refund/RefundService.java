package com.aerofare.service.refund;

import org.springframework.stereotype.Service;

/**
 * Cancels a booking and refunds the customer, net of the applicable
 * cancellation fee.
 *
 * <p>Specified but not yet implemented — see the "Feature to build" section of
 * the README for the acceptance criteria.</p>
 */
@Service
public class RefundService {

    public RefundResult refund(String recordLocator) {
        throw new UnsupportedOperationException(
                "Refunds are not implemented yet. See README 'Feature to build'.");
    }
}
