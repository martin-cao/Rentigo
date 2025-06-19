package cc.martincao.rentigo.rentigobackend.user.util;

import cc.martincao.rentigo.rentigobackend.user.Role;
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
        Claims claims = Jwts.claims().setSubject(user.getUsername());
        claims.put("roles", user.getRoles().stream()
                .map(Role::getName).collect(Collectors.toList()));

        Date now = new Date();
        Date exp = new Date(now.getTime() + validityMs);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(secretKey)
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