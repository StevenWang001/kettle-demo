package com.tianrang.trans;

import com.tianrang.bean.Job;
import com.tianrang.bean.Mapping;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.RowAdapter;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.jsoninput.JsonInputField;
import org.pentaho.di.trans.steps.jsoninput.JsonInputMeta;
import org.pentaho.di.trans.steps.rest.RestMeta;
import org.pentaho.di.trans.steps.rowgenerator.RowGeneratorMeta;
import org.pentaho.di.trans.steps.scriptvalues_mod.ScriptValuesMetaMod;
import org.pentaho.di.trans.steps.scriptvalues_mod.ScriptValuesScript;
import org.pentaho.di.trans.steps.tableoutput.TableOutputMeta;

public class HttpJson2DbTrans implements Runnable {

    private Job job;

    private int jobFieldsNum;

    public HttpJson2DbTrans(Job job) {
        this.job = job;
        this.jobFieldsNum = job.getMapping().getFields().size();
    }

    @Override
    public void run() {
        try {
            TransMeta transMeta = generateTransformation();
            System.out.println("executing transformation");
            Trans transformation = new Trans(transMeta);
            transformation.setLogLevel(LogLevel.MINIMAL);
            transformation.prepareExecution(new String[0]);

            RowAdapter rowAdapter = new RowAdapter() {
                private boolean firstRow = true;
                public void rowWrittenEvent(RowMetaInterface rowMeta, Object[] row) throws KettleStepException {
                    try {
                        if (firstRow) {
                            for (String title : rowMeta.getFieldNames()) {
                                System.out.print(title + "\t");
                            }
                            System.out.println();
//                            firstRow = false;
                        }
                        int cols = rowMeta.getFieldNames().length;
                        for (int i = 0; i < cols; i++) {
                            System.out.print(rowMeta.getString(row, i));
                            System.out.print("\t");
                        }
                        System.out.println();
                    } catch (KettleValueException e) {
                        e.printStackTrace();
                    }
                }
            };
            StepInterface monitor0 = transformation.getStepInterface("Rest", 0);
            monitor0.addRowListener(rowAdapter);

            StepInterface monitor = transformation.getStepInterface("JsonInput", 0);
            monitor.addRowListener(rowAdapter);

            StepInterface monitor2 = transformation.getStepInterface("dbOutput", 0);
            monitor2.addRowListener(rowAdapter);

            System.out.println("starting transformation");
            transformation.startThreads();
            transformation.waitUntilFinished();
            Result result = transformation.getResult();
            System.out.println(result);
            System.out.println("transformation result: " + result);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    private TransMeta generateTransformation() {
        try {
            System.out.println("generating a transformation definition");
            TransMeta transMeta = new TransMeta();
            transMeta.setName(job.getName());
            transMeta.setDescription(job.getDesc());

            RowGeneratorMeta rowGeneratorMeta = new RowGeneratorMeta();
            rowGeneratorMeta.setDefault();
            rowGeneratorMeta.setRowLimit("1");
            StepMeta rawGeneratorStep = new StepMeta("RowGenerator", "RowGenerator", rowGeneratorMeta);
            rawGeneratorStep.setLocation(0, 100);
            rawGeneratorStep.setDraw(true);
            transMeta.addStep(rawGeneratorStep);

            RestMeta restMeta = new RestMeta();
            restMeta.setDefault();
            restMeta.setUrl(job.getSource().getUrl());
            StepMeta restStep = new StepMeta("Rest", "Rest", restMeta);
            restStep.setLocation(100, 100);
            restStep.setDraw(true);
            transMeta.addStep(restStep);

            JsonInputMeta jsonInputMeta = new JsonInputMeta();
            jsonInputMeta.setDefault();
            jsonInputMeta.allocate(1, jobFieldsNum);
            jsonInputMeta.setInFields(true);
            jsonInputMeta.setFieldValue("result");
            jsonInputMeta.setRowLimit(1);

            JsonInputField[] fields = new JsonInputField[jobFieldsNum];
            int i = 0;
            for (Mapping.Field field : job.getMapping().getFields()) {
                JsonInputField jsonInputField = new JsonInputField(field.getName());
                jsonInputField.setPath(field.getPath());
                jsonInputField.setType(field.getType());
                fields[i++] = jsonInputField;
            }
            jsonInputMeta.setInputFields(fields);
            StepMeta jsonStep = new StepMeta("JsonInput", "JsonInput", jsonInputMeta);
            jsonStep.setLocation(200, 100);
            jsonStep.setDraw(true);
            transMeta.addStep(jsonStep);

            //script
            ScriptValuesMetaMod scriptMeta = new ScriptValuesMetaMod();
            scriptMeta.setDefault();
            scriptMeta.setFieldname(job.getMapping().getFieldNames());

            scriptMeta.setReplace(getJsFieldReplaces());
            scriptMeta.setType(getJsFieldTypes());

            ScriptValuesScript expression = new ScriptValuesScript();
            expression.setScriptName("js-transformation");
            expression.setScriptType(ScriptValuesScript.TRANSFORM_SCRIPT);
            expression.setScript(job.getJsCode());

            ScriptValuesScript scripts[] = {expression};
            scriptMeta.setJSScripts(scripts);
            scriptMeta.afterInjection();
            StepMeta scriptStep = new StepMeta("ScriptValueMod", "scriptTrans", scriptMeta);
            scriptStep.setLocation(300, 100);
            scriptStep.setDraw(true);
            transMeta.addStep(scriptStep);

            DatabaseMeta dbMeta = new DatabaseMeta();
            dbMeta.setDefault();
            dbMeta.setName("dbOutput");
//
            dbMeta.setUsername("postgres");
            dbMeta.setPassword("123456");
            dbMeta.setHostname("localhost");
            dbMeta.setDBPort("5432");
            dbMeta.setDBName("tianrang");
            dbMeta.setDatabaseType("PostgreSQL");
//
//            dbMeta.setAccessType(DatabaseMeta.TYPE_ACCESS_JNDI);

            TableOutputMeta tableOutputMeta = new TableOutputMeta();
            tableOutputMeta.setDatabaseMeta(dbMeta);
            tableOutputMeta.setCommitSize(10);
            String[] tableField = job.getMapping().getFieldNames();
            tableOutputMeta.setFieldDatabase(tableField);
            tableOutputMeta.setFieldStream(tableField);
            tableOutputMeta.setSpecifyFields(true);
            tableOutputMeta.setTruncateTable(true);
            tableOutputMeta.setTableName(job.getMapping().getRealTableName());
            StepMeta dbStep = new StepMeta("InsertUpdate", "dbOutput", tableOutputMeta);
            dbStep.setLocation(500, 100);
            dbStep.setDraw(true);
            transMeta.addStep(dbStep);

            transMeta.addTransHop(new TransHopMeta(rawGeneratorStep, restStep));
            transMeta.addTransHop(new TransHopMeta(restStep, jsonStep));
            transMeta.addTransHop(new TransHopMeta(jsonStep, scriptStep));
            transMeta.addTransHop(new TransHopMeta(scriptStep, dbStep));

//            Write2KtrFile.write("/Users/steven/Documents/json2db.ktr", transMeta.getXML());
            return transMeta;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    private boolean[] getJsFieldReplaces() {
        boolean[] replaces = new boolean[jobFieldsNum];
        for (int i = 0; i < jobFieldsNum; i++) {
            replaces[i] = true;
        }
        return replaces;
    }

    private int[] getJsFieldTypes() {
        int[] types = new int[jobFieldsNum];
        int i = 0;
        for (Mapping.Field field : job.getMapping().getFields()) {
            types[i++] = ValueMetaInterface.getTypeCode(field.getType());
        }
        return types;
    }
}
