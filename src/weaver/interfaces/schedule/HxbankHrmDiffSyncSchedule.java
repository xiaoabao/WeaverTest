package weaver.interfaces.schedule;

import com.customization.hxbank.hrmsync.service.HrmDiffSyncService;
import com.weaver.qfengx.LogUtils;

/**
 * Created by YeShengtao on 2020/10/26 14:58
 */
public class HxbankHrmDiffSyncSchedule extends BaseCronJob {

    private LogUtils log = new LogUtils(HxbankHrmDiffSyncSchedule.class);

    private HrmDiffSyncService hrmDiffSyncService = new HrmDiffSyncService();

    @Override
    public void execute() {
        log.writeLog("====================开始人员组织差量同步====================");
        try {
            hrmDiffSyncService.diffSync();
            log.writeLog("同步成功");
        } catch (Exception e) {
            e.printStackTrace();
            log.writeLog("同步出现异常: " + e.getMessage());
        }
        log.writeLog("====================人员组织差量同步结束====================");
    }
}
