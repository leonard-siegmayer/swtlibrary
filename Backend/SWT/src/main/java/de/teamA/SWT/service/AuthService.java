package de.teamA.SWT.service;

import java.net.URL;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

import javax.persistence.EntityNotFoundException;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.UrlJwkProvider;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import de.teamA.SWT.entities.Role;
import de.teamA.SWT.entities.User;
import de.teamA.SWT.entities.reqres.AuthenticationCodeResponse;
import de.teamA.SWT.entities.reqres.UserinfoResponse;
import de.teamA.SWT.repository.UserRepository;

@Service
public class AuthService {

    @Value("${gitlab.issuer}")
    public String issuer;
    @Value("${gitlab.jwk.uri}")
    public String jwkUrl;
    @Value("${gitlab.accesstoken.uri}")
    private String accessTokenUrl;
    @Value("${gitlab.clientid}")
    private String clientId;
    @Value("${gitlab.clientsecret}")
    private String clientSecret;
    @Value("${gitlab.grant_type}")
    private String grantType;
    @Value("${gitlab.userinfo.uri}")
    private String userinfoUrl;

    @Autowired
    private UserRepository userRepository;

    public AuthenticationCodeResponse requestTokens(@NonNull String code, @NonNull String redirectUri) {

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("redirect_uri", redirectUri);
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("grant_type", grantType);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        return restTemplate.postForObject(accessTokenUrl, request, AuthenticationCodeResponse.class);

    }

    public UserinfoResponse requestUserInformationForAccessToken(@NonNull String accessToken) {

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("access_token", accessToken);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        return restTemplate.postForObject(userinfoUrl, request, UserinfoResponse.class);

    }

    public String getSubFromOpenIdToken(String token) throws Exception {

        String kid = JwtHelper.headers(token).get("kid");

        final Jwt tokenDecoded = JwtHelper.decodeAndVerify(token, verifier(kid));

        final Map<String, String> authInfo = new ObjectMapper().readValue(tokenDecoded.getClaims(), Map.class);

        verifyClaims(authInfo);

        return authInfo.get("sub");

    }

    public String getLoggedInUserName() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        Object principal = auth.getPrincipal();
        String user = "";

        if (principal instanceof org.springframework.security.core.userdetails.User) {
            user = ((org.springframework.security.core.userdetails.User) principal).getUsername();
        }

        return user;
    }

    public User getUserById(String id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            return user.get();
        } else {
            throw new EntityNotFoundException("User with id " + id + " doesn't exist");
        }
    }

    public User getLoggedInUser() {
        return getUserById(getLoggedInUserName());
    }

    public User getDefaultUser() {
        return getUserById(String.valueOf(Long.MIN_VALUE));
    }

    public boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        for (GrantedAuthority g : auth.getAuthorities()) {
            if (g.getAuthority().equals(Role.ROLE_ADMIN.toString())) {
                return true;
            }
        }

        return false;
    }

    public boolean isStudent() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        for (GrantedAuthority g : auth.getAuthorities()) {
            if (g.getAuthority().equals(Role.ROLE_ADMIN.toString())
                    || g.getAuthority().equals(Role.ROLE_STAFF.toString())) {
                return false;
            }
        }

        return true;
    }

    private void verifyClaims(Map claims) {
        int exp = (int) claims.get("exp");
        Date expireDate = new Date(exp * 1000L);
        Date now = new Date();
        if (expireDate.before(now) || !claims.get("iss").equals(issuer) || !claims.get("aud").equals(clientId)) {
            throw new RuntimeException("Invalid claims");
        }
    }

    private RsaVerifier verifier(String kid) throws Exception {
        JwkProvider provider = new UrlJwkProvider(new URL(jwkUrl));
        Jwk jwk = provider.get(kid);
        return new RsaVerifier((RSAPublicKey) jwk.getPublicKey());
    }

}