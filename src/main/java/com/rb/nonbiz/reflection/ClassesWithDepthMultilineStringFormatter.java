package com.rb.nonbiz.reflection;

import com.rb.nonbiz.collections.MutableRBMap;
import com.rb.nonbiz.text.Strings;
import org.apache.commons.lang3.StringUtils;

import static com.rb.nonbiz.collections.MutableRBMap.newMutableRBMap;

/**
 * Formats the {@link UniqueClassesWithDepth} into a nice multiline string with indentations for different depths,
 * depending on how deep in a hierarchy a class appears.
 */
public class ClassesWithDepthMultilineStringFormatter {

  public String format(UniqueClassesWithDepth uniqueClassesWithDepth, String tab) {
    StringBuilder sb = new StringBuilder();
    MutableRBMap<Integer, String> tabByDepth = newMutableRBMap(); // caching these just to be a bit paranoid about speed

    uniqueClassesWithDepth.getRawList().forEach(classWithDepth -> {
      Class<?> clazz = classWithDepth.getClassObject();
      int depth = classWithDepth.getDepth();
      if (!tabByDepth.containsKey(depth)) {
        tabByDepth.putAssumingAbsent(depth, StringUtils.repeat(tab, depth));
      }
      sb.append(Strings.format("%s%s\n", tabByDepth.getOrThrow(depth), clazz.getName()));
    });
    return sb.toString();
  }

}
