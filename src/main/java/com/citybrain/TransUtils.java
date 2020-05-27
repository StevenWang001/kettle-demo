package com.citybrain;

import org.apache.commons.lang.RandomStringUtils;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.scriptvalues_mod.ScriptValuesMetaMod;

import java.io.File;
import java.util.Arrays;

public class TransUtils {
    private static String JNDI_CONFIG = (new File(".").getAbsolutePath() + "/src/main/resources");


    public static void main(String[] args) throws Exception {

        loadKtr();
    }

    public static void loadKtr() throws KettleException {
        System.setProperty("KETTLE_JNDI_ROOT", JNDI_CONFIG);
        KettleEnvironment.init();
        TransMeta meta = new TransMeta("/Users/steven/Documents/json2db.ktr", (Repository) null);
        String[] declaredParameters = meta.listParameters();
        for ( int i = 0; i < declaredParameters.length; i++ ) {
            String parameterName = declaredParameters[i];

            // determine the parameter description and default values for display purposes
            String description = meta.getParameterDescription( parameterName );
            String defaultValue = meta.getParameterDefault( parameterName );
            // set the parameter value to an arbitrary string
            String parameterValue =  RandomStringUtils.randomAlphanumeric( 10 );

            String output = String.format( "Setting parameter %s to \"%s\" [description: \"%s\", default: \"%s\"]",
                    parameterName, parameterValue, description, defaultValue );
            System.out.println( output );

            // assign the value to the parameter on the transformation
            meta.setParameterValue( parameterName, parameterValue );
        }

        for (StepMeta m : meta.getStepsArray()) {
            if (m.getName().equalsIgnoreCase("scriptTrans")) {
                System.out.println(m.getTypeId());
                System.out.println(m.getStepMetaInterface().getClass().getName());

                ScriptValuesMetaMod metaMod = (ScriptValuesMetaMod)m.getStepMetaInterface();
                Arrays.stream(metaMod.getFieldname()).forEach(System.out::println);
                Arrays.stream(metaMod.getRename()).forEach(System.out::println);
                Arrays.stream(metaMod.getJSScripts()).forEach(System.out::println);
                for (boolean flag : metaMod.getReplace()) {
                    System.out.println(flag);
                }

                /*
                for (int type : metaMod.getType()) {
                    System.out.println("type " + type);
                }
                for ( String field : metaMod.getFieldname()) {
                    System.out.println(field);
                }
                for ( ScriptValuesScript js : metaMod.getJSScripts()) {
                    System.out.println(js.getScriptType() + " +++ " + js.getScriptName() + " +++ " + js.getScript());

                }*/
            }
        }
    }
}
