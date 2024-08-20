package Lesson8.Tests;

import Lesson8.Pojo.BookingCreation;
import Lesson8.Pojo.BookingData;
import Lesson8.Pojo.Bookings;
import Lesson8.Specs.Specifications;
import Lesson8.Pojo.SuccessReg;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;

public class ApiDocTest {
    private String url = "https://restful-booker.herokuapp.com/";
    private static final Logger logger = LogManager.getLogger(ApiDocTest.class);

    @Test
    @Tag("Test1")
    public void gettokenId() {
        Specifications.installSpecification(Specifications.requestSpec(url), Specifications.responseOK200());
        Map<String, String> user = new HashMap<>();
        user.put("username", "admin");
        user.put("password", "password123");
        SuccessReg successReg = given()
                .body(user)
                .when()
                .post("auth")
                .then().log().all()
                .extract().as(SuccessReg.class);
//        System.out.println(successReg.getToken());
        logger.info("Expected token id is received");
    }

    //  5.1. Get a list of all books and verify that the request was completed correctly
    @Test
    @Tag("Test2")
    public void getAllBookings() {
        Specifications.installSpecification(Specifications.requestSpec(url), Specifications.responseOK200());
        List<Bookings> bookings = given()
                .when()
                .get("booking")
                .then()
                .extract().body().jsonPath().getList("", Bookings.class);
        List<Integer> ids = bookings.stream().map(x -> x.getBookingid()).collect(Collectors.toList());
        Assertions.assertNotEquals(0, ids.size());
        logger.info("The number of ids equals " + ids.size());

//  5.2. From the resulting list, get a book by id (take a random id)
        Random random = new Random();
        int randomIndex = random.nextInt(ids.size());
        int randomValue = ids.get(randomIndex);
        BookingData bookingData = given()
                .when()
                .get("booking/" + randomValue)
                .then()
//                .then().log().all()
                .extract().as(BookingData.class);
        Assertions.assertNotEquals(null, bookingData.getFirstname());
        logger.info("The full name is " + bookingData.getFirstname() + " " + bookingData.getLastname());

    }

//  5.3. Create booking with valid data
    @Test
    @Tag("Test3")
    public void createBooking() {
        Specifications.installSpecification(Specifications.requestSpec(url), Specifications.responseOK200());
        File jsonFile = new File("src/test/resources/artifacts/Booking.json");
        String jsonData = null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            // Читаем JSON файл и конвертируем его в объект User
            jsonData = objectMapper.writeValueAsString(objectMapper.readTree(jsonFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
//        System.out.println(jsonData);
        BookingCreation bookingCreation = given()
                .body(jsonData)
                .when()
                .post("booking")
                .then()
                .extract().as(BookingCreation.class);
        Assertions.assertNotEquals("", bookingCreation.bookingid);
        Assertions.assertEquals("Alla", bookingCreation.booking.getFirstname());
        logger.info("The booking id is " + bookingCreation.bookingid + ". The person full name is " + bookingCreation.booking.getFirstname() + " " + bookingCreation.booking.getLastname());
    }

//  5.4. Create booking with invalid data
    @Test
    @Tag("Test4")
    public void postInvalidBooking(){}

//  5.5. Create booking with firstname =FirsrNameBook1
    @Test
    @Tag("Test5")
    public void createPrivateBooking(){}

//  5.6. Update booking using valid data
    @Test
    @Tag("Test6")
    public void getBooking(){
        Specifications.installSpecification(Specifications.requestSpec(url), Specifications.responseOK200());
        List<Bookings> bookings = given()
                .when()
                .get("booking")
                .then()
                .extract().body().jsonPath().getList("", Bookings.class);
        List<Integer> ids = bookings.stream().map(x -> x.getBookingid()).collect(Collectors.toList());
        BookingData bookingData = given()
                .when()
                .get("booking/" + ids.get(0))
                .then()
//                .then().log().all()
                .extract().as(BookingData.class);
//        Assertions.assertNotEquals(null, bookingData.getFirstname());
        int bookingId = ids.get(0);
        logger.info("The booking id to be used is " + bookingId + " with full name " + bookingData.getFirstname() + " " + bookingData.getLastname());

            File jsonFile = new File("src/test/resources/artifacts/Booking.json");
            String jsonData = null;
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                // Читаем JSON файл и конвертируем его в объект User
                jsonData = objectMapper.writeValueAsString(objectMapper.readTree(jsonFile));
            } catch (IOException e) {
                e.printStackTrace();
            }
        logger.info("ожидаемый JSON на замену " + jsonData);

        Map<String, String> user = new HashMap<>();
        user.put("username", "admin");
        user.put("password", "password123");
        SuccessReg successReg = given()
                .body(user)
                .when()
                .post("auth")
                .then().log().all()
                .extract().as(SuccessReg.class);
        String token = successReg.getToken();
        logger.info("Expected token id " + token + " has been received");

        BookingData updatedBooking = given()
                    .body(jsonData)
                    .header("Cookie", "token=" + token)
                    .when()
                    .put("booking/" + bookingId)
                    .then()
                    .extract().as(BookingData.class);
        Assertions.assertEquals("Alla Pugacheva", updatedBooking.getFirstname() + " " + updatedBooking.getLastname());
        logger.info("The updated booking contains reservatoipn for " + updatedBooking.getFirstname() + " " + updatedBooking.getLastname());
    }
}