package com.chriniko.example.properties.filecreator.core;

import com.chriniko.example.properties.filecreator.core.util.PropertiesFileCreatorUtil;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

public class VariableLoadPropertiesFileCreatorTest implements PropertiesFileCreatorUtil {

    @Test
    public void execute() throws Exception {

        // given
        int noOfTenants = 50;
        int trafficTarget = 1100;
        VariableLoadPropertiesFileCreator creator = new VariableLoadPropertiesFileCreator();


        // when
        creator.execute(noOfTenants, trafficTarget);
        TimeUnit.MILLISECONDS.sleep(500);

        // then
        int acc = sumTrafficTargetOfAllPropertiesFiles(noOfTenants);
        assertEquals(trafficTarget, acc);

        // clear
        clearAllPropertiesFiles(noOfTenants);
    }
}