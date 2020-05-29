package com.tianrang.dao;

import com.tianrang.bean.Indicator;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Set;

@Mapper
public interface IndicatorFieldsMapper {

    @Select("select * from indicator_fields where indicator_id=#{indicatorId}")
    Set<Indicator.Field> getFieldsByIndicatorId(@Param("indicatorId") int indicatorId);

    @Insert("INSERT into indicator_fields(indicator_id, name, type, length) VALUES(#{indicatorId}, #{name}, #{type}, #{length})")
    void insert(Indicator.Field field);
}
