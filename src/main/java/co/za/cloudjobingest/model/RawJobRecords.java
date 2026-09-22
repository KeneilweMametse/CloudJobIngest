package za.co.cloudingest.model;

/**
 * Represents one row of a raw, unvalidated job listing as it arrives
 * from the source CSV — before cleaning/validation happens.
 */
public class RawJobRecord {

    private String title;
    private String company;
    private String location;
    private String description;
    private String requiredSkills;
    private String salaryRange;
    private String sourceUrl;

    public RawJobRecord() {
    }

    public RawJobRecord(String title, String company, String location, String description,
                         String requiredSkills, String salaryRange, String sourceUrl) {
        this.title = title;
        this.company = company;
        this.location = location;
        this.description = description;
        this.requiredSkills = requiredSkills;
        this.salaryRange = salaryRange;
        this.sourceUrl = sourceUrl;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getRequiredSkills() { return requiredSkills; }
    public void setRequiredSkills(String requiredSkills) { this.requiredSkills = requiredSkills; }

    public String getSalaryRange() { return salaryRange; }
    public void setSalaryRange(String salaryRange) { this.salaryRange = salaryRange; }

    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }

    @Override
    public String toString() {
        return "RawJobRecord{title='" + title + "', company='" + company + "', location='" + location + "'}";
    }
}
