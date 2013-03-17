package com.cordys.jenkinsci;

import hudson.matrix.MatrixConfiguration;
import hudson.matrix.MatrixConfigurationSorter;

public abstract class ConsistentMatrixConfigurationSorter extends MatrixConfigurationSorter {

    public final int compare(MatrixConfiguration configA, MatrixConfiguration configB) {
        final int subclassResult = compareConfigurations(configA, configB);
        if (subclassResult == 0) {
            return configA.getCombination().compareTo(configB.getCombination());
        }
        return subclassResult;
    }

    public abstract int compareConfigurations(MatrixConfiguration configA, MatrixConfiguration configB);
}
