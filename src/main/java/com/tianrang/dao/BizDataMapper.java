package com.tianrang.dao;

import org.apache.ibatis.annotations.*;

import java.util.Map;

@Mapper
public interface BizDataMapper {

    @Select("${sql}")
    Map<String, Object> query(@Param("sql") String querySql);

    @Update("CREATE TABLE IF NOT EXISTS ${tableName} (${fields})")
    void createTable(@Param("tableName") String tableName, @Param("fields") String fields);

    @Insert("INSERT INTO ${tableName}(${fields}) VALUES(${values})")
    void insert(@Param("tableName") String tableName, @Param("fields") String fields, @Param("values") String values);
}
