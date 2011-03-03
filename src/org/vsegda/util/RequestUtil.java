package org.vsegda.util;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;

import javax.servlet.ServletRequest;

/**
 * @author Roman Elizarov
 */
public class RequestUtil {
    static {
        ConvertUtils.register(new IdList.Cnv(), IdList.class);
        ConvertUtils.register(new TimeInstant.Cnv(), TimeInstant.class);
        ConvertUtils.register(new TimePeriod.Cnv(), TimePeriod.class);
    }

    public static void populate(Object obj, ServletRequest req) {
        try {
            BeanUtils.populate(obj, req.getParameterMap());
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }
}
