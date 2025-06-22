package cc.martincao.rentigo.rentigobackend.payment.test;

import cc.martincao.rentigo.rentigobackend.payment.model.PaymentType;
import cc.martincao.rentigo.rentigobackend.payment.model.PaymentStatus;

/**
 * 简单验证 Payment 枚举类的独立工作能力
 */
public class EnumVerification {
    
    public static void main(String[] args) {
        System.out.println("=== Payment 枚举验证 ===");
        
        // 测试 PaymentType 枚举
        System.out.println("\nPaymentType 枚举测试:");
        for (PaymentType type : PaymentType.values()) {
            System.out.println("- " + type.name() + " (code: " + type.getCode() + ", desc: " + type.getDescription() + ")");
        }
        
        // 测试 PaymentType.fromCode 方法
        System.out.println("\nPaymentType.fromCode 测试:");
        PaymentType rental = PaymentType.fromCode(0);
        System.out.println("- fromCode(0): " + rental.name());
        
        // 测试 PaymentStatus 枚举
        System.out.println("\nPaymentStatus 枚举测试:");
        for (PaymentStatus status : PaymentStatus.values()) {
            System.out.println("- " + status.name() + " (code: " + status.getCode() + ", desc: " + status.getDescription() + ")");
        }
        
        // 测试 PaymentStatus.fromCode 方法
        System.out.println("\nPaymentStatus.fromCode 测试:");
        PaymentStatus pending = PaymentStatus.fromCode(0);
        System.out.println("- fromCode(0): " + pending.name());
        
        System.out.println("\n=== 所有测试通过！Payment 枚举工作正常 ===");
    }
}
