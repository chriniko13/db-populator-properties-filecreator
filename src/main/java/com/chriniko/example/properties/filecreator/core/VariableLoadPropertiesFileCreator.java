package com.chriniko.example.properties.filecreator.core;

import com.chriniko.example.properties.filecreator.domain.VariableLoadLine;
import com.chriniko.example.properties.filecreator.domain.VariableLoadRecord;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.stream.IntStream;

@Component
public class VariableLoadPropertiesFileCreator implements PropertiesFileCreator {

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

    // Note: in the future this will be nice to get it from a file.
    private final VariableLoadRecord variableLoadRecord = new VariableLoadRecord(

            Arrays.asList(

                    /* Note: 80% of tenants will generate the 20% of traffic
                     *
                     *  So for example if we have 50 tenants and the target traffic is 400,
                     *  then the above translates to the following:
                     *
                     *  40 tenants ---> 80 traffic to generate (so each tenant will gen. 80 / 40 == 2)
                     *
                     *
                     * */
                    new VariableLoadLine(80, 20),

                    /* Note: 20% of tenants will generate the 80% of traffic
                     *
                     *
                     *  So for example if we have 50 tenants and the target traffic is 400,
                     *  then the above translates to the following:
                     *
                     *  10 tenants ---> 320 traffic to generate (so each tenant will gen. 320 / 10 == 32)
                     *
                     *
                     * */
                    new VariableLoadLine(20, 80)

            )
    );

    // Note: in the future this will be nice to pass it as an input argument.
    private final boolean ifItIsUnbalancedWorkDoEqualDistribution = true;

    @Override
    public void execute(int noOfTenants, int trafficTarget) {

        // Note: in order to construct the filenames correctly.
        int tenantsPropertiesFileCreatorWalker = 0;

        for (VariableLoadLine variableLoadLine : variableLoadRecord.getVariableLoadLines()) {


            // Note: find out number of tenants and traffic to generate.
            int proportionOfTenants = variableLoadLine.getProportionOfTenants();
            int trafficToGenerate = variableLoadLine.getTrafficToGenerate();


            // Note: now for each tenant which is in above calculated proportion group generate the properties file.
            int numberOfTenantsToGenerateTraffic = (proportionOfTenants * noOfTenants) / 100;
            double numberOfTrafficToGenerate = (trafficToGenerate * trafficTarget) / 100;

            if (numberOfTrafficToGenerate < numberOfTenantsToGenerateTraffic) {
                throw new UnsupportedOperationException("Error ---> numberOfTrafficToGenerate < numberOfTenantsToGenerateTraffic");
            }

            double trafficPerTenant = numberOfTrafficToGenerate / numberOfTenantsToGenerateTraffic;

            boolean balancedWork = (trafficPerTenant % 1) == 0;

            if (balancedWork) {

                /*
                    Note:   because it is a balanced work the casting here is safe,
                            meaning we will not have decimal parts.
                */
                int trafficPerTenantSafe = (int) trafficPerTenant;

                tenantsPropertiesFileCreatorWalker = balancedWorkProcessing(
                        tenantsPropertiesFileCreatorWalker,
                        numberOfTenantsToGenerateTraffic,
                        trafficPerTenantSafe);

            } else {

                tenantsPropertiesFileCreatorWalker = unbalancedWorkProcessing(
                        tenantsPropertiesFileCreatorWalker,
                        numberOfTenantsToGenerateTraffic,
                        trafficPerTenant);

            }
        }
    }

    private int balancedWorkProcessing(int tenantsPropertiesFileCreatorWalker,
                                       int numberOfTenantsToGenerateTraffic,
                                       int trafficPerTenant) {

        for (int i = 1; i <= numberOfTenantsToGenerateTraffic; i++) {

            int correctIdx = i + tenantsPropertiesFileCreatorWalker;

            // Note: populate the file with the correct values.
            String readyFileContentsToCreateMultipleVersionsOfThis = String.format(
                    fileContentsToCreateMultipleVersionsOfThis,
                    correctIdx,
                    trafficPerTenant
            );


            // Note: create file.
            try {
                createGameInjectorFile(readyFileContentsToCreateMultipleVersionsOfThis, correctIdx);
            } catch (IOException error) {
                System.err.println("GameInjectorVariableLoadPropertiesFileCreator#execute --- error occurred: " + error);
                throw new RuntimeException(error);
            }
        }

        tenantsPropertiesFileCreatorWalker += numberOfTenantsToGenerateTraffic;
        return tenantsPropertiesFileCreatorWalker;
    }

