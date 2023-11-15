package com.twa.reservations.service;

import com.twa.reservations.dto.ItineraryDTO;
import com.twa.reservations.dto.ReservationDTO;
import com.twa.reservations.dto.SegmentDTO;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ReservationServiceIntegrationTest {

    @Autowired
    private ReservationService reservationService;

    private static WireMockServer wireMockServer;

    @BeforeAll
    public static void setup() {

        // Configure WireMock to use the mappings directory
        // This assumes your mappings are in the classpath under "wiremock" directory
        WireMockConfiguration config = WireMockConfiguration.options()
                .usingFilesUnderClasspath("src/test/resources/wiremock");
        config.port(8080);

        // Create a new WireMockServer instance
        wireMockServer = new WireMockServer(config);

        // Start the server
        wireMockServer.start();
    }

    @AfterAll
    public static void teardown() {
        wireMockServer.stop();
    }

    @Test
    void should_save_reservation() {
        ReservationDTO reservation = new ReservationDTO();
        ItineraryDTO itinerary = new ItineraryDTO();

        SegmentDTO segment = new SegmentDTO();
        segment.setOrigin("BUE");
        segment.setDestination("MAD");
        List<SegmentDTO> segments = List.of(segment);
        itinerary.setSegment(segments);
        reservation.setItinerary(itinerary);

        ReservationDTO savedReservation = reservationService.save(reservation);

        assertAll(() -> assertNotNull(savedReservation), () -> assertNotNull(savedReservation.getId()));

    }

    @Test
    void should_fail_to_save_reservation_not_exist_city() {
        ReservationDTO reservation = new ReservationDTO();
        ItineraryDTO itinerary = new ItineraryDTO();

        SegmentDTO segment = new SegmentDTO();
        segment.setOrigin("BUE");
        segment.setDestination("345");
        List<SegmentDTO> segments = List.of(segment);
        itinerary.setSegment(segments);
        reservation.setItinerary(itinerary);

        WebClientResponseException exception = assertThrows(WebClientResponseException.class, () -> {
            reservationService.save(reservation);
        });

        assertAll(() -> assertNotNull(exception), () -> assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode()));

    }

    @Test
    void should_fail_to_save_reservation_latency_problems() {
        ReservationDTO reservation = new ReservationDTO();
        ItineraryDTO itinerary = new ItineraryDTO();

        SegmentDTO segment = new SegmentDTO();
        segment.setOrigin("BUE");
        segment.setDestination("PAR");
        List<SegmentDTO> segments = List.of(segment);
        itinerary.setSegment(segments);
        reservation.setItinerary(itinerary);

        WebClientRequestException exception = assertThrows(WebClientRequestException.class, () -> {
            reservationService.save(reservation);
        });

        assertNotNull(exception);

    }
}