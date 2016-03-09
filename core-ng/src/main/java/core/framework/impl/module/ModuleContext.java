package core.framework.impl.module;

import core.framework.api.async.Executor;
import core.framework.api.http.HTTPMethod;
import core.framework.api.util.Lists;
import core.framework.api.util.Properties;
import core.framework.api.web.WebContext;
import core.framework.api.web.site.TemplateManager;
import core.framework.api.web.site.WebDirectory;
import core.framework.impl.async.ExecutorImpl;
import core.framework.impl.cache.CacheManager;
import core.framework.impl.inject.BeanFactory;
import core.framework.impl.inject.ShutdownHook;
import core.framework.impl.log.DefaultLoggerFactory;
import core.framework.impl.log.LogManager;
import core.framework.impl.scheduler.Scheduler;
import core.framework.impl.web.ControllerHolder;
import core.framework.impl.web.HTTPServer;
import core.framework.impl.web.management.HealthCheckController;
import core.framework.impl.web.management.MemoryUsageController;
import core.framework.impl.web.management.SchedulerController;
import core.framework.impl.web.management.ThreadInfoController;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author neo
 */
public final class ModuleContext {
    public final BeanFactory beanFactory;
    public final List<Runnable> startupHook = Lists.newArrayList();
    public final ShutdownHook shutdownHook = new ShutdownHook();
    public final Properties properties = new Properties();

    public final HTTPServer httpServer;
    public final ExecutorImpl executor;
    public final QueueManager queueManager = new QueueManager();
    public final LogManager logManager;
    public final MockFactory mockFactory;
    public CacheManager cacheManager;
    private Scheduler scheduler;
    private BackgroundTaskExecutor backgroundTask;

    public ModuleContext(BeanFactory beanFactory, MockFactory mockFactory) {
        this.beanFactory = beanFactory;
        this.mockFactory = mockFactory;

        this.logManager = ((DefaultLoggerFactory) LoggerFactory.getILoggerFactory()).logManager;
        if (!isTest()) {
            startupHook.add(logManager::start);
            shutdownHook.add(logManager::stop);
        }

        httpServer = new HTTPServer(logManager);
        beanFactory.bind(WebContext.class, null, httpServer.handler.webContext);
        beanFactory.bind(WebDirectory.class, null, httpServer.siteManager.webDirectory);
        beanFactory.bind(TemplateManager.class, null, httpServer.siteManager.templateManager);  // expose WebDirectory and TemplateManager to allow app handle template programmably, such as cms/widgets
        if (!isTest()) {
            startupHook.add(httpServer::start);
            shutdownHook.add(httpServer::stop);
        }
        executor = new ExecutorImpl(logManager);
        shutdownHook.add(executor::stop);

        beanFactory.bind(Executor.class, null, executor);

        if (!isTest()) {
            httpServer.handler.route.add(HTTPMethod.GET, "/health-check", new ControllerHolder(new HealthCheckController(), true));
            httpServer.handler.route.add(HTTPMethod.GET, "/_sys/memory", new ControllerHolder(new MemoryUsageController(), true));
            ThreadInfoController threadInfoController = new ThreadInfoController();
            httpServer.handler.route.add(HTTPMethod.GET, "/_sys/thread", new ControllerHolder(threadInfoController::threadUsage, true));
            httpServer.handler.route.add(HTTPMethod.GET, "/_sys/thread-dump", new ControllerHolder(threadInfoController::threadDump, true));
        }
    }

    public BackgroundTaskExecutor backgroundTask() {
        if (backgroundTask == null) {
            backgroundTask = new BackgroundTaskExecutor();
            if (!isTest()) {
                startupHook.add(backgroundTask::start);
                shutdownHook.add(backgroundTask::stop);
            }
        }
        return backgroundTask;
    }

    public Scheduler scheduler() {
        if (scheduler == null) {
            scheduler = new Scheduler(logManager);
            if (!isTest()) {
                startupHook.add(scheduler::start);
                shutdownHook.add(scheduler::stop);

                SchedulerController schedulerController = new SchedulerController(scheduler);
                httpServer.handler.route.add(HTTPMethod.GET, "/_sys/job", new ControllerHolder(schedulerController::listJobs, true));
                httpServer.handler.route.add(HTTPMethod.POST, "/_sys/job/:job", new ControllerHolder(schedulerController::triggerJob, true));
            }
        }
        return scheduler;
    }

    public boolean isTest() {
        return mockFactory != null;
    }
}
