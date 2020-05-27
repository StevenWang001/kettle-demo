package com.citybrain;

import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.repository.kdr.KettleDatabaseRepository;
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

import java.io.File;

public class HttpJsonInputTest {
    private static String JNDI_CONFIG = (new File(".").getAbsolutePath() + "/src/main/resources");

    public static void main(String[] args) throws KettleException {
        jsonTest();
    }

    public static void jsonTest() throws KettleException {
        try {
            System.setProperty("KETTLE_JNDI_ROOT", JNDI_CONFIG);
            KettleEnvironment.init();
            KettleDatabaseRepository repository = new KettleDatabaseRepository();
            TransMeta transMeta = generateTransformation();
            System.out.println("executing transformation");
            Trans transformation = new Trans(transMeta);
            transformation.setLogLevel(LogLevel.MINIMAL);
            transformation.prepareExecution(new String[0]);
            //get transformation result


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

            System.out.println("\nstarting transformation");
            transformation.startThreads();
            transformation.waitUntilFinished();
            Result result = transformation.getResult();
            System.out.println(result);
            System.out.println("transformation result: " + result);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static TransMeta generateTransformation() {
        try {

            System.out.println("generating a transformation definition");
            TransMeta transMeta = new TransMeta();
            transMeta.setName("transformation demo");
            System.out.println("adding json input");

            RowGeneratorMeta rowGeneratorMeta = new RowGeneratorMeta();
            rowGeneratorMeta.setDefault();
            rowGeneratorMeta.setRowLimit("1");
            StepMeta rawGeneratorStep = new StepMeta("RowGenerator", "RowGenerator", rowGeneratorMeta);
            rawGeneratorStep.setLocation(-100, 100);
            rawGeneratorStep.setDraw(true);
            transMeta.addStep(rawGeneratorStep);

            RestMeta restMeta = new RestMeta();
            restMeta.setDefault();
            restMeta.setUrl("http://127.0.0.1:8082/api/job");
            StepMeta restStep = new StepMeta("Rest", "Rest", restMeta);
            restStep.setLocation(0, 100);
            restStep.setDraw(true);
            transMeta.addStep(restStep);

            JsonInputMeta jsonInputMeta = new JsonInputMeta();
            jsonInputMeta.setDefault();
            jsonInputMeta.allocate(1, 2);
            jsonInputMeta.setInFields(true);
            jsonInputMeta.setFieldValue("result");
            jsonInputMeta.setRowLimit(1);
//            jsonInputMeta.setReadUrl(false);
//            String[] filename = new String[]{"/Users/steven/Documents/scheduleJob.json"};
//            String[] filename = new String[]{"http://127.0.0.1:8082/api/job"};
//            jsonInputMeta.setFileName(filename);
            JsonInputField nameField = new JsonInputField("name");
            nameField.setPath("$..name");
            nameField.setType("String");

            JsonInputField idField = new JsonInputField("age");
            idField.setPath("$..id");
            idField.setType("Integer");

            JsonInputField[] fields = new JsonInputField[2];
            fields[0] = nameField;
            fields[1] = idField;
            jsonInputMeta.setInputFields(fields);
//            jsonInputMeta.setRowLimit(1);
            StepMeta jsonStep = new StepMeta("JsonInput", "JsonInput", jsonInputMeta);
            jsonStep.setLocation(100, 100);
            jsonStep.setDraw(true);
//            System.out.println(jsonStep.getXML());
            transMeta.addStep(jsonStep);

            //script
            ScriptValuesMetaMod scriptAge = new ScriptValuesMetaMod();
            scriptAge.setDefault();
            String[] inputField = {"name", "age"};
            scriptAge.setFieldname(inputField);
            String[] outputField = {"name2", "age2"};
            scriptAge.setRename(outputField);
            int[] lengths = {inputField[0].length(), inputField[1].length()};
            scriptAge.setLength(lengths);
            int[] precisions = {-1, -1};
            scriptAge.setPrecision(precisions);


            scriptAge.setReplace(new boolean[]{false, false});
            int[] types = {ValueMetaInterface.TYPE_STRING, ValueMetaInterface.TYPE_INTEGER};
            scriptAge.setType(types);

            ScriptValuesScript expression1 = new ScriptValuesScript();
            expression1.setScriptName("filed1-transformation");
            expression1.setScriptType(ScriptValuesScript.TRANSFORM_SCRIPT);
            expression1.setScript("var name = '2' + name; var age = 20 + age;");

            ScriptValuesScript scripts[] = {expression1};
            scriptAge.setJSScripts(scripts);
            scriptAge.afterInjection();
            StepMeta scriptStep = new StepMeta("ScriptValueMod", "scriptTrans", scriptAge);
            scriptStep.setLocation(300, 100);
            scriptStep.setDraw(true);
            transMeta.addStep(scriptStep);

            DatabaseMeta dbMeta = new DatabaseMeta();
            dbMeta.setName("dbOutput");

            dbMeta.setUsername("root");
            dbMeta.setPassword("123456");
            dbMeta.setHostname("localhost");
            dbMeta.setDBPort("3306");
            dbMeta.setDBName("demo");
            dbMeta.setDatabaseType("MySQL");

            dbMeta.setAccessType(DatabaseMeta.TYPE_ACCESS_JNDI);

            TableOutputMeta tableOutputMeta = new TableOutputMeta();
            tableOutputMeta.setDatabaseMeta(dbMeta);
            tableOutputMeta.setCommitSize(10);
            String[] tableField = {"name", "age"};
            String[] tableField2 = {"name2", "age2"};
            tableOutputMeta.setFieldDatabase(tableField);
            tableOutputMeta.setFieldStream(tableField2);
            tableOutputMeta.setSpecifyFields(true);
            tableOutputMeta.setTableName("csv_input");
            StepMeta dbStep = new StepMeta("InsertUpdate", "dbOutput", tableOutputMeta);
            dbStep.setLocation(500, 100);
            dbStep.setDraw(true);
            transMeta.addStep(dbStep);

            transMeta.addTransHop(new TransHopMeta(rawGeneratorStep, restStep));
            transMeta.addTransHop(new TransHopMeta(restStep, jsonStep));
            transMeta.addTransHop(new TransHopMeta(jsonStep, scriptStep));
            transMeta.addTransHop(new TransHopMeta(scriptStep, dbStep));

            Write2KtrFile.write("/Users/steven/Documents/json2db.ktr", transMeta.getXML());
            return transMeta;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
