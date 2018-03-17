package com.chriniko.example.properties.filecreator;

import com.chriniko.example.properties.filecreator.configuration.AppConfiguration;
import com.chriniko.example.properties.filecreator.core.UniformLoadPropertiesFileCreator;
import com.chriniko.example.properties.filecreator.core.VariableLoadPropertiesFileCreator;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Application {

    private static final String UNIFORM_MODE = "uniform";
    private static final String VARIABLE_LOAD_MODE = "variable-load";


    /*
        Note:

        [add as first arg] --> provide number of tenants
        [add as second arg] --> provide type of generation (uniform or variable-load)
        [add as third arg] --> provide number of traffic target (eg: 400,500,1100)

     */
    public static void main(String[] args) {

        final ApplicationContext context = new AnnotationConfigApplicationContext(AppConfiguration.class);

        firstValidation(args);
        secondValidation(args);

        String typeOfGeneration = args[1];

        if (typeOfGeneration.equals(UNIFORM_MODE)) {
            context.getBean(UniformLoadPropertiesFileCreator.class)
                    .execute(Integer.parseInt(args[0]), Integer.parseInt(args[2]));
        }

        if (typeOfGeneration.equals(VARIABLE_LOAD_MODE)) {
            context.getBean(VariableLoadPropertiesFileCreator.class)
                    .execute(Integer.parseInt(args[0]), Integer.parseInt(args[2]));
        }
    }

    private static void firstValidation(String[] args) {
        if (args.length != 3) {

            //args[0]
            System.out.println("[add as first arg] --> provide number of tenants");

            //args[1]
            System.out.println("[add as second arg] --> provide type of generation (uniform or variable-load)");

            //args[2]
            System.out.println("[add as third arg] --> provide number of traffic target (eg: 400,500,1100)");


            System.exit(-1);
        }
    }

    private static void secondValidation(String[] args) {

        try {
            Integer.parseInt(args[0]);
        } catch (NumberFormatException error) {

            System.out.println("number of tenants should be an integer");
            System.exit(-2);
        }

        String typeOfGeneration = args[1];
        if (!typeOfGeneration.equals("uniform") && !typeOfGeneration.equals("variable-load")) {

            System.out.println("[add as second arg] --> provide type of generation (uniform or variable-load)");

            System.exit(-2);
        }

        try {
            Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            System.out.println("number of traffic target should be an integer");
            System.exit(-2);
        }
    }

}
