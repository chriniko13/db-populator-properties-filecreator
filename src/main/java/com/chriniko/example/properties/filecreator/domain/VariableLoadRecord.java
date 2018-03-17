package com.chriniko.example.properties.filecreator.domain;

import java.util.List;

public class VariableLoadRecord {

    private final List<VariableLoadLine> variableLoadLines;

    public VariableLoadRecord(List<VariableLoadLine> variableLoadLines) {
        this.variableLoadLines = variableLoadLines;
    }

    public List<VariableLoadLine> getVariableLoadLines() {
        return variableLoadLines;
    }
}
