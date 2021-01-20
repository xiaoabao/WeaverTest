package weaver.interfaces.schedule;

import com.customization.hxbank.hrmsync.service.HrmSyncService;
import weaver.general.BaseBean;

/**
 * Created by YeShengtao on 2020/7/29 10:56
 */
public class HxbankHrmSyncSchedule extends BaseCronJob {

    private static BaseBean baseBean = new BaseBean();
    private HrmSyncService hrmSyncService = new HrmSyncService();

    @Override
    public void execute() {
        baseBean.writeLog("====================开始人员组织全量同步====================");
        try {
            hrmSyncService.sync();
            baseBean.writeLog("同步成功");
        } catch (Exception e) {
            e.printStackTrace();
            baseBean.writeLog("同步出现异常: " + e.getMessage());
        }
        baseBean.writeLog("====================人员组织同步结束====================");
    }

}
