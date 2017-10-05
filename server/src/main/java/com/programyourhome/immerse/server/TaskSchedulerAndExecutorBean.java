package com.programyourhome.immerse.server;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

@Component
public class TaskSchedulerAndExecutorBean extends ThreadPoolTaskScheduler {

    private static final long serialVersionUID = 1L;

    private final Log log = LogFactory.getLog(this.getClass());

    private void init() {
        // TODO: dynamic configuration and sane values?
        this.getScheduledThreadPoolExecutor().setCorePoolSize(10);
        this.getScheduledThreadPoolExecutor().setMaximumPoolSize(100);
        this.setAwaitTerminationSeconds(5);
        // TODO: proper error handling. Is this scheduled task now dead?
        this.setErrorHandler(throwable -> this.log.error("Exception occured in scheduled or executed task.", throwable));
    }

    @Override
    // So we can force our own init to run after the super class initialize.
    public void initialize() {
        super.initialize();
        this.init();
    }
}
