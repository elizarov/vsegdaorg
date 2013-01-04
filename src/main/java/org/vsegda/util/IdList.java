package org.vsegda.util;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.Converter;

import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * @author Roman Elizarov
 */
public class IdList extends ArrayList<Long> {
    public IdList(String s) {
        for (StringTokenizer st = new StringTokenizer(s, ","); st.hasMoreTokens();) {
            String token = st.nextToken();
            int i = token.indexOf('-', 1);
            long a;
            long b;
            if (i > 0) {
                a = Long.parseLong(token.substring(0, i));
                b = Long.parseLong(token.substring(i + 1));
            } else {
                a = b = Long.parseLong(token);
            }
            for (long id = a; id <= b; id++)
                add(id);
        }
    }

    public boolean isSingleton() {
        return size() == 1;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        long a = 1;
        long b = 0;
        for (long id : this) {
            if (a <= b) {
                if (id == b + 1)
                    b = id;
                else {
                    flushToString(sb, a, b);
                    a = b = id;
                }
            }
        }
        if (a <= b)
            flushToString(sb, a, b);
        return sb.toString();
    }

    private void flushToString(StringBuilder sb, long a, long b) {
        if (sb.length() > 0)
            sb.append(',');
        if (a < b)
            sb.append(a).append('-').append(b);
        else
            sb.append(a);
    }

    public static class Cnv implements Converter {
        public Object convert(Class clazz, Object o) {
            if (o == null)
               return null;
            String s = o.toString();
            if (s == null)
               return null;
            return new IdList(s);
        }
    }
}
