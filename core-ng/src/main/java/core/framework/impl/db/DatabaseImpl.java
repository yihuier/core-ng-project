package core.framework.impl.db;

import core.framework.api.db.Database;
import core.framework.api.db.Repository;
import core.framework.api.db.Transaction;
import core.framework.api.db.UncheckedSQLException;
import core.framework.api.log.ActionLogContext;
import core.framework.api.util.Exceptions;
import core.framework.api.util.Maps;
import core.framework.api.util.StopWatch;
import core.framework.impl.resource.Pool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

/**
 * @author neo
 */
public final class DatabaseImpl implements Database {
    public final Pool<Connection> pool;
    public final DatabaseOperation operation;

    private final Logger logger = LoggerFactory.getLogger(DatabaseImpl.class);
    private final Map<Class, RowMapper> rowMappers = Maps.newHashMap();
    private final ScalarRowMappers scalarRowMappers = new ScalarRowMappers();
    private final Properties driverProperties = new Properties();

    public int tooManyRowsReturnedThreshold = 1000;
    long slowQueryThresholdInMs = Duration.ofSeconds(5).toMillis();
    private Duration timeout;
    private Driver driver;
    private String url;

    public DatabaseImpl() {
        pool = new Pool<>(this::createConnection, Connection::close);
        pool.name("db");
        pool.size(5, 50);    // default optimization for AWS medium/large instances
        pool.maxIdleTime(Duration.ofHours(2));

        operation = new DatabaseOperation(pool);
        timeout(Duration.ofSeconds(30));
    }

    private Connection createConnection() {
        if (url == null) throw new Error("url must not be null");
        try {
            return driver.connect(url, driverProperties);
        } catch (SQLException e) {
            throw new UncheckedSQLException(e);
        }
    }

    public void close() {
        logger.info("close database client, url={}", url);
        pool.close();
    }

    public void user(String user) {
        driverProperties.put("user", user);
    }

    public void password(String password) {
        driverProperties.put("password", password);
    }

    public void timeout(Duration timeout) {
        this.timeout = timeout;
        operation.queryTimeoutInSeconds = (int) timeout.getSeconds();
        pool.checkoutTimeout(timeout);

        if (url != null && url.startsWith("jdbc:mysql:")) {
            driverProperties.put("connectTimeout", String.valueOf(timeout.toMillis()));
            driverProperties.put("socketTimeout", String.valueOf(timeout.toMillis()));
        }
    }

