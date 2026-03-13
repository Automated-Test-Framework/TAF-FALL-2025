package ca.etsmtl.taf.performance.jmeter.model;

import java.io.File;

import ca.etsmtl.taf.performance.jmeter.config.JMeterConfigurator;
import lombok.*;
import lombok.experimental.SuperBuilder;


@AllArgsConstructor
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class FTPTestPlan extends TestPlanBase {
  private String remoteFile;
  private String localFile;
  private String username;
  private String password;

  @Override
  public void generateTestPlan() {
    replaceAndSaveVariables(new File(JMeterConfigurator.getJmeterTemplatesFolder(),"FTPSamplerTemplate.jmx").getAbsolutePath(),
    new File(JMeterConfigurator.getJmeterTemplatesFolder(),"TestPlan.jmx").getAbsolutePath(),
            "FTPSamplerTemplate");
  }

  @Override
  protected String replaceVariables(String xmlContent, String templateKey) {
    xmlContent = xmlContent.replace("$NB_THREADS$", escapeXml(nbThreads))
            .replace("$RAMP_TIME$", escapeXml(rampTime))
            .replace("$DURATION$", escapeXml(duration))
            .replace("$DOMAIN$", escapeXml(domain))
            .replace("$PORT$", escapeXml(port))
            .replace("$REMOTEFILE$", escapeXml(remoteFile))
            .replace("$LOCALFILE$", escapeXml(localFile))
            .replace("$METHOD$", getFtpMethod())
            .replace("$USERNAME$", escapeXml(username))
            .replace("$PASSWORD$", escapeXml(password))
            .replace("$LOOP_COUNTER$", escapeXml(loop));

    return xmlContent;
  }
  private  String getFtpMethod() {
    return getMethod().equals("Retrieve") ?  "false" : "true";
  }

}
