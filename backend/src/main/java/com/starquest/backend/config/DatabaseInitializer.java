package com.starquest.backend.config;

import com.starquest.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * DatabaseInitializer - 数据库初始化器
 *
 * 注意：测试数据现在通过 data.sql 文件维护，而不是代码。
 * 这样更便于管理和维护测试数据。
 */
@Component
@RequiredArgsConstructor
public class DatabaseInitializer implements CommandLineRunner {

    private final UserService userService;

    @Override
    public void run(String... args) throws Exception {
        // 确保默认管理员账号存在（以防data.sql未执行）
        userService.initializeDefaultAdmin();

        System.out.println("🎉 数据库初始化完成！");
        System.out.println("📊 测试数据已通过 data.sql 自动导入");
        System.out.println("👤 测试账号：admin/password (家长端)");
        System.out.println("👶 测试账号：testkid/123456 (学员端)");
    }
}
