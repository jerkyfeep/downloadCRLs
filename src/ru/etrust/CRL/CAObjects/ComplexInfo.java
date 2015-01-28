package ru.etrust.CRL.CAObjects;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by AMironenko on 10.12.2014.
 */
public class ComplexInfo {
    private String ComplexName = null;
    public List<KeyInfo> KeyInfoList = new ArrayList<KeyInfo>();

    public String getComplexName() {
        return ComplexName;
    }

    public void setComplexName(String complexName) {
        ComplexName = complexName;
    }
}
