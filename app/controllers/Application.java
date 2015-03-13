package controllers;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import play.mvc.Controller;
import play.mvc.Result;
import views.html.index;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class Application extends Controller {

  private static final ObjectMapper JSON = new ObjectMapper();

  public static Result index() {
    return ok(index.render("Please put a JSON response for which you would like to generate a model definition below"));
  }

  public static Result generateDefinition() {
    Result out;
    try {
    Map<String, String[]> inputForm = request().body().asFormUrlEncoded();
      ObjectNode outputJSON = processInput(inputForm);
      out = ok(outputJSON);
    }
    catch (JsonProcessingException e) {
      e.printStackTrace();
      out = badRequest(index.render(e.getMessage()));
    }
    catch (IOException e) {
      e.printStackTrace();
      out = internalServerError();
    }
    return out;
  }

  public static ObjectNode processInput(Map<String, String[]> inputForm) throws IOException,
      JsonProcessingException {
    String inputText = inputForm.get("inputJSON")[0];
    JsonNode input = JSON.reader().readTree(inputText);
    ObjectNode outputJSON = JsonNodeFactory.instance.objectNode();
    ObjectNode definitions = outputJSON.objectNode();
    buildDefinitions(definitions, input, inputForm.get("rootNodeName")[0]);
    outputJSON.put("definitions", definitions);
    return outputJSON;
  }

  static void buildDefinitions(ObjectNode defintions, JsonNode input, String nodeName) {
    ObjectNode def = defintions.objectNode();
    ObjectNode props = def.objectNode();

    if (input.isArray()) { // Super-lazy mode, look only at first entry!
      input = input.iterator().next();
    }

    for (Iterator<Entry<String, JsonNode>> fieldsIt = input.fields(); fieldsIt.hasNext();) {
      Entry<String, JsonNode> entry = fieldsIt.next();
      String key = entry.getKey();
      if (!defintions.has(key)) {
        ObjectNode propEntry = buildPropertyWithDefinitions(props, entry, defintions);
        props.put(key, propEntry);
      }
    }

    def.put("properties", props);
    if (!defintions.has("nodeName")) {
      defintions.put(nodeName, def);
    }
  }

  private static ObjectNode buildPropertyWithDefinitions(ObjectNode props, Entry<String, JsonNode> entry,
      ObjectNode defintions) {
    ObjectNode propEntry = props.objectNode();
    String key = entry.getKey();
    JsonNode val = entry.getValue();
    if (val.isObject()) {
      buildDefinitions(defintions, val, key);
      propEntry.put("type", key);
    }
    else if (val.isArray()) {
      propEntry.put("type", "array");
      ObjectNode ref = propEntry.objectNode();
      for (JsonNode item : val) {
        buildDefinitions(defintions, item, key);
      }
      ref.put("$ref", key);
      propEntry.put("items", ref);
    }
    else if (val.isBoolean()) {
      propEntry.put("type", "boolean");
    }
    else if (val.isIntegralNumber()) {
      propEntry.put("type", "integer");
    }
    else if (val.isNumber()) {
      propEntry.put("type", "number");
    }
    else {
      propEntry.put("type", "string");
    }
    return propEntry;
  }

}
