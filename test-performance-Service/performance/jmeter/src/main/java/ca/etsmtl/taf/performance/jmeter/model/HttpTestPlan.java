package ca.etsmtl.taf.performance.jmeter.model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import ca.etsmtl.taf.performance.jmeter.config.JMeterConfigurator;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class HttpTestPlan extends TestPlanBase {

  private String protocol;
  private String path;
  private String data;


  public void generateTestPlan() {
    replaceAndSaveVariables();
  }

  @Override
  protected String replaceVariables(String xmlContent, String templateKey) {
    return null;
  }

  private void replaceAndSaveVariables() {
    try {
      // Read the XML content from the file
      String filePath = JMeterConfigurator.getHTTPSamplerTemplate();
      String xmlContent = Files.readString(Paths.get(filePath));
      String target = new File(JMeterConfigurator.getJmeterTemplatesFolder(), "TestPlan.jmx").getAbsolutePath();

      // Replace variables with Java variables (using default values if not found)
      xmlContent = replaceVariables(xmlContent);

      // Save the modified content back to the file
      Files.writeString(Paths.get(target), xmlContent);

    } catch (IOException e) {
      throw new RuntimeException("Failed to generate JMeter test plan: " + e.getMessage(), e);
    }
  }

  private String replaceVariables(String xmlContent) throws JsonProcessingException {
    // Replace variables in the XML content using instance fields
    xmlContent = xmlContent.replace("$NB_THREADS$", escapeXml(nbThreads))
        .replace("$RAMP_TIME$", escapeXml(rampTime))
        .replace("$DURATION$", escapeXml(duration))
        .replace("$DOMAIN$", escapeXml(domain))
        .replace("$PORT$", escapeXml(port))
        .replace("$PROTOCOL$", escapeXml(protocol))
        .replace("$PATH$", escapeXml(path))
        .replace("$METHOD$", escapeXml(method))
        .replace("$LOOP_COUNTER$", escapeXml(loop));

    if (data != null && !data.isEmpty()) {
      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
      Object jsonMap = objectMapper.readValue(data, Object.class);

      // Serialize the Map to a formatted JSON string
      String formattedJsonString = objectMapper.writeValueAsString(jsonMap);
      String xmlString = " <elementProp name=\"\" elementType=\"HTTPArgument\">\n" +
          "                <boolProp name=\"HTTPArgument.always_encode\">false</boolProp>\n" +
          "                <stringProp name=\"Argument.value\">"
          + escapeXml(formattedJsonString) + "</stringProp>\n" +
          "                <stringProp name=\"Argument.metadata\">=</stringProp>\n" +
          "              </elementProp>";

      xmlContent = xmlContent.replace("$DATA$", xmlString);
    } else {
      xmlContent = xmlContent.replace("$DATA$", "");

    }
    return xmlContent;
  }

}
