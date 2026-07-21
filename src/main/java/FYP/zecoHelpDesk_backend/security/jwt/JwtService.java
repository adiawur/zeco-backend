package FYP.zecoHelpDesk_backend.security.jwt;

import FYP.zecoHelpDesk_backend.user.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    private SecretKey getSigningKey() {

        return Keys.hmacShaKeyFor(secret.getBytes());

    }

    public String generateToken(User user) {

        return Jwts.builder()

                .subject(user.getUsername())

                .claim("role", user.getRole().name())

                .issuedAt(new Date())

                .expiration(
                        new Date(
                                System.currentTimeMillis() + expiration
                        )
                )

                .signWith(getSigningKey())

                .compact();

    }

    public String extractUsername(String token) {

        return extractClaim(
                token,
                Claims::getSubject
        );

    }

    public <T> T extractClaim(
            String token,
            Function<Claims, T> resolver
    ) {

        Claims claims = Jwts.parser()

                .verifyWith(getSigningKey())

                .build()

                .parseSignedClaims(token)

                .getPayload();

        return resolver.apply(claims);

    }

    public boolean isTokenValid(
            String token,
            User user
    ) {

        return extractUsername(token)
                .equals(user.getUsername())
                &&
                !isTokenExpired(token);

    }

    private boolean isTokenExpired(String token) {

        return extractClaim(
                token,
                Claims::getExpiration
        ).before(new Date());

    }

}