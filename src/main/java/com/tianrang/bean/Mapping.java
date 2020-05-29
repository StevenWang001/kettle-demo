package com.tianrang.bean;

import java.util.Set;

public class Mapping {

    private Integer id;

    private Integer jobId;

    private String tableName;

    private String realTableName;

    private Set<Field> fields;

    public String[] getFieldNames() {
        String[] names = new String[fields.size()];
        int i = 0;
        for (Field field : fields) {
            names[i++] = field.getName();
        }
        return names;
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
        private Integer mappingId;
        private String name;
        private String type;
        private Integer length;
        private String path;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public Integer getMappingId() {
            return mappingId;
        }

        public void setMappingId(Integer mappingId) {
            this.mappingId = mappingId;
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

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        @Override
        public String toString() {
            return "Field{" +
                    "id='" + id + '\'' +
                    ", mappingId='" + mappingId + '\'' +
                    ", name='" + name + '\'' +
                    ", type='" + type + '\'' +
                    ", length=" + length +
                    ", path='" + path + '\'' +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "Mapping{" +
                "id='" + id + '\'' +
                ", jobId='" + jobId + '\'' +
                ", tableName='" + tableName + '\'' +
                ", realTableName='" + realTableName + '\'' +
                ", fields=" + fields +
                '}';
    }
}
