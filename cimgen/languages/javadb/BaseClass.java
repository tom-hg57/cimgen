package cim4jdb;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

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

    private static final Logging LOG = Logging.getLogger(BaseClass.class);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "cim_model_id", referencedColumnName = "cim_model_id")
    private CimModel cimModel;

    /**
     * The name of the CIM type.
     */
    private String cimType;

    /**
     * The RDF ID of the CIM object read from rdf:ID or rdf:about.
     */
    private String rdfid;

    /**
     * Nested repository. The implementation is automatically created.
     */
    public interface Repository extends CrudRepository<BaseClass, Long> {
    }

    /**
     * Get a set of all attribute names of the CIM type.
     *
     * The set includes all inherited attributes.
     * The attribute name is only the last part of the full name (without the class name).
     *
     * @return All attributes of the CIM type
     */
    public abstract Set<String> getAttributeNames();

    protected Map<String, AttrDetails> allAttrDetailsMap() {
        Map<String, AttrDetails> map = new LinkedHashMap<>();
        return map;
    }

    /**
     * Get the full name of an attribute.
     *
     * The full name is "<class_name>.<attribute_name>".
     *
     * @param attrName The attribute name
     * @return         The full name
     */
    public abstract String getAttributeFullName(String attrName);

    /**
     * Get an attribute value as string.
     *
     * @param attrName The attribute name
     * @return         The attribute value
     */
    public abstract String getAttribute(String attrName);

    protected String getAttribute(String className, String attrName) {
        LOG.error(String.format("No-one knows an attribute %s.%s", className, attrName));
        return "";
    }

    /**
     * Set an attribute value.
     *
     * @param attrName    The attribute name
     * @param stringValue The attribute value as string
     */
    public abstract void setAttribute(String attrName, String stringValue);

    protected void setAttribute(String className, String attrName, String stringValue) {
        LOG.error(String.format("No-one knows what to do with attribute %s.%s and value %s", className, attrName, stringValue));
    }

    /**
     * Check if the attribute is a primitive attribute.
     *
     * This includes datatype_attributes.
     *
     * @param attrName The attribute name
     * @return         Is it a primitive attribute?
     */
    public abstract boolean isPrimitiveAttribute(String attrName);

    /**
     * Check if the attribute is an enum attribute.
     *
     * @param attrName The attribute name
     * @return         Is it an enum attribute?
     */
    public abstract boolean isEnumAttribute(String attrName);

    /**
     * Check if the attribute is used.
     *
     * Some attributes are declared as unused in the CGMES definition.
     * In most cases these are list attributes, i.e. lists of links to other CIM objects.
     * But there are some exceptions, e.g. the list of ToplogicalNodes in TopologicalIsland.
     *
     * @param attrName The attribute name
     * @return         Is the attribute used?
     */
    public abstract boolean isUsedAttribute(String attrName);

    /**
     * Helper functions.
     */

    protected Boolean getBooleanFromString(String stringValue) {
        return stringValue.toLowerCase().equals("true");
    }

    protected Double getDoubleFromString(String stringValue) {
        try {
            return Double.valueOf(stringValue);
        } catch (NumberFormatException ex) {
            LOG.error("Error getting Double from String", ex);
            return null;
        }
    }

    protected Float getFloatFromString(String stringValue) {
        try {
            return Float.valueOf(stringValue);
        } catch (NumberFormatException ex) {
            LOG.error("Error getting Float from String", ex);
            return null;
        }
    }

    protected Integer getIntegerFromString(String stringValue) {
        try {
            return Integer.parseInt(stringValue.trim());
        } catch (NumberFormatException ex) {
            LOG.error("Error getting Integer from String", ex);
            return null;
        }
    }

    protected String getStringFromString(String stringValue) {
        return stringValue;
    }

    /**
     * Nested helper class.
     */
    protected static class AttrDetails {
        public AttrDetails() {
        }
        public AttrDetails(String f, Supplier<String> g, Consumer<String> s, boolean p, boolean e, boolean u) {
            fullName = f;
            getter = g;
            setter = s;
            isPrimitive = p;
            isEnum = e;
            isUsed = u;
        }

        public String fullName;
        public Supplier<String> getter;
        public Consumer<String> setter;
        public Boolean isPrimitive;
        public Boolean isEnum;
        public boolean isUsed;
    }
}
