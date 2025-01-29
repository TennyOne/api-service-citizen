package se.sundsvall.citizen.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.sundsvall.citizen.Application;
import se.sundsvall.citizen.api.model.CitizenAddress;
import se.sundsvall.citizen.api.model.CitizenExtended;
import se.sundsvall.citizen.api.model.PersonGuidBatch;
import se.sundsvall.citizen.service.CitizenService;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("junit")
class CitizenResourceTest {

    private static final String PATH = "/api/v2/citizen";
    private static final OffsetDateTime CURRENT_TIME = OffsetDateTime.parse("2025-01-29T09:32:35Z");

    @MockBean
    private CitizenService citizenServiceMock;

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void getCitizenById() {
        // Arrange
        final var personId = UUID.randomUUID();
        final var citizen = CitizenExtended.create()
                .withPersonId(personId)
                .withGivenname("Test")
                .withLastname("Testsson")
                .withAddresses(List.of(CitizenAddress.create()
                        .withAddress("Test Street 1")));

        when(citizenServiceMock.getCitizenById(personId, false))
                .thenReturn(citizen);

        // Act
        final var response = webTestClient.get()
                .uri(PATH + "/{personId}", personId)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody(CitizenExtended.class)
                .returnResult()
                .getResponseBody();

        // Assert
        assertThat(response).isNotNull().isEqualTo(citizen);
        verify(citizenServiceMock).getCitizenById(personId, false);
    }

    @Test
    void getCitizenById_NoContent() {
        // Arrange
        final var personId = UUID.randomUUID();
        when(citizenServiceMock.getCitizenById(any(), anyBoolean()))
                .thenReturn(null);

        // Act
        webTestClient.get()
                .uri(PATH + "/{personId}", personId)
                .exchange()
                .expectStatus().isNoContent()
                .expectBody().isEmpty();
    }

    @Test
    void createCitizen() {
        // Arrange
        final var personId = UUID.randomUUID();
        final var classificationCode = "CODE123";
        when(citizenServiceMock.createCitizen(classificationCode))
                .thenReturn(personId);

        // Act
        final var response = webTestClient.post()
                .uri(PATH + "/guid")
                .contentType(APPLICATION_JSON)
                .bodyValue(classificationCode)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectHeader().location("/api/v2/citizen/" + personId)
                .expectBody(UUID.class)
                .returnResult()
                .getResponseBody();

        // Assert
        assertThat(response).isEqualTo(personId);
        verify(citizenServiceMock).createCitizen(classificationCode);
    }

    @Test
    void getPersonIdByPersonalNumber() {
        // Arrange
        final var personId = UUID.randomUUID();
        final var personalNumber = "198001011234";
        when(citizenServiceMock.getPersonIdByPersonalNumber(personalNumber))
                .thenReturn(personId);

        // Act
        final var response = webTestClient.get()
                .uri(PATH + "/{personalNumber}/guid", personalNumber)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody(UUID.class)
                .returnResult()
                .getResponseBody();

        // Assert
        assertThat(response).isEqualTo(personId);
        verify(citizenServiceMock).getPersonIdByPersonalNumber(personalNumber);
    }

    @Test
    void getCitizensBatch() {
        // Arrange
        final var personId = UUID.randomUUID();
        final var citizens = List.of(
                CitizenExtended.create()
                        .withPersonId(personId)
                        .withGivenname("Test")
                        .withLastname("Testsson"));

        when(citizenServiceMock.getCitizensByIds(any(), eq(false)))
                .thenReturn(citizens);

        // Act
        final var response = webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path(PATH + "/batch")
                        .queryParam("showClassified", false)
                        .build())
                .contentType(APPLICATION_JSON)
                .bodyValue(List.of(personId))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody(List.class)
                .returnResult()
                .getResponseBody();

        // Assert
        assertThat(response).isNotNull();
        verify(citizenServiceMock).getCitizensByIds(List.of(personId), false);
    }

    @Test
    void getPersonIdsBatch() {
        // Arrange
        final var personalNumber = "198001011234";
        final var personId = UUID.randomUUID();
        final var batchResponse = List.of(
                PersonGuidBatch.create()
                        .withPersonNumber(personalNumber)
                        .withPersonId(personId)
                        .withSuccess(true));

        when(citizenServiceMock.getPersonIdsInBatch(any()))
                .thenReturn(batchResponse);

        // Act
        final var response = webTestClient.post()
                .uri(PATH + "/guid/batch")
                .contentType(APPLICATION_JSON)
                .bodyValue(List.of(personalNumber))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody(List.class)
                .returnResult()
                .getResponseBody();

        // Assert
        assertThat(response).isNotNull();
        verify(citizenServiceMock).getPersonIdsInBatch(List.of(personalNumber));
    }

    @Test
    void getCitizenById_BadRequest_InvalidUUID() {
        // Act
        webTestClient.get()
                .uri(PATH + "/{personId}", "invalid-uuid")
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void createCitizen_BadRequest_NullBody() {
        // Act
        webTestClient.post()
                .uri(PATH + "/guid")
                .contentType(APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest();
    }
}