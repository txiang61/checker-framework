package java.util;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.checker.nullness.qual.EnsuresNonNullIf;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

// Subclasses of this interface may opt to prohibit null elements.
public interface Deque<E extends @Nullable Object> extends Queue<E> {
  public abstract void addFirst(E a1);
  public abstract void addLast(E a1);
  public abstract boolean offerFirst(E a1);
  public abstract boolean offerLast(E a1);
  public abstract E removeFirst();
  public abstract E removeLast();
  public abstract E getFirst();
  public abstract E getLast();
  public abstract @Nullable E peekFirst();
  public abstract @Nullable E peekLast();
  public abstract boolean removeFirstOccurrence(@NonNull Object a1);
  public abstract boolean removeLastOccurrence(@NonNull Object a1);
  public abstract boolean add(E a1);
  public abstract boolean offer(E a1);
  public abstract E remove();
  public abstract @Nullable E poll();
  public abstract @Nullable E pollFirst();
  public abstract @Nullable E pollLast();
  public abstract E element();
  public abstract @Nullable E peek();
  public abstract void push(E a1);
  public abstract E pop();
  public abstract boolean remove(@NonNull Object a1);
  @Pure public abstract boolean contains(@NonNull Object a1);
  @Pure public abstract int size();
  public abstract Iterator<E> iterator();
  public abstract Iterator<E> descendingIterator();
  @EnsuresNonNullIf(expression={"peek()", "peekFirst()", "peekLast()", "poll()", "pollFirst()", "pollLast()"}, result=false)
  @Pure public abstract boolean isEmpty();
}
