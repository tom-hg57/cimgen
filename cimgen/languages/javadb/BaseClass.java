package cim4jdb;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;

import lombok.Data;

import org.springframework.data.repository.CrudRepository;

@Data
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class BaseClass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String rdfid;
    private String cimType;

    /**
     * Nested repository. The implementation is automatically created.
     */
    public interface Repository extends CrudRepository<BaseClass, Long> {
    }
}
