package ca.etsmtl.taf.user.repository;

import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.lang.NonNull;

import ca.etsmtl.taf.user.entity.User;

public interface UserRepository extends MongoRepository<User, String> {
  Optional<User> findByUsername(String username);

  boolean existsByUsername(String username);

  boolean existsById(@NonNull String id);

  boolean existsByEmail(String email);

  boolean existsByEmailAndIdNot(String email, String id);

}
