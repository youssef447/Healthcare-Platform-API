package com.healthcare.ingestion.config;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CustomOidcUserService extends OidcUserService {

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) {
        OidcUser oidcUser = super.loadUser(userRequest);

        OidcIdToken idToken = userRequest.getIdToken();
        Map<String, Object> claims = idToken.getClaims();

        List<SimpleGrantedAuthority> mappedAuthorities = new ArrayList<>();

        Object realmAccessObj = claims.get("realm_access");
        if (realmAccessObj instanceof Map<?, ?> realmAccessMap) {
            Object rolesObj = realmAccessMap.get("roles");
            if (rolesObj instanceof List<?> rolesList) {
                for (Object role : rolesList) {
                    if (role instanceof String roleStr) {
                        mappedAuthorities.add(new SimpleGrantedAuthority("ROLE_" + roleStr));
                    }
                }
            }
        }

        return new DefaultOidcUser(mappedAuthorities, idToken, oidcUser.getUserInfo());
    }
}
