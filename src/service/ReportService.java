package service;
import java.io.*;
import java.util.ArrayList;
import model.*;

/**
 *
 * @author chunk
 */
public class ReportService {
    public static ArrayList<Report> reportList = new ArrayList<>();

    public static void getReport() {
        reportList.clear();
        try (BufferedReader br = new BufferedReader(new FileReader("C:\\Users\\ASUS\\Desktop\\AMC\\src\\data\\reports.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] reportDetails = line.split(",", 4); 
                if (reportDetails.length < 4)
                    continue;

                String reportID = reportDetails[0];
                String title = reportDetails[1];
                String content = reportDetails[2];
                String dateGenerated = reportDetails[3];

                Report report = new Report(reportID, title, content, dateGenerated);
                reportList.add(report);
            }
        } catch (Exception e) {
            System.out.println("Error loading reports: " + e.getMessage());
        }
    }

    public static void saveReport() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("C:\\Users\\ASUS\\Desktop\\AMC\\src\\data\\reports.txt"))) {
            for (int i = 0; i < reportList.size(); i++) {
                Report report = reportList.get(i);

                String reportID = report.getReportID();
                String title = report.getTitle();
                String content = report.getContent();
                String dateGenerated = report.getDateGenerated();

                String line = String.join(",", reportID, title, content, dateGenerated);

                bw.write(line);
                bw.newLine();
            }
        } catch (Exception e) {
            System.out.println("Error saving reports: " + e.getMessage());
        }
    }

    public static void addReport(Report report) {
        reportList.add(report);
    }

}