    private int unbalancedWorkProcessing(int tenantsPropertiesFileCreatorWalker,
                                         int numberOfTenantsToGenerateTraffic,
                                         double trafficPerTenant) {

        /*
                Note: keep in mind that this is not a balanced work, so with this cast
                      we are losing decimal parts, but we will take care of this.
            */
        final int trafficPerTenantSafe = (int) trafficPerTenant;


        // Note: first calculate the additional work needed...
        double missedWork = trafficPerTenant % 1;
        double totalMissedWork = 0;

        for (int i = 1; i <= numberOfTenantsToGenerateTraffic; i++) {
            totalMissedWork += missedWork;
        }

        // Note: then split it equally across all game injectors or load it to only one.
        if (ifItIsUnbalancedWorkDoEqualDistribution) {

            tenantsPropertiesFileCreatorWalker = unbalancedWorkProcessingWithEqualDistribution(
                    tenantsPropertiesFileCreatorWalker,
                    numberOfTenantsToGenerateTraffic,
                    trafficPerTenantSafe,
                    totalMissedWork
            );

        } else {

            tenantsPropertiesFileCreatorWalker = unbalancedWorkProcessingWithNoEqualDistribution(
                    tenantsPropertiesFileCreatorWalker,
                    numberOfTenantsToGenerateTraffic,
                    trafficPerTenantSafe,
                    totalMissedWork
            );
        }

        return tenantsPropertiesFileCreatorWalker;
    }

    private int unbalancedWorkProcessingWithEqualDistribution(int tenantsPropertiesFileCreatorWalker,
                                                              int numberOfTenantsToGenerateTraffic,
                                                              int trafficPerTenantSafe,
                                                              double totalMissedWork) {

        final Integer[] trafficPerTenants = new Integer[numberOfTenantsToGenerateTraffic];
        IntStream
                .range(0, numberOfTenantsToGenerateTraffic)
                .forEach(idx -> trafficPerTenants[idx] = trafficPerTenantSafe);

        int trafficPerTenantsWalker = 0;
        while (totalMissedWork != 0) {

            // Note: steal one from total missed work.
            trafficPerTenants[trafficPerTenantsWalker] = trafficPerTenants[trafficPerTenantsWalker] + 1;

            totalMissedWork--; // Note: we steal...so remove one also.
            trafficPerTenantsWalker++;
        }

        for (int i = 1; i <= numberOfTenantsToGenerateTraffic; i++) {

            int correctIdx = i + tenantsPropertiesFileCreatorWalker;

            // Note: populate the file with the correct values.
            String readyFileContentsToCreateMultipleVersionsOfThis = String.format(
                    fileContentsToCreateMultipleVersionsOfThis,
                    correctIdx,
                    trafficPerTenants[i - 1]
            );


            // Note: create file.
            try {
                createGameInjectorFile(readyFileContentsToCreateMultipleVersionsOfThis, correctIdx);
            } catch (IOException error) {
                System.err.println("GameInjectorVariableLoadPropertiesFileCreator#execute --- error occurred: " + error);
                throw new RuntimeException(error);
            }
        }

        tenantsPropertiesFileCreatorWalker += numberOfTenantsToGenerateTraffic;
        return tenantsPropertiesFileCreatorWalker;
    }


    private int unbalancedWorkProcessingWithNoEqualDistribution(int tenantsPropertiesFileCreatorWalker,
                                                                int numberOfTenantsToGenerateTraffic,
                                                                int trafficPerTenantSafe,
                                                                double totalMissedWork) {

        for (int i = 1; i <= numberOfTenantsToGenerateTraffic; i++) {

            boolean isLastTenant = i == numberOfTenantsToGenerateTraffic;
            int correctIdx = i + tenantsPropertiesFileCreatorWalker;

            // Note: populate the file with the correct values.
            String readyFileContentsToCreateMultipleVersionsOfThis;

            if (!isLastTenant) {

                readyFileContentsToCreateMultipleVersionsOfThis = String.format(
                        fileContentsToCreateMultipleVersionsOfThis,
                        correctIdx,
                        trafficPerTenantSafe);
            } else {

                // Note: send the total missed work to the last tenant.
                int correctTrafficPerTenantSafe = (int) (trafficPerTenantSafe + totalMissedWork);

                readyFileContentsToCreateMultipleVersionsOfThis = String.format(
                        fileContentsToCreateMultipleVersionsOfThis,
                        correctIdx,
                        correctTrafficPerTenantSafe);
            }

            // Note: create file.
            try {
                createGameInjectorFile(readyFileContentsToCreateMultipleVersionsOfThis, correctIdx);
            } catch (IOException error) {
                System.err.println("GameInjectorVariableLoadPropertiesFileCreator#execute --- error occurred: " + error);
                throw new RuntimeException(error);
            }
        }

        tenantsPropertiesFileCreatorWalker += numberOfTenantsToGenerateTraffic;
        return tenantsPropertiesFileCreatorWalker;
    }

    private void createGameInjectorFile(String readyFileContentsToCreateMultipleVersionsOfThis,
                                        int id)
            throws IOException {

        // Note: prepare...
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
