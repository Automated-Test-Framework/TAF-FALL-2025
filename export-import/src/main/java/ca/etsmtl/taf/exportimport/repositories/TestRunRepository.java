package ca.etsmtl.taf.exportimport.repositories;

import ca.etsmtl.taf.exportimport.models.TestRun;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TestRunRepository extends MongoRepository<TestRun, String> {}