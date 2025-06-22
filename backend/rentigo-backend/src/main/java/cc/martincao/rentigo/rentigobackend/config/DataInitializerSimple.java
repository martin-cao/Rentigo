package cc.martincao.rentigo.rentigobackend.config;

/**
 * 暂时禁用 DataInitializer 以避免 Lombok 相关的编译错误
 * 这样可以让 payment 模块正常启动
 * 
 * 当 Lombok 问题解决后，可以恢复数据初始化功能
 */
public class DataInitializerSimple {
    // 暂时禁用数据初始化功能
    // 原因：其他模块的 Lombok 注解处理失败导致编译错误
    // 解决 Lombok 问题后可以恢复完整的数据初始化逻辑
}
