package Lesson8.Tests;

import Lesson8.Pojo.BookingCreation;
import Lesson8.Pojo.BookingData;
import Lesson8.Specs.Specifications;
import Lesson8.utils.ApiDocUtils;
import Lesson8.utils.BookingCreator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static io.restassured.RestAssured.given;

public class ApiDocTest {
    private final String url = "https://restful-booker.herokuapp.com/";
    private static final Logger logger = LogManager.getLogger(ApiDocTest.class);

    @Test
    @Tag("Test1")
    public void getApiDocTokenId() {
        Specifications.installSpecification(Specifications.requestSpec(url), Specifications.responseOK200());
        logger.info("The test1 has passed successfully, token " + ApiDocUtils.getTokenId(url) + " has been received");
    }

    //  5.1. Get a list of all books and verify that the request was completed correctly
    @Test
    @Tag("Test2")
    public void getAllApiDocBookings() {
        Specifications.installSpecification(Specifications.requestSpec(url), Specifications.responseOK200());
        int bookingListSize = ApiDocUtils.getAllBookings(url).size();
        Assertions.assertNotEquals(0, bookingListSize);
        logger.info("All bookings have been retreived successfully, the quantity of the bookings equals " + bookingListSize);

//  5.2. From the resulting list, get a book by id (take a random id)
        Random random = new Random();
        int randomIndex = random.nextInt(bookingListSize);
        int randomValue = ApiDocUtils.getAllBookings(url).get(randomIndex);
        logger.info("The random booking id is " + randomValue);
        String visitorLastName = ApiDocUtils.getBookingInfo(randomValue).lastname;
        Assertions.assertNotEquals(null, visitorLastName);
        logger.info("The test2 has passed successfully. The visitor's lastname is " + visitorLastName);
    }

    //  5.3. Create booking with valid data
    @Test
    @Tag("Test3")
    public void createBooking() {
        Specifications.installSpecification(Specifications.requestSpec(url), Specifications.responseOK200());
        String jsonData = ApiDocUtils.getJsonObject("src/test/resources/artifacts/Booking.json");
        BookingCreation booking = ApiDocUtils.createBooking(jsonData);
        Assertions.assertEquals("Alla", booking.booking.getFirstname());
        logger.info("The booking id is " + booking.bookingid + ". The person full name is " + booking.booking.getFirstname() + " " + booking.booking.getLastname());
    }

    //  5.4. Create booking with invalid data
    @Test
    @Tag("Test4")
    public void postInvalidBooking() {
        Specifications.installSpecification(Specifications.requestSpec(url), Specifications.responseUnique(200));
        String jsonData = ApiDocUtils.getJsonObject("src/test/resources/artifacts/Booking.json");
        int bookingId = ApiDocUtils.getAllBookings(url).get(0);
        Specifications.installSpecificationResponse(Specifications.responseUnique(403));
        ApiDocUtils.updateWithInvalidData(jsonData, "12345", "booking/" + bookingId, 403);
        logger.info("The expected received status code for incorrect token is 403 - Forbidden");

        Specifications.installSpecificationResponse(Specifications.responseUnique(200));
        String token = ApiDocUtils.getTokenId(url);
        Specifications.installSpecificationResponse(Specifications.responseUnique(405));
        ApiDocUtils.updateWithInvalidData(jsonData, token, "booking/" + 707070770, 405);
        logger.info("The expected received status code for incorrect booking id is 405 - Method not allowed");

        Specifications.installSpecificationResponse(Specifications.responseUnique(404));
        ApiDocUtils.updateWithInvalidData(jsonData, token, "invalid", 404);
        logger.info("The expected received status code for incorrect booking id is 404 - Not Found");
    }

    //  5.5. Create booking with firstname = FirstNameBook1
    @Test
    @Tag("Test5")
    public void createPrivateBooking() {
        String firstname = "FirstNameBook1";
        Specifications.installSpecification(Specifications.requestSpec(url), Specifications.responseOK200());
        BookingCreator creator = new BookingCreator();
        BookingData booking = creator.createBooking(firstname);
        String visitorFirstName = ApiDocUtils.createBooking(booking).booking.getFirstname();
        Assertions.assertEquals(firstname, visitorFirstName);
        logger.info("The booking contains expected first name " + visitorFirstName);
    }

    //  5.6. Update booking using valid data
    @Test
    @Tag("Test6")
    public void getBooking() {
        Specifications.installSpecification(Specifications.requestSpec(url), Specifications.responseOK200());

        String visitorLastName = ApiDocUtils.getBookingInfo(ApiDocUtils.getAllBookings(url).get(0)).lastname;
        int bookingId = ApiDocUtils.getAllBookings(url).get(0);
        logger.info("The booking id to be used is " + bookingId + " with last name " + visitorLastName);

       String jsonData= ApiDocUtils.provideJsonData("src/test/resources/artifacts/Booking.json");
       logger.info("ожидаемый JSON на замену " + jsonData);

        String token = ApiDocUtils.getTokenId(url);
        logger.info("Следующий шаг - подставить токен " + token + " для внесения изменений в бронь");

        BookingData updatedBooking = given()
                .body(jsonData)
                .header("Cookie", "token=" + token)
                .when()
                .put("booking/" + bookingId)
                .then()
                .extract().as(BookingData.class);

        Assertions.assertNotEquals(null, visitorLastName);
        Assertions.assertEquals("Alla Pugacheva", updatedBooking.getFirstname() + " " + updatedBooking.getLastname());
        logger.info("The updated booking with id: " + bookingId + " contains reservatoipn for " + updatedBooking.getLastname());
    }
}