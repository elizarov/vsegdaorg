package org.vsegda.request;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.vsegda.util.IdList;
import org.vsegda.util.TimeInstant;
import org.vsegda.util.TimePeriod;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * @author Roman Elizarov
 */
abstract class AbstractRequest {
    static {
        ConvertUtils.register(new IdList.Cnv(), IdList.class);
        ConvertUtils.register(new TimeInstant.Cnv(), TimeInstant.class);
        ConvertUtils.register(new TimePeriod.Cnv(), TimePeriod.class);
    }

    private List<String> keyValues;

    protected void init(HttpServletRequest req) {
        if (keyValues != null)
            throw new IllegalStateException("init allowed only once");
        // populate first
        try {
            Map<String, String[]> params = (Map<String, String[]>) req.getParameterMap();
            Map<String, String> populate = new LinkedHashMap<String, String>();
            for (Map.Entry<String, String[]> entry : params.entrySet()) {
                String[] value = entry.getValue();
                if (value.length > 0)
                    populate.put(entry.getKey(), value[0]);
            }
            BeanUtils.populate(this, populate);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
        // then keep keyValues as simple strings in order
        keyValues = new ArrayList<String>();
        String queryString = req.getQueryString();
        if (queryString != null)
            Collections.addAll(keyValues, queryString.split("&"));
    }

    public String getQueryString() {
        StringBuilder sb = new StringBuilder();
        for (String keyValue : keyValues) {
            if (sb.length() > 0)
                sb.append("&");
            sb.append(keyValue);
        }
        return sb.toString();
    }

    protected void updateQueryString(String key, String value) {
        if (keyValues == null)
            return;
        String eq = key + "=";
        for (int i = 0; i < keyValues.size(); i++) {
            String keyValue = keyValues.get(i);
            if (keyValue.startsWith(eq)) {
                keyValues.set(i, eq + value);
                return;
            }
        }
        keyValues.add(eq + value);
    }

    @Override
    public String toString() {
        return String.valueOf(keyValues);
    }
}
