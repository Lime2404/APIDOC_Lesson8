package Lesson8.utils;

import Lesson8.Pojo.BookingCreation;
import Lesson8.Pojo.BookingData;
import Lesson8.Pojo.Bookings;
import Lesson8.Pojo.SuccessReg;
import Lesson8.Specs.Specifications;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;

public class ApiDocUtils {
    private static final Logger logger = LogManager.getLogger(ApiDocUtils.class);

    public static String getTokenId(String url) {
        Map<String, String> user = new HashMap<>();
        user.put("username", "admin");
        user.put("password", "password123");
        SuccessReg successReg = given()
                .body(user)
                .when()
                .post("auth")
                .then()
                .extract().as(SuccessReg.class);
        String token = successReg.getToken();
        return token;
    }

    public static List<Integer> getAllBookings(String url) {
        List<Bookings> bookings = given()
                .when()
                .get("booking")
                .then()
                .extract().body().jsonPath().getList("", Bookings.class);
        List<Integer> ids = bookings.stream().map(x -> x.getBookingid()).collect(Collectors.toList());
        return ids;
    }

    public static BookingData getBookingInfo(int value) {
        BookingData bookingData = given()
                .when()
                .get("booking/" + value)
                .then()
                .extract().as(BookingData.class);
        return bookingData;
    }

    public static BookingCreation createBooking(Object object){
        BookingCreation bookingCreation = given()
                .body(object)
                .when()
                .post("booking")
                .then().log().all()
                .extract().as(BookingCreation.class);
        return bookingCreation;
    }
    public static String getJsonObject(String pathname) {
        File jsonFile = new File(pathname);
        String jsonData = null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            jsonData = objectMapper.writeValueAsString(objectMapper.readTree(jsonFile));
        } catch ( IOException e) {
            e.printStackTrace();
        }
        return jsonData;
    }

    public static void updateWithInvalidData(String jsonData, String token, String resource, int statusCode){
        given()
                .body(jsonData)
                .header("Cookie", "token=" + token)
                .when()
                .put(resource)
                .then()
                .assertThat()
                .statusCode(statusCode);
    }
}
