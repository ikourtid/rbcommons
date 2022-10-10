package com.rb.nonbiz.text;

import com.rb.nonbiz.collections.RBMap;
import com.rb.nonbiz.util.RBMapPreconditions;

import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Stream;

import static com.rb.nonbiz.collections.RBMapConstructors.rbMapFromCollection;
import static com.rb.nonbiz.collections.RBMapConstructors.rbMapFromStream;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.emptyRBMap;
import static com.rb.nonbiz.text.Strings.formatMap;
import static java.util.Comparator.comparing;

/**
 * A set of items (of the same type {@code <V>}) that implement {@code HasUniqueId<V>},
 * which can be retrieved just by their {@link UniqueId}.
 *
 * <p> This is useful in cases where the values have unique IDs, and we want to key things off of those IDs.
 * It is particularly useful when the items (V) themselves do not implement equals (which we normally like to avoid)
 * and are thus not indexable in any way other than plain pointers. </p>
 */
public class RBSetOfHasUniqueId<V extends HasUniqueId<V>> {

  private final RBMap<UniqueId<V>, V> rawMap;

  private RBSetOfHasUniqueId(RBMap<UniqueId<V>, V> rawMap) {
    this.rawMap = rawMap;
  }

  public static <V extends HasUniqueId<V>> RBSetOfHasUniqueId<V> rbSetOfHasUniqueId(RBMap<UniqueId<V>, V> rawMap) {
    RBMapPreconditions.checkMapKeysMatchValues(rawMap, v -> v.getUniqueId());
    return new RBSetOfHasUniqueId<>(rawMap);
  }

  public static <V extends HasUniqueId<V>> RBSetOfHasUniqueId<V> rbSetOfHasUniqueId(Collection<V> rawCollection) {
    return new RBSetOfHasUniqueId<>(rbMapFromCollection(rawCollection, v -> v.getUniqueId()));
  }

  public static <V extends HasUniqueId<V>> RBSetOfHasUniqueId<V> rbSetOfHasUniqueId(Stream<V> stream) {
    return new RBSetOfHasUniqueId<>(rbMapFromStream(stream, v -> v.getUniqueId()));
  }

  public static <V extends HasUniqueId<V>> RBSetOfHasUniqueId<V> emptyRBSetOfHasUniqueId() {
    return new RBSetOfHasUniqueId<V>(emptyRBMap());
  }

  public RBMap<UniqueId<V>, V> getRawMap() {
    return rawMap;
  }

  public Set<Entry<UniqueId<V>, V>> entrySet() {
    return rawMap.entrySet();
  }

  public Collection<V> values() {
    return rawMap.values();
  }

  public Stream<V> stream() {
    return values().stream();
  }

  public Stream<V> orderedStream() {
    return values().stream().sorted(comparing(v -> v.getUniqueId()));
  }

  public boolean isEmpty() {
    return rawMap.isEmpty();
  }

  public int size() {
    return rawMap.size();
  }

  @Override
  public String toString() {
    return formatMap(rawMap, " ");
  }

}
