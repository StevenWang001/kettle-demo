package com.tianrang.indicator;

import com.tianrang.JobUtils;
import com.tianrang.bean.Indicator;
import com.tianrang.bean.Job;
import com.tianrang.dao.BizDataMapper;
import javafx.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class IndicatorService {

    @Autowired
    private BizDataMapper bizDataMapper;

    public void launchIndicatorExecutors(Job job) {
        for (Indicator indicator : job.getIndicators()) {
            ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
            Pair<Integer, TimeUnit> pair = JobUtils.parseInterval(indicator.getInterval());
            scheduledExecutorService.scheduleAtFixedRate(new IndicatorTask(indicator), 3, pair.getKey(), pair.getValue());
        }
    }

    class IndicatorTask implements Runnable {

        private Indicator indicator;

        IndicatorTask(Indicator indicator) {
            this.indicator = indicator;
        }

        @Override
        public void run() {
            try {
                System.out.println("xxxxxxxxxxxxxxxxxx indicator calculate in");
                Map<String, Object> map = bizDataMapper.query(indicator.getRealSql());

                StringBuilder fields = new StringBuilder();
                StringBuilder values = new StringBuilder();

                boolean hasValue = false;
                for (Map.Entry<String, Object> entry : map.entrySet()) {
                    for (Indicator.Field field : indicator.getFields()) {
                        if (field.getName().equals(entry.getKey())) {
                            fields.append(field.getName()).append(",");
                            values.append("Integer".equals(field.getType()) ? (Long) entry.getValue() : (String) entry.getValue()).append(",");
                            hasValue = true;
                            break;
                        }
                    }
                }
                if (!hasValue) {
                    return;
                }
                fields.deleteCharAt(fields.length() - 1);
                values.deleteCharAt(values.length() - 1);
                bizDataMapper.insert(indicator.getRealTableName(), fields.toString(), values.toString());
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }
}
