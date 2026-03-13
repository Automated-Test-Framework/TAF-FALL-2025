package ca.etsmtl.taf.entity;

import java.util.Date;
import java.util.Map;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Document(collection = "api_test_definitions")
public class ApiTestDefinition {

    @Id
    private String id;

    @Indexed
    private String username;

    private String method;
    private String apiUrl;
    private Map<String, String> headers;
    private Map<String, String> expectedHeaders;
    private String input;
    private String expectedOutput;
    private Integer statusCode;
    private Integer responseTime;

    @CreatedDate
    private Date createdAt;

    @LastModifiedDate
    private Date updatedAt;
}
