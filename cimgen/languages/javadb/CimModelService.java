package cim4jdb;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class for managing CimModel objects.
 */
@Service
@Transactional
public class CimModelService {

    private static final Logging LOG = Logging.getLogger(CimModelService.class);

    @Autowired
    private CimModel.Repository cimModelRepository;

    @Autowired
    private CimClassMap cimClassMap;

    @Autowired
    private BaseClass.Repository baseClassRepository;

    /**
     * Searches for all CIM objects that match the specified cimModelId, and returns
     * a map of CIM type to lists of IDs.
     *
     * @param cimModelId ID of a CIM model
     * @return           CIM objects of this model as map of CIM type to ID list
     */
    public Map<String, List<Long>> getCimObjectInfos(Long cimModelId) {
        var map = new LinkedHashMap<String, List<Long>>();
        for (var infos : baseClassRepository.findByModel(cimModelId)) {
            var id = (Long) infos[0];
            var cimType = (String) infos[1];
            map.computeIfAbsent(cimType, k -> new ArrayList<>()).add(id);
        }
        return map;
    }

    /**
     * Saves model and several CIM objects to the database and links them to the
     * model.
     *
     * @param model   The CIM model the objects has to be linked to.
     * @param objList The list of objects of any CIM type.
     * @return        The saved CIM model.
     */
    public CimModel saveCimModel(CimModel model, Iterable<BaseClass> objList) {
        model = cimModelRepository.save(model);
        var savedObjects = saveCimObjects(objList, model);
        for (var obj : savedObjects) {
            LOG.debug(String.format("Object in CIM model %d: id=%d type=%s", model.getCimModelId(), obj.getId(),
                    obj.getCimType()));
        }
        return model;
    }

    /**
     * Saves several CIM objects to the database and links them to the model.
     *
     * @param objList The list of objects of any CIM type.
     * @param model   The CIM model the objects has to be linked to.
     * @return        The list of saved CIM objects.
     */
    public List<BaseClass> saveCimObjects(Iterable<BaseClass> objList, CimModel model) {
        var map = new LinkedHashMap<String, List<BaseClass>>();
        for (BaseClass obj : objList) {
            obj.setCimModel(model);
            map.computeIfAbsent(obj.getCimType(), k -> new ArrayList<>()).add(obj);
        }
        var list = new ArrayList<BaseClass>();
        for (String className : map.keySet()) {
            var savedObjects = cimClassMap.saveCimObjects(className, map.get(className));
            for (BaseClass obj : savedObjects) {
                list.add(obj);
            }
        }
        return list;
    }

    /**
     * Reads all CIM objects that match the specified cimModelId from the database,
     * linking the CIM objects after reading.
     *
     * @param cimModelId ID of a CIM model
     * @return           CIM objects of this model
     */
    public Map<String, BaseClass> readCimObjects(Long cimModelId) {
        return readCimObjects(cimModelId, true);
    }

    /**
     * Reads all CIM objects that match the specified cimModelId from the database,
     * with or without linking the CIM objects after reading.
     *
     * @param cimModelId      ID of a CIM model
     * @param linkCimObjects  Enable/disable linking CIM object attributes
     * @return                CIM objects of this model
     */
    public Map<String, BaseClass> readCimObjects(Long cimModelId, boolean linkCimObjects) {
        var model = new LinkedHashMap<String, BaseClass>();
        for (var entry : getCimObjectInfos(cimModelId).entrySet()) {
            var objList = cimClassMap.readCimObjects(entry.getKey(), entry.getValue());
            for (BaseClass obj : objList) {
                model.put(obj.getRdfid(), obj);
            }
        }
        if (linkCimObjects) {
            for (var obj : model.values()) {
                var attrNames = obj.getAttributeNames();
                for (String attrName : attrNames) {
                    Object attr = obj.getAttribute(attrName);
                    if (obj.isPrimitiveAttribute(attrName) || obj.isEnumAttribute(attrName)) {
                        // nothing to do
                    } else if (attr instanceof String) {
                        // After reading from database there is only the Id of the linked object
                        // for class attributes. This Id is provided by getAttribute().
                        // It has to be replaced by the link to the real object.
                        var linkedObj = model.get((String) attr);
                        if (linkedObj != null) {
                            try {
                                obj.setAttribute(attrName, linkedObj);
                            } catch (Exception ex) {
                                LOG.error(String.format("Error while linking attribute %s.%s for rdfid %s",
                                        obj.getCimType(), attrName, obj.getRdfid()), ex);
                            }
                        }
                    } else if (attr instanceof Set<?>) {
                        // For few list attributes (e.g. TopologicalIsland to TopologicalNode)
                        // getAttribute() provides a set of Ids.
                        for (var attrItem : ((Set<?>) attr)) {
                            if (attrItem instanceof String) {
                                var linkedObj = model.get((String) attrItem);
                                if (linkedObj != null) {
                                    try {
                                        obj.setAttribute(attrName, linkedObj);
                                    } catch (Exception ex) {
                                        LOG.error(String.format("Error while linking attribute %s.%s for rdfid %s",
                                                obj.getCimType(), attrName, obj.getRdfid()), ex);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return model;
    }

    /**
     * Deletes the model and all CIM objects that match the specified cimModelId
     * from the database.
     *
     * @param cimModelId ID of a CIM model
     */
    public void deleteCimModel(Long cimModelId) {
        for (var entry : getCimObjectInfos(cimModelId).entrySet()) {
            cimClassMap.deleteCimObjects(entry.getKey(), entry.getValue());
        }
        cimModelRepository.deleteById(cimModelId);
    }
}
