package org.vsegda;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

/**
 * @author Roman Elizarov
 */
public class Factory {
    private static final PersistenceManagerFactory PMF = JDOHelper.getPersistenceManagerFactory("default");

    public static PersistenceManager getPersistenceManager() {
        return PMF.getPersistenceManager();
    }
}
