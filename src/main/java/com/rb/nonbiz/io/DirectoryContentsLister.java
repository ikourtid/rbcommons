package com.rb.nonbiz.io;

import com.rb.nonbiz.util.RBPreconditions;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DirectoryContentsLister {

  public List<String> getSortedFilenamesInDirectory(String directory) {
    File leafDirectory = new File(directory);
    RBPreconditions.checkArgument(
        leafDirectory.isDirectory(),
        "%s should have been a directory but was not",
        directory);
    return Arrays.stream(leafDirectory.listFiles())
        .map(file -> file.getName())
        .sorted()
        .collect(Collectors.toList());
  }

}
