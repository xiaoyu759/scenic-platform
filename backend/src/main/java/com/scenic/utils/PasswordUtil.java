package com.scenic.utils;

import cn.hutool.crypto.digest.BCrypt;

/**
 * 口令加密工具类
 *
 * <p>统一使用 BCrypt 加盐哈希存储口令（安全性与可扩展性优于 MD5/SHA 等固定摘要算法）。
 *
 * <p>为兼容历史数据（早期版本以明文入库），{@link #matches} 在识别到明文口令时退化为等值比较，
 * 业务层可在校验通过后调用 {@link #isEncoded} 判断是否需要把明文重新加密入库，
 * 从而在不强制用户重置口令的前提下完成平滑迁移。
 */
public final class PasswordUtil {

    private PasswordUtil() {
    }

    /**
     * 口令加密（每次调用使用随机盐，同一口令两次加密结果不同）
     *
     * @param rawPassword 原始口令
     * @return BCrypt 密文（60 字符）
     */
    public static String encode(String rawPassword) {
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt());
    }

    /**
     * 口令校验
     *
     * @param rawPassword     用户提交的明文口令
     * @param storedPassword  库中存储的口令（BCrypt 密文或历史明文）
     * @return true 表示匹配
     */
    public static boolean matches(String rawPassword, String storedPassword) {
        if (rawPassword == null || storedPassword == null) {
            return false;
        }
        if (isEncoded(storedPassword)) {
            return BCrypt.checkpw(rawPassword, storedPassword);
        }
        // 历史明文口令：等值比较，由调用方决定是否升级为密文
        return rawPassword.equals(storedPassword);
    }

    /**
     * 判断库中口令是否已是 BCrypt 密文
     */
    public static boolean isEncoded(String storedPassword) {
        if (storedPassword == null || storedPassword.length() != 60) {
            return false;
        }
        return storedPassword.startsWith("$2a$")
                || storedPassword.startsWith("$2b$")
                || storedPassword.startsWith("$2y$");
    }

    /**
     * 校验口令强度：长度不少于 6 位，且同时包含字母与数字
     */
    public static boolean isStrongPassword(String password) {
        if (password == null || password.length() < 6) {
            return false;
        }
        if (!password.matches(".*\\d.*")) {
            return false;
        }
        return password.matches(".*[a-zA-Z].*");
    }

    /**
     * 获取口令强度提示
     */
    public static String getPasswordStrengthTip(String password) {
        if (password == null || password.length() < 6) {
            return "密码长度至少6位";
        }
        if (!password.matches(".*\\d.*")) {
            return "密码至少包含一个数字";
        }
        if (!password.matches(".*[a-zA-Z].*")) {
            return "密码至少包含一个字母";
        }
        if (password.length() >= 8 && password.matches(".*[!@#$%^&*].*")) {
            return "强密码";
        }
        return "中等强度密码";
    }
}