package com.example.shardingseataproxydemo.config;


import com.google.common.collect.Range;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.api.sharding.standard.RangeShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.RangeShardingValue;
import org.springframework.util.CollectionUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据表分表规则
 *
 * @ClassName TableShardingAlgorithm
 * @Author sipeng
 * @Date 2020/3/30 10:35 上午
 * @Version 1.0
 */
@Slf4j
public class IotRiskCaptureInformationTableShardingAlgorithm implements PreciseShardingAlgorithm<String>, RangeShardingAlgorithm<String> {

    // ～ Fields / Constructors
    //========================================

    // 默认表名
    public static final String DEFAULT_CAPTURE_TABLE = "iot_risk_capture_information";

    // 切片依据字段
    public static final String DEFAULT_SHARDING_COLUMN_NAME = "group_id";

    public static final Map<String, String> alreadyTableName = new ConcurrentHashMap<>();

    DataSource dataSource;

    public IotRiskCaptureInformationTableShardingAlgorithm(DataSource dataSource) {
        this.dataSource = dataSource;
        init();
    }


    // ～ Methods
    //========================================

    public void init() {
        try {
            Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement("select table_name from information_schema.TABLES where TABLE_SCHEMA=(select database())");
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String tableName = resultSet.getString(1);
                if (tableName.contains(DEFAULT_CAPTURE_TABLE)) {
                    alreadyTableName.put(tableName, tableName);
                } else if(log.isDebugEnabled()){
                    log.debug("find table {} but is not sharding table.", tableName);
                }
            }
        } catch (SQLException e) {
            log.error("IotRiskCaptureInformationTableShardingAlgorithm init failed.", e);
        }
    }

    /**
     * 精确的分片算法
     *
     * @param collection           可用的数据源或表名称
     * @param preciseShardingValue 分片依据
     * @return 数据源或表名称的分片结果
     */
    @Override
    public String doSharding(Collection<String> collection, PreciseShardingValue<String> preciseShardingValue) {
        String tableName = DEFAULT_CAPTURE_TABLE.concat("_").concat(preciseShardingValue.getValue());
        return alreadyTableName.containsKey(tableName) ? tableName : DEFAULT_CAPTURE_TABLE;
    }

    /**
     * 范围分片算法, 会根据表名来反向判断需要查询的表，
     * 如果出现找不到的数据，则会在默认表中进行查询
     *
     * @param collection    可用的数据源或表名称
     * @param shardingValue 分片依据
     * @return 数据源或表名称的分片结果
     */
    @Override
    public Collection<String> doSharding(Collection<String> collection, RangeShardingValue<String> shardingValue) {
        List<String> result = new ArrayList<>();
        boolean isDefault = false;
        Range<String> valueRange = shardingValue.getValueRange();
        for (String dbName : alreadyTableName.keySet()) {
            if (valueRange.contains(dbName.substring(dbName.indexOf("_")))) {
                result.add(dbName);
            } else {
                isDefault = true;
            }
        }
        if (CollectionUtils.isEmpty(result) || isDefault) {
            result.add(DEFAULT_CAPTURE_TABLE);
        }
        return result;
    }
}
