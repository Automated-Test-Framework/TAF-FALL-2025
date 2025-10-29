package ca.etsmtl.taf.repositoryTestAPI;

import ca.etsmtl.taf.entityTestAPI.TestSuite;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TestSuiteRepo extends MongoRepository<TestSuite, String>{}
