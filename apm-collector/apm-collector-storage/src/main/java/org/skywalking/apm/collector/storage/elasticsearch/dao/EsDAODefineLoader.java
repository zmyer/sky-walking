package org.skywalking.apm.collector.storage.elasticsearch.dao;

import java.util.ArrayList;
import java.util.List;
import org.skywalking.apm.collector.core.framework.DefineException;
import org.skywalking.apm.collector.core.framework.Loader;
import org.skywalking.apm.collector.core.util.DefinitionLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author pengys5
 */
public class EsDAODefineLoader implements Loader<List<EsDAO>> {

    private final Logger logger = LoggerFactory.getLogger(EsDAODefineLoader.class);

    @Override public List<EsDAO> load() throws DefineException {
        List<EsDAO> esDAOs = new ArrayList<>();

        EsDAODefinitionFile definitionFile = new EsDAODefinitionFile();
        logger.info("elasticsearch dao definition file name: {}", definitionFile.fileName());
        DefinitionLoader<EsDAO> definitionLoader = DefinitionLoader.load(EsDAO.class, definitionFile);
        for (EsDAO dao : definitionLoader) {
            logger.info("loaded elasticsearch dao definition class: {}", dao.getClass().getName());
            esDAOs.add(dao);
        }
        return esDAOs;
    }
}
