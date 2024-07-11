package com.x.program.center.schedule;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.x.base.core.container.EntityManagerContainer;
import com.x.base.core.container.factory.EntityManagerContainerFactory;
import com.x.base.core.project.logger.Logger;
import com.x.base.core.project.logger.LoggerFactory;
import com.x.program.center.Business;
import com.x.program.center.ThisApplication;
import com.x.program.center.yunzhijia.SyncOrganization;

public class YunzhijiaSyncOrganization extends BaseAction {
    private static Logger logger = LoggerFactory.getLogger(YunzhijiaSyncOrganization.class);

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            if (pirmaryCenter()) {
                try (EntityManagerContainer emc = EntityManagerContainerFactory.instance().create()) {
                    if (!ThisApplication.yunzhijiaSyncOrganizationCallbackRequest.isEmpty()) {
                        ThisApplication.yunzhijiaSyncOrganizationCallbackRequest.clear();
                        Business business = new Business(emc);
                        SyncOrganization o = new SyncOrganization();
                        o.execute(business);
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e);
            throw new JobExecutionException(e);
        }
    }
}
