package org.vsegda.factory;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Transaction;

/**
 * @author Roman Elizarov
 */
public class Factory {
    private static final PersistenceManagerFactory PM_FACTORY = JDOHelper.getPersistenceManagerFactory("default");
    private static final ThreadLocal<PersistenceManager> PM_THREAD_LOCAL = new ThreadLocal<PersistenceManager>();
    private static final ThreadLocal<Transaction> TX_THREAD_LOCAL = new ThreadLocal<Transaction>();

    public static PersistenceManager getPM() {
        PersistenceManager pm = PM_THREAD_LOCAL.get();
        if (pm == null) {
            pm = PM_FACTORY.getPersistenceManager();
            PM_THREAD_LOCAL.set(pm);
        }
        return pm;
    }

    public static void beginTransaction() {
        Transaction tx = TX_THREAD_LOCAL.get();
        if (tx == null) {
            tx = getPM().currentTransaction();
            tx.begin();
            TX_THREAD_LOCAL.set(tx);
        }
    }

    static void commit() {
        Transaction tx = TX_THREAD_LOCAL.get();
        if (tx != null && tx.isActive())
            tx.commit();
    }

    static void close() {
        Transaction tx = TX_THREAD_LOCAL.get();
        if (tx != null) {
            if (tx.isActive())
                tx.rollback();
            TX_THREAD_LOCAL.set(null);
        }
        PersistenceManager pm = PM_THREAD_LOCAL.get();
        if (pm != null) {
            PM_THREAD_LOCAL.set(null);
            pm.close();
        }
    }

}
