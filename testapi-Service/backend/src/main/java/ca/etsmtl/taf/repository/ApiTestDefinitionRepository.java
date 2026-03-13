package ca.etsmtl.taf.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import ca.etsmtl.taf.entity.ApiTestDefinition;

@Repository
public interface ApiTestDefinitionRepository extends MongoRepository<ApiTestDefinition, String> {
    List<ApiTestDefinition> findByUsernameOrderByCreatedAtAsc(String username);
    void deleteByIdAndUsername(String id, String username);
}
