package com.example.shardingseataproxydemo.config;

import com.zaxxer.hikari.HikariDataSource;
import io.seata.rm.datasource.DataSourceProxy;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.mapping.DatabaseIdProvider;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.TableRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.strategy.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.shardingjdbc.api.ShardingDataSourceFactory;
import org.apache.shardingsphere.transaction.base.seata.at.SeataATShardingTransactionManager;
import org.apache.shardingsphere.transaction.core.ResourceDataSource;
import org.apache.shardingsphere.transaction.spi.ShardingTransactionManager;
import org.apache.shardingsphere.underlying.common.database.type.DatabaseTypes;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.boot.autoconfigure.MybatisProperties;
import org.mybatis.spring.boot.autoconfigure.SpringBootVFS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @ClassName ShardingJDBCConfig
 * @Author sipeng
 * @Date 2020/3/30 10:34 上午
 * @Version 1.0
 */
@Slf4j
@Configuration
@EnableTransactionManagement
public class ShardingJDBCConfig {

    // ～ Fields
    //========================================

    // 默认的数据源名
    private static final String DATASOURCE_NAME = "dataSource0";

    // ～ Sharding config bean
    //========================================


    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.hikari")
    public DataSource dataSource(DataSourceProperties properties) {
        HikariDataSource dataSource = createDataSource(properties,
                HikariDataSource.class);
        if (StringUtils.hasText(properties.getName())) {
            dataSource.setPoolName(properties.getName());
        }
        return dataSource;
    }

    /**
     * 创建指定类型的数据源
     *
     * @param properties
     * @param type
     * @param <T>
     * @return
     */
    protected static <T> T createDataSource(DataSourceProperties properties,
                                            Class<? extends DataSource> type) {
        return (T) properties.initializeDataSourceBuilder().type(type).build();
    }

    @Bean
    public ShardingTransactionManager buildShardingTransactionManager(final DataSource dataSource) {
        SeataATShardingTransactionManager seataATShardingTransactionManager = new SeataATShardingTransactionManager();
        seataATShardingTransactionManager.init(DatabaseTypes.getActualDatabaseType("MySQL"), Collections.singletonList(new ResourceDataSource(DATASOURCE_NAME, dataSource)));
        return seataATShardingTransactionManager;
    }

    /**
     * 构建分表策略
     *
     * @param dataSource
     * @return
     */
    public IotRiskCaptureInformationTableShardingAlgorithm buildIotRiskCaptureInformationTableShardingAlgorithm(DataSource dataSource) {
        return new IotRiskCaptureInformationTableShardingAlgorithm(dataSource);
    }

    /**
     * 配置数据源规则，即将单个数据源交给sharding-jdbc管理，并且可以设置默认的数据源，
     * 当表没有配置分库规则时会使用默认的数据源
     *
     * @param dataSource
     * @return
     */
    @Bean("shardingDatasource")
    public DataSource dataSourceRule(final DataSource dataSource) throws SQLException {
        Map<String, DataSource> dataSourceMap = new HashMap<>(); //设置分库映射
        dataSourceMap.put(DATASOURCE_NAME, dataSource);

        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTableRuleConfigs().add(captureInformationRuleConfiguration(dataSource));
        Properties properties = new Properties();
        properties.setProperty("sql.show", Boolean.TRUE.toString());
        DataSource shardingDatasource = ShardingDataSourceFactory.createDataSource(dataSourceMap, shardingRuleConfig, properties);
        // 整合seata分布式事务
        DataSourceProxy dataSourceProxy = new DataSourceProxy(shardingDatasource);

        log.info("init shardingDatasource success.");
        return dataSourceProxy;
    }

    /**
     * 手动指定事务管理器
     *
     * @param shardingDatasource
     * @return
     */
    @Bean
    public PlatformTransactionManager txManager(final @Qualifier("shardingDatasource") DataSource shardingDatasource) {
        return new DataSourceTransactionManager(shardingDatasource);
    }

    @Bean
    public JdbcTemplate jdbcTemplate(final @Qualifier("shardingDatasource") DataSource shardingDatasource) {
        return new JdbcTemplate(shardingDatasource);
    }

    /**
     * 抓拍图片库分表策略
     *
     * @return
     */
    private TableRuleConfiguration captureInformationRuleConfiguration(DataSource dataSource) {
        IotRiskCaptureInformationTableShardingAlgorithm iotRiskCaptureInformationTableShardingAlgorithm = buildIotRiskCaptureInformationTableShardingAlgorithm(dataSource);
        TableRuleConfiguration tableRuleConfiguration = new TableRuleConfiguration(IotRiskCaptureInformationTableShardingAlgorithm.DEFAULT_CAPTURE_TABLE, DATASOURCE_NAME.concat(".").concat(IotRiskCaptureInformationTableShardingAlgorithm.DEFAULT_CAPTURE_TABLE));
        tableRuleConfiguration.setTableShardingStrategyConfig(new StandardShardingStrategyConfiguration(IotRiskCaptureInformationTableShardingAlgorithm.DEFAULT_SHARDING_COLUMN_NAME, iotRiskCaptureInformationTableShardingAlgorithm));
        return tableRuleConfiguration;
    }


    // ～ jpa config bean
    //========================================

    // ～ MyBatis config bean
    //========================================


    @Autowired(required = false)
    private DatabaseIdProvider databaseIdProvider;

    @Autowired(required = false)
    private Interceptor[] interceptors;

    @Bean
    public SqlSessionFactory sqlSessionFactory(@Qualifier("shardingDatasource") DataSource dataSource, MybatisProperties properties) throws Exception {
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
        factory.setDataSource(dataSource);
        factory.setVfs(SpringBootVFS.class);
        if (StringUtils.hasText(properties.getConfigLocation())) {
            factory.setConfigLocation(resourceLoader.getResource(properties.getConfigLocation()));
        }
        factory.setConfiguration(properties.getConfiguration());
        if (!ObjectUtils.isEmpty(interceptors)) {
            factory.setPlugins(interceptors);
        }
        if (databaseIdProvider != null) {
            factory.setDatabaseIdProvider(databaseIdProvider);
        }
        if (StringUtils.hasLength(properties.getTypeAliasesPackage())) {
            factory.setTypeAliasesPackage(properties.getTypeAliasesPackage());
        }
        if (StringUtils.hasLength(properties.getTypeHandlersPackage())) {
            factory.setTypeHandlersPackage(properties.getTypeHandlersPackage());
        }
        if (!ObjectUtils.isEmpty(properties.resolveMapperLocations())) {
            factory.setMapperLocations(properties.resolveMapperLocations());
        }
        return factory.getObject();
    }

    @Bean
    public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory, MybatisProperties properties) {
        ExecutorType executorType = properties.getExecutorType();
        if (executorType != null) {
            return new SqlSessionTemplate(sqlSessionFactory, executorType);
        } else {
            return new SqlSessionTemplate(sqlSessionFactory);
        }
    }
}
