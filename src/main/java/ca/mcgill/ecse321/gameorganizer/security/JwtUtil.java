package ca.mcgill.ecse321.gameorganizer.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    private Key key;

    // IMPORTANT: Ensure the jwt.secret in application.properties is a Base64 encoded string
    // representing at least 64 secure random bytes (512 bits) for HS512 algorithm.
    // Generate a new one if the current key is too short.
    // Example generation (using jjwt library or online tool): Keys.secretKeyFor(SignatureAlgorithm.HS512) then Base64 encode the result.

    @PostConstruct
    public void init() {
        byte[] secretBytes = Base64.getDecoder().decode(secret);
        key = new SecretKeySpec(secretBytes, SignatureAlgorithm.HS512.getJcaName());
    }

    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    public String extractUsername(String token) {
        try {
            return extractClaim(token, Claims::getSubject);
        } catch (Exception e) {
            // Logged within extractClaim/extractAllClaims if parsing fails
            return null; // Return null if extraction fails
        }
    }

    public Date extractExpiration(String token) {
         try {
            return extractClaim(token, Claims::getExpiration);
         } catch (Exception e) {
             // Logged within extractClaim/extractAllClaims if parsing fails
             return null; // Return null if extraction fails
         }
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        if (claims != null) { // Check if claims extraction was successful
            try {
                return claimsResolver.apply(claims);
            } catch (Exception e) {
                logger.error("Failed to resolve claim: {}", e.getMessage());
                return null;
            }
        }
        return null; // Return null if claims are null (parsing failed)
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        } catch (ExpiredJwtException e) {
            // Log expired as warn, but return claims. Validation logic handles the expiration check.
            logger.warn("JWT token is expired: {}", e.getMessage());
            return e.getClaims(); // Return claims even if expired, for potential use
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.error("JWT token is malformed: {}", e.getMessage());
        } catch (SignatureException e) {
            logger.error("JWT signature validation failed: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty or null: {}", e.getMessage());
        } catch (Exception e) { // Catch any other unexpected exceptions
            logger.error("An unexpected error occurred during JWT parsing: {}", e.getMessage(), e); // Log stack trace too
        }
        return null; // Return null if any parsing error other than expired occurs
    }

    public boolean isTokenExpired(String token) {
        Date expirationDate = extractExpiration(token);
        // Check if expirationDate is null (due to parsing error or missing claim)
        if (expirationDate == null) {
             logger.warn("Could not determine token expiration due to parsing error or missing claim.");
             return true; // Treat as expired if expiration cannot be extracted
        }
        boolean expired = expirationDate.before(new Date());
        if (expired) {
            // Log details only when confirmed expired
            logger.warn("Token is expired. Expiration: {}, Current: {}", expirationDate, new Date());
        }
        return expired;
    }

    // Updated validateToken
    public boolean validateToken(String token, String username) {
        final String extractedUsername = extractUsername(token);

        // Check if username could be extracted (parsing might have failed)
        if (extractedUsername == null) {
            logger.warn("Token validation failed: Could not extract username from token (check logs for parsing errors).");
            return false;
        }

        boolean isUsernameMatch = extractedUsername.equals(username);
        if (!isUsernameMatch) {
            logger.warn("Token validation failed: Username mismatch. Token Subject='{}', Expected Username='{}'", extractedUsername, username);
            return false;
        }

        // isTokenExpired now handles null expiration date and logs if expired
        boolean isExpired = isTokenExpired(token);
        if (isExpired) {
            // No need to log again here, isTokenExpired already logged it.
            return false;
        }

        // If all checks pass
        logger.debug("Token validated successfully for user: {}", username);
        return true; // Only return true if username matches AND token is not expired
    }
}