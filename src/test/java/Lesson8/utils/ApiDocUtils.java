package Lesson8.utils;

import Lesson8.Pojo.BookingData;
import Lesson8.Pojo.Bookings;
import Lesson8.Pojo.SuccessReg;
import Lesson8.Specs.Specifications;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;

public class ApiDocUtils {
    private static final Logger logger = LogManager.getLogger(ApiDocUtils.class);

    public static String getTokenId(String url) {
        Specifications.installSpecification(Specifications.requestSpec(url), Specifications.responseOK200());
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
        logger.info("Expected token id " + token + " has been received");
        return token;
    }

    public static List<Integer> getAllBookings(String url) {
        Specifications.installSpecification(Specifications.requestSpec(url), Specifications.responseOK200());
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
//                .then().log().all()
                .extract().as(BookingData.class);
        return bookingData;
    }
}
