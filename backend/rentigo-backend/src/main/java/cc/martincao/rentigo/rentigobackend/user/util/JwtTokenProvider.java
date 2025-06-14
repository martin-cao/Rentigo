package cc.martincao.rentigo.rentigobackend.user.util;

import cc.martincao.rentigo.rentigobackend.user.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long validityMs;

    public JwtTokenProvider(@Value("${security.jwt.secret}") String secret,
                            @Value("${security.jwt.expire-ms:86400000}") long validityMs) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.validityMs = validityMs;
    }

    /** 生成 Token */
    public String generateToken(User user) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + validityMs);
        String roles = user.getRoles().stream()
                .map(r -> r.getName()).collect(Collectors.joining(","));
        return Jwts.builder()
                .setSubject(user.getId().toString())
                .claim("u", user.getUsername())
                .claim("r", roles)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /** 解析校验 */
    public Jws<Claims> parse(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token);
    }
}