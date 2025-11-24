package br.com.greendrop.backend.mapper;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserMapperConfig {

    @Bean
    public UserMapper userMapper(UserMapperImpl generatedImpl) {

        UserMapperDecorator decorator = new UserMapperDecorator() {};
        decorator.setDelegate(generatedImpl);

        return decorator;
    }
}
