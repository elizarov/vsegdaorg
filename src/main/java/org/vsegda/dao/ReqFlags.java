package org.vsegda.dao;

/**
 * @author Roman Elizarov
 */
public class ReqFlags {
    private boolean hasMore;
    private boolean hasNext;

    public boolean hasMore() {
        return hasMore;
    }

    public void setHasMore() {
        this.hasMore = true;
    }

    public boolean hasNext() {
        return hasNext;
    }

    public void setHasNext() {
        this.hasNext = true;
    }
}
