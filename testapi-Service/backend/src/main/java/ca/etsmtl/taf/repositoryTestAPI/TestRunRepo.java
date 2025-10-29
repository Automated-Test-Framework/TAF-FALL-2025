package ca.etsmtl.taf.repositoryTestAPI;

import ca.etsmtl.taf.entityTestAPI.TestRun;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TestRunRepo extends MongoRepository<TestRun, String> { }
