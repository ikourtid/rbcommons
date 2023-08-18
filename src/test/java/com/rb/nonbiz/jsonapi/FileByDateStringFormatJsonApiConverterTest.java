package com.rb.nonbiz.jsonapi;

import com.rb.nonbiz.collections.RBVoid;
import com.rb.nonbiz.io.FileByDateStringFormat;
import com.rb.nonbiz.testutils.RBCommonsIntegrationTest;
import org.junit.Test;

import static com.rb.nonbiz.io.FileByDateStringFormat.fileByDateStringFormat;
import static com.rb.nonbiz.io.FileByDateStringFormat.fixedFilenameIgnoringDate;
import static com.rb.nonbiz.io.FileByDateStringFormatTest.fileByDateStringFormatMatcher;
import static com.rb.nonbiz.json.RBGson.jsonString;
import static com.rb.nonbiz.json.RBJsonObjectSimpleConstructors.emptyJsonObject;
import static com.rb.nonbiz.json.RBJsonObjectSimpleConstructors.jsonObject;
import static com.rb.nonbiz.json.RBJsonObjectSimpleConstructors.singletonJsonObject;
import static com.rb.nonbiz.jsonapi.JsonApiPair.jsonApiPair;
import static com.rb.nonbiz.jsonapi.JsonApiTestData.jsonApiTestData;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;

public class FileByDateStringFormatJsonApiConverterTest
    extends RBCommonsIntegrationTest<FileByDateStringFormatJsonApiConverter> {

  @Test
  public void fromJsonObject_hasNeitherProperty_throws() {
    assertIllegalArgumentException( () -> makeRealObject().fromJsonObject(emptyJsonObject()));
  }

  @Test
  public void fromJsonObject_hasBothProperties_throws() {
    assertIllegalArgumentException( () -> makeRealObject().fromJsonObject(
        jsonObject(
            "fileFormatParameterizedByDate", jsonString("folder1/prefix1.%s.csv"),
            "fixedFilenameIgnoringDate", jsonString("folder2/prefix2.csv"))));
  }

  @Test
  public void testRoundTripConversions() {
    // This is a test, so we can just use RBVoid. The generic argument is really just a semantic clarification so
    // the reader of the code knows that the FileByDateStringFormat is for loading an object of a certain type.
    JsonApiTestData<FileByDateStringFormat<RBVoid>> jsonApiTestData = jsonApiTestData(
        f -> fileByDateStringFormatMatcher(f),

        jsonApiPair(
            fileByDateStringFormat("folder1/prefix1.%s.csv"),
            singletonJsonObject(
                "fileFormatParameterizedByDate", jsonString("folder1/prefix1.%s.csv"))),

        jsonApiPair(
            fixedFilenameIgnoringDate("folder2/prefix2.csv"),
            singletonJsonObject(
                "fixedFilenameIgnoringDate", jsonString("folder2/prefix2.csv"))));


    jsonApiTestData.testRoundTripConversions(
        v -> makeRealObject().toJsonObject(v),
        v -> makeRealObject().fromJsonObject(v));
  }

  @Override
  protected Class<FileByDateStringFormatJsonApiConverter> getClassBeingTested() {
    return FileByDateStringFormatJsonApiConverter.class;
  }

}
