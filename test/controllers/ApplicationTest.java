package controllers;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ApplicationTest {

  @Test
  public void testBuildDefinitions() throws IOException {
    JsonNode input = new ObjectMapper().readTree(new File("test/resources/simple-test.json"));
    ObjectNode output = JsonNodeFactory.instance.objectNode();
    String rootName = "low_fare_search_response";

    Application.buildDefinitions(output, input, rootName);

    System.out.println(output);
    assertTrue(output.has(rootName));
  }

}
