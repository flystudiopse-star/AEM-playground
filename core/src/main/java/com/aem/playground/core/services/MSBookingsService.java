package com.aem.playground.core.services;

import java.util.List;

/**
 * MS Bookings API integration service.
 * Handles all communication with Microsoft Bookings API.
 */
public interface MSBookingsService {

    List<BookableService> getServices();
    List<Branch> getBranches();
    List<TimeSlot> getAvailableSlots(String serviceId, String branchId, String date);
    BookingConfirmation bookAppointment(BookingRequest request);
    boolean confirmBooking(String bookingId);
    boolean cancelBooking(String bookingId);

    interface BookableService {
        String getId();
        String getDisplayName();
        String getDescription();
        int getDurationMinutes();
        double getPrice();
    }

    interface Branch {
        String getId();
        String getDisplayName();
        String getAddress();
        String getCity();
    }

    interface TimeSlot {
        String getStartTime();
        String getEndTime();
        String getBranchId();
        boolean isAvailable();
    }

    interface BookingRequest {
        String getServiceId();
        String getBranchId();
        String getSlotStartTime();
        String getCustomerName();
        String getCustomerEmail();
        String getCustomerPhone();
        String getNotes();
    }

    interface BookingConfirmation {
        String getBookingId();
        String getServiceName();
        String getBranchName();
        String getDateTime();
        String getStatus();
    }
}
