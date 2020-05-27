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
import org.pentaho.di.trans.steps.csvinput.CsvInputMeta;
import org.pentaho.di.trans.steps.scriptvalues_mod.ScriptValuesMetaMod;
import org.pentaho.di.trans.steps.scriptvalues_mod.ScriptValuesScript;
import org.pentaho.di.trans.steps.tableoutput.TableOutputMeta;
import org.pentaho.di.trans.steps.textfileinput.TextFileInputField;

import java.io.File;

public class CsvInputTest {
    private static String JNDI_CONFIG = (new File(".").getAbsolutePath() + "/src/main/resources");

    public static void main(String[] args) throws KettleException {
        csvTest();
    }

    public static void csvTest() throws KettleException {
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


            StepInterface monitor = transformation.getStepInterface("scriptTrans", 0);
            RowAdapter rowAdapter = new RowAdapter() {
                private boolean firstRow = true;
                public void rowWrittenEvent(RowMetaInterface rowMeta, Object[] row) throws KettleStepException {
                    try {
                        if (firstRow) {
                            for (String title : rowMeta.getFieldNames()) {
                                System.out.print(title + "\t");
                            }
                            System.out.println();
                            firstRow = false;
                        }
                        System.out.print(rowMeta.getString(row, 0));
                        System.out.print("\t");
                        System.out.print(rowMeta.getString(row, 1));
                        System.out.print("\t");
                        System.out.print(rowMeta.getString(row, 2));
                        System.out.print("\t");
                        System.out.println(rowMeta.getInteger(row, 3));

                    } catch (KettleValueException e) {
                        e.printStackTrace();
                    }
                }
            };
            monitor.addRowListener(rowAdapter);

            StepInterface monitor2 = transformation.getStepInterface("dbOutput", 0);
            RowAdapter rowAdapter2 = new RowAdapter() {
                private boolean firstRow = true;
                public void rowWrittenEvent(RowMetaInterface rowMeta, Object[] row) throws KettleStepException {
                    try {
                        if (firstRow) {
                            for (String title : rowMeta.getFieldNames()) {
                                System.out.print(title + "\t");
                            }
                            System.out.println();
                            firstRow = false;
                        }
                        System.out.print(rowMeta.getString(row, 0));
                        System.out.print("\t");
                        System.out.println(rowMeta.getString(row, 1));

                    } catch (KettleValueException e) {
                        e.printStackTrace();
                    }
                }
            };
            monitor2.addRowListener(rowAdapter2);

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
            System.out.println("adding CSV input");

            CsvInputMeta csvInputMeta = new CsvInputMeta();
            csvInputMeta.setDefault();
            csvInputMeta.setFilename("test.csv");
            csvInputMeta.setDelimiter(",");
            csvInputMeta.setBufferSize("10");

            TextFileInputField nameField = new TextFileInputField();
            nameField.setName("name");
            nameField.setPosition(0);
            nameField.setType(ValueMetaInterface.TYPE_STRING);
            TextFileInputField ageField = new TextFileInputField();
            ageField.setName("age");
            ageField.setPosition(1);
            ageField.setType(ValueMetaInterface.TYPE_INTEGER);
            TextFileInputField[] fields = new TextFileInputField[2];
            fields[0] = nameField;
            fields[1] = ageField;
            csvInputMeta.setInputFields(fields);
            StepMeta csvStep = new StepMeta("1", "csvInput", csvInputMeta);
            transMeta.addStep(csvStep);

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
            expression1.setScript("var name = '2';var age = 20;");

//            ScriptValuesScript expression2 = new ScriptValuesScript();
//            expression2.setScriptName("filed2-transformation");
//            expression2.setScriptType(ScriptValuesScript.TRANSFORM_SCRIPT);
//            expression2.setScript("var age4 = 20;");

            ScriptValuesScript scripts[] = {expression1};
            scriptAge.setJSScripts(scripts);
            scriptAge.afterInjection();
            StepMeta scriptStep = new StepMeta("1.1", "scriptTrans", scriptAge);
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
            StepMeta dbStep = new StepMeta("2", "dbOutput", tableOutputMeta);

            transMeta.addStep(dbStep);

            transMeta.addTransHop(new TransHopMeta(csvStep, scriptStep));
            transMeta.addTransHop(new TransHopMeta(scriptStep, dbStep));

            return transMeta;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
