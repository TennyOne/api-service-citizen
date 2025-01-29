package se.sundsvall.citizen.api.model;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;

import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "CreateCitizenResponse model")
public class CreateCitizenResponse {

    @JsonProperty("_meta")
    @Schema(implementation = MetaData.class, accessMode = READ_ONLY)
    private MetaData metaData;

    @Schema(description = "The created citizen's ID", example = "b82bd8ac-1507-4d9a-958d-369261eecc15", accessMode = READ_ONLY)
    private UUID personId;

    public static CreateCitizenResponse create() {
        return new CreateCitizenResponse();
    }

    public MetaData getMetaData() {
        return metaData;
    }

    public void setMetaData(MetaData metaData) {
        this.metaData = metaData;
    }

    public CreateCitizenResponse withMetaData(MetaData metaData) {
        this.metaData = metaData;
        return this;
    }

    public UUID getPersonId() {
        return personId;
    }

    public void setPersonId(UUID personId) {
        this.personId = personId;
    }

    public CreateCitizenResponse withPersonId(UUID personId) {
        this.personId = personId;
        return this;
    }

    @Override
    public int hashCode() {
        return Objects.hash(personId, metaData);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        CreateCitizenResponse other = (CreateCitizenResponse) obj;
        return Objects.equals(personId, other.personId) && Objects.equals(metaData, other.metaData);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("CreateCitizenResponse [metaData=").append(metaData)
                .append(", personId=").append(personId)
                .append("]");
        return builder.toString();
    }
}