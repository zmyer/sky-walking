package org.skywalking.apm.collector.core.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import org.skywalking.apm.collector.core.framework.DefinitionFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author pengys5
 */
public class DefinitionLoader<D> implements Iterable<D> {

    private final Logger logger = LoggerFactory.getLogger(DefinitionLoader.class);

    private final Class<D> definition;
    private final DefinitionFile definitionFile;

    protected DefinitionLoader(Class<D> svc, DefinitionFile definitionFile) {
        this.definition = Objects.requireNonNull(svc, "definition interface cannot be null");
        this.definitionFile = definitionFile;
    }

    public static <D> DefinitionLoader<D> load(Class<D> definition, DefinitionFile definitionFile) {
        return new DefinitionLoader(definition, definitionFile);
    }

    @Override public final Iterator<D> iterator() {
        logger.info("load definition file: {}", definitionFile.get());
        List<String> definitionList = new LinkedList<>();
        try {
            Enumeration<URL> urlEnumeration = this.getClass().getClassLoader().getResources(definitionFile.get());
            while (urlEnumeration.hasMoreElements()) {
                URL definitionFileURL = urlEnumeration.nextElement();
                logger.info("definition file url: {}", definitionFileURL.getPath());
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(definitionFileURL.openStream()));
                Properties properties = new Properties();
                properties.load(bufferedReader);

                Enumeration defineItem = properties.propertyNames();
                while (defineItem.hasMoreElements()) {
                    String fullNameClass = (String)defineItem.nextElement();
                    definitionList.add(fullNameClass);
                }
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

        Iterator<String> moduleDefineIterator = definitionList.iterator();

        return new Iterator<D>() {
            @Override public boolean hasNext() {
                return moduleDefineIterator.hasNext();
            }

            @Override public D next() {
                String definitionClass = moduleDefineIterator.next();
                logger.info("definitionClass: {}", definitionClass);
                try {
                    Class c = Class.forName(definitionClass);
                    return (D)c.newInstance();
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
                return null;
            }
        };
    }
}
