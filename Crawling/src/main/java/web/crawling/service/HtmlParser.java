package web.crawling.service;

import java.util.List;

import org.json.simple.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public abstract class HtmlParser {

	protected WebDriver webDriver;
	protected int delay;

	//data는 url 또는 html 소스
	public HtmlParser(String data, boolean isUrl) throws InterruptedException {
		this(data, isUrl, getDefaultOptions(), 20); //기본 20초 delay
	}
	
	//delay는 초단위
	public HtmlParser(String data, boolean isUrl, int delay) throws InterruptedException {
		this(data, isUrl, getDefaultOptions(), delay);
	}
	
	public HtmlParser(String data, boolean isUrl, ChromeOptions options, int delay) throws InterruptedException {
		//셀레니움 시스템 프로퍼티 설정
		System.setProperty("webdriver.chrome.driver", HtmlParser.class.getResource("/").getPath() + "chromedriver.exe");
		//webDriver 선언
		this.webDriver = new ChromeDriver(options);
		//html 요청, html 소스로 직접 요청할 때에는 접두사가 붙음
		this.webDriver.get(isUrl? data : "data:text/html;charset=utf-8," + data);
		//delay 설정(초단위)
		this.delay = delay;
		Thread.sleep(1000 * delay);
	}
	
	//크롬 GPU OFF 옵션 반환
	private static ChromeOptions getDefaultOptions() {
		//크롬 옵션 설정
		//GPU OFF
		ChromeOptions options = new ChromeOptions();
		options.addArguments("headless");
		options.addArguments("disabled-gpu");
		return options;
	}

	//파싱 실행(오버라이드 메서드)
	public abstract JSONObject getJsonData();
	
	protected WebElement getElementById(String id) {
		return webDriver.findElement(By.id(id));
	}
	
	protected WebElement getElementByClassName(String className) {
		return webDriver.findElement(By.className(className));
	}
	
	protected WebElement getElementsByName(String name) {
		return webDriver.findElement(By.name(name));
	}
	
	protected WebElement getElementByCssSelector(String selector) {
		return webDriver.findElement(By.cssSelector(selector));
	}
	
	protected List<WebElement> getElementsByClassName(String className) {
		return webDriver.findElements(By.className(className));
	}
	
	protected List<WebElement> getElementsByCssSelector(String selector) {
		return webDriver.findElements(By.cssSelector(selector));
	}
	
	//html 태그 전부 삭제 후 순수 텍스트만 반환
	public String removeHTMLTag(String source) {
		return source.replaceAll("<(/)?([a-zA-Z]*)(\\s[a-zA-Z]*=[^>]*)?(\\s)*(/)?>", "");
	}
	
	//셀레니움 종료
	public void close() {
		webDriver.close();
		webDriver.quit();
	}
	
}
