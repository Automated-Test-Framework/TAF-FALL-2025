package ca.etsmtl.taf.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import ca.etsmtl.taf.entity.TestRun;

@Repository
public interface TestRunRepository extends MongoRepository<TestRun, String> {
}
