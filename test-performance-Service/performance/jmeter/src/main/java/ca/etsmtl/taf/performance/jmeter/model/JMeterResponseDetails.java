package ca.etsmtl.taf.performance.jmeter.model;

import ca.etsmtl.taf.performance.jmeter.config.JMeterConfigurator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.File;
import java.nio.file.Path;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class JMeterResponseDetails {

    @JsonProperty("content-type")
    private String contentType;

    @JsonProperty("location-url")
    private String locationURL;

    public void setLocationURL(String url) {
        Path dashboardPath = new File(JMeterConfigurator.getJmeterResultsFolder()).toPath()
                .relativize(new File(url).toPath());
        this.locationURL = "/team3/reports/performance/jmeter/dashboard/" + dashboardPath
                + "/index.html";
    }
}