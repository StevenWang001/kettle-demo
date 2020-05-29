package com.tianrang.dao;

import com.tianrang.bean.Mapping;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Set;

@Mapper
public interface BizDataMappingFieldsMapper {

    @Select("select * from biz_data_mapping_fields where mapping_id=#{mappingId}")
    Set<Mapping.Field> getFieldsByMappingId(@Param("mappingId") int mappingId);

    @Insert("INSERT into biz_data_mapping_fields(mapping_id, name, type, length, path) VALUES(#{mappingId}, #{name}, #{type}, #{length}, #{path})")
    void insertBizDataMappingField(Mapping.Field field);
}
