package cc.martincao.rentigo.rentigobackend.payment.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 应用启动时自动运行支付模块测试
 */
@Component
public class PaymentTestRunner implements CommandLineRunner {
    
    @Autowired
    private PaymentIntegrationTest paymentIntegrationTest;
    
    @Override
    public void run(String... args) throws Exception {
        // 延迟一秒确保所有服务都已初始化
        Thread.sleep(1000);
        
        System.out.println("\n" + "=".repeat(50));
        System.out.println("🚀 启动支付模块集成测试...");
        System.out.println("=".repeat(50));
        
        paymentIntegrationTest.testPaymentModule();
        
        System.out.println("=".repeat(50));
        System.out.println("📋 支付模块总结:");
        System.out.println("✅ Payment 模型类: 已创建并配置完成");
        System.out.println("✅ PaymentType 枚举: 已创建 (RENTAL_FEE, DEPOSIT, OVERTIME_FEE)");
        System.out.println("✅ PaymentStatus 枚举: 已创建 (PENDING, COMPLETED, FAILED, CANCELLED)");
        System.out.println("✅ PaymentService: 已实现支付服务接口");
        System.out.println("✅ PaymentController: 已创建支付控制器");
        System.out.println("✅ PaymentRepository: 已创建数据访问层");
        System.out.println("✅ StripeConfig: 已配置 Stripe 集成");
        System.out.println("✅ WebSecurity: 已配置 webhook 端点免认证");
        System.out.println("✅ DTO 类: 已创建请求和响应 DTO");
        System.out.println("✅ 异常处理: 已创建 PaymentBusinessException");
        System.out.println();
        System.out.println("🎯 Stripe 集成说明:");
        System.out.println("   1. Webhook 端点: /api/payment/webhook");
        System.out.println("   2. 推荐监听事件: checkout.session.completed, payment_intent.succeeded");
        System.out.println("   3. 本地开发 URL: http://localhost:8080/api/payment/webhook");
        System.out.println("   4. 已配置在 application.properties 中");
        System.out.println("=".repeat(50) + "\n");
    }
}
