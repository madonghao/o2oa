package com.x.program.center.schedule;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.x.base.core.project.logger.Logger;
import com.x.base.core.project.logger.LoggerFactory;
import com.x.program.center.ThisApplication;

public class YunzhijiaSyncOrganizationTrigger extends BaseAction {
    private static Logger logger = LoggerFactory.getLogger(YunzhijiaSyncOrganizationTrigger.class);

    /* 向列表发送一个同步信号 */
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            if (pirmaryCenter()) {
                ThisApplication.yunzhijiaSyncOrganizationCallbackRequest.add(new Object());
            }
        } catch (Exception e) {
            logger.error(e);
            throw new JobExecutionException(e);
        }
    }

}
