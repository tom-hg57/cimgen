package cim4jdb;

import java.util.HashMap;
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
    private BaseClass.Repository baseClassRepository;

    /**
     * Searches for all CIM objects that match the specified cimModelId, and returns
     * a map of ID to CIM type.
     *
     * @param cimModelId ID of a CIM model
     * @return CIM objects of this model as map of ID to CIM type
     */
    public Map<Long, String> getCimObjects(Long cimModelId) {
        var map = new HashMap<Long, String>();
        for (var infos : baseClassRepository.findByModel(cimModelId)) {
            var id = (Long) infos[0];
            var cimType = (String) infos[1];
            map.put(id, cimType);
        }
        return map;
    }
}
