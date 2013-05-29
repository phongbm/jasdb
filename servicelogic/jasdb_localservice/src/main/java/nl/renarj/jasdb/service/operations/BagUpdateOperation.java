/*
 * The JASDB software and code is Copyright protected 2011 and owned by Renze de Vries
 * 
 * All the code and design principals in the codebase are also Copyright 2011 
 * protected and owned Renze de Vries. Any unauthorized usage of the code or the 
 * design and principals as in this code is prohibited.
 */
package nl.renarj.jasdb.service.operations;

import nl.renarj.jasdb.api.SimpleEntity;
import nl.renarj.jasdb.api.model.IndexManager;
import nl.renarj.jasdb.core.exceptions.JasDBStorageException;
import nl.renarj.jasdb.core.storage.RecordResult;
import nl.renarj.jasdb.core.storage.RecordWriter;
import nl.renarj.jasdb.index.Index;
import nl.renarj.jasdb.index.keys.Key;
import nl.renarj.jasdb.index.keys.impl.UUIDKey;
import nl.renarj.jasdb.service.BagOperationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * User: renarj
 * Date: 3/17/12
 * Time: 7:48 PM
 */
public class BagUpdateOperation implements DataOperation {
    private static final Logger log = LoggerFactory.getLogger(BagUpdateOperation.class);
    
    private RecordWriter recordWriter;
    private IndexManager indexManager;
    private String bagName;

    public BagUpdateOperation(String bagName, IndexManager indexManager, RecordWriter recordWriter) {
        this.bagName = bagName;
        this.recordWriter = recordWriter;
        this.indexManager = indexManager;
    }

    @Override
    public void doDataOperation(SimpleEntity entity) throws JasDBStorageException {
        UUIDKey documentKey = new UUIDKey(entity.getInternalId());
        RecordResult result = recordWriter.readRecord(documentKey);

        if(result != null && result.isRecordFound()) {
            SimpleEntity oldEntity = BagOperationUtil.toEntity(result.getStream());

            //let's update the actual record, so we can get the new record pointer and set this on the entity
            log.debug("Updating record: {}", entity.getInternalId());
            recordWriter.updateRecord(documentKey, BagOperationUtil.toStream(entity));

            doIndexModifications(oldEntity, entity);
        } else {
            throw new JasDBStorageException("Unable to update entity: " + entity.getInternalId() + " record does not exist");
        }
    }

    private void doIndexModifications(SimpleEntity oldEntity, SimpleEntity entity) throws JasDBStorageException {
        log.debug("Starting index update for entity: {}", entity.getInternalId());
//        boolean recordPointerUpdated = oldRecord != updatedRecord;
        //let's start updating the indexes
        Map<String, Index> indexes = indexManager.getIndexes(bagName);
        for(Map.Entry<String, Index> indexEntry : indexes.entrySet()) {
            Index index = indexEntry.getValue();

            boolean dataPresentOldEntity = BagOperationUtil.isDataPresent(oldEntity, index);
            boolean dataPresentNewEntity = BagOperationUtil.isDataPresent(entity, index);

            Set<Key> keys = BagOperationUtil.createEntityKeys(entity, index);
            if(dataPresentNewEntity && dataPresentOldEntity) {
                //we could have a data update, or just a record pointer update
                Set<Key> oldKeys = BagOperationUtil.createEntityKeys(oldEntity, index);
                Set<Key> removedKeys = new HashSet<Key>(oldKeys);
                for(Key key : keys) {
                    if(oldKeys.contains(key)) {
                        //the key is still present and not removed
                        removedKeys.remove(key);
                    } else {
                        //not present in index, let's add
                        index.insertIntoIndex(key);
                    }
                }
                
                for(Key removedKey : removedKeys) {
                    //we need to remove from index
                    index.removeFromIndex(removedKey);
                }
            } else if(dataPresentNewEntity) {
                //data was not present yet, so we just do an insert
                BagOperationUtil.doIndexInsert(keys, index);
            } else {
                //data was removed, need to remove from index
                Set<Key> removeKeys = BagOperationUtil.createEntityKeys(oldEntity, index);
                for(Key key : removeKeys) {
                    index.removeFromIndex(key);
                }
            }
        }
        
    }

}
