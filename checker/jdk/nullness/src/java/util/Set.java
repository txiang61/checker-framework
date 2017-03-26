package java.util;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.nullness.qual.PolyNull;

// Subclasses of this interface may opt to prohibit null elements.
public interface Set<E extends @Nullable Object> extends Collection<E> {
  @Pure public abstract int size();
  @Pure public abstract boolean isEmpty();
  @Pure public abstract boolean contains(@NonNull Object a1);
  public abstract Iterator<E> iterator();
  public abstract Object [] toArray();
  public abstract <T> @Nullable T @PolyNull [] toArray(T @PolyNull [] a1);
  public abstract boolean add(E a1);
  public abstract boolean remove(@NonNull Object a1);
  @Pure public abstract boolean containsAll(Collection<?> a1);
  public abstract boolean addAll(Collection<? extends E> a1);
  public abstract boolean retainAll(Collection<?> a1);
  public abstract boolean removeAll(Collection<?> a1);
  public abstract void clear();
  @Pure public abstract boolean equals(@Nullable Object a1);
  @Pure public abstract int hashCode();
}
