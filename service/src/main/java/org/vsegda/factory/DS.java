package org.vsegda.factory;

import com.google.appengine.api.datastore.*;

import java.util.List;

/**
 * Factory for {@link DatastoreService} and its transactions.
 *
 * @author Roman Elizarov
 */
public class DS {
    private static final ThreadLocal<DatastoreService> DS_THREAD_LOCAL = new ThreadLocal<>();
    private static final ThreadLocal<Transaction> TX_THREAD_LOCAL = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> REQ_THREAD_LOCAL = new ThreadLocal<>();

    private static DatastoreService instance() {
        if (REQ_THREAD_LOCAL.get() != Boolean.TRUE)
            throw new IllegalStateException("Not serving a request");
        DatastoreService ds = DS_THREAD_LOCAL.get();
        if (ds == null) {
            ds = DatastoreServiceFactory.getDatastoreService();
            DS_THREAD_LOCAL.set(ds);
        }
        return ds;
    }

    // --------- public ---------

    public static void beginTransaction() {
        Transaction tx = TX_THREAD_LOCAL.get();
        if (tx == null) {
            DatastoreService ds = instance();
            tx = ds.getCurrentTransaction();
            if (tx == null) tx = ds.beginTransaction();
            TX_THREAD_LOCAL.set(tx);
        }
    }

    public static Entity get(Key key) {
        Transaction tx = TX_THREAD_LOCAL.get();
        DatastoreService ds = instance();
        try {
            if (tx != null) {
                return ds.get(tx, key);
            } else {
                return ds.get(key);
            }
        } catch (EntityNotFoundException e) {
            return null;
        }
    }

    public static Key put(Entity entity) {
        Transaction tx = TX_THREAD_LOCAL.get();
        DatastoreService ds = instance();
        if (tx != null) {
            return ds.put(tx, entity);
        } else {
            return ds.put(entity);
        }
    }

    public static List<Key> put(List<Entity> entities) {
        Transaction tx = TX_THREAD_LOCAL.get();
        DatastoreService ds = instance();
        if (tx != null) {
            return ds.put(tx, entities);
        } else {
            return ds.put(entities);
        }
    }

    public static PreparedQuery prepare(Query query) {
        Transaction tx = TX_THREAD_LOCAL.get();
        DatastoreService ds = instance();
        PreparedQuery pq;
        if (tx != null) {
            pq = ds.prepare(tx, query);
        } else {
            pq = ds.prepare(query);
        }
        return pq;
    }

    public static void delete(Key key) {
        Transaction tx = TX_THREAD_LOCAL.get();
        DatastoreService ds = instance();
        if (tx != null) {
            ds.delete(tx, key);
        } else {
            ds.delete(key);
        }
    }

    public static void delete(List<Key> keys) {
        Transaction tx = TX_THREAD_LOCAL.get();
        DatastoreService ds = instance();
        if (tx != null) {
            ds.delete(tx, keys);
        } else {
            ds.delete(keys);
        }
    }

    // --------- package-private ---------

    static void start() {
        REQ_THREAD_LOCAL.set(Boolean.TRUE);
    }

    static void commit() {
        Transaction tx = TX_THREAD_LOCAL.get();
        if (tx != null && tx.isActive())
            tx.commit();
    }

    static void close() {
        REQ_THREAD_LOCAL.set(null);
        Transaction tx = TX_THREAD_LOCAL.get();
        if (tx != null) {
            if (tx.isActive())
                tx.rollback();
            TX_THREAD_LOCAL.set(null);
        }
        DatastoreService ds = DS_THREAD_LOCAL.get();
        if (ds != null) {
            DS_THREAD_LOCAL.set(null);
        }
    }
}
