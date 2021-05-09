package com.rb.biz.types.asset;

import java.util.List;

/**
 * We wanted an interface that has more methods than Iterable (which only has #iterator) and Collection
 * (which has a bunch of methods), but we couldn't find an existing one anywhere.
 */
public interface HasList<T> {

  List<T> getList();

}
