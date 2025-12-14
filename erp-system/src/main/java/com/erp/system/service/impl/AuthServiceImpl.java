package com.erp.system.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.erp.common.core.vo.LoginResult;
import com.erp.common.exception.BusinessException;
import com.erp.system.entity.SysUser;
import com.erp.system.param.LoginParam;
import com.erp.system.service.IAuthService;
import com.erp.system.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * 认证Service实现类
 * 
 * @author ERP System
 * @since 2025-12-12
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService {

    private final ISysUserService userService;

    @Override
    public LoginResult login(LoginParam param) {
        // 参数校验
        if (StrUtil.isBlank(param.getUsername()) || StrUtil.isBlank(param.getPassword())) {
            throw new BusinessException("用户名或密码不能为空");
        }

        // 查询用户
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, param.getUsername());
        wrapper.eq(SysUser::getPassword, param.getPassword());
        SysUser user = userService.getOne(wrapper);

        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }

        // 验证密码(使用BCrypt加密算法)
//        if (!com.erp.common.util.PasswordUtil.matches(param.getPassword(), user.getPassword())) {
//            throw new BusinessException("用户名或密码错误");
//        }

        // 检查用户状态
        if (user.getStatus() == 0) {
            throw new BusinessException("用户已被禁用");
        }

        // 生成Token(简单实现,实际应该使用JWT)
        String token = generateToken(user);

        // 构建返回结果
        LoginResult.UserInfo userInfo = LoginResult.UserInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .build();

        return LoginResult.builder()
                .token(token)
                .user(userInfo)
                .build();
    }

    @Override
    public void logout() {
        // TODO: 清除token缓存
    }

    /**
     * 生成Token(简单实现)
     * 实际项目应该使用JWT
     */
    private String generateToken(SysUser user) {
        return "Bearer_" + UUID.randomUUID().toString().replace("-", "");
    }
}
