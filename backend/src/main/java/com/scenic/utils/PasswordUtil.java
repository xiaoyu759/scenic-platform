//package com.scenic.utils;
//
//import cn.hutool.crypto.digest.BCrypt;
//
///**
// * 密码加密工具类
// * 使用 BCrypt 加密，安全性高
// */
//public class PasswordUtil {
//
//    /**
//     * 密码加密
//     *
//     * @param rawPassword 原始密码
//     * @return 加密后的密码
//     */
//    public static String encode(String rawPassword) {
//        return BCrypt.hashpw(rawPassword);
//    }
//
//    /**
//     * 密码验证
//     *
//     * @param rawPassword     原始密码（用户输入的）
//     * @param encodedPassword 加密后的密码（数据库中存储的）
//     * @return true: 密码匹配, false: 密码不匹配
//     */
//    public static boolean matches(String rawPassword, String encodedPassword) {
//        return BCrypt.checkpw(rawPassword, encodedPassword);
//    }
//
//    /**
//     * 验证密码强度
//     *
//     * @param password 密码
//     * @return true: 强度合格, false: 强度不足
//     */
//    public static boolean isStrongPassword(String password) {
//        if (password == null || password.length() < 6) {
//            return false;
//        }
//        // 至少包含一个数字
//        if (!password.matches(".*\\d.*")) {
//            return false;
//        }
//        // 至少包含一个字母
//        if (!password.matches(".*[a-zA-Z].*")) {
//            return false;
//        }
//        return true;
//    }
//
//    /**
//     * 获取密码强度提示
//     */
//    public static String getPasswordStrengthTip(String password) {
//        if (password == null || password.length() < 6) {
//            return "密码长度至少6位";
//        }
//        if (!password.matches(".*\\d.*")) {
//            return "密码至少包含一个数字";
//        }
//        if (!password.matches(".*[a-zA-Z].*")) {
//            return "密码至少包含一个字母";
//        }
//        if (password.length() >= 8 && password.matches(".*[!@#$%^&*].*")) {
//            return "强密码";
//        }
//        return "中等强度密码";
//    }
//}