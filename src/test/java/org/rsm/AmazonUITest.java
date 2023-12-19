package org.rsm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

@TestMethodOrder(OrderAnnotation.class)
class AmazonUITest {
  private static WebDriver driver;
  private static WebElement topResult;
  private static String paperbackPrice;

  @BeforeAll
  static void setup() {
    // System.setProperty("webdriver.chrome.driver", "drivers/chromedriver.exe");
    var options = new ChromeOptions();
    options.addArguments("--start-maximized");
    driver = new ChromeDriver(options);
    driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
  }

  @AfterAll
  static void teardown() {
    driver.quit();
  }

  private WebElement find(String locator) {
    return driver.findElement(By.cssSelector(locator));
  }

  private WebElement find(WebElement elem, String locator) {
    return elem.findElement(By.cssSelector(locator));
  }

  private List<WebElement> findMultiple(String locator) {
    return driver.findElements(By.cssSelector(locator));
  }

  private void waitFor(WebElement elem) {
    var wait = new WebDriverWait(driver, Duration.ofSeconds(5));
    wait.until(d -> elem.isDisplayed());
  }

  @Test
  @Order(1)
  void openAmazonHomeTest() {
    driver.get("https://www.amazon.com/");
    assertEquals("https://www.amazon.com/", driver.getCurrentUrl());
  }

  @Test
  @Order(2)
  void selectBooksSearchTypeTest() {
    var elem = find("#searchDropdownBox");
    elem.click();
    var dropdown = new Select(elem);
    dropdown.selectByVisibleText("Books");
    assertEquals("Books", dropdown.getFirstSelectedOption().getText());
  }

  @Test
  @Order(3)
  void searchHarryPotterTest() {
    var overlay = find(".nav-search-field ");
    overlay.click();
    var input = find("#twotabsearchtextbox");
    input.sendKeys("Harry Potter and the Cursed Child");
    input.sendKeys(Keys.ENTER);
    waitFor(find("#search"));
    assertTrue(driver.getCurrentUrl().startsWith("https://www.amazon.com/s?k=Harry+Potter"));
  }

  @Test
  @Order(4)
  void booksLeftNavTest() {
    var elems = findMultiple("#departments .a-list-item");
    assertTrue(elems.size() > 0);
    assertEquals("Books", elems.get(0).getText());
  }

  @Test
  @Order(5)
  void booksTopNavTest() {
    var elem = find("#nav-progressive-subnav");
    assertTrue(elem.getText().startsWith("Books"));
  }

  @Test
  @Order(6)
  void topResultTitleTest() {
    topResult = find("[data-cel-widget='search_result_1']");
    var elem = find(topResult, "[data-cy='title-recipe']");
    assertTrue(elem.getText().startsWith("Harry Potter and the Cursed Child, Parts One and Two"));
  }

  @Test
  @Order(6)
  void topResultPriceTest() {
    var priceSymbol = find(topResult, ".a-price-symbol");
    assertEquals("$", priceSymbol.getText());
    var dollarAmount = find(topResult, ".a-price-whole");
    assertTrue(dollarAmount.getText().matches("^\\d+$"));
    var centsAmount = find(topResult, ".a-price-fraction");
    assertTrue(centsAmount.getText().matches("^\\d+$"));
    // price is $10.39 for example
    paperbackPrice = priceSymbol.getText() + dollarAmount.getText() + "." + centsAmount.getText();
  }

  @Test
  @Order(7)
  void topResultCategoriesTest() {
    // No nice way to locate available categories
    assertTrue(topResult.findElement(By.linkText("Paperback")).isDisplayed());
    assertTrue(topResult.findElement(By.linkText("Kindle")).isDisplayed());
    assertTrue(topResult.findElement(By.linkText("Hardcover")).isDisplayed());
  }

  @Test
  @Order(8)
  void selectPaperBackTest() {
    var elem = topResult.findElement(By.linkText("Paperback"));
    elem.click();
    waitFor(find("#productTitle"));
    assertTrue(driver.getCurrentUrl().startsWith("https://www.amazon.com/Harry-Potter-Cursed-Child-Parts"));
  }

  @Test
  @Order(9)
  void productTitleTest() {
    var elem = find("#productTitle");
    assertTrue(elem.getText().startsWith("Harry Potter and the Cursed Child, Parts One and Two"));
  }

  @Test
  @Order(10)
  void productDetailsTest() {
    var matrix = find("#mediamatrix_feature_div");
    // Paperback is automatically selected/highlighted
    var selected = find(matrix, "[class*='swatchElement selected']");
    assertTrue(selected.getText().contains("Paperback"));
    assertTrue(selected.getText().contains(paperbackPrice));
  }

  @Test
  @Order(11)
  void addToCartTest() {
    find("#add-to-cart-button").click();
    waitFor(find("#sw-atc-confirmation"));
    assertTrue(driver.getCurrentUrl().startsWith("https://www.amazon.com/cart"));
  }

  // TODO: I don't see the gift option on my end on amazon.com

  @Test
  @Order(12)
  void cartItemTopNavTest() {
    var elem = find("#nav-cart-count");
    assertEquals("1", elem.getText());
  }

  @Test
  @Order(13)
  void cartItemDetailsTest() {
    // Active Items
    find("#nav-cart").click();
    var cart = find("#activeCartViewForm");
    waitFor(cart);
    assertTrue(driver.getCurrentUrl().startsWith("https://www.amazon.com/gp/cart"));
    var items = findMultiple("[data-itemtype='active']");
    assertEquals(1, items.size());
  }
}
