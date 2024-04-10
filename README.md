# Rowboat Commons Library

Built on top of Google Guava and Apache Commons, this includes core infrastructure classes
used in the Rowboat Advisors Portfolio Optimizer and Tax-Aware Backtester.

However, most code is not domain-specific, such as our immutable collections.

Built since March 2016, As of April 2024, there are 115,000+ lines of source code; 65,000+ of them are tests. 
So this is heavily tested.

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
* Unit-testing functionality

### General
* Null avoidance via helper methods on Optional.

---

## Highlights

Follow the detailed Javadoc documentation (linked above) for more.

However, we will call out some highlights below, in decreasing order of how useful we think a general developer
(i.e. without our specific business focus) would find these.

### Mutable/Immutable collections that avoid null

Java is not great when it comes to immutable classes. ```final``` is not as expressive as C++,
so it only prevents you from changing the reference (pointer) to an object, but it will still let
you modify its contents if methods. Java 14 (and later) Records address this somewhat, but not perfectly.

Let's take the widely used ```Map``` interface. Google's Guava library has ```ImmutableMap```, which 
throws an runtime exception if you try to change its contents via its ```put()``` method. 
However, an even safer approch is not to have a ```put()``` method at all. It gives you compile-time safety.

Another issue with ```Map``` is that its semantics rely on null, which is frowned upon by modern software 
development practices (for good reason!). For example, ```get()``` returns null if an item is not present,
which means the caller has to check explicitly.

Our solution is to implement two classes, ```MutableRBMap``` and ```RBMap```.

```MutableRBMap``` is clearly mutable; it's in the name. In fact, our data classes are immutable across the board
consistently, so our naming pattern has to explicitly call out cases of immutability. Moreover, unlike ```RBMap```,
it has several variants of the ```Map#put()``` method with cleaner semantics:

* ```putAssumingAbsent``` throws an exception if you are trying to overwrite a key. This is rarely the desired behavior.  	
* ```putAssumingAbsentAllowingNullValue``` allows nulls, which is uncommon.
* ```putAssumingNoChange``` takes in a predicate that compares the old and new value being added under an existing key;
this is useful for epsilon comparisons.
* ``` putAssumingPresent``` throws an exception if the key does not already have a value.
* ```putIfAbsent``` only adds a key/value pair, but does nothing if the key already maps to a value.
* ```putOrModifyExisting``` avoids the 'if empty then modify object, else add object' type of logic, and lets you
pass in a lambda.

You could say 'if this is so good, why doesn't Java already do it?'. Well, our codebase optimizes for safety, even 
at a slight cost to performance. That's not practical to do with language-standard map classes, because some users
want the extra performance.

This repo has several general collection classes that follow this MutableXYZ / XYZ pattern. We almost never pass
a MutableXYZ as an argument, except possibly inside a private method - and even that is rare. Instead, there is 
often a single method that instantiates a MutableXYZ, fills in its contents, and returns an XYZ. From that point on,
there's no way to change XYZ's contents.

The main collection classes that implement this are ```RBMap``` / ```RBSet``` and ```IidMap``` / ```IidSet``` (see below).


### Performance-optimized collections

For performance, certain data are effectively a Java long. We have created wrappers around GNU Trove, a
set of collections (maps and sets, mostly) optimized for memory and performance for the cases where the key is a primitive.


### 1-, 2-, and 3-dimensional indexable arrays

A 2-d array is indexable by two indices that start with 0. 
It is sometimes convenient to index by some other unique keys. For example, you may want to store stock returns
y unique numeric instrument ID (roughly equivalent to a stock ticker) and date. You could use a map of maps,
but that is not guaranteed to be 'rectangular' like a 2d array, and it would also incur the overhead of having 
multiple maps, one per row (or column).
Because our data is almost always immutable, this can be modeled by a 2d array (to store the actual data in a 
compacted form) and two maps of (date) -> (row index) and (instrument id) -> (column index). This results in space
savings. Plus, the semantics are clearer, because the data type tells you you are dealing with rectangular 2d data.