    public void url(String url) {
        if (!url.startsWith("jdbc:")) throw Exceptions.error("jdbc url must start with \"jdbc:\", url={}", url);

        logger.info("set database connection url, url={}", url);
        this.url = url;
        try {
            if (url.startsWith("jdbc:mysql://")) {
                driver = (Driver) Class.forName("com.mysql.jdbc.Driver").newInstance();
                timeout(timeout);
            } else if (url.startsWith("jdbc:hsqldb:")) {
                driver = (Driver) Class.forName("org.hsqldb.jdbc.JDBCDriver").newInstance();
            } else {
                throw Exceptions.error("not supported database, please contact arch team, url={}", url);
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new Error(e);
        }
    }

    public void slowQueryThreshold(Duration slowQueryThreshold) {
        slowQueryThresholdInMs = slowQueryThreshold.toMillis();
    }

    public <T> void view(Class<T> viewClass) {
        new DatabaseClassValidator(viewClass).validateViewClass();
        registerViewClass(viewClass);
    }

    public <T> Repository<T> repository(Class<T> entityClass) {
        StopWatch watch = new StopWatch();
        try {
            new DatabaseClassValidator(entityClass).validateEntityClass();
            RowMapper<T> mapper = registerViewClass(entityClass);
            return new RepositoryImpl<>(this, entityClass, mapper);
        } finally {
            logger.info("create db repository, entityClass={}, elapsedTime={}", entityClass.getCanonicalName(), watch.elapsedTime());
        }
    }

    @Override
    public Transaction beginTransaction() {
        return operation.transactionManager.beginTransaction();
    }

    @Override
    public <T> List<T> select(String sql, Class<T> viewClass, Object... params) {
        StopWatch watch = new StopWatch();
        List<T> results = null;
        try {
            results = operation.select(sql, rowMapper(viewClass), params);
            return results;
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("db", elapsedTime);
            logger.debug("select, sql={}, params={}, elapsedTime={}", sql, params, elapsedTime);
            if (elapsedTime > slowQueryThresholdInMs)
                logger.warn("slow query detected");
            if (results != null && results.size() > tooManyRowsReturnedThreshold)
                logger.warn("too many rows returned, returnedRows={}", results.size());
        }
    }

    @Override
    public <T> Optional<T> selectOne(String sql, Class<T> viewClass, Object... params) {
        StopWatch watch = new StopWatch();
        try {
            return operation.selectOne(sql, rowMapper(viewClass), params);
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("db", elapsedTime);
            logger.debug("selectOne, sql={}, params={}, elapsedTime={}", sql, params, elapsedTime);
            if (elapsedTime > slowQueryThresholdInMs)
                logger.warn("slow query detected");
        }
    }

    @Override
    public Optional<String> selectString(String sql, Object... params) {
        StopWatch watch = new StopWatch();
        try {
            return operation.selectOne(sql, scalarRowMappers.singleString, params);
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("db", elapsedTime);
            logger.debug("selectString, sql={}, params={}, elapsedTime={}", sql, params, elapsedTime);
            if (elapsedTime > slowQueryThresholdInMs)
                logger.warn("slow query detected");
        }
    }

    @Override
    public Optional<Integer> selectInt(String sql, Object... params) {
        StopWatch watch = new StopWatch();
        try {
            return operation.selectOne(sql, scalarRowMappers.singleInt, params);
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("db", elapsedTime);
            logger.debug("selectInt, sql={}, params={}, elapsedTime={}", sql, params, elapsedTime);
            if (elapsedTime > slowQueryThresholdInMs)
                logger.warn("slow query detected");
        }
    }

    @Override
    public Optional<Long> selectLong(String sql, Object... params) {
        StopWatch watch = new StopWatch();
        try {
            return operation.selectOne(sql, scalarRowMappers.singleLong, params);
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("db", elapsedTime);
            logger.debug("selectLong, sql={}, params={}, elapsedTime={}", sql, params, elapsedTime);
            if (elapsedTime > slowQueryThresholdInMs)
                logger.warn("slow query detected");
        }
    }

    @Override
    public int execute(String sql, Object... params) {
        StopWatch watch = new StopWatch();
        try {
            return operation.update(sql, params);
        } finally {
            long elapsedTime = watch.elapsedTime();
            ActionLogContext.track("db", elapsedTime);
            logger.debug("execute, sql={}, params={}, elapsedTime={}", sql, params, elapsedTime);
            if (elapsedTime > slowQueryThresholdInMs)
                logger.warn("slow query detected");
        }
    }

    private <T> RowMapper<T> rowMapper(Class<T> viewClass) {
        @SuppressWarnings("unchecked")
        RowMapper<T> mapper = rowMappers.get(viewClass);
        if (mapper == null)
            throw Exceptions.error("view class is not registered, please register in module by db().view(), viewClass={}", viewClass.getCanonicalName());
        return mapper;
    }


    private <T> RowMapper<T> registerViewClass(Class<T> viewClass) {
        if (rowMappers.containsKey(viewClass)) {
            throw Exceptions.error("duplicated view class found, viewClass={}", viewClass.getCanonicalName());
        }
        RowMapper<T> mapper = new RowMapperBuilder<>(viewClass, operation.enumMapper).build();
        rowMappers.put(viewClass, mapper);
        return mapper;
    }
}
