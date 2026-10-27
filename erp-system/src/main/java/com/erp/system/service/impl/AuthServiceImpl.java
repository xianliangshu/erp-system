package com.erp.system.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.erp.common.constant.SessionKeys;
import com.erp.common.core.vo.LoginResult;
import com.erp.common.exception.BusinessException;
import com.erp.common.util.PasswordUtil;
import com.erp.common.util.ServletUtils;
import com.erp.system.entity.SysLoginLog;
import com.erp.system.entity.SysUser;
import com.erp.system.param.LoginParam;
import com.erp.system.service.IAuthService;
import com.erp.system.service.ISysLoginLogService;
import com.erp.system.service.ISysUserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 认证Service实现类
 * 
 * @author ERP System
 * @since 2025-12-12
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService {

    private final ISysUserService userService;

    private final ISysLoginLogService loginLogService;

    @Override
    public LoginResult login(LoginParam param) {
        // 请求信息在这里一次性取出，后面成功/失败都要用来写登录日志
        HttpServletRequest request = ServletUtils.getRequest();
        String ip = ServletUtils.getClientIp(request);
        String userAgent = request == null ? null : request.getHeader("User-Agent");

        // 参数校验
        if (StrUtil.isBlank(param.getUsername()) || StrUtil.isBlank(param.getPassword())) {
            recordLoginLog(param.getUsername(), ip, userAgent, 1, "用户名或密码不能为空");
            throw new BusinessException("用户名或密码不能为空");
        }

        // 只按用户名查, 密码交给下面统一比对
        // 修复说明(2026-10-01): 原实现是 wrapper.eq(SysUser::getPassword, param.getPassword())
        // 直接拿明文拼 SQL 比对, 等于把密码明晃晃写进 SQL 日志里;
        // 下面换成先取用户再走 PasswordUtil.matches(), 语义不变(当前仍是明文比较),
        // 但把"怎么比"收敛到一个地方, 将来换 BCrypt 只改 PasswordUtil 一个文件。
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, param.getUsername());
        SysUser user = userService.getOne(wrapper);

        // 用户不存在与密码错误返回同一句提示, 避免暴露"这个账号存不存在"
        if (user == null || !PasswordUtil.matches(param.getPassword(), user.getPassword())) {
            recordLoginLog(param.getUsername(), ip, userAgent, 1, "用户名或密码错误");
            throw new BusinessException("用户名或密码错误");
        }

        // 检查用户状态
        // 检查用户状态（用 equals 而不是 == 0，避免 Integer 拆箱；null 视为未启用）
        if (Integer.valueOf(0).equals(user.getStatus())) {
            recordLoginLog(user.getUsername(), ip, userAgent, 1, "用户已被禁用");
            throw new BusinessException("用户已被禁用");
        }

        // Sa-Token 签发登录态, loginId 用 userId(StpInterfaceImpl 据此查权限)
        StpUtil.login(user.getId());

        // 把登录名/昵称放进会话：操作日志切面据此写「操作人」，
        // 否则日志里只能看到一个 userId，或者每条日志都要回查一次数据库
        StpUtil.getSession().set(SessionKeys.USERNAME, user.getUsername());
        StpUtil.getSession().set(SessionKeys.NICKNAME, user.getNickname());

        // 回写最后登录时间与 IP。sys_user 上这两个字段建表时就有，但一直没人写，
        // 属于"表里躺着但不生效"的字段，顺手补上。
        updateLastLogin(user.getId(), ip);

        recordLoginLog(user.getUsername(), ip, userAgent, 0, "登录成功");

        // 构建返回结果。token 从 Sa-Token 取, 不再是自己拼的假字符串
        LoginResult.UserInfo userInfo = LoginResult.UserInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .build();

        return LoginResult.builder()
                .token(StpUtil.getTokenInfo().getTokenValue())
                .user(userInfo)
                .build();
    }

    @Override
    public void logout() {
        // 修复说明(2026-10-01): 原实现是 // TODO 空方法, 前端"退出登录"其实什么也没发生。
        // 现在真正注销当前会话(并把会话数据从 Redis 清掉)。
        StpUtil.logout();
    }

    /**
     * 回写 sys_user 的最后登录时间与 IP
     *
     * @param userId 用户ID
     * @param ip     客户端IP
     */
    private void updateLastLogin(Long userId, String ip) {
        try {
            SysUser update = new SysUser();
            update.setId(userId);
            update.setLastLoginTime(LocalDateTime.now());
            update.setLastLoginIp(ip);
            userService.updateById(update);
        } catch (Exception e) {
            // 只是统计信息，写失败不能挡住用户登录
            log.warn("回写最后登录信息失败: userId={}", userId, e);
        }
    }

    /**
     * 记录一条登录日志
     *
     * @param username  用户名
     * @param ip        客户端IP
     * @param userAgent UA（用于解析浏览器与操作系统）
     * @param status    0-成功 1-失败
     * @param msg       提示消息
     */
    private void recordLoginLog(String username, String ip, String userAgent, int status, String msg) {
        SysLoginLog loginLog = new SysLoginLog();
        loginLog.setUsername(username);
        loginLog.setLoginIp(ip);
        loginLog.setBrowser(ServletUtils.getBrowser(userAgent));
        loginLog.setOs(ServletUtils.getOs(userAgent));
        loginLog.setStatus(status);
        // msg 列长度 255，异常消息可能更长，先截断避免插入报错
        loginLog.setMsg(truncate(msg, 250));
        loginLogService.record(loginLog);
    }

    /**
     * 按字符数截断，null 安全
     */
    private static String truncate(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() <= max ? value : value.substring(0, max);
    }
}
