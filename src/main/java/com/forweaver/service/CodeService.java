package com.forweaver.service;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.forweaver.domain.Code;
import com.forweaver.domain.Data;
import com.forweaver.domain.RePost;
import com.forweaver.domain.SimpleCode;
import com.forweaver.domain.Weaver;
import com.forweaver.mongodb.dao.CodeDao;
import com.forweaver.mongodb.dao.DataDao;
import com.forweaver.mongodb.dao.RePostDao;
import com.forweaver.util.WebUtil;

/** 코드 서비스
 *
 */
@Service
public class CodeService {

	@Autowired CodeDao codeDao;
	@Autowired DataDao dataDao;
	@Autowired RePostDao rePostDao;

	/** 코드를 추가함.
	 * @param code
	 * @param file
	 */
	public boolean add(Code code, MultipartFile file,MultipartFile output) {
		String zipPath = "/tmp/"+new org.bson.types.ObjectId().toString()+".zip";
		byte[] buf = new byte[1024];
		int len;
		try {			
			if ((file.getContentType().equals("application/zip") ||
					file.getContentType().equals("application/x-zip-compressed")) && 
					file.getOriginalFilename().toLowerCase().endsWith(".zip")) { 
				// zip파일의 경우 내부를 살펴봄

				WebUtil.multipartFileToTempFile(zipPath, file);

				try{
					ZipInputStream zis = 
							new ZipInputStream(new FileInputStream(zipPath),Charset.forName("8859_1"));
					ZipEntry ze = zis.getNextEntry();

					while(ze!=null){
						String fileName = ze.getName();
						if (!ze.isDirectory() && WebUtil.isAllowedFileName(fileName) ) { // 만약 파일의 경우

							StringBuilder content = new StringBuilder();

							while ((len = zis.read(buf)) > 0)
								if(WebUtil.isCodeName(new String(fileName.getBytes("8859_1"),"EUC-KR")))
									content.append(new String(buf, 0, len,Charset.forName("EUC-KR")));
								else
									content.append(new String(buf, 0, len,Charset.forName("8859_1")));

							if (ze.getName().toUpperCase().endsWith("README.MD")){ // 리드미파일의 경우
								code.setReadme(content.toString());
								code.addFirstSimpleCode(new SimpleCode(fileName,content.toString()));
							}else{
								code.addSimpleCode(new SimpleCode(new String(fileName.getBytes("8859_1"),"EUC-KR"),content.toString())); // 일반 파일의 경우
							}
						}
						ze = zis.getNextEntry();
					}

					zis.closeEntry();
					zis.close();

					
				}catch(IOException ex){
					System.err.println(ex.getMessage());
					new File(zipPath).delete();
					return false;
				}
			} else if(WebUtil.isCodeName(file.getOriginalFilename())){ // 압축파일이 아닌 일반 파일의 경우

				StringBuilder content = new StringBuilder();
				InputStream is = file.getInputStream();

				while ((len = is.read(buf)) != -1)
					content.append(new String(buf, 0, len));

				code.addSimpleCode(new SimpleCode(file.getOriginalFilename(), content.toString())); // 일반 파일의 경우
				
			}

			if(output != null && output.getSize()>0 && WebUtil.isImageName(output.getOriginalFilename())){
				StringBuilder content = new StringBuilder();
				InputStream is = output.getInputStream();

				while ((len = is.read(buf)) != -1)
					content.append(new String(buf, 0, len,Charset.forName("8859_1")));

				code.addSimpleCode(new SimpleCode(output.getOriginalFilename().replace(" ", "_"), content.toString())); // 결과화면
			}
			codeDao.insert(code);
			
			new File(zipPath).delete();
			return true;
		} catch (Exception e) {
			System.err.println(e.getMessage());
			new File(zipPath).delete();
			return false;
		} 
	}

	/** 코드를 가져옴
	 * @param codeID
	 * @return
	 */
	public Code get(int codeID,boolean onlyCode) {
		Code code = codeDao.get(codeID) ;
		if(code != null && onlyCode)
			code.onlyViewCode();
		return code;
	}

	/** 코드를 다운로드함.
	 * @param code
	 * @param os
	 */
	public void dowloadCode(Code code, OutputStream os){
		try {
			ZipOutputStream zip = new ZipOutputStream(new BufferedOutputStream(os),Charset.forName("8859_1"));
			for(SimpleCode simpleCode :code.getCodes()){
				zip.putNextEntry(new ZipEntry(new String (simpleCode.getFileName().getBytes(),"8859_1") ));
				if(WebUtil.isCodeName(new String(simpleCode.getFileName().getBytes("8859_1"),"EUC-KR")))
					zip.write(simpleCode.getContent().getBytes("EUC-KR"));
				else
					zip.write(simpleCode.getContent().getBytes("8859_1"));

			}	
			code.download();
			codeDao.update(code);
			zip.close();
		} catch(Exception e) {
			System.err.println(e.getMessage());
		}
	}

	/** 코드를 검색하고 갯수를 파악함.
	 * @param weaver
	 * @param tags
	 * @param serach
	 * @param sort
	 * @return
	 */
	public long countCodes(Weaver weaver,List<String> tags,
			String search, String sort) {
		if(sort.equals("my"))
			return codeDao.countCodes(weaver, tags, search, sort);
		return codeDao.countCodes(null, tags, search, sort);
	}

	/** 코드를 검색함.
	 * @param weaver
	 * @param tags
	 * @param search
	 * @param sort
	 * @param page
	 * @param size
	 * @return
	 */
	public List<Code> getCodes(Weaver weaver, List<String> tags,
			String search, String sort, int page, int size) {
		if(sort.equals("my"))
			return codeDao.getCodes(tags, search, weaver, sort, page, size);
		return codeDao.getCodes(tags, search, null, sort, page, size);
	}
	
	/** 코드를 검색하고 갯수를 파악함.
	 * @param weaver
	 * @param tags
	 * @param serach
	 * @param sort
	 * @return
	 */
	public long countMyCodes(Weaver weaver,List<String> tags,
			String search, String sort) {
		return codeDao.countCodes(weaver, tags, search, sort);
	}

	/** 코드를 검색함.
	 * @param weaver
	 * @param tags
	 * @param search
	 * @param sort
	 * @param page
	 * @param size
	 * @return
	 */
	public List<Code> getMyCodes(Weaver weaver, List<String> tags,
			String search, String sort, int page, int size) {
		return codeDao.getCodes(tags, search, weaver, sort, page, size);
	}


	/** 코드 삭제.
	 * @param weaver
	 * @param code
	 * @return
	 */
	public boolean delete(Weaver weaver,Code code) {
		if(weaver == null || code == null)
			return false;

		if(weaver.isAdmin() || weaver.equals(code.getWriter())){
			
			for(RePost rePost:rePostDao.gets(code)) { // 관련 답변들을 전부 불러옴
				
				for(Data data:rePost.getDatas()) //자료 전부 삭제.
					dataDao.delete(data);
				
				rePostDao.delete(rePost); // 답변 차례대로 삭제
			}
			
			codeDao.delete(code);
			return true;
		}
		return false;
	}

	/** 코드 수정
	 * @param code
	 */
	public void update(Code code) {
		codeDao.update(code);
	}

}
