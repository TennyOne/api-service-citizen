package se.sundsvall.citizen.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import org.zalando.problem.Problem;
import org.zalando.problem.violations.ConstraintViolationProblem;
import se.sundsvall.citizen.api.model.CitizenExtended;
import se.sundsvall.citizen.api.model.PersonGuidBatch;
import se.sundsvall.citizen.service.CitizenService;
import se.sundsvall.dept44.common.validators.annotation.ValidUuid;

import java.util.List;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static org.springframework.http.ResponseEntity.created;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@Validated
@RequestMapping("/api/v2/citizen")
@Tag(name = "Citizen", description = "Show information about Citizens")
@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(oneOf = { Problem.class, ConstraintViolationProblem.class })))
@ApiResponse(responseCode = "500", description = "Server Error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
@ApiResponse(responseCode = "503", description = "Server Error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
public class CitizenResource {

    private final CitizenService citizenService;

    public CitizenResource(CitizenService citizenService) {
        this.citizenService = citizenService;
    }

    @GetMapping(path = "/{personId}", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Show information about specific citizen")
    @ApiResponse(responseCode = "200", description = "Success")
    @ApiResponse(responseCode = "204", description = "No Content")
    public ResponseEntity<CitizenExtended> getCitizenById(
            @Parameter(description = "ID for specific citizen")
            @ValidUuid @PathVariable final String personId,
            @Parameter(description = "If true, include search for classified persons")
            @RequestParam(defaultValue = "false") final boolean showClassified) {

        var citizen = citizenService.getCitizenById(UUID.fromString(personId), showClassified);
        return citizen != null ? ok(citizen) : ResponseEntity.noContent().build();
    }

    @PostMapping(path = "/guid", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Create person")
    @ApiResponse(responseCode = "200", description = "Success")
    @ApiResponse(responseCode = "409", description = "Conflict")
    public ResponseEntity<UUID> createCitizen(
            @Parameter(description = "Classification code")
            @NotNull @RequestBody final String classificationCode) {

        var personId = citizenService.createCitizen(classificationCode);
        return created(UriComponentsBuilder.fromPath("/api/v2/citizen/{id}")
                .buildAndExpand(personId).toUri())
                .body(personId);
    }

    @GetMapping(path = "/{personNumber}/guid", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get the personId from Personal identity number")
    @ApiResponse(responseCode = "200", description = "Success")
    @ApiResponse(responseCode = "204", description = "No Content")
    @ApiResponse(responseCode = "404", description = "Not Found")
    public ResponseEntity<UUID> getPersonIdByPersonalNumber(
            @Parameter(description = "Personal identity number for specific citizen")
            @PathVariable final String personNumber) {

        return ok(citizenService.getPersonIdByPersonalNumber(personNumber));
    }

    @PostMapping(path = "/batch", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Show information about list of citizens")
    @ApiResponse(responseCode = "200", description = "Success")
    @ApiResponse(responseCode = "400", description = "Bad Request")
    @ApiResponse(responseCode = "500", description = "Server Error")
    @ApiResponse(responseCode = "503", description = "Server Error")
    public ResponseEntity<List<CitizenExtended>> getCitizensBatch(
            @Parameter(description = "If true, include search for classified persons")
            @RequestParam(defaultValue = "false") final boolean showClassified,
            @Valid @RequestBody List<UUID> personIds) {

        return ok(citizenService.getCitizensByIds(personIds, showClassified));
    }

    @PostMapping(path = "/guid/batch", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get an array of personIds from Personal identity numbers")
    @ApiResponse(responseCode = "200", description = "Success")
    @ApiResponse(responseCode = "400", description = "Bad Request")
    @ApiResponse(responseCode = "500", description = "Server Error")
    @ApiResponse(responseCode = "503", description = "Server Error")
    public ResponseEntity<List<PersonGuidBatch>> getPersonIdsBatch(
            @Valid @RequestBody List<String> personalNumbers) {

        return ok(citizenService.getPersonIdsInBatch(personalNumbers));
    }
}