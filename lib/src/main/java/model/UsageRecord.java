package model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class UsageRecord{

    private Long dumpsterId;
    private LocalDate date;
    private int estimatedNumCont;
    private String fillLevel;

    public UsageRecord(Long dumpsterId, LocalDate date, int estimatedNumCont, String fillLevel) {
        this.dumpsterId = dumpsterId;
        this.date = date;
        this.estimatedNumCont = estimatedNumCont;
        this.fillLevel = fillLevel;
    }

    public Long getDumpsterId() {
        return dumpsterId;
    }

    public void setDumpsterId(Long dumpsterId) {
        this.dumpsterId = dumpsterId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public int getEstimatedNumCont() {
        return estimatedNumCont;
    }

    public void setEstimatedNumCont(int estimatedNumCont) {
        this.estimatedNumCont = estimatedNumCont;
    }

    public String getFillLevel() {
        return fillLevel;
    }

    public void setFillLevel(String fillLevel) {
        this.fillLevel = fillLevel;
    }
    
}
