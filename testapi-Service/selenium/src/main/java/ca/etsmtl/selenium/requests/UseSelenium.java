package ca.etsmtl.selenium.requests;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.TimeoutException; // Ajouté pour gestion des attentes
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.io.File;
import java.sql.Timestamp;

import org.springframework.web.bind.annotation.*;

import ca.etsmtl.selenium.requests.payload.request.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/microservice/selenium")
public class UseSelenium {

    @PostMapping("/test")
    public SeleniumResponse testWithSelenium(@RequestBody SeleniumCase seleniumCase) {
        List<SeleniumAction> seleniumActions = seleniumCase.getActions();

        SeleniumResponse seleniumResponse = new SeleniumResponse();
        seleniumResponse.setCase_id(seleniumCase.getCase_id());
        seleniumResponse.setCaseName(seleniumCase.getCaseName());
        seleniumResponse.setSeleniumActions(seleniumActions);
        long currentTimestamp = (new Timestamp(System.currentTimeMillis())).getTime();
        seleniumResponse.setTimestamp(currentTimestamp/1000);

        // Déclaration du driver ici pour qu'il soit accessible au catch externe
        WebDriver driver = null;
        long startTime = System.currentTimeMillis();

        try {
            // Initialisation du driver
            System.setProperty("webdriver.chrome.driver", "src/main/resources/chromedriver");

            ChromeOptions options = new ChromeOptions();
            options.addArguments("--no-sandbox");
            options.addArguments("--headless");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--window-size=1920x1080");
            driver = new ChromeDriver(options); // L'objet est initialisé ici

            // Le temps d'attente implicite est mieux défini au niveau du driver
            // driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10)); // Exemple de bonne pratique

            // Bloc try pour l'exécution des actions
            try {
                for (SeleniumAction seleniumAction : seleniumActions) {
                    System.out.println("action type name : " + seleniumAction.getAction_type_name());

                    switch (seleniumAction.getAction_type_id()) {
                        case 1: // goToUrl
                            System.out.println("go to : " + seleniumAction.getInput());
                            driver.get(seleniumAction.getInput());
                            // Utilisez Duration.ofSeconds si vous êtes en Selenium 4+
                            driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
                            break;
                        case 2: // FillField
                            System.out.println("fill : " + seleniumAction.getObject() + " with " + seleniumAction.getInput());
                            WebElement textBox = driver.findElement(By.name(seleniumAction.getObject()));
                            textBox.sendKeys(seleniumAction.getInput());
                            break;
                        case 3: // GetAttribute
                            WebElement webElement = driver.findElement(By.name(seleniumAction.getTarget()));
                            String pageAttribute = webElement.getAttribute(seleniumAction.getObject());
                            if (!pageAttribute.equals(seleniumAction.getInput())) {
                                String outputMessage = "Attribute " + seleniumAction.getObject()
                                        + " of " + seleniumAction.getTarget()
                                        + " is " + pageAttribute
                                        + " instead of " + seleniumAction.getInput();
                                return finalizeTest(driver, seleniumResponse, startTime, false, outputMessage);
                            }
                            break;
                        case 4: // GetPageTitle
                            System.out.println("Verifying page title...");
                            String pageTitle = driver.getTitle();

                            if (!pageTitle.equals(seleniumAction.getTarget())) {
                                String outputMessage = "Page title is \""
                                        + pageTitle + "\" instead of \""
                                        + seleniumAction.getTarget() + "\"";
                                return finalizeTest(driver, seleniumResponse, startTime, false, outputMessage);
                            }
                            break;
                        case 5: // Clear
                            WebElement textBoxToClear = driver.findElement(By.name(seleniumAction.getObject()));
                            textBoxToClear.clear();
                            break;
                        case 6: // Click
                            WebElement submitButton = driver.findElement(By.name(seleniumAction.getObject()));
                            submitButton.click();
                            break;
                        case 7: // isDisplayed
                            WebElement message = driver.findElement(By.name(seleniumAction.getObject()));
                            message.getText();
                            break;
                        case 8: // VerifyText
                            try {
                                System.out.println("Verify text of : " + seleniumAction.getObject() + " is " + seleniumAction.getInput());
                                WebElement textElement = driver.findElement(By.name(seleniumAction.getObject()));
                                String actualText = textElement.getText().trim();

                                if (!actualText.equals(seleniumAction.getInput().trim())) {
                                    String outputMessage = "Text of " + seleniumAction.getObject() + " is '" + actualText
                                            + "' instead of '" + seleniumAction.getInput() + "'";
                                    return finalizeTest(driver, seleniumResponse, startTime, false, outputMessage);
                                }
                            } catch (Exception ex) {
                                return finalizeTest(driver, seleniumResponse, startTime, false,
                                        "Error verifying text for element: " + seleniumAction.getObject() + " - " + ex.getMessage());
                            }
                            break;
                        case 9: // SelectDropdown
                            try {
                                System.out.println("Select option : " + seleniumAction.getInput()
                                        + " in dropdown " + seleniumAction.getObject());
                                WebElement selectElement = driver.findElement(By.name(seleniumAction.getObject()));
                                Select select = new Select(selectElement);
                                select.selectByVisibleText(seleniumAction.getInput());
                            } catch (NoSuchElementException ex) {
                                return finalizeTest(driver, seleniumResponse, startTime, false,
                                        "Option '" + seleniumAction.getInput()
                                                + "' not found in dropdown '" + seleniumAction.getObject() + "'");
                            } catch (Exception ex) {
                                return finalizeTest(driver, seleniumResponse, startTime, false,
                                        "Error selecting dropdown: " + seleniumAction.getObject() + " - " + ex.getMessage());
                            }
                            break;
                        case 10: // HoverOver
                            try {
                                System.out.println("Hovering over element: " + seleniumAction.getObject());
                                WebElement hoverElement = driver.findElement(By.name(seleniumAction.getObject()));
                                new Actions(driver).moveToElement(hoverElement).pause(Duration.ofMillis(500)).perform();
                            } catch (Exception ex) {
                                return finalizeTest(driver, seleniumResponse, startTime, false,
                                        "Failed to hover over element: " + seleniumAction.getObject() + " (" + ex.getMessage() + ")");
                            }
                            break;
                        case 11: // ToggleCheckbox
                            try {
                                System.out.println("Toggling checkbox: " + seleniumAction.getObject() + " to " + seleniumAction.getInput());
                                WebElement checkbox = driver.findElement(By.name(seleniumAction.getObject()));
                                boolean shouldBeChecked = "check".equalsIgnoreCase(seleniumAction.getInput());

                                if (checkbox.isSelected() != shouldBeChecked) {
                                    checkbox.click();
                                }

                                if (checkbox.isSelected() != shouldBeChecked) {
                                    return finalizeTest(driver, seleniumResponse, startTime, false,
                                            "Checkbox '" + seleniumAction.getObject() + "' state not updated correctly.");
                                }
                            } catch (Exception ex) {
                                return finalizeTest(driver, seleniumResponse, startTime, false,
                                        "Failed to toggle checkbox: " + seleniumAction.getObject() + " - " + ex.getMessage());
                            }
                            break;
                        case 12: // SelectRadio
                            try {
                                System.out.println("Selecting radio button: " + seleniumAction.getObject());
                                WebElement radio = driver.findElement(By.name(seleniumAction.getObject()));
                                if (!radio.isSelected()) {
                                    radio.click();
                                }
                                if (!radio.isSelected()) {
                                    return finalizeTest(driver, seleniumResponse, startTime, false,
                                            "Radio button '" + seleniumAction.getObject() + "' not selected.");
                                }
                            } catch (Exception ex) {
                                return finalizeTest(driver, seleniumResponse, startTime, false,
                                        "Failed to select radio: " + seleniumAction.getObject() + " - " + ex.getMessage());
                            }
                            break;
                        case 13: // File upload
                            try {
                                System.out.println("Upload file : " + seleniumAction.getInput() + " to field " + seleniumAction.getObject());
                                File file = new File(seleniumAction.getInput());
                                if (!file.exists()) {
                                    return finalizeTest(driver, seleniumResponse, startTime, false,
                                            "File not found: " + seleniumAction.getInput());
                                }
                                WebElement fileUploadElement = driver.findElement(By.name(seleniumAction.getObject()));
                                fileUploadElement.sendKeys(file.getAbsolutePath());
                            } catch (Exception ex) {
                                return finalizeTest(driver, seleniumResponse, startTime, false,
                                        "Failed to upload file: " + seleniumAction.getObject() + " - " + ex.getMessage());
                            }
                            break;
                        case 14: // JS alert
                            try {
                                System.out.println("Accepting JavaScript alert.");
                                new WebDriverWait(driver, Duration.ofSeconds(5)).until(ExpectedConditions.alertIsPresent()).accept();
                            } catch (NoAlertPresentException | TimeoutException ex) {
                                return finalizeTest(driver, seleniumResponse, startTime, false, "No alert found to accept or timeout.");
                            } catch (Exception ex) {
                                return finalizeTest(driver, seleniumResponse, startTime, false,
                                        "Error handling JavaScript alert: " + ex.getMessage());
                            }
                            break;
                        case 15: // Generic input (similar to FillField)
                            try {
                                System.out.println("Generic input action on : " + seleniumAction.getObject()
                                        + " with " + seleniumAction.getInput());
                                WebElement genericInput = driver.findElement(By.name(seleniumAction.getObject()));
                                genericInput.clear();
                                genericInput.sendKeys(seleniumAction.getInput());
                            } catch (Exception ex) {
                                return finalizeTest(driver, seleniumResponse, startTime, false,
                                        "Failed to perform generic input on: " + seleniumAction.getObject() + " - " + ex.getMessage());
                            }
                            break;
                        case 16: // Redirect link (similaire à goToUrl)
                            try {
                                System.out.println("Redirecting to : " + seleniumAction.getTarget());
                                driver.get(seleniumAction.getTarget());

                                new WebDriverWait(driver, Duration.ofSeconds(10)).until(ExpectedConditions.urlContains(seleniumAction.getTarget()));

                                String currentUrl = driver.getCurrentUrl();
                                if (!currentUrl.contains(seleniumAction.getTarget())) {
                                    return finalizeTest(driver, seleniumResponse, startTime, false,
                                            "Redirection failed. Current URL: " + currentUrl);
                                }
                            } catch (Exception ex) {
                                return finalizeTest(driver, seleniumResponse, startTime, false,
                                        "Error redirecting to URL: " + seleniumAction.getTarget() + " - " + ex.getMessage());
                            }
                            break;

                        case 17: // CallCase (modularité)
                            System.out.println("Calling sub-scenario with ID : " + seleniumAction.getTarget()
                                    + " (Requires DB/Repository for full implementation)");
                            break;

                        default:
                            System.out.println("action type id : " + seleniumAction.getAction_type_id() + " not found");
                            break;
                    }
                }

                return finalizeTest(driver, seleniumResponse, startTime, true, null);

            }

            // CATCH INTERNE : Gère les erreurs survenant pendant la boucle d'actions
            catch(Exception e) {
                return finalizeTest(driver, seleniumResponse, startTime, false,
                        "Test failed during action execution: " + e.getMessage());
            }

        }

        catch(Exception e) {
            System.out.println(e);
            return finalizeTest(driver, seleniumResponse, startTime, false,
                    "Test initialisation failed: " + e.toString());
        }
    }

    // finalizeTest
    private SeleniumResponse finalizeTest(
            WebDriver driver,
            SeleniumResponse response,
            long startTime,
            boolean success,
            String output) {

        if (driver != null) {
            try {
                driver.quit();
            } catch (Exception e) {
                // TODO : Logger l’erreur si nécessaire
            }
        }

        long totalTime = System.currentTimeMillis() - startTime;

        response.setDuration(totalTime);
        response.setSuccess(success);
        if (!success && output != null) {
            response.setOutput(output);
        }
        return response;
    }

    @GetMapping("/all")
    public String allAccess() {
        return "Bienvenue au TAF.";
    }
}