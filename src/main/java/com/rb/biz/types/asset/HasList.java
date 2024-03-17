package com.rb.biz.types.asset;

import java.util.List;

/**
 * Implemented by data classes that store a list of some object.
 *
 * <p> We wanted an interface that has more methods than Iterable (which only has #iterator) and Collection
 * (which has a bunch of methods), but we couldn't find an existing one anywhere. </p>
 */
public interface HasList<T> {

  List<T> getList();

}
