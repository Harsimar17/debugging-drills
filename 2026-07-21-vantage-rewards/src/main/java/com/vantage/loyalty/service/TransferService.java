package com.vantage.loyalty.service;

import com.vantage.loyalty.domain.Member;
import com.vantage.loyalty.domain.PointsLedgerEntry;
import com.vantage.loyalty.domain.enums.LedgerEntryType;
import com.vantage.loyalty.dto.TransferPointsRequest;
import com.vantage.loyalty.repository.MemberRepository;
import com.vantage.loyalty.repository.PointsLedgerEntryRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Peer-to-peer points transfer between two members.
 *
 * <p>This capability is specified but not yet implemented — see the "Feature to
 * build" section of the README for the acceptance criteria.</p>
 */
@Service
public class TransferService {
	
	@Autowired
	MemberRepository memberRepo;
	
	@Autowired
	PointsLedgerEntryRepository pointsLedgerEntryRepo;

	@Transactional
    public void transfer(Long fromMemberId, TransferPointsRequest request) {
    	
		Member fromMember = memberRepo.findById(fromMemberId)
		        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "From member not found"));

		Member toMember = memberRepo.findByMemberNumber(request.getToMemberNumber())
		        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "To member not found"));

    	List<PointsLedgerEntry> fromMemberAllLedger = pointsLedgerEntryRepo.findByMemberId(fromMember.getId());

    	Long fromMemBal = 0L;

    	for(PointsLedgerEntry entry : fromMemberAllLedger) 
		{
    		fromMemBal += entry.getPoints();
		}

    	if (fromMemBal < request.getPoints())
    	{
    		throw new ResponseStatusException(
    				HttpStatus.CONFLICT,
    				"Insufficient points balance");
    	}

		long reqPoints = request.getPoints();

		if (fromMemBal >= reqPoints) 
		{
    			
			LocalDateTime inheritedExpiry = fromMemberAllLedger.stream()
			        .filter(e -> !e.isExpired() && e.getPoints() > 0 && e.getExpiresAt() != null)
			        .map(PointsLedgerEntry::getExpiresAt)
			        .min(Comparator.naturalOrder())
			        .orElse(null);

			PointsLedgerEntry toMemberLedger = new PointsLedgerEntry(toMember.getId(), LedgerEntryType.ADJUSTMENT, request.getPoints(), "Transfered from " + fromMember.getMemberNumber(), fromMember.getId().toString(), LocalDateTime.now(), inheritedExpiry);

			pointsLedgerEntryRepo.saveAndFlush(toMemberLedger);
			
			PointsLedgerEntry fromMemberLedger = new PointsLedgerEntry(fromMember.getId(), LedgerEntryType.ADJUSTMENT, -request.getPoints(), "Transfered to " + request.getToMemberNumber(), toMember.getId().toString(), LocalDateTime.now(), null);

			pointsLedgerEntryRepo.saveAndFlush(fromMemberLedger);
    	}
    }
}
