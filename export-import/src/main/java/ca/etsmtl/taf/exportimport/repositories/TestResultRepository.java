package ca.etsmtl.taf.exportimport.repositories;

import ca.etsmtl.taf.exportimport.models.TestResult;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TestResultRepository extends MongoRepository<TestResult, String> {}