package com.cts.mfrp.au.dto;

import java.util.List;

public class AiRiskResponse {
    private int riskScore;
    private List<String> flags;
    private String summary;
    private String error;

    public AiRiskResponse() {}

    public AiRiskResponse(int riskScore, List<String> flags, String summary) {
        this.riskScore = riskScore;
        this.flags = flags;
        this.summary = summary;
    }

    public static AiRiskResponse withError(String error) {
        AiRiskResponse r = new AiRiskResponse();
        r.error = error;
        return r;
    }

    public int getRiskScore() { return riskScore; }
    public void setRiskScore(int riskScore) { this.riskScore = riskScore; }
    public List<String> getFlags() { return flags; }
    public void setFlags(List<String> flags) { this.flags = flags; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
}