### Compact daily time series

A common scenario is having one item per consecutive day when the market is open. This concept of 'market' is general,
and can include all weekdays, all calendar days, etc.


### Eager transformations
Our collections have many 'eager' transformation methods (in the software sense, i.e. as opposed to 'lazy').
These are similar to e.g. Guava Maps.transform, except:
* eagerness eliminates an entire class of bugs where the underlying object is changed after the lazy transformation has
been created. This is very unlikely in our code in the first place, because we use immutable classes throughout, but
it's a belt-and-suspenders extra safe approach.
* they apply to our own data structures - e.g. ```RBMap```.

Again, the focus is on clarity and reducing bugs over performance.


### Unit testing infrastructure

Almost all our classes are in one of two categories:
* Plain-old Java objects (POJOs) that store data. We also call these 'noun classes' and 'data classes'.
* 'Verb classes' which have code that operates on the data. Verb classes are injected into other verb classes, using
Google Guice dependency injection. This greatly aids testing.

A common need when unit testing is 'instantiate a dummy object for class XYZ'. Almost all of our unit test for
data classes extend ```RBTestMatcher```. It forces you to implement 3 instantiation methods: a trivial object
(e.g. for a collection class, that would be an empty collection), a non-trivial one, and another non-trivial one that is
epsilon-different (when applicable) than the non-trivial one. You also have to create a ```TypeSafeMatcher```.
This is a more general notion than hashCode / equals; for example, it allows for epsilon comparisons.

Then, ```RBTestMatcher``` gives you some unit tests 'for free': it tests that your ```TypeSafeMatcher``` works
correctly: 
* each one of the (trivial, non-trivial, matching non-trivial) objects must match itself
* the trivial one must not match the non-trivial ones
* the non-trivial and matching non-trivial must match each other.

By forcing you to implement ```TypeSafeMatcher```s throughout, it is possible to nest matchers in a very legible way:

```
  public static TypeSafeMatcher<Orders> ordersMatcher(Orders expected, MatcherEpsilons e) {
    return makeMatcher(expected,
        match(    v -> v.getBuyOrders(),  f -> buyOrdersMatcher(f, e)),
        match(    v -> v.getSellOrders(), f -> sellOrdersMatcher(f, e)));
  }
```
This utilizes some additional syntactic sugar we have created (```makeMatcher```, ```match```) to create a succinct
way of saying: "for two Orders objects to match subject to epsilons, both the buy orders and sell orders must match
using those same epsilons".

That, in turn, greatly facilitates adding single-statement tests. Here is example, although not from the codebase.
It says 'regardless of starting portfolio, if trading is disabled, do not generate any orders':

```
   assertThat(
       makeTestObject() // this constructs a verb class that makes the trading decision
           .generateOrders(
               new PortfolioTest().makeDummyObject(),
               TradingStatus.DISABLED),
       ordersMatcher(
           emptyOrders(),
           emptyMatcherEpsilons());
```

Another example. Imagine an integration test that calculates orders based on specific inputs, and you expect it to
buy 1.1 shares of AAPL and 2.2 shares of GOOG:

```
    assertThat(
        generateActualOrders(...), // some code that is expected to generate orders for an initial investment
        ordersMatcher(
            orders(
                testBuyOrders(
                    buyOrder(I_AAPL, buyQuantity(1.1), price(11.1), LocalDateTime.of(1974, 4, 4, 9, 30, 0))
                    buyOrder(I_GOOG, buyQuantity(2.2), price(22.2), LocalDateTime.of(1974, 4, 4, 9, 30, 0))),
                emptySellOrders())));
```

This is better than asserting ```equals()``` on the result, because it allows for epsilon-based equality.

The test is more succinct than comparing results one item at a time (e.g. first compare buy orders, then sell orders).
Moreover, it is more robust: if more data get added to the Orders class later, you just update 
```OrdersTest#ordersMatcher```, and then all the tests that use ```ordersMatcher``` will automatically perform the 
stronger checks.

---

There is a lot more documentation in the Javadoc.
