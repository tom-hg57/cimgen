package cim4jdb;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

import org.springframework.data.repository.CrudRepository;

/**
 * Base class of the CIM type hierarchy - all not primitive CIM classes inherit
 * from this class.
 *
 * Represents a CIM object which gets stored in the database and is linked to a
 * CIM model.
 *
 * The rdfid is a unique identifier inside of a CIM model.
 * The id of the CIM model and the rdfid together are a unique identifier in
 * this database table.
 * The cimType is the name of the real class of the CIM object - a subclass of
 * BaseClass. To read or write a CIM object the repository of this subclass
 * should be used.
 */
@Data
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class BaseClass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "cim_model_id", referencedColumnName = "cim_model_id")
    private CimModel cimModel;

    private String rdfid;
    private String cimType;

    /**
     * Nested repository. The implementation is automatically created.
     */
    public interface Repository extends CrudRepository<BaseClass, Long> {
    }
}
