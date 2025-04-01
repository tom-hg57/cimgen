package cim4jdb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import cim4jdb.utils.RdfReader;
import de.psi.cimarchive.utils.ZipFileUtils;

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
     * Saves model and several CIM objects to the database and links them to the
     * model.
     *
     * @param model   The CIM model the objects has to be linked to.
     * @param zipData The content of the zip file
     * @return        The saved CIM model.
     */
    public CimModel saveCimModel(CimModel model, byte[] zipData) {
        model = cimModelRepository.save(model);
        var cimFileAsStringList = ZipFileUtils.extractCimFilesFromZipFile(zipData);
        try {
            var map = RdfReader.readFromStrings(cimFileAsStringList);
            saveCimObjects(map.values(), model);
        } catch (Exception ex) {
            String txt = "Error while reading zip data";
            LOG.error(txt, ex);
            throw new RuntimeException(txt, ex);
        }
        for (var entry : getCimObjectInfos(model.getCimModelId()).entrySet()) {
            LOG.debug(String.format("Object in CIM model %d: id=%d type=%s", model.getCimModelId(), entry.getKey(),
                    entry.getValue()));
        }
        return model;
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
     * @return        The list of saved CIM objects.
     */
    public List<BaseClass> saveCimObjects(Iterable<BaseClass> objList, CimModel model) {
        var list = new ArrayList<BaseClass>();
        for (BaseClass obj : objList) {
            obj.setCimModel(model);
            obj = cimClassMap.saveCimObject(obj.getClass(), obj);
            list.add(obj);
        }
        return list;
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

    /**
     * Deletes the model and all CIM objects that match the specified cimModelId
     * from the database.
     *
     * @param cimModelId ID of a CIM model
     */
    public void deleteCimModel(Long cimModelId) {
        for (var id_and_type : getCimObjectInfos(cimModelId).entrySet()) {
            cimClassMap.deleteCimObject(id_and_type.getValue(), id_and_type.getKey());
        }
        cimModelRepository.deleteById(cimModelId);
    }
}
