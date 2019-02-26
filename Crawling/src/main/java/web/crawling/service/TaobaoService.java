package web.crawling.service;

import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeOptions;

@SuppressWarnings("unchecked")
public class TaobaoService extends HtmlParser {

	private String IMG_PATH = "C:\\Users\\knowch\\Downloads\\taobao\\";
	
	public TaobaoService(String data, boolean isUrl) throws InterruptedException {
		super(data, isUrl);
	}
	
	public TaobaoService(String data, boolean isUrl, int delay) throws InterruptedException {
		super(data, isUrl, delay);
	}
	
	public TaobaoService(String data, boolean isUrl, ChromeOptions options, int delay) throws InterruptedException {
		super(data, isUrl, options, delay);
	}
	
	@Override
	public JSONObject getJsonData() {
		System.out.println(webDriver.getPageSource());
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
		//상품 옵션
		jsonData.put("options", getItemOptions());
		//상품 상세 정보
		jsonData.put("detail_infos", getDetailInfo());
		return jsonData;
	}
	
	//상품 id 반환
	public String getItemId() {
		String itemId = getElementById("J_Pine").getAttribute("data-itemid");
		//상품 이미지, 내용 이미지 저장할 폴더가 없으면 생성
		IMG_PATH += itemId;
		HttpImageUtil.makeSavePath(IMG_PATH + "\\item");
		HttpImageUtil.makeSavePath(IMG_PATH + "\\content");
		return itemId;
	}
	
	//상품명 반환
	public String getTitle() {
		return getElementByClassName("tb-main-title").getText();
	}

	//상품가 반환
	public String getPrice() {
		return getElementByClassName("tb-rmb").getText() + " " + getElementByClassName("tb-rmb-num").getText();
	}

	//상품 대표이미지 경로 반환
	//기본 썸네일이 50x50
	public JSONArray getImg(String size) {
		List<WebElement> list = getElementsByCssSelector(".tb-s50 img");
		JSONArray imgArray = new JSONArray();
		for(int i=0; i<list.size(); i++) {
			String imgUrl = list.get(i).getAttribute("src").replaceAll("50x50", size);
			String savePath = IMG_PATH + "\\item\\img_" + size + "_" + i + ".png"; //대표이미지는 상품 id/item 폴더에 저장
			imgArray.add(HttpImageUtil.saveImg(imgUrl, savePath));
		}
		
		return imgArray;
	}

	//상품 내용 반환
	public JSONObject getContent() {
		JSONObject contentData = new JSONObject();
		//content 영역
		WebElement contentElement =  getElementById("J_DivItemDesc");
		//content 이미지 경로 가져오기
		JSONArray contentImgArray = new JSONArray();
		List<WebElement> contentImgList = contentElement.findElements(By.tagName("img"));
		for(int i=0; i<contentImgList.size(); i++) {
			WebElement contentImg = contentImgList.get(i); //content 영역의 img 태그
			String lazyLoadImgSrc = contentImg.getAttribute("data-ks-lazyload"); //lazy load 이미지 경로
			String basicImgSrc = contentImg.getAttribute("src"); //기본 설정 이미지 경로
			String imgUrl = lazyLoadImgSrc != null && lazyLoadImgSrc != ""? lazyLoadImgSrc : basicImgSrc; //lozy load 이미지 경로가 있으면 그 경로로 설정
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
		JSONArray optionArray = new JSONArray();
		getElementsByClassName("J_TSaleProp").forEach(element -> {
			JSONObject optionData = new JSONObject();
			optionData.put("name", element.getAttribute("data-property"));
			//옵션 값들은 여러개이므로 배열이 되어야 함(예->생상 : 흰색, 검정, 파랑)
			List<WebElement> list = element.findElements(By.cssSelector("li span"));
			JSONArray valueArray = new JSONArray();
			for(int i=0; i<list.size(); i++)
				valueArray.add(removeHTMLTag(list.get(i).getAttribute("innerHTML")));
			optionData.put("value", valueArray);
			optionArray.add(optionData);
		});

		return optionArray;
	}

	//상품 상세 정보 반환
	public JSONArray getDetailInfo() {
		JSONArray infoArray = new JSONArray();
		getElementsByCssSelector(".attributes-list li").forEach(element -> {
			String[] infos = element.getText().split(":"); //이름: 값 형식으로 되어있음
			String name = infos[0], value = infos.length == 2? infos[1].replaceAll(" ", "") : ""; //": 값"의 공백 제거
			JSONObject infoData = new JSONObject();
			infoData.put("name", name);
			infoData.put("value", value);
			infoArray.add(infoData);
		});

		return infoArray;
	}

}