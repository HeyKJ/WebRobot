package web.crawling.service;

import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeOptions;

@SuppressWarnings("unchecked")
public class AlibabaService extends HtmlParser {

	private String IMG_PATH = "C:\\Users\\knowch\\Downloads\\alibaba\\";
	
	public AlibabaService(String data, boolean isUrl) throws InterruptedException {
		super(data, isUrl);
	}
	
	public AlibabaService(String data, boolean isUrl, int delay) throws InterruptedException {
		super(data, isUrl, delay);
	}
	
	public AlibabaService(String data, boolean isUrl, ChromeOptions options, int delay) throws InterruptedException {
		super(data, isUrl, options, delay);
	}
	
	@Override
	public JSONObject getJsonData() {
		JSONObject jsonData = new JSONObject();
		//상품 아이디
		jsonData.put("id", getItemId());
		//상품명
		jsonData.put("title", getTitle());
		//상품가
		jsonData.put("price", getPrice());
		//대표 이미지 600x600
		jsonData.put("img_600", getImg("600x600"));
		//대표 이미지 500x500
		jsonData.put("img_500", getImg("500x500"));
		//대표 이미지 350x350
		jsonData.put("img_350", getImg("350x350"));
		//상품 내용(이미지)
		jsonData.put("content", getContent());
		//상품 옵션은 상세 정보에 모두 들어있으므로 사용 안함
		//jsonData.put("options", getItemOptions());
		//상품 상세 정보
		jsonData.put("detail_infos", getDetailInfo());
		return jsonData;
	}
	
	//상품 id 반환
	public String getItemId() {
		String itemId = getElementsByName("b2c_auction").getAttribute("content");
		//상품 이미지, 내용 이미지 저장할 폴더가 없으면 생성
		IMG_PATH += itemId;
		HttpImageUtil.makeSavePath(IMG_PATH + "\\item");
		HttpImageUtil.makeSavePath(IMG_PATH + "\\content");
		return itemId;
	}
	
	//상품명 반환
	public String getTitle() {
		return getElementById("mod-detail-title").findElement(By.className("d-title")).getText();
	}

	//상품가 반환
	public JSONArray getPrice() {
		//.price -> td -> attr:data-range
		//data-range="{"begin":"2","end":"4","price":"65.00"}"
		//price-original-sku
		JSONArray priceArray = new JSONArray();
		List<WebElement> priceList = getElementsByCssSelector(".price td[data-range]");
		if(priceList.size() == 0) { //없을 경우 .price-original-sku 데이터에 상품가가 있음
			JSONObject data = new JSONObject();
			data.put("price", removeHTMLTag(getElementByClassName("price-original-sku").getAttribute("innerHTML")));
			data.put("begin", "");
			data.put("end", "");
			priceArray.add(data);
		}

		//어차피 size 0이면 for문 진입암함
		for(int i=0; i<priceList.size(); i++) {
			WebElement td = priceList.get(i);
			String dataRange = td.getAttribute("data-range");
			if(dataRange == null) continue;

			//string to json
			JSONParser parser = new JSONParser();
			JSONObject data = null;
			try {
				data = (JSONObject) parser.parse(dataRange);
				
			} catch (ParseException e) {
				e.printStackTrace();
			}
			//실패시 null이 들어감
			priceArray.add(data);
		}

		return priceArray;
	}

	//상품 대표이미지 경로 반환
	//기본 썸네일이 60x60
	public JSONArray getImg(String size) {
		//알리바바는 대표 썸네일 애로우 아이콘을 클릭해야 lazy load되는 이미지들이 있음
		//#dt-tab -> .tab-content-container -> ul -> li -> img -> attr:src 
		List<WebElement> list = getElementsByCssSelector("#dt-tab .tab-content-container ul li img");
		JSONArray imgArray = new JSONArray();
		for(int i=0; i<list.size(); i++) {
			WebElement img = list.get(i);
			String lazyLoadImgSrc = img.getAttribute("data-lazy-src"); //lazy load 이미지들은 여기에 경로가 있음
			String basicImgSrc = img.getAttribute("src"); //기본 설정 이미지 경로
			String imgUrl = lazyLoadImgSrc != null && lazyLoadImgSrc != ""? lazyLoadImgSrc : basicImgSrc; //lozy load 이미지 경로가 있으면 그 경로로 설정
			imgUrl = imgUrl.replaceAll("60x60", size);
			String savePath = IMG_PATH + "\\item\\img_" + size + "_" + i + ".png"; //대표이미지는 상품 id/item 폴더에 저장
			imgArray.add(HttpImageUtil.saveImg(imgUrl, savePath));
		}
		
		return imgArray;
	}

