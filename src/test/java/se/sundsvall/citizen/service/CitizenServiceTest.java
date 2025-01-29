package se.sundsvall.citizen.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zalando.problem.ThrowableProblem;
import se.sundsvall.citizen.api.model.CitizenExtended;
import se.sundsvall.citizen.integration.db.CitizenRepository;
import se.sundsvall.citizen.integration.db.model.CitizenEntity;
import se.sundsvall.citizen.service.mapper.CitizenMapper;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CitizenServiceTest {

    @Mock
    private CitizenRepository citizenRepositoryMock;

    @InjectMocks
    private CitizenService citizenService;

    @Test
    void getCitizenById() {
        // Arrange
        final var personId = UUID.randomUUID();
        final var citizenEntity = CitizenEntity.create().withPersonId(personId.toString());
        final var expectedCitizen = new CitizenExtended();

        when(citizenRepositoryMock.findById(personId.toString())).thenReturn(Optional.of(citizenEntity));

        try (MockedStatic<CitizenMapper> mapperMock = Mockito.mockStatic(CitizenMapper.class)) {
            mapperMock.when(() -> CitizenMapper.toCitizenExtended(any())).thenReturn(expectedCitizen);

            // Act
            final var result = citizenService.getCitizenById(personId, true);

            // Assert
            assertThat(result).isSameAs(expectedCitizen);
            verify(citizenRepositoryMock).findById(personId.toString());
            mapperMock.verify(() -> CitizenMapper.toCitizenExtended(same(citizenEntity)));
        }
    }

    @Test
    void getCitizenById_NotFound() {
        // Arrange
        final var personId = UUID.randomUUID();
        when(citizenRepositoryMock.findById(personId.toString())).thenReturn(Optional.empty());

        // Act & Assert
        final var exception = assertThrows(ThrowableProblem.class,
                () -> citizenService.getCitizenById(personId, true));

        assertThat(exception.getMessage())
                .contains(String.format("No citizen found with ID: %s", personId));
    }

    @Test
    void getCitizenById_ClassifiedHidden() {
        // Arrange
        final var personId = UUID.randomUUID();
        final var citizenEntity = CitizenEntity.create()
                .withPersonId(personId.toString())
                .withClassified("CLASSIFIED");

        when(citizenRepositoryMock.findById(personId.toString())).thenReturn(Optional.of(citizenEntity));

        // Act
        final var result = citizenService.getCitizenById(personId, false);

        // Assert
        assertThat(result).isNull();
        verify(citizenRepositoryMock).findById(personId.toString());
    }

    @Test
    void createCitizen() {
        // Arrange
        final var classificationCode = "CODE";
        final var personId = UUID.randomUUID();
        final var citizenEntity = CitizenEntity.create()
                .withPersonId(personId.toString())
                .withClassified(classificationCode);

        when(citizenRepositoryMock.save(any())).thenReturn(citizenEntity);

        // Act
        final var result = citizenService.createCitizen(classificationCode);

        // Assert
        assertThat(result).isEqualTo(personId);
        verify(citizenRepositoryMock).save(any());
    }

    @Test
    void createCitizen_InvalidCode() {
        // Act & Assert
        final var exception = assertThrows(ThrowableProblem.class,
                () -> citizenService.createCitizen(null));

        assertThat(exception.getMessage())
                .contains("Invalid classification code");
        verifyNoInteractions(citizenRepositoryMock);
    }

    @Test
    void getPersonIdByPersonalNumber() {
        // Arrange
        final var personalNumber = "198001011234";
        final var personId = UUID.randomUUID();
        final var citizenEntity = CitizenEntity.create().withPersonId(personId.toString());

        when(citizenRepositoryMock.findByPersonalNumber(personalNumber)).thenReturn(Optional.of(citizenEntity));

        // Act
        final var result = citizenService.getPersonIdByPersonalNumber(personalNumber);

        // Assert
        assertThat(result).isEqualTo(personId);
        verify(citizenRepositoryMock).findByPersonalNumber(personalNumber);
    }

    @Test
    void getPersonIdByPersonalNumber_NotFound() {
        // Arrange
        final var personalNumber = "198001011234";
        when(citizenRepositoryMock.findByPersonalNumber(personalNumber)).thenReturn(Optional.empty());

        // Act & Assert
        final var exception = assertThrows(ThrowableProblem.class,
                () -> citizenService.getPersonIdByPersonalNumber(personalNumber));

        assertThat(exception.getMessage())
                .contains(String.format("No citizen found with personal number: %s", personalNumber));
    }

    @Test
    void getCitizensByIds() {
        // Arrange
        final var personId1 = UUID.randomUUID();
        final var personId2 = UUID.randomUUID();
        final var citizenEntity1 = CitizenEntity.create().withPersonId(personId1.toString());
        final var citizenEntity2 = CitizenEntity.create().withPersonId(personId2.toString());
        final var expectedCitizen1 = new CitizenExtended();
        final var expectedCitizen2 = new CitizenExtended();

        when(citizenRepositoryMock.findById(personId1.toString())).thenReturn(Optional.of(citizenEntity1));
        when(citizenRepositoryMock.findById(personId2.toString())).thenReturn(Optional.of(citizenEntity2));

        try (MockedStatic<CitizenMapper> mapperMock = Mockito.mockStatic(CitizenMapper.class)) {
            mapperMock.when(() -> CitizenMapper.toCitizenExtended(citizenEntity1)).thenReturn(expectedCitizen1);
            mapperMock.when(() -> CitizenMapper.toCitizenExtended(citizenEntity2)).thenReturn(expectedCitizen2);

            // Act
            final var result = citizenService.getCitizensByIds(List.of(personId1, personId2), true);

            // Assert
            assertThat(result)
                    .hasSize(2)
                    .containsExactly(expectedCitizen1, expectedCitizen2);
        }
    }

    @Test
    void getPersonIdsInBatch() {
        // Arrange
        final var personalNumber = "198001011234";
        final var personId = UUID.randomUUID();
        final var citizenEntity = CitizenEntity.create().withPersonId(personId.toString());

        when(citizenRepositoryMock.findByPersonalNumber(personalNumber)).thenReturn(Optional.of(citizenEntity));

        // Act
        final var result = citizenService.getPersonIdsInBatch(List.of(personalNumber));

        // Assert
        assertThat(result)
                .hasSize(1)
                .first()
                .satisfies(batch -> {
                    assertThat(batch.getPersonNumber()).isEqualTo(personalNumber);
                    assertThat(batch.getPersonId()).isEqualTo(personId);
                    assertThat(batch.isSuccess()).isTrue();
                    assertThat(batch.getErrorMessage()).isNull();
                });
    }

    @Test
    void getPersonIdsInBatch_NotFound() {
        // Arrange
        final var personalNumber = "198001011234";
        when(citizenRepositoryMock.findByPersonalNumber(personalNumber)).thenReturn(Optional.empty());

        // Act
        final var result = citizenService.getPersonIdsInBatch(List.of(personalNumber));

        // Assert
        assertThat(result)
                .hasSize(1)
                .first()
                .satisfies(batch -> {
                    assertThat(batch.getPersonNumber()).isEqualTo(personalNumber);
                    assertThat(batch.getPersonId()).isNull();
                    assertThat(batch.isSuccess()).isFalse();
                    assertThat(batch.getErrorMessage()).isEqualTo("Citizen not found");
                });
    }
}