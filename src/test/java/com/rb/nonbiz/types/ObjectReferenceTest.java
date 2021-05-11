package com.rb.nonbiz.types;

import org.junit.Test;

import static com.rb.nonbiz.types.ObjectReference.objectReference;
import static org.junit.Assert.assertEquals;

public class ObjectReferenceTest {

  @Test
  public void happyPath_initializedPointer() {
    ObjectReference<String> objectReference = objectReference("abc");
    assertEquals("abc", objectReference.get());
    objectReference.set("xyz");
    assertEquals("xyz", objectReference.get());
  }

}