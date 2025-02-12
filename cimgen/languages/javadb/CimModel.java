package cim4jdb;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.data.repository.CrudRepository;

/**
 * Represents the CIM model which gets stored in the database and is linked to a
 * list of CIM objects.
 */
@Data
@Entity
@NoArgsConstructor
public class CimModel {

    @Id
    @Column(name = "cim_model_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cimModelId;

    /**
     * Nested repository. The implementation is automatically created.
     */
    public interface Repository extends CrudRepository<CimModel, Long> {
    }
}
