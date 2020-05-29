package com.tianrang.dao;

import com.tianrang.bean.Indicator;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface IndicatorMapper {

    @Select("select * from indicator where job_id=#{jobId}")
    @Results({
            @Result(property = "jobId", column = "job_id"),
            @Result(property = "tableName", column = "table_name"),
            @Result(property = "realTableName", column = "real_table_name"),
            @Result(property = "realSql", column = "real_sql")
    })
    List<Indicator> getByJobId(@Param("jobId") int jobId);

    @SelectKey(statement = "SELECT id from indicator where job_id=#{jobId} order by id desc limit 1", keyProperty = "id", keyColumn = "id", before = false, resultType = int.class)
    @Insert("INSERT into indicator(job_id, name, description, sql, real_sql, interval, table_name, real_table_name) VALUES(#{jobId}, #{name}, #{desc}, #{sql}, #{realSql}, #{interval}, #{tableName}, #{realTableName})")
    void insert(Indicator indicator);
}
