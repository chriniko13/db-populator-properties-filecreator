package com.chriniko.example.properties.filecreator.domain;

public class VariableLoadLine {

    private final int proportionOfTenants;
    private final int trafficToGenerate;

    public VariableLoadLine(int proportionOfTenants, int trafficToGenerate) {
        this.proportionOfTenants = proportionOfTenants;
        this.trafficToGenerate = trafficToGenerate;
    }

    public int getProportionOfTenants() {
        return proportionOfTenants;
    }

    public int getTrafficToGenerate() {
        return trafficToGenerate;
    }
}
