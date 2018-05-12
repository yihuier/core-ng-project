package core.framework.module;

import core.framework.impl.module.ModuleContext;
import core.framework.util.Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.Optional;

/**
 * @author neo
 */
public abstract class Module {
    private final Logger logger = LoggerFactory.getLogger(Module.class);

    protected ModuleContext context;

    protected void load(Module module) {
        logger.info("load module, module={}", module.getClass().getName());
        module.context = context;
        module.initialize();
    }

    public void onShutdown(Runnable runnable) {
        context.shutdownHook.add(runnable);
    }

    public void onStartup(Runnable runnable) {
        context.startupHook.add(runnable);
    }

    public <T> T bind(Class<T> instanceClass) {
        T instance = context.beanFactory.create(instanceClass);
        context.beanFactory.bind(instanceClass, null, instance);
        return instance;
    }

    public <T> T bind(T instance) {
        return bind(instance.getClass(), null, instance);
    }

    public <T> T bind(Class<? super T> instanceClass, T instance) {
        return bind(instanceClass, null, instance);
    }

    public <T> T bind(Type type, String name, T instance) {
        context.beanFactory.inject(instance);
        context.beanFactory.bind(type, name, instance);
        return instance;
    }

    public <T> T bean(Type instanceType, String name) {
        return context.beanFactory.bean(instanceType, name);
    }

    public <T> T bean(Class<T> instanceType) {
        return bean(instanceType, null);
    }

    public void loadProperties(String classpath) {
        logger.info("load properties, classpath={}", classpath);
        context.propertyManager.properties.load(classpath);
    }

    public Optional<String> property(String key) {
        return context.propertyManager.property(key);
    }

    public String requiredProperty(String key) {
        return property(key).orElseThrow(() -> Exceptions.error("property key not found, key={}", key));
    }

    public LogConfig log() {
        return context.config(LogConfig.class, null);
    }

    public HTTPConfig http() {
        return context.config(HTTPConfig.class, null);
    }

    public RouteConfig route() {
        return new RouteConfig(context);
    }

    public SiteConfig site() {
        return context.config(SiteConfig.class, null);
    }

    public CacheConfig cache() {
        return context.config(CacheConfig.class, null);
    }

    public SchedulerConfig schedule() {
        return context.config(SchedulerConfig.class, null);
    }

    public ExecutorConfig executor() {
        return context.config(ExecutorConfig.class, null);
    }

    public APIConfig api() {
        return context.config(APIConfig.class, null);
    }

    public DBConfig db() {
        return db(null);
    }

    public DBConfig db(String name) {
        return context.config(DBConfig.class, name);
    }

    public RedisConfig redis() {
        return context.config(RedisConfig.class, null);
    }

    public MongoConfig mongo() {
        return mongo(null);
    }

    public MongoConfig mongo(String name) {
        return context.config(MongoConfig.class, name);
    }

    public KafkaConfig kafka() {
        return kafka(null);
    }

    public KafkaConfig kafka(String name) {
        return context.config(KafkaConfig.class, name);
    }

    public <T> T config(Class<T> configClass) {
        return config(configClass, null);
    }

    public <T> T config(Class<T> configClass, String name) {
        return context.config(configClass, name);
    }

    protected abstract void initialize();
}
