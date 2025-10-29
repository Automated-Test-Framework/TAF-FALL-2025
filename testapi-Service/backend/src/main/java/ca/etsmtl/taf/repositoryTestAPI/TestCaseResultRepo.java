package ca.etsmtl.taf.repositoryTestAPI;

import ca.etsmtl.taf.entityTestAPI.TestCaseResult;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TestCaseResultRepo extends MongoRepository<TestCaseResult, String> {}
