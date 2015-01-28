package ru.etrust.CRL.CAObjects;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by AMironenko on 10.12.2014.
 */
public class CAInfo {
    private String CAId = "default CA info";
    public List<ComplexInfo> ComplexInfoList = new ArrayList<ComplexInfo>();

    public String getCAId() {
        return CAId;
    }

    public void setCAId(String CAId) {
        this.CAId = CAId;
    }

}
