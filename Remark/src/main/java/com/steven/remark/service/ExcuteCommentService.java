package com.steven.remark.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import com.steven.remark.common.Const;
import com.steven.remark.enumeration.ExtensionName;

public class ExcuteCommentService {

	private static final int BUFFER_SIZE = 4096;

	private FileInputStream fis = null;
	private FileOutputStream fos = null;
	private InputStreamReader isr = null;
	private FileWriter fw = null;
	private BufferedReader br = null;
	private BufferedWriter bw = null;
	
	// @formatter:off
	public void processRoot(String filePath) {
		File file = new File(filePath);
		String subInPath = "";
		String rootPath = "";
		
		if (file.isDirectory()) {
			
			rootPath = processOutPutRootPath(filePath);// 先寫出root
			File fi = new File(rootPath);

			if (!fi.exists() && !fi.isDirectory()) {// 判斷該路徑是否有資料夾
				fi.mkdirs();
			}
			
			String outPath = rootPath.concat(Const.ROOT);
			File results[] = file.listFiles();// 取得目錄下檔案和資料夾

			for (int i = 0; i < results.length; i++) {

				if (results[i].isFile()) {
					
					parseFile(results[i], outPath);// 解析檔案
					
				} else {
					
					subInPath = filePath.concat(Const.ROOT).concat(results[i].getName());// 產生資料夾路徑
					processRoot(subInPath);
					
				}
			}
		}
	}
	
