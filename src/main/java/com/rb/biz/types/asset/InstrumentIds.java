package com.rb.biz.types.asset;

import com.rb.nonbiz.text.Strings;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.rb.biz.types.asset.InstrumentId.instrumentId;

/**
 * Various static methods pertaining to {@link InstrumentId} instances.
 */
public class InstrumentIds {

  public static InstrumentId parseInstrumentId(String asString) {
    try {
      return instrumentId(Long.parseLong(asString));
    } catch (NumberFormatException e) {
      throw new NumberFormatException(Strings.format("'%s' is not a valid instrument ID", asString));
    }
  }

  public static Set<InstrumentId> instrumentIdSet(Long ... ids) {
    return Stream
        .of(ids)
        .map(longId -> instrumentId(longId))
        .collect(Collectors.toSet());
  }

  public static List<InstrumentId> instrumentIdList(Long ... ids) {
    return Arrays.stream(ids)
        .map(longId -> instrumentId(longId))
        .collect(Collectors.toList());
  }

  public static List<Long> instrumentIdsToLongs(List<InstrumentId> instrumentIds) {
    return instrumentIds.stream().map(iid -> iid.asLong()).collect(Collectors.toList());
  }

  public static List<InstrumentId> instrumentLongsToIds(List<Long> instrumentLongs) {
    return instrumentLongs.stream().map(iid -> instrumentId(iid)).collect(Collectors.toList());
  }

  public static List<InstrumentId> sortedInstrumentIds(Collection<InstrumentId> instrumentIds) {
    return instrumentIds
        .stream()
        .sorted()
        .collect(Collectors.toList());
  }

  public static InstrumentId[] instrumentIdArray(Long ... ids) {
    InstrumentId[] instrumentIds = new InstrumentId[ids.length];
    for (int i = 0; i < ids.length; i++) {
      instrumentIds[i] = instrumentId(ids[i]);
    }
    return instrumentIds;
  }
}
