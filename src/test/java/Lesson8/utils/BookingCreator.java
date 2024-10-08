package Lesson8.utils;

import Lesson8.Pojo.BookingData;
import Lesson8.Pojo.BookingDates;

public class BookingCreator {
    public BookingData createBooking(String name) {
        BookingDates bookingDates = new BookingDates();
        bookingDates.setCheckin("2018-01-01");
        bookingDates.setCheckout("2019-01-01");

        BookingData booking = new BookingData();
        booking.setFirstname(name);
        booking.setLastname("Harlamov");
        booking.setTotalprice(111);
        booking.setDepositpaid(true);
        booking.setBookingdates(bookingDates);
        booking.setAdditionalneeds("Breakfast");

        return booking;
    }
}
