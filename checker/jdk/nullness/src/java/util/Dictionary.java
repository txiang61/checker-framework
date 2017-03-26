package java.util;
import org.checkerframework.dataflow.qual.Pure;

import org.checkerframework.checker.nullness.qual.Nullable;

// This class prohibits null keys and values.
public abstract class Dictionary<K extends @NonNull Object, V extends @NonNull Object> {
  public Dictionary() { throw new RuntimeException("skeleton method"); }
  @Pure public abstract int size();
  @Pure public abstract boolean isEmpty();
  public abstract Enumeration<K> keys();
  public abstract Enumeration<V> elements();
  @Pure public abstract @Nullable V get(@NonNull Object a1);
  public abstract @Nullable V put(K a1, V a2);
  public abstract @Nullable V remove(@NonNull Object a1);
}
