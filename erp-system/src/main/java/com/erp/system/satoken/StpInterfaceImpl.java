package com.erp.system.satoken;

import cn.dev33.satoken.stp.StpInterface;
import com.erp.system.mapper.SysMenuMapper;
import com.erp.system.mapper.SysRoleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Supplier;

/**
 * Sa-Token 权限数据提供者。
 * <p>
 * Sa-Token 在每次 {@code @SaCheckPermission} / {@code StpUtil.hasPermission()} 时,
 * 都会回调本类的 getPermissionList / getRoleList 来拿"当前登录用户拥有什么"。
 * 我们把它接到既有的 RBAC 数据上: {@code sys_user_role → sys_role_menu → sys_menu.permission},
 * 所以<b>权限数据一行都不用改</b>, menu 表里已有的 {@code system:user:add} 这类权限串直接可用。
 * <p>
 * 本类放在 erp-system 而不是 erp-web, 是因为它要读 sys_menu / sys_role 的 Mapper;
 * 反过来放 erp-web 会让 erp-system 反向依赖表现层。
 *
 * @author ERP System
 * @since 2026-10-01
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {

    /**
     * 超级管理员角色编号(见 sys_role 种子数据)
     */
    private static final String SUPER_ADMIN_ROLE_CODE = "R000001";

    /**
     * Sa-Token 通配权限。存到"用户权限列表"里会被当成模式串匹配,
     * 因此超管返回它即可通过任意权限校验, 免得以后每加一个菜单都要重新授权。
     */
    private static final List<String> ALL_PERMISSIONS = List.of("*");

    private final SysMenuMapper menuMapper;
    private final SysRoleMapper roleMapper;
    private final SaPermissionCache cache;

    /**
     * 返回账号拥有的权限标识集合
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        Long userId = toUserId(loginId);
        if (userId == null) {
            return List.of();
        }

        // 超管短路: 不查菜单授权表, 直接给通配权限
        if (getRoleList(loginId, loginType).contains(SUPER_ADMIN_ROLE_CODE)) {
            return ALL_PERMISSIONS;
        }

        return loadWithCache(
                SaPermissionCache.PERM_KEY + userId,
                () -> menuMapper.selectPermissionsByUserId(userId));
    }

    /**
     * 返回账号拥有的角色编号集合
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        Long userId = toUserId(loginId);
        if (userId == null) {
            return List.of();
        }
        return loadWithCache(
                SaPermissionCache.ROLE_KEY + userId,
                () -> roleMapper.selectRoleCodesByUserId(userId));
    }

    /**
     * 先查缓存, 未命中则走 loader 查库并回填
     */
    private List<String> loadWithCache(String key, Supplier<List<String>> loader) {
        List<String> cached = cache.get(key);
        if (cached != null) {
            return cached;
        }
        List<String> values = loader.get();
        if (values == null) {
            values = List.of();
        }
        cache.put(key, values);
        return values;
    }

    /**
     * Sa-Token 的 loginId 是 Object, 我们登录时传的是 Long 的 userId
     */
    private Long toUserId(Object loginId) {
        if (loginId == null) {
            return null;
        }
        if (loginId instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.valueOf(loginId.toString());
        } catch (NumberFormatException e) {
            log.warn("无法把 loginId 解析为 userId: {}", loginId);
            return null;
        }
    }
}
