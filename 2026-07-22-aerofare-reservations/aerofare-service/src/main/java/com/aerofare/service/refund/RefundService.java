package com.aerofare.service.refund;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.aerofare.common.enums.BookingStatus;
import com.aerofare.common.enums.CabinClass;
import com.aerofare.common.enums.PassengerType;
import com.aerofare.common.enums.PaymentMethod;
import com.aerofare.common.enums.PaymentStatus;
import com.aerofare.common.exception.RefundNotAllowedException;
import com.aerofare.common.exception.ResourceNotFoundException;
import com.aerofare.common.util.MoneyUtil;
import com.aerofare.domain.Booking;
import com.aerofare.domain.FareRule;
import com.aerofare.domain.Passenger;
import com.aerofare.domain.Payment;
import com.aerofare.domain.SeatInventory;
import com.aerofare.repository.BookingRepository;
import com.aerofare.repository.FareRuleRepository;
import com.aerofare.repository.PaymentRepository;
import com.aerofare.repository.SeatInventoryRepository;

/**
 * Cancels a booking and refunds the customer, net of the applicable
 * cancellation fee.
 *
 * <p>Specified but not yet implemented — see the "Feature to build" section of
 * the README for the acceptance criteria.</p>
 */
@Service
public class RefundService {
	
	@Autowired
	BookingRepository bookingRepo;
	
	@Autowired
	FareRuleRepository fareRuleRepo;
	
	@Autowired
	PaymentRepository paymentRepo;
	
	@Autowired
	SeatInventoryRepository seatInventoryRepository;

    public RefundResult refund(String recordLocator) throws Exception {
    	
    	Booking booking = bookingRepo.findByRecordLocator(recordLocator)
    	        .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + recordLocator));   // 404

		if 
		 (
			booking.getStatus() != BookingStatus.CONFIRMED && 
			booking.getStatus() != BookingStatus.TICKETED &&
			booking.getStatus() != BookingStatus.REFUNDED
			) {
			   throw new RefundNotAllowedException(
    	            "Booking " + recordLocator + " is " + booking.getStatus() + " and cannot be refunded");  // 409
    	}

    	FareRule ruleForBooking = fareRuleRepo
    	        .findByCabinClassAndPassengerType(booking.getCabinClass(), PassengerType.ADULT)
    	        .orElseThrow(() -> new ResourceNotFoundException(
    	                "No fare rule for cabin " + booking.getCabinClass()));                                // 404

    	if (!ruleForBooking.isRefundable()) {
    	    throw new RefundNotAllowedException("Fare is non-refundable for booking " + recordLocator);       // 409
    	}

    	SeatInventory bookedSeats = seatInventoryRepository
    	        .findByFlightIdAndCabinClass(booking.getFlightId(), booking.getCabinClass())
    	        .orElseThrow(() -> new ResourceNotFoundException(
    	                "No seat inventory for flight " + booking.getFlightId())); 
    	
    	bookedSeats.release(booking.getPassengers().size());
		
		seatInventoryRepository.saveAndFlush(bookedSeats);
		
		BigDecimal totalAmt = booking.getTotalAmount();

		BigDecimal cancellationFeePercent = ruleForBooking.getCancellationFeePercent();

		BigDecimal moneyToRefund = MoneyUtil
				.normalize(booking.getTotalAmount().subtract(MoneyUtil.percentage(totalAmt, cancellationFeePercent)));

		Payment refundPayment = new Payment(booking.getId(), moneyToRefund, PaymentMethod.VOUCHER,
				PaymentStatus.REFUNDED, "Refund");

		paymentRepo.saveAndFlush(refundPayment);
		
		RefundResult retVal = new RefundResult();
		
		retVal.setRecordLocator(recordLocator);
		
		retVal.setCancellationFee(cancellationFeePercent);
		
		retVal.setOriginalAmount(totalAmt);
		
		retVal.setRefundedAmount(moneyToRefund);
		
		retVal.setStatus("REFUNDED");
		
		return retVal;
    }
}
