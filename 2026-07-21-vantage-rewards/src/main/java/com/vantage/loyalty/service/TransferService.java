package com.vantage.loyalty.service;

import com.vantage.loyalty.dto.TransferPointsRequest;
import org.springframework.stereotype.Service;

/**
 * Peer-to-peer points transfer between two members.
 *
 * <p>This capability is specified but not yet implemented — see the "Feature to
 * build" section of the README for the acceptance criteria.</p>
 */
@Service
public class TransferService {

    public void transfer(Long fromMemberId, TransferPointsRequest request) {
        throw new UnsupportedOperationException(
                "Points transfer is not implemented yet. See README 'Feature to build'.");
    }
}
