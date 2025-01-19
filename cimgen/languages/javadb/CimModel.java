package cim4jdb;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.repository.CrudRepository;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

import lombok.Data;
import lombok.NoArgsConstructor;

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

    @OneToMany(mappedBy = "cimModel", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private List<BaseClass> cimObjects = new ArrayList<>();

    /**
     * Nested repository. The implementation is automatically created.
     */
    public interface Repository extends CrudRepository<CimModel, Long> {
    }
}
