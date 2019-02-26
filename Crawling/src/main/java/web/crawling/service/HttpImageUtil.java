package web.crawling.service;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;

public class HttpImageUtil {
	
	//이미지 다운로드 후 저장된 경로 반환, 실패시 null 반환
	public static String saveImg(String imgUrl, String savePath) {
		InputStream in = null;
		ByteArrayOutputStream out = null;
		FileOutputStream fos = null;
		try {
			//img url 요청
			URL url = new URL(imgUrl);
			in = new BufferedInputStream(url.openStream());
			out = new ByteArrayOutputStream();
			//이미지 다운로드
			byte[] buf = new byte[1024];
			int n = 0;
			while ((n = in.read(buf)) != -1)
				out.write(buf, 0, n);
			out.close();
			in.close();
			//response = 이미지 다운로드 결과물
			byte[] response = out.toByteArray();
			//savePath(경로 + 파일명)에 저장
			fos = new FileOutputStream(savePath);
			fos.write(response);
			fos.close();

		} catch (Exception e) {
			e.printStackTrace();
			savePath = null;
		
		} finally {
			try {
				if(in != null) in.close();
				if(out != null) out.close();
				if(fos != null) fos.close();
				
			} catch (Exception e2) {}
		}
		
		return savePath;
	}
	
	public static void makeSavePath(String path) {
		File file = new File(path);
		if(!file.exists()) file.mkdirs();
	}

}
