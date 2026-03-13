package ca.etsmtl.taf.user;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import ca.etsmtl.taf.user.repository.RoleRepository;
import ca.etsmtl.taf.user.entity.ERole;
import ca.etsmtl.taf.user.entity.Role;
import java.util.Optional;

@EnableDiscoveryClient
@SpringBootApplication
@Log4j2
public class AuthGatewayApplication implements CommandLineRunner {

    @Autowired
    RoleRepository roleRepository;

	public static void main(String[] args) {
		SpringApplication.run(AuthGatewayApplication.class, args);
	}


	// MES MODIFICATION DEBUT
	 @Override
    public void run(String... args) {
        System.out.println("Running startup script...");
        this.createRoles();
    }

	//Create roles if not exits
	private void createRoles(){
        Optional<Role> userRoleExist = roleRepository.findByName(ERole.ROLE_USER);
        if(userRoleExist.isEmpty()){
            Role toSave = new Role(ERole.ROLE_USER);
            roleRepository.save(toSave);
        }

        userRoleExist = roleRepository.findByName(ERole.ROLE_ADMIN);
        if(userRoleExist.isEmpty()){
            Role toSave = new Role(ERole.ROLE_ADMIN);
            roleRepository.save(toSave);
        }
	}
}