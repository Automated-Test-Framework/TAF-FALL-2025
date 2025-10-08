package ca.etsmtl.taf.exportimport.repositories;

import ca.etsmtl.taf.exportimport.models.Project;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends MongoRepository<Project, String> {}