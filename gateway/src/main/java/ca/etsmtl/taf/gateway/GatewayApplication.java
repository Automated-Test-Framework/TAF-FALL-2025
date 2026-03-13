package ca.etsmtl.taf.gateway;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import java.util.List;

@EnableDiscoveryClient
@SpringBootApplication
@Log4j2
public class GatewayApplication implements CommandLineRunner {

	@Autowired
	private DiscoveryClient discoveryClient;

	public void run(String... args) throws Exception {
		System.out.println("LIST INSTANCES");
		List<String> list = discoveryClient.getServices();

		list.forEach(item -> {
			System.out.println("Service: " + item);
			List<ServiceInstance> instances = this.discoveryClient.getInstances(item);
			instances.forEach(instance -> {
				System.out.println("URI: " + instance.getUri());
				System.out.println("HOST: " + instance.getHost());
				System.out.println("PORT: " + instance.getPort());
				System.out.println("INSTANCE ID: " + instance.getInstanceId());
				System.out.println("SERVICE ID: " + instance.getServiceId());
			});
		});
	}

	public static void main(String[] args) {
		SpringApplication.run(GatewayApplication.class, args);
	}

}