package org.checkerframework.dataflow.analysis;

import java.util.HashSet;
import java.util.Set;

public class StoreSet<S extends Store<S>> {

    private S singleStore;

    private Set<S> stores;

    public StoreSet() {
        this.stores = new HashSet<S>();
    }

    public StoreSet(S store) {
        this.stores = new HashSet<S>();
        this.stores.add(store);
    }

    public void add(S store) {
        this.stores.add(store);
    }

    public StoreSet(Set<S> stores) {
        this.stores = stores;
    }

    public Set<S> getStores() {
        return stores;
    }

    public static enum Kind {
        THEN,
        ELSE,
        BOTH
    }

    public static enum FlowRule {
        EACH_TO_EACH, // The normal case, then store flows to the then store
        // and else store flows to the else store.
        THEN_TO_BOTH, // Then store flows to both then and else of successor.
        ELSE_TO_BOTH, // Else store flows to both then and else of successor.
        THEN_TO_THEN, // Then store flows to the then of successor.  Else store is ignored.
        ELSE_TO_ELSE, // Else store flows to the else of successor.  Then store is ignored.
        THEN_TO_BACK, // Then store flows to the beginning of the loop.
    }

    public boolean allInactive() {
        for (S s : stores) {
            if (s.isActive()) return false;
        }
        return true;
    }

    public StoreSet<S> copy() {
        Set<S> copy = new HashSet<>();
        for (S s : stores) {
            copy.add(s.copy());
        }
        return new StoreSet<>(copy);
    }

    public StoreSet<S> leastUpperBound(StoreSet<S> other) {
        if (allInactive() && other.allInactive()) {
            Set<S> newStores = new HashSet<>(getStores());
            newStores.addAll(other.getStores());
            return new StoreSet<>(newStores);
        }

        Set<S> newStores = new HashSet<>();
        for (S s : getStores()) {
            if (s.isActive()) newStores.add(s);
        }
        for (S s : other.getStores()) {
            if (s.isActive()) newStores.add(s);
        }
        return new StoreSet<>(newStores);
    }

    public StoreSet<S> widenedUpperBound(StoreSet<S> previoous) {
        return leastUpperBound(previoous);
    }

    public S getLeastUpperBoundofAllStores() {
        S store = null;
        for (S s : stores) {
            if (store == null) {
                store = s;
            }
            store = store.leastUpperBound(s);
        }
        return store;
    }
}
