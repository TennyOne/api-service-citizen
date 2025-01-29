package se.sundsvall.citizen.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.citizen.api.model.CitizenWithChangedAddress;
import se.sundsvall.citizen.integration.db.CitizenAddressRepository;
import se.sundsvall.citizen.integration.db.model.CitizenAddressEntity;
import se.sundsvall.citizen.service.mapper.CitizenAddressMapper;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CitizenAddressServiceTest {

    @Mock
    private CitizenAddressRepository citizenAddressRepositoryMock;

    @InjectMocks
    private CitizenAddressService citizenAddressService;

    @Test
    void getCitizensWithChangedAddress() {
        // Arrange
        final var changedDateFrom = OffsetDateTime.parse("2025-01-29T08:52:05Z");
        final var citizenAddressEntity = new CitizenAddressEntity();
        final var expectedCitizenWithChangedAddress = new CitizenWithChangedAddress();

        when(citizenAddressRepositoryMock.findByCitizen_UpdatedAtGreaterThanEqual(changedDateFrom))
                .thenReturn(List.of(citizenAddressEntity));

        try (MockedStatic<CitizenAddressMapper> mapperMock = Mockito.mockStatic(CitizenAddressMapper.class)) {
            mapperMock.when(() -> CitizenAddressMapper.toCitizenWithChangedAddress(any()))
                    .thenReturn(expectedCitizenWithChangedAddress);

            // Act
            final var result = citizenAddressService.getCitizensWithChangedAddress(changedDateFrom);

            // Assert
            assertThat(result)
                    .isNotNull()
                    .hasSize(1)
                    .containsExactly(expectedCitizenWithChangedAddress);

            verify(citizenAddressRepositoryMock).findByCitizen_UpdatedAtGreaterThanEqual(same(changedDateFrom));
            mapperMock.verify(() -> CitizenAddressMapper.toCitizenWithChangedAddress(same(citizenAddressEntity)));
        }
    }

    @Test
    void getCitizensWithChangedAddress_NoResults() {
        // Arrange
        final var changedDateFrom = OffsetDateTime.parse("2025-01-29T08:52:05Z");

        when(citizenAddressRepositoryMock.findByCitizen_UpdatedAtGreaterThanEqual(changedDateFrom))
                .thenReturn(Collections.emptyList());

        // Act
        final var result = citizenAddressService.getCitizensWithChangedAddress(changedDateFrom);

        // Assert
        assertThat(result)
                .isNotNull()
                .isEmpty();

        verify(citizenAddressRepositoryMock).findByCitizen_UpdatedAtGreaterThanEqual(same(changedDateFrom));
    }
}