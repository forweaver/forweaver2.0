package com.forweaver.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.HtmlUtils;

import com.github.rjeschke.txtmark.Processor;



/** 각종 웹 유틸 클래스
 * @author go
 *
 */
public class WebUtil {


	/** 압축파일을 열었을 때 모든 파일들이 한 디렉토리에 담겨있는지 검사함.
	 * @param file
	 * @return
	 */
	public static boolean isOneDirectory(String zipFile){
		boolean oneDirectory = true;
		try{
			//압축파일을 품.  
			ZipInputStream zis = 
					new ZipInputStream(new FileInputStream(zipFile));
			ZipEntry ze = zis.getNextEntry();
			while(ze!=null){
				if (!ze.isDirectory() && !ze.getName().contains("/"))
					oneDirectory = false;
				ze = zis.getNextEntry();
			}
			zis.closeEntry();
			zis.close();
		}catch(Exception e){

		}
		return oneDirectory;
	}

	/** 파일명에서 확장자 가져오기
	 * @param fileName
	 * @return
	 */
	public static String getFileExtension(String fileName) {
		int lastIndexOf = fileName.lastIndexOf(".");
		if (lastIndexOf == -1) {
			return "";
		}
		return fileName.substring(lastIndexOf);
	}



	/** 특수문자 제거
	 * @param str
	 * @return
	 */
	public static boolean isCodeName(String str){     
		str = str.toLowerCase();
		if(str.endsWith(".c") || str.endsWith(".h")|| str.endsWith(".ino")
				|| str.endsWith(".java")|| str.endsWith(".py")|| str.endsWith(".cpp") || str.endsWith(".hpp")
				|| str.endsWith(".html")|| str.endsWith(".css")|| str.endsWith(".pl") || str.endsWith(".r")
				|| str.endsWith(".sql")|| str.endsWith(".php")|| str.endsWith(".cs") || str.endsWith(".scala")
				|| str.endsWith("makefile")|| str.endsWith(".bas")|| str.endsWith(".asm") || str.endsWith(".jsp")
				|| str.endsWith("pym")|| str.endsWith(".less")|| str.endsWith(".rh") || str.endsWith(".cc")
				|| str.endsWith("cp")|| str.endsWith(".mf")|| str.endsWith(".rh") || str.endsWith(".meta")
				|| str.endsWith(".m")|| str.endsWith(".wiki")|| str.endsWith(".lua") || str.endsWith(".json")
				|| str.endsWith(".rb")|| str.endsWith(".txt")|| str.endsWith(".js") || str.endsWith(".do")
				|| str.endsWith(".aspx")|| str.endsWith(".tcl")|| str.endsWith(".el") || str.endsWith(".cxx")
				|| str.endsWith(".iml")|| str.endsWith(".htm")|| str.endsWith(".rhtml") || str.endsWith(".pde")
				|| str.endsWith("license")|| str.endsWith(".gradle")|| str.endsWith("notice") || str.endsWith(".bat")
				|| str.endsWith(".gitignore")|| str.endsWith(".asp")|| str.endsWith(".ss") || str.endsWith(".properties")
				|| str.endsWith(".xml")|| str.endsWith(".md") || str.endsWith(".log")|| str.endsWith(".pom"))
			return true;
		return false;
	}

	public static boolean isAllowedFileName(String fileName){     
		fileName = fileName.toLowerCase();

		if (fileName.contains(".git/") 	|| fileName.contains("debug/") 	||
				fileName.contains("classes/") || fileName.contains("gen-external-apklibs/") ||  
				fileName.contains("release/") || fileName.endsWith(".exe") || 
				fileName.endsWith(".ipch") || fileName.endsWith(".sdf") || 
				fileName.endsWith(".bak") || fileName.endsWith(".log") || 
				fileName.endsWith(".pyc") || fileName.endsWith(".pyd") || 
				fileName.endsWith(".opensdf") || fileName.endsWith(".ilk") || 	
				fileName.endsWith(".pyo") || fileName.endsWith(".lib") ||
				fileName.endsWith(".class") || fileName.endsWith(".dex") ||
				fileName.endsWith(".bak") || fileName.endsWith(".tlb") ||
				fileName.endsWith(".class") || fileName.endsWith(".dex") ||
				fileName.endsWith(".ap_") || fileName.endsWith(".apk"))
			return false;

		return true;
	}



