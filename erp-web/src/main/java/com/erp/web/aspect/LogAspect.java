package com.erp.web.aspect;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import com.erp.common.constant.SessionKeys;
import com.erp.common.core.result.Result;
import com.erp.common.util.ServletUtils;
import com.erp.system.annotation.Log;
import com.erp.system.entity.SysOperationLog;
import com.erp.system.enums.BusinessType;
import com.erp.system.enums.OperatorType;
import com.erp.system.service.ISysOperationLogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 操作日志切面
 *
 * <p>设计取向与 RuoYi 那类实现不同：<b>不要求逐个接口标注 {@code @Log}</b>，
 * 而是切住 {@code com.erp.web.controller} 下所有 public 方法，自动判断哪些值得记：</p>
 * <ul>
 *   <li>所有非 GET 请求（POST/PUT/PATCH/DELETE）默认记录；</li>
 *   <li>GET 里只有 {@code /export} 记（导出行为有审计价值），其余查询一律不记
 *       —— 否则日志表会被列表页的翻页请求瞬间刷爆；</li>
 *   <li>{@code /auth/**} 跳过，登录/登出记录到 {@code sys_login_log}，两边不重复。</li>
 * </ul>
 *
 * <p>{@code @Log} 因此变成「覆盖项」：只在自动推导不合适时使用，例如
 * 「分配菜单权限」这类标题更有审计价值的方法，或含密码需要
 * {@code saveParam = false} 的方法。</p>
 *
 * <p><b>写日志失败绝不影响业务</b>：{@code record} 内部已吞掉异常，
 * 切面本身也不再做额外处理。</p>
 *
 * @author ERP System
 * @since 2026-10-03
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LogAspect {

    private final ISysOperationLogService operationLogService;

    private final ObjectMapper objectMapper;

    /**
     * 请求参数入库前的最大长度（oper_param 是 text，但不该塞进一个表单全文）
     */
    private static final int MAX_PARAM_LENGTH = 2000;

    /**
     * 返回结果入库前的最大长度
     */
    private static final int MAX_RESULT_LENGTH = 2000;

    /**
     * 错误消息入库前的最大长度
     */
    private static final int MAX_ERROR_LENGTH = 2000;

    /**
     * 需要脱敏的 JSON 键（键名里含这些词的，值统一替换成 ***）
     *
     * <p>登录接口已经走 {@code /auth/**} 跳过，但改密码、重置密码这类接口
     * 仍可能把明文密码带进日志，所以这里再加一道兜底。</p>
     */
    private static final Pattern SENSITIVE_JSON = Pattern.compile(
            "(\"[^\"]*(?:password|passwd|pwd|secret|token)[^\"]*\"\\s*:\\s*)\"[^\"]*\"",
            Pattern.CASE_INSENSITIVE);

    /**
     * Controller 类名 → 中文模块名
     *
     * <p>日志列表页的「操作模块」列直接显示这里的值。新增 Controller 时
     * 若忘了登记，标题会退化成类名（如 {@code BaseBrandController}），
     * 不美观但不影响功能。</p>
     */
    private static final Map<String, String> MODULE_NAMES = Map.ofEntries(
            Map.entry("BaseBrandController", "品牌管理"),
            Map.entry("BaseCustomerController", "客户管理"),
            Map.entry("BaseMaterialCategoryController", "物料分类"),
            Map.entry("BaseMaterialController", "物料信息"),
            Map.entry("BaseSupplierController", "供应商管理"),
            Map.entry("BaseUnitController", "计量单位"),
            Map.entry("BaseWarehouseController", "仓库管理"),
            Map.entry("ChartController", "统计报表"),
            Map.entry("CustomerCheckSheetController", "客户对账单"),
            Map.entry("CustomerFeeSheetController", "客户费用单"),
            Map.entry("CustomerPreSheetController", "客户预收单"),
            Map.entry("CustomerSettleSheetController", "客户结算单"),
            Map.entry("ProductStockLogController", "库存流水"),
            Map.entry("PurchaseOrderController", "采购订单"),
            Map.entry("PurchaseReceiptController", "采购入库"),
            Map.entry("PurchaseReturnController", "采购退货"),
            Map.entry("RetailConfigController", "零售配置"),
            Map.entry("RetailOutSheetController", "零售出库"),
            Map.entry("RetailReturnController", "零售退货"),
            Map.entry("SaleDeliveryController", "销售发货"),
            Map.entry("SaleOrderController", "销售订单"),
            Map.entry("SaleReturnController", "销售退货"),
            Map.entry("SettleCheckSheetController", "结算对账单"),
            Map.entry("SettleFeeSheetController", "结算费用单"),
            Map.entry("SettleInOutItemController", "收支项目"),
            Map.entry("SettlePreSheetController", "结算预收付单"),
            Map.entry("SettleSheetController", "结算单"),
            Map.entry("StockAdjustReasonController", "库存调整原因"),
            Map.entry("StockAdjustSheetController", "库存调整单"),
            Map.entry("StockCheckController", "库存盘点"),
            Map.entry("StockController", "库存查询"),
            Map.entry("StockCostAdjustController", "库存成本调整"),
            Map.entry("StockTransferController", "库存调拨"),
            Map.entry("StockWarningController", "库存预警"),
            Map.entry("SysDeptController", "部门管理"),
            Map.entry("SysDictController", "字典管理"),
            Map.entry("SysLoginLogController", "登录日志"),
            Map.entry("SysMenuController", "菜单管理"),
            Map.entry("SysOperationLogController", "操作日志"),
            Map.entry("SysRoleController", "角色管理"),
            Map.entry("SysUserController", "用户管理"),
            Map.entry("TakeStockConfigController", "盘点参数设置")
    );

    /**
     * 切住所有 Controller 的 public 方法
     */
    @Pointcut("execution(public * com.erp.web.controller..*.*(..))")
    public void controllerPointcut() {
    }

    /**
     * 环绕通知：记录操作人、URL、参数、结果、耗时与异常
     *
     * @param point 连接点
     * @return 原方法返回值
     * @throws Throwable 原方法抛出的异常原样向上抛，不改变业务语义
     */
    @Around("controllerPointcut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        HttpServletRequest request = ServletUtils.getRequest();
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        Log annotation = resolveAnnotation(method);

        // 不满足记录条件时直接放行，不产生任何额外开销
        if (!shouldRecord(request, annotation)) {
            return point.proceed();
        }

        SysOperationLog record = new SysOperationLog();
        record.setTitle(resolveTitle(method, annotation));
        record.setBusinessType(resolveBusinessType(request, annotation));
        record.setMethod(cut(method.getDeclaringClass().getSimpleName() + "." + method.getName() + "()", 200));
        record.setRequestMethod(request.getMethod());
        record.setOperatorType(OperatorType.MANAGE.getValue());
        record.setOperName(resolveOperatorName());
        record.setOperUrl(cut(request.getRequestURI(), 500));
        record.setOperIp(ServletUtils.getClientIp(request));
        if (annotation == null || annotation.saveParam()) {
            record.setOperParam(serializeArgs(point.getArgs()));
        }

        long start = System.currentTimeMillis();
        try {
            Object result = point.proceed();
            record.setStatus(0);
            record.setJsonResult(resolveResultSummary(result, annotation));
            return result;
        } catch (Throwable e) {
            record.setStatus(1);
            String message = e.getMessage() == null ? e.getClass().getName() : e.getMessage();
            record.setErrorMsg(cut(message, MAX_ERROR_LENGTH));
            throw e;
        } finally {
            record.setCostTime(System.currentTimeMillis() - start);
            // record 内部已吞异常，这里不需要再 try/catch
            operationLogService.record(record);
        }
    }

    /**
     * 取方法级注解，没有则退到类级注解
     */
    private Log resolveAnnotation(Method method) {
        Log methodLevel = method.getAnnotation(Log.class);
        return methodLevel != null ? methodLevel : method.getDeclaringClass().getAnnotation(Log.class);
    }

    /**
     * 判断这次调用是否值得写日志
     *
     * @param request    当前请求
     * @param annotation 方法/类上的 {@code @Log}，非空时强制记录
     * @return true 表示需要记录
     */
    private boolean shouldRecord(HttpServletRequest request, Log annotation) {
        if (request == null) {
            return false;
        }
        if (annotation != null) {
            return true;
        }
        String uri = request.getRequestURI();
        if (uri == null) {
            return false;
        }
        // 登录/登出单独记在 sys_login_log
        if (uri.contains("/auth/")) {
            return false;
        }
        if (!"GET".equals(request.getMethod())) {
            return true;
        }
        // GET 里只记导出，查询类不记，否则翻页会把日志表刷爆
        return uri.endsWith("/export");
    }

    /**
     * 推导业务类型：注解优先，其次按 URL 后缀，最后按 HTTP 方法
     */
    private Integer resolveBusinessType(HttpServletRequest request, Log annotation) {
        if (annotation != null && annotation.businessType() != BusinessType.OTHER) {
            return annotation.businessType().getValue();
        }
        String uri = request.getRequestURI();
        if (uri != null) {
            if (uri.endsWith("/import")) {
                return BusinessType.IMPORT.getValue();
            }
            if (uri.endsWith("/export")) {
                return BusinessType.EXPORT.getValue();
            }
        }
        return switch (request.getMethod()) {
            case "POST" -> BusinessType.INSERT.getValue();
            case "PUT", "PATCH" -> BusinessType.UPDATE.getValue();
            case "DELETE" -> BusinessType.DELETE.getValue();
            default -> BusinessType.OTHER.getValue();
        };
    }

    /**
     * 推导操作模块：注解 title 优先，其次按 Controller 类名查表，最后退化成类名
     */
    private String resolveTitle(Method method, Log annotation) {
        if (annotation != null && StrUtil.isNotBlank(annotation.title())) {
            return cut(annotation.title(), 50);
        }
        String controller = method.getDeclaringClass().getSimpleName();
        return cut(MODULE_NAMES.getOrDefault(controller, controller), 50);
    }

    /**
     * 取当前操作人登录名
     *
     * <p>从 Sa-Token 会话里读，避免每条日志都回查数据库。未登录时
     * （例如某些刻意放开的端点）返回「匿名」。</p>
     */
    private String resolveOperatorName() {
        try {
            if (StpUtil.isLogin()) {
                String username = StpUtil.getSession().getString(SessionKeys.USERNAME);
                if (StrUtil.isNotBlank(username)) {
                    return cut(username, 50);
                }
                return cut(String.valueOf(StpUtil.getLoginIdDefaultNull()), 50);
            }
        } catch (Exception e) {
            log.debug("获取当前登录用户失败，操作人记为匿名", e);
        }
        return "匿名";
    }

    /**
     * 序列化请求参数
     *
     * <p>跳过流、响应对象与上传文件 —— 它们既不可序列化，也没有审计意义。
     * 最后统一做敏感字段脱敏与长度截断。</p>
     *
     * @param args 方法入参
     * @return JSON 文本，无有效参数时返回 null
     */
    private String serializeArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return null;
        }
        List<Object> serializable = new ArrayList<>();
        for (Object arg : args) {
            if (arg == null
                    || arg instanceof ServletRequest
                    || arg instanceof ServletResponse
                    || arg instanceof MultipartFile
                    || arg instanceof MultipartFile[]
                    || arg instanceof InputStream
                    || arg instanceof OutputStream) {
                continue;
            }
            serializable.add(arg);
        }
        if (serializable.isEmpty()) {
            return null;
        }
        String json = toJson(serializable);
        return cut(SENSITIVE_JSON.matcher(json).replaceAll("$1\"***\""), MAX_PARAM_LENGTH);
    }

    /**
     * 生成返回结果摘要
     *
     * <p>默认只留 {@code Result} 里的 code 与 message —— 对审计来说
     * 「这次操作成功没有、报了什么错」才是关键，完整返回体既占地方又没价值。
     * 确需完整结果时用 {@code @Log(saveResult = true)} 打开。</p>
     *
     * @param result     原方法返回值
     * @param annotation 注解
     * @return JSON 文本或 null
     */
    private String resolveResultSummary(Object result, Log annotation) {
        if (result == null) {
            return null;
        }
        if (annotation != null && annotation.saveResult()) {
            return cut(toJson(result), MAX_RESULT_LENGTH);
        }
        if (result instanceof Result<?> wrapper) {
            Map<String, Object> summary = new LinkedHashMap<>();
            summary.put("code", wrapper.getCode());
            summary.put("message", wrapper.getMessage());
            return cut(toJson(summary), MAX_RESULT_LENGTH);
        }
        return null;
    }

    /**
     * 安全序列化：失败时退化为 toString，绝不因为日志把业务搞挂
     */
    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            return String.valueOf(value);
        }
    }

    /**
     * null 安全截断，超出长度的部分直接丢弃
     */
    private static String cut(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() <= max ? value : value.substring(0, max);
    }
}
