package cc.martincao.rentigo.rentigobackend.payment.config;

import com.stripe.Stripe;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

/**
 * Stripe 配置类
 */
@Configuration
public class StripeConfig {
    
    @Value("${stripe.secret-key}")
    private String secretKey;
    
    @PostConstruct
    public void init() {
        Stripe.apiKey = secretKey;
    }
}
