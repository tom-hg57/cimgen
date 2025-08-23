package cim4jdb;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import org.springframework.data.repository.CrudRepository;

/**
 * Represents the CIM model which gets stored in the database and is linked to a
 * list of CIM objects.
 */
@Entity
public class CimModel {

    @Id
    @Column(name = "cim_model_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cimModelId;

    public Long getCimModelId() {
        return cimModelId;
    }

    public void setCimModelId(Long cimModelId) {
        this.cimModelId = cimModelId;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * @param obj  the reference object with which to compare.
     * @return     true if this object is the same as the obj argument; false otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || obj.getClass() != getClass())
            return false;
        CimModel other = (CimModel) obj;
        if (cimModelId == null ? other.cimModelId != null : !cimModelId.equals(other.cimModelId))
            return false;
        return true;
    }

    /**
     * Returns a hash code value for the object.
     *
     * This method is supported for the benefit of hash tables such as those
     * provided by HashMap.
     *
     * @return  a hash code value for this object.
     */
    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = (result * PRIME) + (cimModelId == null ? 0 : cimModelId.hashCode());
        return result;
    }

    /**
     * Nested repository. The implementation is automatically created.
     */
    public interface Repository extends CrudRepository<CimModel, Long> {
    }
}
