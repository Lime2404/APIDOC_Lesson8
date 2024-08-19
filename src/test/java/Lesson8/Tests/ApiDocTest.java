package Lesson8.Tests;

import Lesson8.BookingCreation;
import Lesson8.BookingData;
import Lesson8.Specifications;
import Lesson8.SuccessReg;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import io.restassured.http.ContentType;

import java.io.*;
import java.nio.file.Paths;
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
        Assertions.assertEquals("Jim", bookingCreation.booking.getFirstname());
        logger.info("The booking id is " + bookingCreation.bookingid + ". The person full name is " + bookingCreation.booking.getFirstname() + " " + bookingCreation.booking.getLastname());
    }
}