	//상품 내용 반환
	public JSONObject getContent() {
		JSONObject contentData = new JSONObject();
		//lazy load을 실행시키기 위해 가장 하단 영역으로 이동해야함
		JavascriptExecutor js = ((JavascriptExecutor) webDriver);
		js.executeScript("window.scrollTo(0, document.body.scrollHeight)");
		//하단 이동 후 lazy load 기다림(3초)
		try { Thread.sleep(1000 * 3); } catch (InterruptedException e) { e.printStackTrace(); }
		//content 영역 #desc-lazyload-container
		WebElement contentElement =  getElementById("desc-lazyload-container");
		//content 이미지 경로 가져오기
		JSONArray contentImgArray = new JSONArray();
		List<WebElement> contentImgList = contentElement.findElements(By.tagName("img"));
		for(int i=0; i<contentImgList.size(); i++) {
			WebElement contentImg = contentImgList.get(i); //content 영역의 img 태그
			String imgUrl = contentImg.getAttribute("src"); //기본 설정 이미지 경로
			String savePath = IMG_PATH + "\\content\\content" + "_" + i + ".png"; //상품 내용 이미지는 상품 id/content 폴더에 저장 
			contentImgArray.add(HttpImageUtil.saveImg(imgUrl, savePath));
		}
		contentData.put("imgs", contentImgArray);

		//content 텍스트 가져오기, contentElement의 내부 태그 전부 제거 후 순수 텍스트만 추출
		contentData.put("text", removeHTMLTag(contentElement.getAttribute("innerHTML")));
		return contentData;
	}

	//상품 옵션 반환
	public JSONArray getItemOptions() {
		//.offerdetail_ditto_purchasing -> obj-leading -> .obj-title
		//.offerdetail_ditto_purchasing -> obj-leading -> .unit-detail-spec-operator:data-unit-config //{"name":"黑灰色"}
		//.offerdetail_ditto_purchasing -> obj-sku
		//옵션은 여러개
		JSONArray optionArray = new JSONArray();
		JSONObject optionData = new JSONObject();
		//옷 상품에서는 leading 옵션은 색상관련 옵션임
		//옵션이 없는 상품도 있기때문에 list형태로 불러와야 Exception이 발생안함
		WebElement leading = null;
		List<WebElement> leadingList = getElementsByCssSelector(".offerdetail_ditto_purchasing .obj-leading"); 
		if(leadingList.size() > 0) leading = leadingList.get(0); 
		else return null;

		optionData.put("name", leading.findElement(By.className("obj-title")).getText());
		JSONArray optionValueArray = new JSONArray(); //옵션 값은 여러개
		leading.findElements(By.cssSelector(".unit-detail-spec-operator")).forEach(div -> {
			String value = null;
			try {
				String dataUnitConfig = div.getAttribute("data-unit-config"); //{"name":"黑灰色"} 이런 형태로 되어있음
				JSONParser parser = new JSONParser();
				JSONObject data;
				data = (JSONObject) parser.parse(dataUnitConfig);
				value = (String) data.get("name");
				
			} catch (ParseException e) {
				e.printStackTrace();
			}
			//data-unit-config 값이 없을 경우 null이 들어감
			optionValueArray.add(value);
		});
		
		optionData.put("value", optionValueArray);
		optionArray.add(optionData);
		return optionArray;
	}

	//상품 상세 정보 반환
	public JSONArray getDetailInfo() {
		//#mod-detail-attributes -> td
		JSONArray infoArray = new JSONArray();
		//첫번쨰 td는 정보명, 두번쨰 td는 정보값, 세번째 td는 정보명, 네번째 td는 정보값 ... 형태로 되어있음
		//따라서 정보명을 먼저 jsonArray에 담고 이후에 jsonArray에 있던 정보명 json을 꺼내면서 다시 value 값을 추가
		//(정보명 td는 de-feature 클래스를 가짐)
		//정보명 설정
		getElementsByCssSelector("#mod-detail-attributes .de-feature").forEach(td -> {
			JSONObject infoData = new JSONObject();
			infoData.put("name", td.getText());
			infoArray.add(infoData);
		});
		//배열에 넣었던 json을 꺼내서 차례대로 정보값을 넣음
		//정보값 설정
		List<WebElement> valueList = getElementsByCssSelector("#mod-detail-attributes .de-value");	
		for(int i=0; i<valueList.size(); i++) {
			JSONObject infoData = (JSONObject) infoArray.get(i);
			infoData.put("value", valueList.get(i).getText());
		}

		return infoArray;
	}

}