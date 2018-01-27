package org.vsegda.factory;

import javax.servlet.*;
import java.io.IOException;

/**
 * @author Roman Elizarov
 */
public class PersistenceManagerFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // nothing special here
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        try {
            DS.start();
            filterChain.doFilter(servletRequest, servletResponse);
            DS.commit();
        } finally {
            DS.close();
        }
    }

    @Override
    public void destroy() {
        // nothing special here
    }
}