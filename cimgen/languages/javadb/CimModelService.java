package cim4jdb;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class for managing CimModel objects.
 */
@Service
@Transactional
public class CimModelService {

    @Autowired
    private CimClassMap cimClassMap;
    @Autowired
    private BaseClass.Repository baseClassRepository;

    /**
     * Searches for all CIM objects that match the specified cimModelId, and returns
     * a map of ID to CIM type.
     *
     * @param cimModelId ID of a CIM model
     * @return           CIM objects of this model as map of ID to CIM type
     */
    public Map<Long, String> getCimObjectInfos(Long cimModelId) {
        var map = new HashMap<Long, String>();
        for (var infos : baseClassRepository.findByModel(cimModelId)) {
            var id = (Long) infos[0];
            var cimType = (String) infos[1];
            map.put(id, cimType);
        }
        return map;
    }

    /**
     * Saves a CIM object to the database and links it to the model.
     *
     * @param obj   The CIM object.
     * @param model The CIM model the object has to be linked to.
     * @return      The saved CIM object.
     */
    public <T extends BaseClass> T saveCimObject(T obj, CimModel model) {
        obj.setCimModel(model);
        return cimClassMap.saveCimObject(obj.getClass(), obj);
    }

    /**
     * Saves several CIM objects to the database and links them to the model.
     *
     * @param objList The list of objects of any CIM type.
     * @param model   The CIM model the objects has to be linked to.
     */
    public void saveCimObjects(Iterable<BaseClass> objList, CimModel model) {
        for (BaseClass obj : objList) {
            obj.setCimModel(model);
            obj = cimClassMap.saveCimObject(obj.getClass(), obj);
        }
    }

    /**
     * Reads all CIM objects that match the specified cimModelId from the database.
     *
     * @param cimModelId ID of a CIM model
     * @return           CIM objects of this model
     */
    public Map<String, BaseClass> readCimObjects(Long cimModelId) {
        var model = new LinkedHashMap<String, BaseClass>();
        for (var id_and_type : getCimObjectInfos(cimModelId).entrySet()) {
            var obj = cimClassMap.readCimObject(id_and_type.getValue(), id_and_type.getKey());
            model.put(obj.getRdfid(), obj);
        }
        return model;
    }
}