	private void parseFile(File file, String outPath) {
		String fileName = file.getName();
		String suffixStr = "";
		String fullPath = "";
		
		if (null != fileName && !"".equals(fileName)) {// 根據副檔名做相應處理
			
			suffixStr = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());// 取得副檔名			
			fullPath = outPath.concat(file.getName());
			
			if (suffixStr.toUpperCase().equals(ExtensionName.JAVA.toString())) {				
				processExtensionJava(file, fullPath);
			} else if (suffixStr.toUpperCase().equals(ExtensionName.JSP.toString())) {
				processExtensionJsp(file, fullPath);
			} else if (suffixStr.toUpperCase().equals(ExtensionName.XML.toString())) {
				processExtensionXml(file, fullPath);
			} else {
				processExtensionOther(file, fullPath);
			}
		}
	}
	
	/**
	 *
	 * Description : 處理Java檔
	 * 
	 * @since 2020/09/08
	 * @author Steven
	 * @param file   輸入檔案
	 * @param fullPath  檔案輸出位置
	 *
	 **/
	private void processExtensionJava(File file, String fullPath) {
		try {
			String status = "";
			boolean flag = false;// 判斷是否為多行註解用

			if (file.isFile()) {
				fis = new FileInputStream(file);
				isr = new InputStreamReader(fis);
				br = new BufferedReader(isr);
			}

			fw = new FileWriter(new File(fullPath));
			bw = new BufferedWriter(fw);
			
			String content = "";
			while ((content = br.readLine()) != null) {

				if (!flag) { // 不在多行註解範圍內

					if (content.contains(Const.JAVA_MULTI_ST)) { 
						
						if (content.contains(Const.JAVA_MULTI_END)) {// 多行註解開始結束在同一行

							status = processWriteLine(content, Const.JAVA_MULTI_ST, Const.JAVA_MULTI_END, bw, false);
						
						} else if (!content.contains(Const.JAVA_MULTI_END)) {// 多行註解開始結束不在同一行
							
							status = processWriteLine(content, Const.JAVA_MULTI_ST, Const.JAVA_MULTI_END, bw, true);

							if (status.equals(Const.WRITE_LINE_STATUS_FLAG)) {
								
								flag = true;
								
							}
						}
					} else {

						if (content.contains(Const.JAVA_SIGLE)) {// 單行註解
							
							status = processWriteLine(content, Const.JAVA_SIGLE, null, bw, false);

						} else {
							
							bw.write(content);
							bw.newLine();
							
						}
					}
				} else {
					
					if (content.contains(Const.JAVA_MULTI_END)) {// 註解結束
						
						status = processWriteEnd(content, Const.JAVA_MULTI_END, bw);

						if (status.equals(Const.WRITE_LINE_STATUS_FLAG)) {
							
							flag = false;
						
						}
					}
				}
			}
			bw.flush();
			fis.close();
			isr.close();
			br.close();
			fw.close();
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 *
	 * Description : 處理Jsp檔
	 * 
	 * @since 2020/09/08
	 * @author Steven
	 * @param file   輸入檔案
	 * @param fullPath  檔案輸出位置
	 *
	 **/
	private void processExtensionJsp(File file, String fullPath) {
		try {

			boolean flag = false;
			String status = "";

			if (file.isFile()) {
				fis = new FileInputStream(file);
				isr = new InputStreamReader(fis);
				br = new BufferedReader(isr);
			}

			fw = new FileWriter(new File(fullPath));
			bw = new BufferedWriter(fw);

			String content = "";
			while ((content = br.readLine()) != null) {

				if (!flag) {
					if (content.contains(Const.JAVA_MULTI_ST)) {

						if (content.contains(Const.JAVA_MULTI_END)) {// 多行註解開始結束在同一行

							status = processWriteLine(content, Const.JAVA_MULTI_ST, Const.JAVA_MULTI_END, bw, false);


						} else if (!content.contains(Const.JAVA_MULTI_END)) {// 多行註解開始結束不在同一行

							status = processWriteLine(content, Const.JAVA_MULTI_ST, Const.JAVA_MULTI_END, bw, true);

							if (status.equals(Const.WRITE_LINE_STATUS_FLAG)) {
								
								flag = true;
								
							}
						}
					} else if (content.contains(Const.JSP_TAG_ST)) {
						bw.write(content);
						bw.newLine();
					} else if (content.contains(Const.JSP_MULTI_TYPE_A_ST)) {

						if (content.contains(Const.JSP_MULTI_TYPE_A_END)) {

							status = processWriteLine(content, Const.JSP_MULTI_TYPE_A_ST, Const.JSP_MULTI_TYPE_A_END, bw, false);

						} else if (!content.contains(Const.JSP_MULTI_TYPE_A_END)) {

							status = processWriteLine(content, Const.JSP_MULTI_TYPE_A_ST, Const.JSP_MULTI_TYPE_A_END, bw, true);

							if (status.equals(Const.WRITE_LINE_STATUS_FLAG)) {
								
								flag = true;
								
							}
						}
					} else if (content.contains(Const.JSP_MULTI_TYPE_B_ST)) {
						
						if (content.contains(Const.JSP_MULTI_TYPE_B_END)) {
							status = processWriteLine(content, Const.JSP_MULTI_TYPE_B_ST, Const.JSP_MULTI_TYPE_B_END, bw, false);

						} else if (!content.contains(Const.JSP_MULTI_TYPE_B_END)) {
							status = processWriteLine(content, Const.JSP_MULTI_TYPE_B_ST, Const.JSP_MULTI_TYPE_B_END, bw, true);
							if (status.equals(Const.WRITE_LINE_STATUS_FLAG)) {
								flag = true;
							}
						}					
					} else {

						if (content.contains(Const.JAVA_SIGLE)) {// 單行註解

							status = processWriteLine(content, Const.JAVA_SIGLE, null, bw, false);

							if (status.equals(Const.WRITE_LINE_STATUS_FLAG)) {
								
								flag = true;
								
							}
						} else {
							bw.write(content);
							bw.newLine();
						}
					}
				} else {
					
					if (content.contains(Const.JAVA_MULTI_END)) {

						if (content.contains(Const.JAVA_MULTI_END)) {

							status = processWriteEnd(content, Const.JAVA_MULTI_END, bw);

							if (status.equals(Const.WRITE_LINE_STATUS_FLAG)) {
								
								flag = false;
							}
						}
					} else if (content.contains(Const.JSP_MULTI_TYPE_A_END)) {

						if (content.contains(Const.JSP_MULTI_TYPE_A_END)) {

							status = processWriteEnd(content, Const.JSP_MULTI_TYPE_A_END, bw);

							if (status.equals(Const.WRITE_LINE_STATUS_FLAG)) {
								
								flag = false;
								
							}
						}
					} else if (content.contains(Const.JSP_MULTI_TYPE_B_END)) {

						if (content.contains(Const.JSP_MULTI_TYPE_B_END)) {

							status = processWriteEnd(content, Const.JSP_MULTI_TYPE_B_END, bw);

							if (status.equals(Const.WRITE_LINE_STATUS_FLAG)) {
								
								flag = false;
								
							}
						}
					}
				}
			}
			bw.flush();
			fis.close();
			isr.close();
			br.close();
			fw.close();
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 *
	 * Description : 處理Xml檔
	 * 
	 * @since 2020/09/08
	 * @author Steven
	 * @param file   輸入檔案
	 * @param fullPath  檔案輸出位置
	 *
	 **/
	private void processExtensionXml(File file, String fullPath) {
		try {
			String status = "";
			boolean flag = false;

			if (file.isFile()) {
				fis = new FileInputStream(file);
				isr = new InputStreamReader(fis);
				br = new BufferedReader(isr);
			}

			fw = new FileWriter(new File(fullPath));
			bw = new BufferedWriter(fw);
			
			String content = "";
			while ((content = br.readLine()) != null) {

				if (!flag) {

					if (content.contains(Const.JSP_MULTI_TYPE_A_ST)) {
						
						if (content.contains(Const.JSP_MULTI_TYPE_A_END)) {
							
							status = processWriteLine(content, Const.JSP_MULTI_TYPE_A_ST, Const.JSP_MULTI_TYPE_A_END, bw, false);
							
						} else if (!content.contains(Const.JSP_MULTI_TYPE_A_END)) {
							
							status = processWriteLine(content, Const.JSP_MULTI_TYPE_A_ST, Const.JSP_MULTI_TYPE_A_END, bw, true);

							if (status.equals(Const.WRITE_LINE_STATUS_FLAG)) {
								
								flag = true;
							
							}
						}
					} else {
						bw.write(content);
						bw.newLine();
					}
				} else {
					
					if (content.contains(Const.JSP_MULTI_TYPE_A_END)) {

						status = processWriteEnd(content, Const.JSP_MULTI_TYPE_A_END, bw);
						flag = false;

					}			
				}
			}
			bw.flush();
			fis.close();
			isr.close();
			br.close();
			fw.close();
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 *
	 * Description : 處理其他檔案
	 * 
	 * @since 2020/09/08
	 * @author Steven
	 * @param file   輸入檔案
	 * @param fullPath  檔案輸出位置
	 *
	 **/
	private void processExtensionOther(File file, String fullPath) {
		try {

			fis = new FileInputStream(file);
			fos = new FileOutputStream(fullPath);

			byte[] buffer = new byte[BUFFER_SIZE];
			int bytesRead = -1;

			while ((bytesRead = fis.read(buffer)) != -1) {
				fos.write(buffer, 0, bytesRead);
			}

			fos.flush();
			fis.close();
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 *
	 * Description : 處理開頭註解
	 * 
	 * @since 2020/09/08
	 * @author Steven
	 * @param content   處理內容
	 * @param prefixSt  註解開始
	 * @param prefixEnd 註解結束
	 * @param bw        寫出用物件
	 * @param multiLine 開始結束是否均在同一行
	 * @return String 狀態判斷用
	 *
	 **/
	private String processWriteLine(String content, String prefixSt, String prefixEnd, BufferedWriter bw,
			boolean multiLine) throws IOException {

		int indexSt = content.indexOf(prefixSt);
		int indexEnd = 0;
		
		if (!prefixSt.equals(Const.JAVA_SIGLE)) {// 單行註解沒有結束標籤
			indexEnd= content.indexOf(prefixEnd);
		}
		
		String stStr = content.substring(0, indexSt);
		String endStr = "";

		if (prefixSt.equals(Const.JAVA_SIGLE)) {
			
			if (indexSt == 0 || stStr.isBlank()) {// 整行註解

				stStr ="";
				bw.write(stStr);
				bw.newLine();

			} else {// 非整行註解, ex: if(...){ //

				stStr = content.substring(0, indexSt);
				bw.write(stStr);
				bw.newLine();
				
			}	
		} else if (multiLine) { // 多行註解,開始結束在不同行  <%...%>  或  /*...*/  或 <!--...-->

				   if (indexSt == 0 || stStr.isBlank()) {
					   
						stStr ="";
						bw.write(stStr);
						bw.newLine();
						
					   return Const.WRITE_LINE_STATUS_FLAG;// 重設狀態且開始多行註解
					   
				   } else {

					   stStr = content.substring(0, indexSt);
					   bw.write(stStr);
					   bw.newLine();

					   return Const.WRITE_LINE_STATUS_FLAG;
					   
				   }
		} else {// 多行註解,開始結束在同一行
			
			int offSetStart = 0;
			int offSetEnd = 0;
			
			if (prefixSt.equals(Const.JSP_MULTI_TYPE_A_ST) && prefixEnd.equals(Const.JSP_MULTI_TYPE_A_END)) {
				
				offSetStart = 2;
				offSetEnd = 3;
				
			} else {
				
				offSetStart = 2;
				offSetEnd = 2;
				
			}			
			processContent(indexSt, indexEnd, content, stStr, endStr, bw, offSetStart, offSetEnd);			
		}		
		return Const.WRITE_LINE_STATUS_NONE;		
	}
	
	/**
	 *
	 * Description : 處理寫出內容
	 * 
	 * @since 2020/09/08
	 * @author Steven
	 * @param indexSt 註解開始索引
	 * @param indexEnd 註解結束索引
	 * @param content 讀入內容
	 * @param stStr 註解開始前字串
	 * @param endStr 註解結束後字串
	 * @param bw 寫出用物件
	 * @param offSetSt 判斷開始用索引補償
	 * @param offSetEnd 判斷結束用索引補償
	 * @return String 狀態判斷用
	 *
	 **/
	private void processContent(int indexSt, int indexEnd, String content, String stStr, String endStr, 
			BufferedWriter bw, int offSetSt, int offSetEnd) throws IOException  {
		
		if (indexSt == 0 || stStr.isBlank()) {// 註解開始該行最前面
			
			if (indexEnd + offSetSt == content.length()) {// 判斷註解結束是否在最後面

				endStr ="";// 在最後面則寫出空字串
				bw.write(endStr);
				bw.newLine();

			} else {
				
				endStr = content.substring(indexEnd + offSetEnd, content.length());// 寫出註解結束後面的程式碼
				bw.write(endStr);
				bw.newLine();
				
			}			
		} else {

			stStr = content.substring(0, indexSt);
			
			if (indexEnd + offSetSt == content.length()) { // 判斷註解結束是否在最後面

				bw.write(stStr);
				bw.newLine();

			} else {// 寫出註解開始前與註解結束後程式碼

				endStr = content.substring(indexEnd + offSetEnd, content.length());
				bw.write(stStr);
				bw.write(endStr);
				bw.newLine();

			}
		}
	}

	/**
	 *
	 * Description : 處理結束註解
	 * 
	 * @since 2020/09/08
	 * @author Steven
	 * @param content   處理內容
	 * @param prefixEnd 註解結束
	 * @param bw        寫出用物件,
	 * @return String 狀態判斷用
	 *
	 **/
	private String processWriteEnd(String content, String prefixEnd, BufferedWriter bw) throws IOException {

		String endStr = "";
		int indexEnd = content.indexOf(prefixEnd);
		
		if (prefixEnd.equals(Const.JSP_MULTI_TYPE_A_END)) {// 判斷註解格式
			
			if (indexEnd + 3 == content.length()) {// 註解結束在該行最後
				
				endStr = "";
				bw.write(endStr);
				bw.newLine();

			} else {

				endStr = content.substring(indexEnd + 4, content.length());// 寫出註解結束後程式碼
				bw.write(endStr);
				bw.newLine();
				
			}			
		} else {// 註解結束不在該行最後
			
			if (indexEnd + 2 == content.length()) {
				
				endStr = "";
				bw.write(endStr);
				bw.newLine();

			} else {

				endStr = content.substring(indexEnd + 3, content.length());
				bw.write(endStr);
				bw.newLine();

			}	
		}
		return Const.WRITE_LINE_STATUS_FLAG;// 重設狀態且結束多行註解		
	}

	/**
	 *
	 * Description : 處理輸出路徑
	 * 
	 * @since 2020/09/10
	 * @author Steven
	 * @param input   輸入路徑
	 * @return String 輸出路徑
	 *
	 **/
	private String processOutPutRootPath(String input) {
		
		int stIndex = input.indexOf("/");
		int endIndex = input.indexOf("/", stIndex + 1);
		
		String out = input.substring(stIndex + 1, endIndex);
		
		return input.replace(out, Const.FILE_OUT_PATH);
	}
	// @formatter:on
}
