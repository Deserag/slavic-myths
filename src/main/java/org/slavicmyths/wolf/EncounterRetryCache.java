package org.slavicmyths.wolf;

import java.util.LinkedHashMap;

/** Transient, bounded retry deadlines; reaching capacity never disables future exploration. */
public final class EncounterRetryCache {
    private final LinkedHashMap<Long,Long> next = new LinkedHashMap<>();
    private final int capacity;
    public EncounterRetryCache(int capacity) { if(capacity<1)throw new IllegalArgumentException();this.capacity=capacity; }
    public boolean attempt(long key,long now) {
        Long deadline=next.get(key);
        if(deadline!=null&&now<deadline)return false;
        next.remove(key);
        next.put(key,now+600);
        if(next.size()>capacity)next.remove(next.keySet().iterator().next());
        return true;
    }
    public int size(){return next.size();}
}
