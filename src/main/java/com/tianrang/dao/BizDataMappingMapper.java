package com.tianrang.dao;

import com.tianrang.bean.Mapping;
import org.apache.ibatis.annotations.*;

@Mapper
public interface BizDataMappingMapper {

    @Select("select * from biz_data_mapping where job_id=#{jobId}")
    @Results({
            @Result(property = "jobId", column = "job_id"),
            @Result(property = "tableName", column = "table_name"),
            @Result(property = "realTableName", column = "real_table_name")
    })
    Mapping getByJobId(@Param("jobId") int jobId);

    @SelectKey(statement = "SELECT id from biz_data_mapping where job_id=#{jobId}", keyProperty = "id", keyColumn = "id", before = false, resultType = int.class)
    @Insert("INSERT into biz_data_mapping(job_id, table_name, real_table_name) VALUES(#{jobId}, #{tableName}, #{realTableName})")
    void insertBizDataMapping(Mapping mapping);
}
