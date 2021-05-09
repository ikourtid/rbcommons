package com.rb.biz.marketdata.instrumentmaster;

import com.google.common.collect.Range;
import com.rb.biz.types.Symbol;
import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.collections.MutableRBMap;
import com.rb.nonbiz.collections.Pair;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;
import static com.rb.biz.marketdata.instrumentmaster.InstrumentMasterIndexBySymbol.instrumentMasterIndexBySymbol;
import static com.rb.nonbiz.collections.MutableRBMap.newMutableRBMap;
import static com.rb.nonbiz.collections.NonContiguousRangeMap.nonContiguousRangeMap;
import static com.rb.nonbiz.collections.Pair.pair;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.newRBMap;
import static java.util.Comparator.comparing;

/**
 * Creates an object that makes it efficient to do the reverse lookup of symbol + date to instrument ID.
 */
public class InstrumentMasterIndexerBySymbol {

  public InstrumentMasterIndexBySymbol generateIndex(
      AllUnindexedInstrumentMasterData allUnindexedInstrumentMasterData) {
    MutableRBMap<Symbol, List<Pair<Range<LocalDate>, InstrumentId>>> intermediateMap = newMutableRBMap();
    allUnindexedInstrumentMasterData.getMasterDataMap().values()
        .forEach(singleInstrumentMasterData ->
            singleInstrumentMasterData.getDateToData().getUnderlyingMap().getRawRangeMap().asMapOfRanges()
                .forEach( (range, singlePeriodData) -> {
                  Symbol symbol = singlePeriodData.getSymbol();
                  Pair<Range<LocalDate>, InstrumentId> pair = pair(range, singlePeriodData.getInstrumentId());
                  Optional<List<Pair<Range<LocalDate>, InstrumentId>>> list = intermediateMap.getOptional(symbol);
                  if (!list.isPresent()) {
                    intermediateMap.putAssumingAbsent(symbol, newArrayList(pair));
                  } else {
                    list.get().add(pair);
                  }
                }));
    return instrumentMasterIndexBySymbol(newRBMap(intermediateMap).transformValuesCopy(list -> {
      List<Pair<Range<LocalDate>, InstrumentId>> sortedPairs = list
          .stream()
          .sorted(comparing(pair -> pair.getLeft().lowerEndpoint()))
          .collect(Collectors.toList());
      return nonContiguousRangeMap(
          sortedPairs.stream().map(pair -> pair.getLeft()).collect(Collectors.toList()),
          sortedPairs.stream().map(pair -> pair.getRight()).collect(Collectors.toList()));
    }));
  }


}
