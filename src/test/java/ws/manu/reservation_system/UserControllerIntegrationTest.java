package ws.manu.reservation_system;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = ReservationSystemApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class UserControllerIntegrationTest {
    @LocalServerPort
    private int port;

    TestRestTemplate restTemplate = new TestRestTemplate();

    HttpHeaders headers = new HttpHeaders();

    // test that normal reservations flow wit proper input is working
    @Test
    @Sql("/test-data.sql")
    public void testMakeReservations() throws Exception {
        LocalDateTime dateToTest = getNextAvailableAppointmentDate();
        String r = "{\"serviceTier\":\"Tier1\",\"start\":\""+dateToTest+"\"}";
        ResponseEntity<String> response = makeReservation("manu", r);
        String expected = "{\"start\":\""+dateToTest+":00\",\"name\":\"agent 1\"}";
        JSONAssert.assertEquals(expected, response.getBody(), false);
    }

    // test that we are not allowing reservations on a sunday
    @Test
    @Sql("/test-data.sql")
    public void testMakeReservationsOnSunday() throws Exception {
        String r = "{\"serviceTier\":\"Tier3\",\"start\":\"2099-02-08T12:34:38.073\"}";
        ResponseEntity<String> response = makeReservation("manu", r);
        String expected = "{\"message\":\"Validation Failed\",\"details\":\"org.springframework.validation.BeanPropertyBindingResult: 1 errors\\nField error in object 'reservation' on field 'start': rejected value [2099-02-08T12:34:38.073]; codes [CustomDateTime.reservation.start,CustomDateTime.start,CustomDateTime.java.time.LocalDateTime,CustomDateTime]; arguments [org.springframework.context.support.DefaultMessageSourceResolvable: codes [reservation.start,start]; arguments []; default message [start]]; default message [Please check the requested appointment time as our working hours are from monday to saturday from 8am to 5pm]\"}";
        JSONAssert.assertEquals(expected, response.getBody(), false);
    }

    // test that we are not allowing reservations after office hours
    @Test
    @Sql("/test-data.sql")
    public void testMakeReservationsAfterOfficeHours() throws Exception {
        String r = "{\"serviceTier\":\"Tier3\",\"start\":\"2099-02-10T22:34:38.073\"}";
        ResponseEntity<String> response = makeReservation("manu", r);
        String expected = "{\"message\":\"Validation Failed\",\"details\":\"org.springframework.validation.BeanPropertyBindingResult: 1 errors\\nField error in object 'reservation' on field 'start': rejected value [2099-02-10T22:34:38.073]; codes [CustomDateTime.reservation.start,CustomDateTime.start,CustomDateTime.java.time.LocalDateTime,CustomDateTime]; arguments [org.springframework.context.support.DefaultMessageSourceResolvable: codes [reservation.start,start]; arguments []; default message [start]]; default message [Please check the requested appointment time as our working hours are from monday to saturday from 8am to 5pm]\"}";
        JSONAssert.assertEquals(expected, response.getBody(), false);
    }

    // test that we are not allowing reservations to be made for a past date time
    @Test
    @Sql("/test-data.sql")
    public void testMakeReservationsInThePast() throws Exception {
        String r = "{\"serviceTier\":\"Tier3\",\"start\":\"2008-02-01T12:34:38.073\"}";
        ResponseEntity<String> response = makeReservation("manu", r);
        String expected="{\n" +
                "    \"message\": \"Validation Failed\",\n" +
                "    \"details\": \"org.springframework.validation.BeanPropertyBindingResult: 1 errors\\nField error in object 'reservation' on field 'start': rejected value [2008-02-01T12:34:38.073]; codes [FutureOrPresent.reservation.start,FutureOrPresent.start,FutureOrPresent.java.time.LocalDateTime,FutureOrPresent]; arguments [org.springframework.context.support.DefaultMessageSourceResolvable: codes [reservation.start,start]; arguments []; default message [start]]; default message [must be a date in the present or in the future]\"\n" +
                "}";
        JSONAssert.assertEquals(expected, response.getBody(), false);
    }

    // test that reservations can only be made on dates returned by the /reservations get call
    @Test
    @Sql("/test-data.sql")
    public void testMakeReservationsWithInvalidDate() throws Exception {
        // try invalid date of next thursday at 11
        LocalDateTime dateToTest = getNextAvailableAppointmentDate();
        dateToTest = dateToTest.plus(1, ChronoUnit.DAYS).with(TemporalAdjusters.next(DayOfWeek.THURSDAY)).toLocalDate().atTime(11, 0);
        String r = "{\"serviceTier\":\"Tier3\",\"start\":\""+dateToTest+"\"}";
        ResponseEntity<String> response = makeReservation("manu", r);
        String expected="{\n" +
                "    \"message\": \"Invalid appointment time. Please check from /reservations endpoint if this appointment is available or not.\",\n" +
                "    \"details\": \"uri=/v1/users/manu/appointments\"\n" +
                "}";
        JSONAssert.assertEquals(expected, response.getBody(), false);
    }

    // test that reservations beyond 60 days are not allowed
    @Test
    @Sql("/test-data.sql")
    public void testReservationsWithin60Days() throws Exception {
        // create one reservation
        String r = "{\"serviceTier\":\"Tier3\",\"start\":\"2099-02-11T12:34:38.073\"}";
        ResponseEntity<String> response = makeReservation("manu", r);
        String expected="{\"message\":\"Requested appointment can only be within the range of next 60 days.\",\"details\":\"uri=/v1/users/manu/appointments\"}";
        JSONAssert.assertEquals(expected, response.getBody(), false);
    }

    // test that 2 or more reservations within 60 days are not allowed
    @Test
    @Sql("/test-data.sql")
    public void testMakeMultipleReservationsWithin60Days() throws Exception {
        // create one reservation
        LocalDateTime dateToTest = getNextAvailableAppointmentDate();
        String r = "{\"serviceTier\":\"Tier1\",\"start\":\""+dateToTest+"\"}";
        ResponseEntity<String> response = makeReservation("manu", r);
        String expected = "{\"start\":\""+dateToTest+":00\",\"name\":\"agent 1\"}";
        JSONAssert.assertEquals(expected, response.getBody(), false);

        // try creating another reservation
        LocalDateTime nextDay = dateToTest.plus(1, ChronoUnit.DAYS);
        r = "{\"serviceTier\":\"Tier1\",\"start\":\""+nextDay+"\"}";
        response = makeReservation("manu", r);
        expected = "{\"message\":\"User already has an existing appointment. Only one reservation in a period of 60 days is allowed.\",\"details\":\"uri=/v1/users/manu/appointments\"}";
        JSONAssert.assertEquals(expected, response.getBody(), false);
    }

    // test that only returning users can book reservations before 10 am
    @Test
    @Sql("/test-data.sql")
    public void testMakeReservationsBefore10AM() throws Exception {
        // user 2 is a new user so not allowed to book appointment before 10
        LocalDateTime dateToTest = getNextAvailableAppointmentDate();
        // add 4 days to available date
        dateToTest = dateToTest.plus(4, ChronoUnit.DAYS);
        // set dateToTest to next thursday at 9 am
        dateToTest = dateToTest.with(TemporalAdjusters.next(DayOfWeek.THURSDAY)).toLocalDate().atTime(9, 0);
        String r = "{\"serviceTier\":\"Tier1\",\"start\":\""+dateToTest+"\"}";
        ResponseEntity<String> response = makeReservation("manu", r);
        String expected="{\"message\":\"Appointments before 10am are not available for new users.\",\"details\":\"uri=/v1/users/manu/appointments\"}";
        JSONAssert.assertEquals(expected, response.getBody(), false);

        // user 1 is a "returning user" so  allowed to book appointment before 10
        dateToTest = getNextAvailableAppointmentDate();
        r = "{\"serviceTier\":\"Tier1\",\"start\":\""+dateToTest+"\"}";
        response = makeReservation("nanu", r);
        expected = "{\"start\":\""+dateToTest+":00\",\"name\":\"agent 1\"}";
        JSONAssert.assertEquals(expected, response.getBody(), false);
    }


    private String createURLWithPort(String uri) {
        return "http://localhost:" + port + uri;
    }

    private LocalDateTime getNextAvailableAppointmentDate() {
        HttpEntity<String> entity = new HttpEntity<String>(null, headers);
        ResponseEntity<String> response = restTemplate.exchange(
                createURLWithPort("/v1/user/manu/appointments?service_tier=Tier1"),
                HttpMethod.GET, entity, String.class);
        return LocalDateTime.parse(response.getBody().split("\"")[1]).toLocalDate().atTime(8, 0);
    }

    private ResponseEntity<String> makeReservation(String uid, String requestJson) {
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<String>(requestJson, headers);
        ResponseEntity<String> response = restTemplate.exchange(
                createURLWithPort("/v1/users/"+uid+"/appointments"),
                HttpMethod.POST, entity, String.class);
        return response;
    }
}