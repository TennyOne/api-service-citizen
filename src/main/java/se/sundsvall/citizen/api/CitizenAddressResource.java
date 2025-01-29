package se.sundsvall.citizen.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.zalando.problem.Problem;
import se.sundsvall.citizen.api.model.CitizenWithChangedAddress;
import se.sundsvall.citizen.service.CitizenAddressService;

import java.time.OffsetDateTime;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@Validated
@RequestMapping("/api/v2/citizen/changedaddress")
@Tag(name = "Citizen", description = "Citizen operations")
@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
@ApiResponse(responseCode = "500", description = "Server Error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
@ApiResponse(responseCode = "503", description = "Server Error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
public class CitizenAddressResource {

    private final CitizenAddressService citizenAddressService;

    public CitizenAddressResource(CitizenAddressService citizenAddressService) {
        this.citizenAddressService = citizenAddressService;
    }

    @GetMapping(produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Show citizens that have moved since the given date")
    @ApiResponse(responseCode = "200", description = "Success")
    @ApiResponse(responseCode = "204", description = "No Content")
    public ResponseEntity<List<CitizenWithChangedAddress>> getCitizensWithChangedAddress(
            @Parameter(description = "From-date for move")
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            final OffsetDateTime changedDateFrom) {

        var changedAddresses = citizenAddressService.getCitizensWithChangedAddress(changedDateFrom);

        if (changedAddresses.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ok(changedAddresses);
    }
}