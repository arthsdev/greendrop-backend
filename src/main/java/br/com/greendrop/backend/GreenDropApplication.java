package br.com.greendrop.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

@SpringBootApplication
@EnableRedisRepositories
public class GreenDropApplication {

	public static void main(String[] args) {
		SpringApplication.run(GreenDropApplication.class, args);
	}

}
