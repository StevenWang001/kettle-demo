package com.tianrang;

import com.tianrang.bean.Indicator;
import com.tianrang.bean.Job;
import com.tianrang.bean.Mapping;
import com.tianrang.dao.*;
import com.tianrang.indicator.IndicatorService;
import com.tianrang.trans.HttpJson2DbTrans;
import javafx.util.Pair;
import org.pentaho.di.core.KettleEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class JobScheduler implements CommandLineRunner {

    @Autowired
    private JobMapper jobMapper;

    @Autowired
    private BizDataMappingMapper bizDataMappingMapper;

    @Autowired
    private BizDataMappingFieldsMapper bizDataMappingFieldsMapper;

    @Autowired
    private IndicatorMapper indicatorMapper;

    @Autowired
    private IndicatorFieldsMapper indicatorFieldsMapper;

    @Autowired
    private BizDataMapper bizDataMapper;

    @Autowired
    private IndicatorService indicatorService;

    @Override
    public void run(String... args) throws Exception {
        KettleEnvironment.init();

        Job job = insertBizMetaData("convert_json_job.yml");
        createTable(job);
        execJob(job);

//        job = insertBizMetaData("convert_json_job2.yml");
//        createTable(job);
//        execJob(job);
//
//        job = insertBizMetaData("convert_json_job3.yml");
//        createTable(job);
//        execJob(job);
    }

    private void execJob(Job job) {

        HttpJson2DbTrans trans = new HttpJson2DbTrans(job);
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        Pair<Integer, TimeUnit> intervalPair = JobUtils.parseInterval(job.getInterval());
        scheduledExecutorService.scheduleAtFixedRate(trans, 2, intervalPair.getKey(), intervalPair.getValue());

        indicatorService.launchIndicatorExecutors(job);
    }

    private void createTable(Job job) {
        //1. create biz data table
        StringBuilder fields = new StringBuilder("id serial primary key");
        for (Mapping.Field field : job.getMapping().getFields()) {
            fields.append(", ")
                    .append(field.getName()).append(" ")
                    .append("String".equals(field.getType()) ? "varchar" : "int")
                    .append((null == field.getLength() || 0 == field.getLength()) ? "" : "(" + field.getLength() + ")");
        }
        System.out.println(job.getMapping().getRealTableName());
        System.out.println(fields);
        bizDataMapper.createTable(job.getMapping().getRealTableName(), fields.toString());

        //2. create indicator table
        for (Indicator indicator : job.getIndicators()) {
            StringBuilder indicatorFields = new StringBuilder("id serial primary key");
            for (Indicator.Field field : indicator.getFields()) {
                indicatorFields.append(", ")
                        .append(field.getName()).append(" ")
                        .append("String".equals(field.getType()) ? "varchar" : "int")
                        .append((null == field.getLength() || 0 == field.getLength()) ? "" : "(" + field.getLength() + ")");
            }
            indicatorFields.append(", created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP");
            System.out.println(indicator.getRealTableName());
            System.out.println(indicatorFields);
            bizDataMapper.createTable(indicator.getRealTableName(), indicatorFields.toString());
        }
    }


    private Job insertBizMetaData(String filename) throws Exception {
        Job job = ParseJob.parse(new ClassPathResource(filename).getInputStream());

        Job oldJob = jobMapper.getByName(job.getName());
        if (null == oldJob) {
            jobMapper.insertJob(job);
        } else {
            job.setId(oldJob.getId());
        }
        job.getMapping().setJobId(job.getId());
        System.out.println(job);

        //1. insert biz meta data
        Mapping oldMapping = bizDataMappingMapper.getByJobId(job.getId());
        if (null == oldMapping) {
            job.getMapping().setRealTableName(JobUtils.getRandomTableName("m"));
            bizDataMappingMapper.insertBizDataMapping(job.getMapping());
        } else {
            job.getMapping().setId(oldMapping.getId());
            job.getMapping().setRealTableName(oldMapping.getRealTableName());
        }

        Set<Mapping.Field> oldFields = bizDataMappingFieldsMapper.getFieldsByMappingId(job.getMapping().getId());
        if (CollectionUtils.isEmpty(oldFields)) {
            for (Mapping.Field field : job.getMapping().getFields()) {
                field.setMappingId(job.getMapping().getId());
                bizDataMappingFieldsMapper.insertBizDataMappingField(field);
            }
        }

        //2. insert indicator meta data
        List<Indicator> indicators = indicatorMapper.getByJobId(job.getId());
        if (CollectionUtils.isEmpty(indicators)) {
            for (Indicator indicator : job.getIndicators()) {
                indicator.setJobId(job.getId());
                indicator.setRealTableName(JobUtils.getRandomTableName("i"));
                indicator.setRealSql(indicator.getSql().replaceAll(job.getMapping().getTableName(), job.getMapping().getRealTableName()));
                indicatorMapper.insert(indicator);
            }
        } else {
            job.setIndicators(indicators);
        }

        for (Indicator indicator : job.getIndicators()) {
            Set<Indicator.Field> oldIndicatorFields = indicatorFieldsMapper.getFieldsByIndicatorId(indicator.getId());
            if (CollectionUtils.isEmpty(oldIndicatorFields)) {
                for (Indicator.Field field : indicator.getFields()) {
                    field.setIndicatorId(indicator.getId());
                    indicatorFieldsMapper.insert(field);
                }
            } else {
                indicator.setFields(oldIndicatorFields);
            }
        }
        System.out.println(job);
        return job;
    }
}
