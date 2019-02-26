

import web.crawling.service.AlibabaService;
import web.crawling.service.HtmlParser;

public class Main {

	public static void main(String[] args) throws InterruptedException {
//		String taobaoUrl = "https://item.taobao.com/item.htm?spm=a21wu.241046-kr.4691948847.1.41cab6cbpXNEvJ&scm=1007.15423.84311.100200300000005&id=554579950115&pvid=2474ab58-c510-4f86-9f01-a52c09223f7a";
//		String taobaoUrl = "https://item.taobao.com/item.htm?spm=5706.1529727.a31f2.3.2a766c7eRfIX7o&scm=1007.12883.24200.100200300000004&id=552494561699&pvid=3d48a548-adbe-4750-956a-7a7dfbe047a7";
//		String alibabaUrl = "https://detail.1688.com/offer/586966220457.html?spm=a26239.11860558.jknkkcmq.2.331558afKx500Y&scm=1007.20967.122822.0&pvid=7397e945-5cdf-4ab4-a4a1-7563063ed1d1&tpp_trace=0b0b60cd15510794288361635d0732";
		String alibabaUrl = "https://detail.1688.com/offer/582083327845.html?spm=a2615.7691456.autotrace-offerGeneral.1.32d15668Z9qk4P";
//		HtmlParser parser = new TaobaoService(taobaoUrl, true);
		HtmlParser parser = new AlibabaService(alibabaUrl, true);
		System.out.println(parser.getJsonData());
		parser.close();
	}
}
