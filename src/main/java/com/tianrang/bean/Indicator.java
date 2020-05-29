package com.tianrang.bean;

import java.util.Set;
import java.util.stream.Collectors;

public class Indicator {

    private Integer id;

    private Integer jobId;

    private String name;

    private String desc;

    private String sql;

    private String realSql;

    private String interval;

    private String tableName;

    private String realTableName;

    private Set<Field> fields;

    public Set<String> getFieldNames() {
        return fields.stream().map(Field::getName).collect(Collectors.toSet());
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getJobId() {
        return jobId;
    }

    public void setJobId(Integer jobId) {
        this.jobId = jobId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public String getRealSql() {
        return realSql;
    }

    public void setRealSql(String realSql) {
        this.realSql = realSql;
    }

    public String getInterval() {
        return interval;
    }

    public void setInterval(String interval) {
        this.interval = interval;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getRealTableName() {
        return realTableName;
    }

    public void setRealTableName(String realTableName) {
        this.realTableName = realTableName;
    }

    public Set<Field> getFields() {
        return fields;
    }

    public void setFields(Set<Field> fields) {
        this.fields = fields;
    }

    public static class Field {
        private Integer id;
        private Integer indicatorId;
        private String name;
        private String type;
        private Integer length;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public Integer getIndicatorId() {
            return indicatorId;
        }

        public void setIndicatorId(Integer indicatorId) {
            this.indicatorId = indicatorId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Integer getLength() {
            return length;
        }

        public void setLength(Integer length) {
            this.length = length;
        }

        @Override
        public String toString() {
            return "Field{" +
                    "id='" + id + '\'' +
                    ", indicatorId='" + indicatorId + '\'' +
                    ", name='" + name + '\'' +
                    ", type='" + type + '\'' +
                    ", length=" + length +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "Indicator{" +
                "id='" + id + '\'' +
                ", jobId='" + jobId + '\'' +
                ", name='" + name + '\'' +
                ", desc='" + desc + '\'' +
                ", sql='" + sql + '\'' +
                ", realSql='" + realSql + '\'' +
                ", interval='" + interval + '\'' +
                ", tableName='" + tableName + '\'' +
                ", fields=" + fields +
                '}';
    }
}
