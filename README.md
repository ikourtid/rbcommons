# Rowboat Commons
Built on top of Google Guava and Apache Commons, 

## Summary

### Collections
* <strong>Immutable maps and sets</strong> (<em>compile-time</em>, not just at runtime)
* <strong>Performance-optimized collections</strong> for classes exposing numeric long keys (utilizes <em>GNU Trove</em>)
* <Strong>Contiguous</strong> ```RangeMap```; e.g. [2, 3) maps to "a", [3, 8) maps to "b", etc.
* Scala-like ```Either```, ```EitherOrBoth```, ```EitherOrNeither```
* <strong>Indexable immutable arrays</strong>: compact array storage but map-like key-based lookup
* Java-generic <strong>partitions</strong> with weights summing to 100% + support to splice in & remove items

### JSON
* Fluent extensions of <strong>Google GSON</strong>
* <strong>JSON converters</strong> for all data classes above - clearer than GSON default conversions.

### Math
* Support for arithmetic & geometric progressions and numeric sequences.
* Fluent extensions of matrices (CERN's Colt library)

### Contents - Domain-specific
* Instrument master
* BigDecimal-based typesafe wrappers (e.g. ```SignedMoney```, ```BuyQuantity```)
* <strong>Daily time series</strong> for contiguous market-open days

### Other
* <strong>Quick-and-dirty IO</strong>
* Extensions on Guava <strong>preconditions</strong>
* Testing 

### General
* Null avoidance
* 