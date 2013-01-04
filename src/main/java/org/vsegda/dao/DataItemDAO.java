package org.vsegda.dao;

import com.google.appengine.api.datastore.Key;
import net.sf.jsr107cache.Cache;
import net.sf.jsr107cache.CacheException;
import net.sf.jsr107cache.CacheManager;
import org.vsegda.data.DataItem;
import org.vsegda.factory.Factory;

import javax.jdo.JDOObjectNotFoundException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author Roman Elizarov
 */
public class DataItemDAO {
    private static final Logger log = Logger.getLogger(DataItemDAO.class.getName());
    private static final Cache CACHE;

    static {
        try {
            CACHE = CacheManager.getInstance().getCacheFactory().createCache(Collections.emptyMap());
        } catch (CacheException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private DataItemDAO() {} // do not create

    public static DataItem getDataItemByKey(Key key) {
        DataItem dataItem = (DataItem) CACHE.get(key);
        if (dataItem == null) {
            try {
                dataItem = Factory.getPM().getObjectById(DataItem.class, key);
            } catch (JDOObjectNotFoundException e) {
                log.info("Data item is not found by key=" + key);
                return null;
            }
            CACHE.put(key, dataItem);
        }
        return dataItem;
    }

    public static void persistDataItem(DataItem dataItem) {
        Factory.getPM().makePersistent(dataItem);
        CACHE.put(dataItem.getKey(), dataItem);
    }

    public static void persistDataItems(List<DataItem> items) {
        for (DataItem item : items) {
            persistDataItem(item);
        }
    }
}
