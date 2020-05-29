package com.tianrang.dao;

import com.tianrang.bean.Job;
import org.apache.ibatis.annotations.*;

@Mapper
public interface JobMapper {

    @Select("select * from job where name=#{name}")
    @Results({
            @Result(property = "jsCode", column = "js_code")
    })
    Job getByName(@Param("name") String name);

    @SelectKey(statement = "SELECT id from job where name=#{name}", keyProperty = "id", keyColumn = "id", before = false, resultType = int.class)
    @Insert("INSERT into job(name, description, interval, js_code, source_url, source_type) VALUES(#{name}, #{desc}, #{interval}, #{jsCode}, #{source.url}, #{source.type})")
    void insertJob(Job job);
}
