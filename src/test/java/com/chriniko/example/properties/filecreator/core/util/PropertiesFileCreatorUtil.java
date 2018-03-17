package com.chriniko.example.properties.filecreator.core.util;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public interface PropertiesFileCreatorUtil {

    default int sumTrafficTargetOfAllPropertiesFiles(int noOfTenants) throws Exception {

        int acc = 0;
        for (int i = 1; i <= noOfTenants; i++) {

            try (InputStream resourceAsStream = new FileInputStream("./dbPopulatorForTenant" + i + ".properties")) {

                Properties properties = new Properties();
                properties.load(resourceAsStream);

                String trafficTargetPerPropertyFile = properties.getProperty("dbpopulator.trafficTarget");
                int trafficTargetPerPropertyFileAsInt = Integer.parseInt(trafficTargetPerPropertyFile);
                acc += trafficTargetPerPropertyFileAsInt;

            }
        }

        return acc;
    }

    default void clearAllPropertiesFiles(int noOfTenants) throws Exception {
        for (int i = 1; i <= noOfTenants; i++) {
            Files.deleteIfExists(Paths.get("./dbPopulatorForTenant" + i + ".properties"));
        }
    }
}
