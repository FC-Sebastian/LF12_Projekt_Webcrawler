package controller;

import model.EnergyDataset;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static java.time.temporal.ChronoUnit.DAYS;

public class WebCrawler {

    private final String[] relevantHeaders = {
            "Biomasse",
            "Steinkohle",
            "Wasserkraft",
            "Braunkohle",
            "Erdgas",
            "Pumpspeicher",
            "Solar",
            "Wind Offshore",
            "Wind Onshore",
            "Kernkraft",
            "Andere"
    };
    private final String csvName = "stromerzeugung_und_-verbrauch.csv";
    private final String url = "https://www.agora-energiewende.de/daten-tools/agorameter/chart/today/power_generation/";
    private final String downloadButtonClass = "cd_excel";
    private final int requestMaxDays = 364;
    private WebDriver chrDriver;
    private String downloadPath;

    private String[] headers;

    /**
     * Gets energy data from Agorameter-website, splits larger requests (1 year and above) into multiple requests
     *
     * @param from from date
     * @param to to date
     * @return arraylist containing relevant energy data
     */
    public List<EnergyDataset> getEnergyData(LocalDate from, LocalDate to) {
        List<EnergyDataset> data = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        if (DAYS.between(from, to) < 365) {
            data = handleDownload(from.format(formatter) + "/" + to.format(formatter) + "/hourly");
        } else {
            long between = DAYS.between(from, to);
            LocalDate newFrom = from;
            boolean fetching = true;

            while (fetching) {
                LocalDate tempTo;

                if (between - requestMaxDays < 1) {
                    tempTo = to;
                    fetching = false;
                } else {
                    tempTo = newFrom.plusDays(requestMaxDays);
                    between -= requestMaxDays;
                }

                System.out.println("NEW LOOP");
                System.out.println("FROM: "+ newFrom.format(formatter));
                System.out.println("TO: " + tempTo.format(formatter));
                System.out.println();

                data.addAll(handleDownload(newFrom.format(formatter) + "/" + tempTo.format(formatter) + "/hourly"));

                newFrom = tempTo.plusDays(1);
            }
        }

        return data;
    }

    private void dbImport(List<String[]> data) throws SQLException {
        System.out.println("import start");
        List<String[]> batchData = new ArrayList<>();
        for (String header: relevantHeaders) {
            System.out.println("HEADER: "+header);
            int headerIndex = getHeaderIndex(header);
            int dateIndex = getHeaderIndex("date_id");
            if (!(headerIndex < 0) && !(dateIndex < 0)) { //index will be -1 if not found in csv
                for (String[] resultSet: data) {
                    batchData.add(new String[]{resultSet[dateIndex], header, resultSet[headerIndex]});
                }
            }
        }
        EnergyDataset model = new EnergyDataset();
        model.batchInsert(batchData);
    }

    /**
     * Handles download of energy data as csv for given timespan
     *
     * @param timeString timespan as url parameter
     * @return csv-data
     */
    private List<EnergyDataset> handleDownload(String timeString) {
        initWebDriver();
        List<String[]> data = new ArrayList<>();
        try {
            startDownload(timeString);

            // reading and printing csv if it was downloaded
            if (confirmDownload()) {
                data = getCsvData();
            } else {
                System.out.println("Downloaded File doesnt exist");
            }
        } catch (Exception e) {
            System.out.println("Something went wrong, check your internet connection");
            e.printStackTrace();
        }

        if (data.size() > 0) {
            try {
                dbImport(data);
            } catch (SQLException e) {
                System.out.println("DB ERROR");
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }

        List<EnergyDataset> returnList = new ArrayList<>();
        for (String header: relevantHeaders) {
            System.out.println("HEADER: " + header);
            int headerIndex = getHeaderIndex(header);
            int dateIndex = getHeaderIndex("date_id");
            if (!(headerIndex < 0) && !(dateIndex < 0)) { //index will be -1 if not found in csv
                for (String[] dataSet: data) {
                    returnList.add(new EnergyDataset(dataSet[dateIndex], header, dataSet[headerIndex]));
                }
            }
        }

        chrDriver.close();
        return returnList;
    }

    /**
     * Parses recently downloaded csv and returns its contents as ArrayList
     *
     * @return list containing csv-data
     * @throws IOException if file not found
     */
    private List<String[]> getCsvData() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(downloadPath + "\\" + csvName));
        headers = reader.readLine().split(",");
        List<String[]> data = new ArrayList<>();

        String line;
        while ((line = reader.readLine()) != null) {
            data.add(line.split(","));
        }
        return data;
    }

    /**
     * Waits for Download to finish, returns true on success or false on failure/timeout
     *
     * @return bool
     */
    private boolean confirmDownload() {
        // waiting for download to finish
        File csvFile = new File(downloadPath, csvName);

        Wait<File> downloadWait = new FluentWait<>(csvFile)
                .withTimeout(Duration.ofSeconds(300))
                .pollingEvery(Duration.ofSeconds(1))
                .ignoring(Exception.class)
                .withMessage("File didnt load in 5 mins");
        return downloadWait.until(f -> f.exists() && f.canRead());
    }

    /**
     * Navigates WebDriver to Agorameter, waits for download button to be clickable and downloads results as csv
     *
     * @param timeString timespan to get data for
     */
    private void startDownload(String timeString) {
        chrDriver.manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS);
        chrDriver.get(url +  timeString);

        Wait<WebDriver> pageLoadWait = new FluentWait<>(chrDriver)
                .withTimeout(Duration.ofSeconds(300))
                .pollingEvery(Duration.ofSeconds(1))
                .ignoring(NoSuchElementException.class);

        pageLoadWait.until(ExpectedConditions.presenceOfElementLocated(By.className(downloadButtonClass)));
        WebElement downloadButton = chrDriver.findElement(By.className(downloadButtonClass));

        // Wait for the button to be clickable then scroll it into view
        pageLoadWait.until(ExpectedConditions.elementToBeClickable(downloadButton));
        scrollElementIntoView(downloadButton);

        // Optional: Click using JavaScript if standard click fails
        try {
            downloadButton.click();
        } catch (ElementClickInterceptedException e) {
            ((JavascriptExecutor) chrDriver).executeScript("arguments[0].click();", downloadButton);
        }
    }

    /**
     * Scrolls given element into view using javascript
     *
     * @param element WebElement
     */
    private void scrollElementIntoView(WebElement element) {
        ((JavascriptExecutor) chrDriver).executeScript("arguments[0].scrollIntoView(true);", element);
    }

    /**
     * initialises WebDriver, sets download directory and prevents popups
     */
    private void initWebDriver() {
        // setting options
        System.setProperty("webdriver.chrome.driver",System.getProperty("user.dir") + "\\ChromeDriver\\chromedriver-win64\\chromedriver.exe");
        HashMap<String, Object> chromePrefs = new HashMap<>();
        chromePrefs.put("profile.default_content_settings.popups", 0);

        downloadPath = System.getProperty("user.dir") + "\\downloads\\" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("ddMMyy_HHmmss"));
        chromePrefs.put("download.default_directory", downloadPath);
        ChromeOptions chrOpt = new ChromeOptions();
        chrOpt.addArguments("--headless");
        chrOpt.setExperimentalOption("prefs",chromePrefs);

        // initiating webDriver
        chrDriver = new ChromeDriver(chrOpt);
    }

    /**
     * gets index of given header from csv headers
     *
     * @param header header as string
     * @return index int
     */
    private int getHeaderIndex(String header) {
        for (int i = 0; i < headers.length; i++) {
            if (Objects.equals(headers[i], header)) {
                return i;
            }
        }
        return -1;
    }
}
