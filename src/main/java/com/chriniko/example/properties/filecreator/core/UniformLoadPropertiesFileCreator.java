package com.chriniko.example.properties.filecreator.core;

import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@Component
public class UniformLoadPropertiesFileCreator implements PropertiesFileCreator {

    private final String filename = "dbPopulatorForTenant%d.properties";

    // Note: in the future it will be nice if more values of the following properties were pass as input arguments.
    private final String fileContentsToCreateMultipleVersionsOfThis
            = "# jdbc.X\n" +
            "jdbc.driverClassName=com.mysql.cj.jdbc.Driver\n" +
            "jdbc.url=jdbc:mysql://localhost:3306/db_populator_example_%d?createDatabaseIfNotExist=true&useSSL=false\n" +
            "jdbc.user=root\n" +
            "jdbc.pass=nikos\n" +
            "\n" +
            "\n" +
            "# hibernate.X\n" +
            "hibernate.dialect=org.hibernate.dialect.MySQL5Dialect\n" +
            "hibernate.show_sql=false\n" +
            "hibernate.hbm2ddl.auto=create-drop\n" +
            "\n" +
            "\n" +
            "# dbpopulator.X\n" +
            "\n" +
            "# how many threads will handle the traffic target.\n" +
            "dbpopulator.concurrency=1\n" + // Note: change depending on your needs.
            "\n" +
            "# duration is in seconds.\n" +
            "dbpopulator.duration=60\n" +
            "\n" +
            "# traffic target is the amount of records to populate the db with.\n" +
            "dbpopulator.trafficTarget=%d\n" + // Note: change depending on your needs. (eg: 1100 traffic across 50 tenants, so 1100/50=22)
            "\n" +
            "# equal distribute missed work across populator workers.\n" +
            "dbpopulator.equal.distribution=true";

    @Override
    public void execute(int noOfTenants, int trafficTarget) {

        boolean continueOnError = false;

        for (int i = 1; i <= noOfTenants; i++) {

            try {

                createGameInjectorFile(i, trafficTarget / noOfTenants);

            } catch (Exception error) {

                System.err.println("GameInjectorUniformPropertiesFileCreator#execute --- error occurred: " + error);

                if (!continueOnError) {
                    throw new RuntimeException(error);
                }
            }
        }

    }

    private void createGameInjectorFile(int id, int trafficTargetPerTenant) throws IOException {

        // Note: prepare...
        String readyFileContentsToCreateMultipleVersionsOfThis = String.format(
                fileContentsToCreateMultipleVersionsOfThis,
                id,
                trafficTargetPerTenant
        );

        String readyFilename = String.format(filename, id);


        // Note: open file,etc...
        Path path = Paths.get(readyFilename);
        Files.deleteIfExists(path);


        // Note: write contents...
        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(path, StandardOpenOption.CREATE_NEW)) {
            bufferedWriter.write(readyFileContentsToCreateMultipleVersionsOfThis);
        }
    }

}
