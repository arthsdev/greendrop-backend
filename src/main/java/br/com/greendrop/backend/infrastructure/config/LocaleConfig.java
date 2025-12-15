package br.com.greendrop.backend.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.Locale;

@Configuration
public class LocaleConfig {

    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();

        // Default language when Accept-Language is not provided - EN
        //resolver.setDefaultLocale(Locale.ENGLISH);

         //PT-BR as default:
         resolver.setDefaultLocale(new Locale("pt", "BR"));

        return resolver;
    }
}
