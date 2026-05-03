package com.cts.mfrp.au.dto;

public class VerifierApplicationRequest {
    private String name;
    private String email;
    private String phone;
    private int age;
    private String qualification;
    private int experienceYears;
    private String domainsInterested;
    private String motivation;
    private String portfolioUrl;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    public String getQualification() { return qualification; }
    public void setQualification(String qualification) { this.qualification = qualification; }
    public int getExperienceYears() { return experienceYears; }
    public void setExperienceYears(int experienceYears) { this.experienceYears = experienceYears; }
    public String getDomainsInterested() { return domainsInterested; }
    public void setDomainsInterested(String domainsInterested) { this.domainsInterested = domainsInterested; }
    public String getMotivation() { return motivation; }
    public void setMotivation(String motivation) { this.motivation = motivation; }
    public String getPortfolioUrl() { return portfolioUrl; }
    public void setPortfolioUrl(String portfolioUrl) { this.portfolioUrl = portfolioUrl; }
}
