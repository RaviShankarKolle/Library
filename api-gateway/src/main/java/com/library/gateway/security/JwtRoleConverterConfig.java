package com.library.gateway.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import reactor.core.publisher.Flux;
import java.util.List;

@Configuration
public class JwtRoleConverterConfig {

    @Bean
    ReactiveJwtAuthenticationConverter reactiveJwtAuthenticationConverter() {
        ReactiveJwtAuthenticationConverter converter = new ReactiveJwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new RolesClaimAdapter());
        return converter;
    }

    private static final class RolesClaimAdapter implements Converter<Jwt, Flux<GrantedAuthority>> {

        @Override
        public Flux<GrantedAuthority> convert(Jwt jwt) {
            List<String> roles = jwt.getClaimAsStringList("roles");
            if (roles == null || roles.isEmpty()) {
                return Flux.empty();
            }
            return Flux.fromIterable(roles).map(r -> new SimpleGrantedAuthority("ROLE_" + r));
        }
    }
}