	public static boolean isImageName(String filename){ 
		filename = filename.toUpperCase();

		if(filename.endsWith(".ANI") || filename.endsWith(".BMP") || filename.endsWith(".CAL")
				|| filename.endsWith(".CAL") || filename.endsWith(".FAX") || filename.endsWith(".GIF")
				|| filename.endsWith(".IMG") || filename.endsWith(".JPE") || filename.endsWith(".JPEG")
				|| filename.endsWith(".JPG") || filename.endsWith(".MAC") || filename.endsWith(".PBM")
				|| filename.endsWith(".PCD") || filename.endsWith(".PCX") || filename.endsWith(".PCT")
				|| filename.endsWith(".PGM") || filename.endsWith(".PNG") || filename.endsWith(".PPM")
				|| filename.endsWith(".PSD") || filename.endsWith(".RAS") || filename.endsWith(".TGA")
				|| filename.endsWith(".TIF") || filename.endsWith(".TIFF") || filename.endsWith(".WMF")){
			return true;
		}
		return false;
	}

	/** 글의 http 주소가 있으면 링크로 바꿔줌.
	 * @param plain
	 * @return
	 */
	public static String addLink(String text){
		if (text == null) {
			return text;
		}

		String escapedText = HtmlUtils.htmlEscape(text)
				.replaceAll("(\\A|\\s)((http|https|ftp|mailto):\\S+)(\\s|\\z)","$1<a href=\"$2\">$2</a>$4");

		return escapedText;
	}

	/** 제출날짜 가져오기
	 * @param pageUrl
	 * @return 제출 날짜
	 */
	public static Date getDeadLine(int day){

		Date time = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(time);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.add(Calendar.DATE, day+1);
		return cal.getTime();
	}


	/** page url 부분을 해석하여 페이지 사이즈를 가져오는 메서드.
	 * @param pageUrl
	 * @param size
	 * @return 페이지 사이즈
	 */
	public static int getPageSize(String pageUrl,int size){  
		if(size <1)
			size = 15;

		try{
			if(pageUrl.contains(",")){
				size = Integer.parseInt(pageUrl.split(",")[1]);
			}
		}catch(Exception e){}

		return size;
	}

	/** page url 부분을 해석하여 페이지 번호를 가져오는 메서드.
	 * @param pageUrl
	 * @return 페이지의 번호
	 */
	public static int getPageNumber(String pageUrl){
		int page = 1;
		try{
			if(pageUrl.contains(",")){
				page = Integer.parseInt(pageUrl.split(",")[0]);
			}else{
				page =Integer.parseInt(pageUrl);
			}
		}catch(Exception e){}

		return page;
	}


