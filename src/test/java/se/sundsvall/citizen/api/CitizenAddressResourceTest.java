package se.sundsvall.citizen.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.sundsvall.citizen.Application;
import se.sundsvall.citizen.api.model.CitizenAddress;
import se.sundsvall.citizen.api.model.CitizenWithChangedAddress;
import se.sundsvall.citizen.service.CitizenAddressService;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("junit")
class CitizenAddressResourceTest {

    private static final String PATH = "/api/v2/citizen/changedaddress";
    private static final OffsetDateTime CURRENT_TIME = OffsetDateTime.parse("2025-01-29T09:32:35Z");

    @MockBean
    private CitizenAddressService citizenAddressServiceMock;

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void getCitizensWithChangedAddress() {
        // Arrange
        final var personId = UUID.randomUUID();
        final var expectedResponse = List.of(
                CitizenWithChangedAddress.create()
                        .withPersonId(personId)
                        .withPersonNumber("198001011234")
                        .withGivenname("Test")
                        .withLastname("Testsson")
                        .withAddresses(List.of(
                                CitizenAddress.create()
                                        .withAddress("Test Street 1")
                                        .withCity("Test City")
                                        .withPostalCode("12345")
                        ))
        );

        when(citizenAddressServiceMock.getCitizensWithChangedAddress(any()))
                .thenReturn(expectedResponse);

        // Act
        final var response = webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(PATH)
                        .queryParam("changedDateFrom", CURRENT_TIME)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody(List.class)
                .returnResult()
                .getResponseBody();

        // Assert
        assertThat(response).isNotNull();
        verify(citizenAddressServiceMock).getCitizensWithChangedAddress(CURRENT_TIME);
    }

    @Test
    void getCitizensWithChangedAddress_NoContent() {
        // Arrange
        when(citizenAddressServiceMock.getCitizensWithChangedAddress(any()))
                .thenReturn(Collections.emptyList());

        // Act
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(PATH)
                        .queryParam("changedDateFrom", CURRENT_TIME)
                        .build())
                .exchange()
                .expectStatus().isNoContent()
                .expectBody().isEmpty();

        // Assert
        verify(citizenAddressServiceMock).getCitizensWithChangedAddress(CURRENT_TIME);
    }

    @Test
    void getCitizensWithChangedAddress_BadRequest_MissingParameter() {
        // Act
        webTestClient.get()
                .uri(PATH)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void getCitizensWithChangedAddress_BadRequest_InvalidDateFormat() {
        // Act
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(PATH)
                        .queryParam("changedDateFrom", "invalid-date")
                        .build())
                .exchange()
                .expectStatus().isBadRequest();
    }
}