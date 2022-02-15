package com.rb.nonbiz.io;

import com.rb.nonbiz.text.RBLog;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

import static com.rb.nonbiz.text.RBLog.rbLog;

/**
 * This abstracts away the writing of a file, which is a low-level operation that's hard to mock in tests otherwise.
 */
public class FileToBufferedWriter {

  private static final RBLog log = rbLog(FileToBufferedWriter.class);

  public Optional<BufferedWriter> getBufferedWriter(String filename) {
    try {
      return Optional.of(Files.newBufferedWriter(Paths.get(filename)));
    } catch (IOException e) {
      // We used to do e.printStackTrace() but that can look as a bona fide unexpected error when we look at the logs,
      // even though the fact that we return Optional.empty here means that the semantics are such that we are
      // expected to recover from this, i.e. that it's not a real error.
      log.warn(e.toString());
      return Optional.empty();
    }
  }

}