	/** 문자열을 MD5 변환하여 암호화하는 메서드.
	 * @param str
	 * @return MD5로 변환된 문자열.
	 */
	public static String convertMD5(String str) {
		// TODO Auto-generated method stub
		String email = str;
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] messageDigest = md.digest(email.getBytes());
			BigInteger number = new BigInteger(1, messageDigest);
			return number.toString(16);
		} catch (Exception e) {
			return "";
		}

	}


	/** 마크다운 문자열을 해석하여 html화된 문자열 변환하는 메서드.
	 * @param str
	 * @return html화된 문자열.
	 */
	public static String markDownEncoder(String str){

		str = str.replaceAll("(\\A|\\s)((http|https|ftp|mailto):\\S+)(\\s|\\z)","$1<$2>$4"); // 자동 링크 추가
		try{
			str = Processor.process(str, true).replaceAll("\n", "</p><p>");
		}catch(Exception e){}	
		if(str.contains("&lt;iframe width=\"560\" height=\"315\" src=\"https://www.youtube.com/embed/")){// 유투브 기본 동영상 태그는 허용함.
			str = str.replaceAll("&lt;iframe width", "<iframe width");
			str = str.replaceAll("allowfullscreen>&lt;/iframe>", "allowfullscreen></iframe>");
		}

		return str;
	}

	/**	이전시간과 현재시간과의 차이를 계산하여 지난시간 반환
	 * @param date 날짜를 문자열로 받는다.
	 * @return 지난시간을 문자열로 반환한다. (Ex] 1초전, 1시간, 1년)
	 */
	public static String TimeLagTest(String date) throws ParseException {
		long timeLag, year, month, day;
		int hour, min, sec;
		String str;

		SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date getDate, setDate;

		getDate = format.parse(date);
		setDate = new Date();

		timeLag = setDate.getTime() - getDate.getTime();
		sec = (int) (timeLag / 1000);

		day = sec / 86400;

		if (day < 30)
			month = 0;
		else
			month = day / 30;

		if (month < 12)
			year = 0;
		else
			year = month / 12;

		if (sec < 60) {
			min = 0;
		} else {
			min = (int) (sec / 60);
			sec = (int) (sec % 60);
		}

		if (min < 60) {
			hour = 0;
		} else {
			hour = min / 60;
			min = min % 60;
		}

		if (day == 0) {
			if (min < 1) {
				str = new String(sec + "초전");
			} else if (hour < 1) {
				str = new String(min + "분전");
			} else {
				str = new String(hour + "시간");
			}
		} else {
			if (month < 1) {
				str = new String(day + "일");
			} else if (year < 1) {
				str = new String(month + "달");
			} else {
				str = new String(year + "년");
			}
		}
		return str;
	}


	public static int nth(String source, String pattern, int n) {

		int i = 0, pos = 0, tpos = 0;

		while (i < n) {

			pos = source.indexOf(pattern);
			if (pos > -1) {
				source = source.substring(pos+1);
				tpos += pos+1;
				i++;
			} else {
				return -1;
			}
		}

		return tpos - 1;
	}



	public static List<String> getFileList(List<String> list, String filePath){
		List<String> returnList = new ArrayList<String>();
		int spiltNumber = 0;

		if(filePath.equals("/"))
			spiltNumber= 1;
		else
			spiltNumber=filePath.split("/").length;

		for(String path : list){
			if(path.startsWith(filePath)){
				if(path.split("/").length>spiltNumber+1){
					path = path.substring(0, nth(path,"/",spiltNumber+1));
				}
				if(!returnList.contains(path)){
					returnList.add(path);
				}		
			}
		}
		return returnList;
	}


	public static boolean multipartFileToTempFile(String fileName,MultipartFile multipartFile){
		String destination = fileName;
		File file = new File(destination);
		try{
			multipartFile.transferTo(file);
		}catch(Exception e){
			return false;
		}

		return true;
	}
	public static void unZip(String zipFile, String outputFolder,boolean skipDirectory){

		byte[] buffer = new byte[1024];

		try{
			ZipInputStream zis = 
					new ZipInputStream(new FileInputStream(zipFile),Charset.forName("EUC-KR"));
			ZipEntry ze = zis.getNextEntry();

			while(ze!=null){

				String fileName = ze.getName();
				if (!ze.isDirectory() && isAllowedFileName(fileName)) { 
					String path = "";

					if(skipDirectory)
						path = outputFolder + File.separator + fileName.substring(fileName.indexOf("/"));
					else
						path = outputFolder + File.separator + fileName;

					File newFile = new File(path);

					new File(newFile.getParent()).mkdirs();

					FileOutputStream fos = new FileOutputStream(newFile);             

					int len;
					while ((len = zis.read(buffer)) > 0) {
						fos.write(buffer, 0, len);
					}

					fos.close();   
				}
				ze = zis.getNextEntry();
			}

			zis.closeEntry();
			zis.close();

		}catch(IOException ex){
			System.out.println(ex.getLocalizedMessage());
		}
	}    

	public static byte[] concatenateByteArrays(byte[] a, byte[] b) {
		byte[] result = new byte[a.length + b.length]; 
		System.arraycopy(a, 0, result, 0, a.length); 
		System.arraycopy(b, 0, result, a.length, b.length); 
		return result;
	} 

}
