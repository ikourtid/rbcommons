package com.rb.nonbiz.types;

/**
 * A unique numeric ID for a class T.
 *
 * This is similar to {@code UniqueId<T>}, except that it's numeric instead of string. Although this software layer
 * is not tied to databases, in practice databases will use a number as an ID, because it's more efficient
 * performance-wise than a string ID. So the idea is that if e.g. an Account has a string ID in the broker
 * (e.g. what shows in the trade confirmations or monthly statements), there will be an Account object that has that
 * information, but its ID internally in our system will be a number.
 *
 * This will play well with any object-relational mapping layers such as Hibernate, but even with non-RDBMS cloud
 * databases, this will be useful.
 *
 * The semantics of uniqueness are left a tiny bit unspecified: for example, is a subaccount ID globally unique,
 * or just unique within the account it lives in? In practice, if an RBLongId really ends up representing some DB
 * ID, then it will likely be globally unique. But don't make that assumption here; this will be on a case-by-case
 * basis.
 *
 * Note again that there's nothing explicitly DB-related about RBLongId. It's just a unique ID that happens to look
 * like a number instead of string.
 *
 * Note also that RBLongId is an actual class, not an interface. But it does implement HasLongRepresentation.
 * The classes that represent specific IDs, such as RBAccountId, should extend RBLongId.
 */
public abstract class RBLongId<T> implements HasLongRepresentation {

  private final long rawId;

  protected RBLongId(long rawId) {
    this.rawId = rawId;
  }

  @Override
  public long asLong() {
    return rawId;
  }

  public String asString() {
    return Long.toString(asLong());
  }

  // IDE-generated
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    RBLongId<?> that = (RBLongId<?>) o;

    return rawId == that.rawId;
  }

  @Override
  public int hashCode() {
    // Integer.hashCode just returns the int itself. Makes sense; the hashcode doesn't make things more random.
    // Let's do it here as well.
    return (int) rawId;
  }

}
