package org.vsegda.util;

import org.apache.commons.beanutils.Converter;

import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * @author Roman Elizarov
 */
public class IdList extends ArrayList<String> {
    public IdList(String s) {
        for (StringTokenizer st = new StringTokenizer(s, ","); st.hasMoreTokens();) {
            String token = st.nextToken();
            int i = token.indexOf('-', 1);
            long a;
            long b;
            if (i > 0) {
                a = Long.parseLong(token.substring(0, i));
                b = Long.parseLong(token.substring(i + 1));
                for (long id = a; id <= b; id++)
                    add(String.valueOf(id));
            } else {
                add(token);
            }
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
        for (String code : this) {
            long id;
            try {
                id = Long.parseLong(code);
                if (a <= b && id == b + 1)
                    b = id;
                else {
                    flushToString(sb, a, b);
                    a = b = id;
                }
            } catch (NumberFormatException e) {
                // ignore -- just add this code
                flushToString(sb, a, b);
                flushToString(sb, code);
                a = 1;
                b = 0;
            }
        }
        flushToString(sb, a, b);
        return sb.toString();
    }

    private void flushToString(StringBuilder sb, String code) {
        if (sb.length() > 0)
            sb.append(',');
        sb.append(code);
    }

    private void flushToString(StringBuilder sb, long a, long b) {
        if (a < b)
            flushToString(sb, a + "-" + b);
        else if (a == b)
            flushToString(sb, String.valueOf(a));
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
