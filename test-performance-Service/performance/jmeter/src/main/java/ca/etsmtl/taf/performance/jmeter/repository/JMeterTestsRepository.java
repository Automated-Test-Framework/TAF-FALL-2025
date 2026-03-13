package ca.etsmtl.taf.performance.jmeter.repository;

import ca.etsmtl.taf.performance.jmeter.model.JMeterTestDocument;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface JMeterTestsRepository extends MongoRepository<JMeterTestDocument, ObjectId> {
}
