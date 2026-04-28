package model;

/**
 *
 * @author chunk
 */
public class Report {
    private String reportID;
    private String title;
    private String content;
    private String dateGenerated;

    public Report(String reportID, String title, String content, String dateGenerated) {
        this.reportID = reportID;
        this.title = title;
        this.content = content;
        this.dateGenerated = dateGenerated;
    }

    
    public String getReportID() {
        return reportID;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getDateGenerated() {
        return dateGenerated;
    }

    
    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setDateGenerated(String dateGenerated) {
        this.dateGenerated = dateGenerated;
    }
}
