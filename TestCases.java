import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class YouTubeAutomation {

    public static void main(String[] args) throws IOException, InterruptedException {
        // Set ChromeDriver path
        System.setProperty("webdriver.chrome.driver", "path_to_chromedriver");

        // Initialize ChromeDriver
        WebDriver driver = new ChromeDriver();
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        driver.manage().window().maximize();

        // Navigate to YouTube
        driver.get("https://www.youtube.com");

        // Assert we are on the correct URL
        if (!driver.getCurrentUrl().equals("https://www.youtube.com/")) {
            System.out.println("URL Mismatch");
            driver.quit();
            return;
        }

        // Click on "About" in the sidebar
        WebElement aboutLink = driver.findElement(By.xpath("//a[@title='About']"));
        aboutLink.click();

        // Print the message on the screen
        WebElement aboutContent = driver.findElement(By.xpath("//yt-formatted-string[@class='style-scope ytd-metadata-row-renderer']"));
        System.out.println("About: " + aboutContent.getText());

        // Navigate to the "Films" tab
        WebElement filmsTab = driver.findElement(By.xpath("//a[@title='Films']"));
        filmsTab.click();

        // Scroll to the extreme right in the "Top Selling" section
        WebElement topSellingSection = driver.findElement(By.xpath("//h2[text()='Top selling']/ancestor::div[@id='contents']"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollLeft = arguments[0].scrollWidth", topSellingSection);

        // Soft assert on movie rating and genre
        WebElement lastMovie = driver.findElement(By.xpath("//a[@title='Last Movie']"));
        String maturityRating = lastMovie.getAttribute("aria-label");
        String genres = lastMovie.getAttribute("data-style").toLowerCase();
        softAssert(maturityRating.contains("A"), "Movie is not marked 'A' for mature");
        softAssert(genres.contains("comedy") || genres.contains("animation"), "Movie is not Comedy or Animation");

        // Navigate to the "Music" tab
        WebElement musicTab = driver.findElement(By.xpath("//a[@title='Music']"));
        musicTab.click();

        // Get the name of the playlist
        WebElement playlist = driver.findElement(By.xpath("//h2[@id='title']/span"));
        System.out.println("Playlist: " + playlist.getText());

        // Soft assert on the number of tracks
        List<WebElement> tracks = driver.findElements(By.xpath("//a[@class='yt-simple-endpoint style-scope ytd-thumbnail']"));
        softAssert(tracks.size() <= 50, "Number of tracks is greater than 50");

        // Navigate to the "News" tab
        WebElement newsTab = driver.findElement(By.xpath("//a[@title='News']"));
        newsTab.click();

        // Get the first 3 news posts
        List<WebElement> newsPosts = driver.findElements(By.xpath("//div[@id='contents']/ytd-grid-renderer/div[@class='style-scope ytd-rich-grid-renderer']"));
        int totalLikes = 0;
        for (int i = 0; i < 3 && i < newsPosts.size(); i++) {
            WebElement post = newsPosts.get(i);
            String title = post.findElement(By.xpath(".//a[@id='video-title']")).getText();
            String body = post.findElement(By.xpath(".//yt-formatted-string[@id='description-text']")).getText();
            String likesStr = post.findElement(By.xpath(".//yt-formatted-string[@id='text']/span")).getText();
            int likes = likesStr.isEmpty() ? 0 : Integer.parseInt(likesStr.replaceAll("[^0-9]", ""));
            System.out.println("News Post " + (i + 1) + ": " + title + " - " + body);
            totalLikes += likes;
        }
        System.out.println("Total Likes on first 3 news posts: " + totalLikes);

        // Search for items in Excel and scroll to reach 10 Cr views for each
        FileInputStream fis = new FileInputStream(new File("path_to_excel_file"));
        Workbook workbook = new XSSFWorkbook(fis);
        Sheet sheet = workbook.getSheetAt(0);
        for (Row row : sheet) {
            String searchItem = row.getCell(0).getStringCellValue();
            WebElement searchBox = driver.findElement(By.name("search_query"));
            searchBox.clear();
            searchBox.sendKeys(searchItem);
            searchBox.sendKeys(Keys.RETURN);
            // Wait for search results
            Thread.sleep(5000);
            // Scroll to reach 10 Cr views
            long totalViews = 0;
            do {
                List<WebElement> videoElements = driver.findElements(By.xpath("//ytd-video-renderer"));
                for (WebElement videoElement : videoElements) {
                    String viewsStr = videoElement.findElement(By.xpath(".//span[@class='style-scope ytd-video-meta-block']"))
                            .getText().replaceAll("[^0-9]", "");
                    if (!viewsStr.isEmpty()) {
                        totalViews += Long.parseLong(viewsStr);
                    }
                }
                if (totalViews < 100_000_000) {
                    ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight)");
                    Thread.sleep(2000);
                }
            } while (totalViews < 100_000_000);
        }
        driver.quit();
    }

    private static void softAssert(boolean condition, String message) {
        if (!condition) {
            System.out.println("Soft Assertion failed: " + message);
        }
    }
}
