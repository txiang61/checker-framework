package org.checkerframework.dataflow.analysis;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import javax.lang.model.type.TypeMirror;

/**
 * Implementation of a {@link TransferResult} with just one non-exceptional store. The result of
 * {@code getThenStore} and {@code getElseStore} is equal to the only underlying store.
 *
 * @param <S> the {@link Store} used to keep track of intermediate results
 */
public class RegularTransferResult<A extends AbstractValue<A>, S extends Store<S>>
        extends TransferResult<A, S> {

    /** The regular result store. */
    protected final Set<S> store;

    private final boolean storeChanged;

    /**
     * Create a {@code TransferResult} with {@code resultStore} as the resulting store. If the
     * corresponding {@link org.checkerframework.dataflow.cfg.node.Node} is a boolean node, then
     * {@code resultStore} is used for both the 'then' and 'else' edge.
     *
     * <p><em>Exceptions</em>: If the corresponding {@link
     * org.checkerframework.dataflow.cfg.node.Node} throws an exception, then it is assumed that no
     * special handling is necessary and the store before the corresponding {@link
     * org.checkerframework.dataflow.cfg.node.Node} will be passed along any exceptional edge.
     *
     * <p><em>Aliasing</em>: {@code resultStore} is not allowed to be used anywhere outside of this
     * class (including use through aliases). Complete control over the object is transfered to this
     * class.
     */
    public RegularTransferResult(A value, Set<S> resultStore, boolean storeChanged) {
        this(value, resultStore, null, storeChanged);
    }

    public RegularTransferResult(A value, Set<S> resultStore) {
        this(value, resultStore, false);
    }

    /**
     * Create a {@code TransferResult} with {@code resultStore} as the resulting store. If the
     * corresponding {@link org.checkerframework.dataflow.cfg.node.Node} is a boolean node, then
     * {@code resultStore} is used for both the 'then' and 'else' edge.
     *
     * <p>For the meaning of storeChanged, see {@link
     * org.checkerframework.dataflow.analysis.TransferResult#storeChanged}.
     *
     * <p><em>Exceptions</em>: If the corresponding {@link
     * org.checkerframework.dataflow.cfg.node.Node} throws an exception, then the corresponding
     * store in {@code exceptionalStores} is used. If no exception is found in {@code
     * exceptionalStores}, then it is assumed that no special handling is necessary and the store
     * before the corresponding {@link org.checkerframework.dataflow.cfg.node.Node} will be passed
     * along any exceptional edge.
     *
     * <p><em>Aliasing</em>: {@code resultStore} and any store in {@code exceptionalStores} are not
     * allowed to be used anywhere outside of this class (including use through aliases). Complete
     * control over the objects is transfered to this class.
     */
    public RegularTransferResult(
            A value,
            Set<S> resultStore,
            Map<TypeMirror, S> exceptionalStores,
            boolean storeChanged) {
        super(value, exceptionalStores);
        this.store = resultStore;
        this.storeChanged = storeChanged;
    }

    public RegularTransferResult(
            A value, Set<S> resultStore, Map<TypeMirror, S> exceptionalStores) {
        this(value, resultStore, exceptionalStores, false);
    }

    @Override
    public Set<S> getRegularStores() {
        return store;
    }

    @Override
    public Set<S> getThenStores() {
        return store;
    }

    @Override
    public Set<S> getElseStores() {
        // copy the store such that it is the same as the result of getThenStore
        // (that is, identical according to equals), but two different objects.
        return new HashSet<S>(store);
    }

    @Override
    public boolean containsTwoStores() {
        return false;
    }

    @Override
    public String toString() {
        StringJoiner result = new StringJoiner(System.lineSeparator());
        result.add("RegularTransferResult(");
        result.add("  resultValue = " + resultValue);
        result.add("  store = " + store);
        result.add(")");
        return result.toString();
    }

    /** @see org.checkerframework.dataflow.analysis.TransferResult#storeChanged() */
    @Override
    public boolean storeChanged() {
        return storeChanged;
    }
}
