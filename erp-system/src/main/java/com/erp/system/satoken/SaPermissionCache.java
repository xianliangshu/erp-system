package com.erp.system.satoken;

import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Set;

/**
 * Sa-Token 鉴权数据(权限标识 / 角色编号)的 Redis 缓存。
 * <p>
 * 为什么必须要有这一层: {@code StpInterface} 的 getPermissionList / getRoleList
 * 会在<b>每一次</b> {@code @SaCheckPermission} 校验时被调用, 而它背后是
 * sys_menu ⋈ sys_role_menu ⋈ sys_user_role ⋈ sys_role 四表 join。
 * Sa-Token 自身<b>不会</b>缓存这两个方法的返回值, 不加缓存等于每个请求都打一次库。
 * <p>
 * 设计取舍:
 * <ul>
 *   <li>Redis 异常时<b>降级为直接查库</b>, 而不是让请求失败 —— 缓存挂了不该导致全站不可用。</li>
 *   <li>授权变更(角色改菜单 / 用户改角色)时调用 {@link #evictAll()}, 宁可多查一次库,
 *       也不能让用户拿着旧权限继续用 —— 权限变更不及时比慢更危险。</li>
 *   <li>用 JSON 字符串存, 便于用 redis-cli 直接查看某个用户当前拿到了哪些权限。</li>
 * </ul>
 *
 * @author ERP System
 * @since 2026-10-01
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SaPermissionCache {

    /**
     * 缓存 key 统一前缀, {@link #evictAll()} 按此前缀清理
     */
    public static final String KEY_PREFIX = "erp:auth:";

    /**
     * 权限缓存 key 前缀
     */
    public static final String PERM_KEY = KEY_PREFIX + "perms:";

    /**
     * 角色缓存 key 前缀
     */
    public static final String ROLE_KEY = KEY_PREFIX + "roles:";

    /**
     * 兜底过期时间。即便 evict 漏调, 最迟 30 分钟后也会自愈
     */
    private static final Duration TTL = Duration.ofMinutes(30);

    private final StringRedisTemplate redisTemplate;

    /**
     * 读取缓存, 未命中或 Redis 异常时返回 null(调用方回退查库)
     */
    public List<String> get(String key) {
        try {
            String json = redisTemplate.opsForValue().get(key);
            if (json != null && !json.isEmpty()) {
                return JSONUtil.toList(json, String.class);
            }
        } catch (Exception e) {
            log.warn("读取鉴权缓存失败, 将回退查库. key={}, err={}", key, e.getMessage());
        }
        return null;
    }

    /**
     * 写入缓存
     */
    public void put(String key, List<String> values) {
        if (values == null || values.isEmpty()) {
            return;
        }
        try {
            redisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(values), TTL);
        } catch (Exception e) {
            log.warn("写入鉴权缓存失败, 不影响本次请求. key={}, err={}", key, e.getMessage());
        }
    }

    /**
     * 清空所有鉴权缓存。
     * 角色改菜单、删角色、用户改角色后必须调用。
     */
    public void evictAll() {
        try {
            Set<String> keys = redisTemplate.keys(KEY_PREFIX + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.info("已清空鉴权缓存 {} 个 key", keys.size());
            }
        } catch (Exception e) {
            log.warn("清空鉴权缓存失败: {}", e.getMessage());
        }
    }

    /**
     * 清空指定用户的鉴权缓存
     */
    public void evictUser(Long userId) {
        try {
            redisTemplate.delete(List.of(PERM_KEY + userId, ROLE_KEY + userId));
        } catch (Exception e) {
            log.warn("清空用户鉴权缓存失败. userId={}, err={}", userId, e.getMessage());
        }
    }
}
