package ca.etsmtl.taf.exportimport.repositories;

import ca.etsmtl.taf.exportimport.models.TestSuite;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TestSuiteRepository extends MongoRepository<TestSuite, String> {}