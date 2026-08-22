package com.routbuddy.bookings.service;

import com.routbuddy.bookings.domain.entity.Booking;
import com.routbuddy.common.domain.enums.BookingStatus;
import com.routbuddy.common.exception.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@Component
public class BookingStateMachine {

    private static final Logger log = LoggerFactory.getLogger(BookingStateMachine.class);

    private static final Map<BookingStatus, Set<BookingStatus>> ALLOWED_TRANSITIONS = Map.of(
            BookingStatus.PENDING, EnumSet.of(BookingStatus.CONFIRMED, BookingStatus.CANCELLED, BookingStatus.EXPIRED),
            BookingStatus.CONFIRMED, EnumSet.of(BookingStatus.BOARDED, BookingStatus.CANCELLED, BookingStatus.EXPIRED),
            BookingStatus.BOARDED, EnumSet.of(BookingStatus.COMPLETED),
            BookingStatus.COMPLETED, EnumSet.noneOf(BookingStatus.class),
            BookingStatus.CANCELLED, EnumSet.noneOf(BookingStatus.class),
            BookingStatus.EXPIRED, EnumSet.noneOf(BookingStatus.class)
    );

    public void validateTransition(Booking booking, BookingStatus targetStatus) {
        BookingStatus currentStatus = booking.getBookingStatus();
        Set<BookingStatus> validTargets = ALLOWED_TRANSITIONS.getOrDefault(currentStatus, EnumSet.noneOf(BookingStatus.class));

        if (!validTargets.contains(targetStatus)) {
            String errorMsg = String.format(
                    "Illegal booking state transition from [%s] to [%s]. Allowed transitions from [%s]: %s",
                    currentStatus, targetStatus, currentStatus, validTargets
            );
            log.warn(errorMsg);
            throw new BadRequestException(errorMsg);
        }
    }

    public void transitionTo(Booking booking, BookingStatus targetStatus) {
        validateTransition(booking, targetStatus);
        BookingStatus previousStatus = booking.getBookingStatus();
        booking.setBookingStatus(targetStatus);

        Instant now = Instant.now();
        if (targetStatus == BookingStatus.BOARDED && booking.getBoardedAt() == null) {
            booking.setBoardedAt(now);
        } else if (targetStatus == BookingStatus.COMPLETED && booking.getCompletedAt() == null) {
            booking.setCompletedAt(now);
        } else if (targetStatus == BookingStatus.CANCELLED && booking.getCancelledAt() == null) {
            booking.setCancelledAt(now);
        }

        log.info("Booking [{}] transitioned from [{}] to [{}]", booking.getBookingCode(), previousStatus, targetStatus);
    }
}
