package ca.etsmtl.taf.repository;

import ca.etsmtl.taf.entity.Project;
import ca.etsmtl.taf.entity.SeleniumTestCase;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
//SeleniumCaseRepository

@Repository
public interface SeleniumCaseRepository extends MongoRepository<SeleniumTestCase, String> {
    // Vous pouvez ajouter des méthodes personnalisées pour rechercher des cas spécifiques si nécessaire
}