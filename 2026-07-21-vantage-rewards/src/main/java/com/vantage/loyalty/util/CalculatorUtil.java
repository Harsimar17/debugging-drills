package com.vantage.loyalty.util;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.vantage.loyalty.domain.PointsLedgerEntry;
import com.vantage.loyalty.repository.PointsLedgerEntryRepository;

@Component
public class CalculatorUtil {
	
	@Autowired
	 private PointsLedgerEntryRepository ledgerRepository;

	public Long calculateTotalBalance(Long memberId) 
	{
		List<PointsLedgerEntry> fromMemberAllLedger = ledgerRepository.findByMemberId(memberId);

		Long balance = 0L;

		for (PointsLedgerEntry entry : fromMemberAllLedger)
		{
			balance += entry.getPoints();
		}

		return balance;
	}
}
