package ca.etsmtl.taf.entityTestAPI;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document("test_suites")
@CompoundIndex(name="owner_name", def="{ 'owner':1, 'name':1 }", unique=true)
@Getter
@Setter
public class TestSuite {

    @Id
    public String id;
    public String name;
    public String owner;     // utilisateur/tenant
    public String format;    // yaml | json
    public String content;   // spec brute
    public Instant createdAt;
    public Instant updatedAt;
    public Integer version;
}
