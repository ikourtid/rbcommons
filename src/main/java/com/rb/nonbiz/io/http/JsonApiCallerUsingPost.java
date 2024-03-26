package com.rb.nonbiz.io.http;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rb.nonbiz.util.RBPreconditions;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URI;

/**
 * Makes an HTTP POST request to the specified URI, passing in the specified {@link JsonObject} in the POST request
 * contents. You can do this using by passing in a {@link CloseableHttpClient}, which we will not auto-close
 * (to allow for reuse).
 */
public class JsonApiCallerUsingPost {

  public JsonObject makeCall(URI uri, JsonObject jsonInputs) {
    try (CloseableHttpClient client = HttpClients.createDefault()) {
      return makeCall(client, uri, jsonInputs);
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  public JsonObject makeCall(CloseableHttpClient client, URI uri, JsonObject jsonInputs) {
    try {
      HttpPost httpPost = new HttpPost(uri);

      httpPost.setEntity(new StringEntity(jsonInputs.toString()));
      httpPost.setHeader("Accept", "application/json");
      httpPost.setHeader("Content-type", "application/json");

      try (CloseableHttpResponse response = client.execute(httpPost)) {
        RBPreconditions.checkArgument(
            response.getStatusLine().getStatusCode() == 200,
            "Bad HTTP POST response status of %s ; expected 200",
            response.getStatusLine().getStatusCode());
        String result = EntityUtils.toString(response.getEntity());
        return JsonParser.parseString(result).getAsJsonObject();
      } catch (IOException e) {
        e.printStackTrace();
        throw new RuntimeException(e);
      }
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

}