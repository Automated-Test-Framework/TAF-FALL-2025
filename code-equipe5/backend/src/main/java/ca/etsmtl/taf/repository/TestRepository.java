package ca.etsmtl.taf.repository;

import ca.etsmtl.taf.entity.Test;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.CrudRepository;


public interface TestRepository extends CrudRepository<Test, Integer> {
    
}
