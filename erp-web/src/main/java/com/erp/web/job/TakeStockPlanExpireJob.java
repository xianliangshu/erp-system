package com.erp.web.job;

import com.erp.business.service.ITakeStockPlanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 盘点任务超时自动作废
 *
 * <p>规则来源：{@code take_stock_config.cancel_hours}（盘点参数页可配）。
 * 只把<b>「任务生成」</b>状态且创建时间早于 {@code now - cancel_hours} 的任务置为「已作废」；
 * 已生成差异的任务不动 —— 它的差异已经落成盘点单，作废任务会让盘点单失去来源。</p>
 *
 * <p>默认 10 分钟扫一次。扫描不到超时任务时不做任何写操作，开销可忽略。
 * 关闭方式：把盘点参数里的「超时作废小时数」置 0 或负数，任务会直接跳过。</p>
 *
 * @author ERP System
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TakeStockPlanExpireJob {

    private final ITakeStockPlanService takeStockPlanService;

    @Scheduled(cron = "0 0/10 * * * ?")
    public void cancelExpiredPlans() {
        try {
            int count = takeStockPlanService.cancelExpired();
            if (count > 0) {
                log.info("盘点任务超时自动作废：{} 条", count);
            }
        } catch (Exception e) {
            // 定时任务绝不能把异常抛出去，否则后续调度会被取消
            log.error("盘点任务超时作废执行失败", e);
        }
    }
}
