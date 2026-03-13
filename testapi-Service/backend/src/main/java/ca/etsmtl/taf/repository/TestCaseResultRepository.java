package ca.etsmtl.taf.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import ca.etsmtl.taf.entity.TestCaseResult;

@Repository
public interface TestCaseResultRepository extends MongoRepository<TestCaseResult, String> {
}
