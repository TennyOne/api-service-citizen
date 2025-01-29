package se.sundsvall.citizen.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;
import se.sundsvall.citizen.api.model.CitizenExtended;
import se.sundsvall.citizen.api.model.PersonGuidBatch;
import se.sundsvall.citizen.integration.db.CitizenRepository;
import se.sundsvall.citizen.integration.db.model.CitizenEntity;
import se.sundsvall.citizen.service.mapper.CitizenMapper;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static java.lang.String.format;
import static org.zalando.problem.Status.NOT_FOUND;
import static org.zalando.problem.Status.BAD_REQUEST;
import static se.sundsvall.citizen.service.ServiceConstants.ERROR_CITIZEN_NOT_FOUND;
import static se.sundsvall.citizen.service.ServiceConstants.ERROR_PERSONAL_NUMBER_NOT_FOUND;
import static se.sundsvall.citizen.service.ServiceConstants.ERROR_INVALID_CLASSIFICATION_CODE;
import static se.sundsvall.citizen.service.mapper.CitizenMapper.toCitizenExtended;

@Service
@Transactional
public class CitizenService {

    private final CitizenRepository citizenRepository;

    public CitizenService(CitizenRepository citizenRepository) {
        this.citizenRepository = citizenRepository;
    }

    public CitizenExtended getCitizenById(final UUID personId, final boolean showClassified) {
        final var citizenEntity = citizenRepository.findById(personId.toString())
                .orElseThrow(() -> Problem.valueOf(NOT_FOUND, format(ERROR_CITIZEN_NOT_FOUND, personId)));

        if (!showClassified && citizenEntity.getClassified() != null) {
            return null;
        }

        return toCitizenExtended(citizenEntity);
    }

    public UUID createCitizen(final String classificationCode) {
        if (!isValidClassificationCode(classificationCode)) {
            throw Problem.valueOf(BAD_REQUEST, ERROR_INVALID_CLASSIFICATION_CODE);
        }

        final var citizenEntity = CitizenEntity.create()
                .withClassified(classificationCode);

        var savedEntity = citizenRepository.save(citizenEntity);
        return UUID.fromString(savedEntity.getPersonId());
    }

    public UUID getPersonIdByPersonalNumber(final String personNumber) {
        final var citizenEntity = citizenRepository.findByPersonalNumber(personNumber)
                .orElseThrow(() -> Problem.valueOf(NOT_FOUND,
                        format(ERROR_PERSONAL_NUMBER_NOT_FOUND, personNumber)));

        return UUID.fromString(citizenEntity.getPersonId());
    }

    public List<CitizenExtended> getCitizensByIds(List<UUID> personIds, boolean showClassified) {
        return personIds.stream()
                .map(id -> citizenRepository.findById(id.toString())
                        .orElse(null))
                .filter(Objects::nonNull)
                .filter(citizen -> showClassified || citizen.getClassified() == null)
                .map(CitizenMapper::toCitizenExtended)
                .toList();
    }

    public List<PersonGuidBatch> getPersonIdsInBatch(List<String> personalNumbers) {
        return personalNumbers.stream()
                .map(personalNumber -> {
                    var result = PersonGuidBatch.create()
                            .withPersonNumber(personalNumber);

                    try {
                        var citizen = citizenRepository.findByPersonalNumber(personalNumber);
                        if (citizen.isPresent()) {
                            result.withPersonId(UUID.fromString(citizen.get().getPersonId()))
                                    .withSuccess(true);
                        } else {
                            result.withSuccess(false)
                                    .withErrorMessage("Citizen not found");
                        }
                    } catch (Exception e) {
                        result.withSuccess(false)
                                .withErrorMessage("Error processing request: " + e.getMessage());
                    }
                    return result;
                })
                .toList();
    }

    private boolean isValidClassificationCode(String classificationCode) {
        return classificationCode != null && !classificationCode.isEmpty();
    }
}