package com.rb.nonbiz.math.eigen;

import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBPreconditions;

import static com.rb.nonbiz.math.eigen.Eigenvector.emptyEigenvector;

/**
 * There's already a protobuf code-generated class that captures the notion of an eigenpair.
 * This class is a developer-friendlier equivalent.
 * It also adds some sanity-checking in its static constructor.
 */
public class Eigenpair {

  private final Eigenvalue eigenvalue;
  private final Eigenvector eigenvector;

  private Eigenpair(Eigenvalue eigenvalue, Eigenvector eigenvector) {
    this.eigenvalue = eigenvalue;
    this.eigenvector = eigenvector;
  }

  public static Eigenpair eigenpair(Eigenvalue eigenvalue, Eigenvector eigenvector) {
    RBPreconditions.checkArgument(
        eigenvector.size() > 0,
        "Encountered eigenvector with length 0! Eigenvalue was %s",
        eigenvalue);
    return new Eigenpair(eigenvalue, eigenvector);
  }

  /**
   * As a space optimization, we may decide to only store the eigenvalue and not otherwise
   * bother storing the eigenvector into memory. We are not using it anywhere currently (Sep 2017).
   * Hence the 'unsafe' in the name here. This is a hack.
   */
  public static Eigenpair eigenpairUnsafe(Eigenvalue eigenvalue) {
    return new Eigenpair(eigenvalue, emptyEigenvector());
  }

  public Eigenvalue getEigenvalue() {
    return eigenvalue;
  }

  public Eigenvector getEigenvector() {
    return eigenvector;
  }

  @Override
  public String toString() {
    return Strings.format("eigenvalue= %s ; eigenvector= %s",
        eigenvalue, eigenvector);
  }

}
