/*
 * The JASDB software and code is Copyright protected 2011 and owned by Renze de Vries
 * 
 * All the code and design principals in the codebase are also Copyright 2011 
 * protected and owned Renze de Vries. Any unauthorized usage of the code or the 
 * design and principals as in this code is prohibited.
 */
package nl.renarj.jasdb.service;

import nl.renarj.core.statistics.StatRecord;
import nl.renarj.core.statistics.StatisticsMonitor;
import nl.renarj.jasdb.api.SimpleEntity;
import nl.renarj.jasdb.core.exceptions.JasDBStorageException;
import nl.renarj.jasdb.core.exceptions.MetadataParseException;
import nl.renarj.jasdb.core.streams.ClonableByteArrayInputStream;
import nl.renarj.jasdb.core.streams.ClonableDataStream;
import nl.renarj.jasdb.index.Index;
import nl.renarj.jasdb.index.keys.Key;
import nl.renarj.jasdb.index.keys.factory.KeyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Set;

/**
 * Simple utility class that does Bag entity operations
 *
 * @author Renze de Vries
 */
public class BagOperationUtil {
    private static final Logger log = LoggerFactory.getLogger(BagOperationUtil.class);

    private static final String ENTITY_ENCODING = "UTF8";

    public static ClonableDataStream toStream(SimpleEntity entity) throws JasDBStorageException {
        try {
            String entityJson = SimpleEntity.toJson(entity);

            return new ClonableByteArrayInputStream(entityJson.getBytes(ENTITY_ENCODING));
        } catch(MetadataParseException e) {
            throw new JasDBStorageException("Unable to serialize to json", e);
        } catch(UnsupportedEncodingException e) {
            throw new JasDBStorageException("Unable to encode entity into Unicode", e);
        }
    }

    public static SimpleEntity toEntity(InputStream stream) throws JasDBStorageException {
        return SimpleEntity.fromStream(stream);
    }

    public static boolean isDataPresent(SimpleEntity sEntity, Index index) {
        for(String indexField : index.getKeyInfo().getKeyFields()) {
            if(!sEntity.hasProperty(indexField)) {
                return false;
            }
        }
        return true;
    }
    
    public static Set<Key> createEntityKeys(SimpleEntity entity, Index index) throws JasDBStorageException {
        StatRecord createKey = StatisticsMonitor.createRecord("bag:createKey");
        KeyFactory keyFactory = index.getKeyInfo().getKeyFactory();
        Set<Key> insertKeys;
        if(keyFactory.isMultiValueKey(entity)) {
            insertKeys = keyFactory.createMultivalueKeys(entity);
        } else {
            insertKeys = new HashSet<Key>();
            insertKeys.add(keyFactory.createKey(entity));
        }
        createKey.stop();
        return insertKeys;
    }
    
    public static void doIndexInsert(Set<Key> keys, Index index) throws JasDBStorageException {
        log.trace("Inserting {} keys into index: {}", keys.size(), index.getKeyInfo().getKeyName());
        StatRecord indexInsert = StatisticsMonitor.createRecord("bag:indexInsert");
        for(Key key : keys) {
            index.insertIntoIndex(key);
        }
        indexInsert.stop();

    }
}
