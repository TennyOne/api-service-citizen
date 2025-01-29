package se.sundsvall.citizen.integration.db;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.data.jpa.repository.JpaRepository;
import se.sundsvall.citizen.integration.db.model.CitizenAddressEntity;

import java.time.OffsetDateTime;
import java.util.List;

@CircuitBreaker(name = "CitizenAddressRepository")
public interface CitizenAddressRepository extends JpaRepository<CitizenAddressEntity, String> {

    List<CitizenAddressEntity> findByCitizen_UpdatedAtGreaterThanEqual(OffsetDateTime changedDateFrom);
}