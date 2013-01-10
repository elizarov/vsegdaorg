package org.vsegda.request;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.vsegda.util.IdList;
import org.vsegda.util.TimeInstant;
import org.vsegda.util.TimePeriod;

import javax.servlet.ServletRequest;

/**
 * @author Roman Elizarov
 */
class RequestUtil {
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
