package com.rz.frame.utils;


import com.rz.frame.RzLogger;
import com.rz.frame.dto.HttpResultDto;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.CharArrayBuffer;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HttpTicketUtils {

	private CloseableHttpClient httpClient;
	
	public void setHttpClient(CloseableHttpClient httpClient) {
		this.httpClient = httpClient;
	}
	public String doGet(String url) {
		
		try {
			HttpResponse httpResponse = this.doAction(new HttpGet(url));
			
			HttpEntity entity = httpResponse.getEntity();
			String content = entityToString(entity);
			
			EntityUtils.consume(entity);
			return content;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public HttpResultDto doGetResult(String url) {

		try {
			HttpResponse httpResponse = this.doAction(new HttpGet(url));

			HttpEntity entity = httpResponse.getEntity();
			HttpResultDto httpResultDto=new HttpResultDto();
			httpResultDto.setHttpCode(httpResponse.getStatusLine().getStatusCode());
			String content = entityToString(entity);
			httpResultDto.setHttpContent(content);
			EntityUtils.consume(entity);
			return httpResultDto;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public String doGet(HttpGet httpGet) {
		
		try {
			HttpResponse httpResponse = this.doAction(httpGet);
			
			HttpEntity entity = httpResponse.getEntity();
			String content = entityToString(entity);
			
			EntityUtils.consume(entity);
			return content;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
		
	}
	
	public String doPost(String url, List<NameValuePair> paras) {
		try {
			HttpPost httpPost = new HttpPost(url);
			
			UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity(paras, "UTF-8");
			httpPost.setEntity(urlEncodedFormEntity);
			
			HttpResponse httpResponse = doAction(httpPost);
			HttpEntity entity = httpResponse.getEntity();
			String content = entityToString(entity);
			EntityUtils.consume(entity);
			return content;
		} catch (Exception e) {
			return null;
		}
	}
	
	public String doPost(String url, Map<String, String> paras) {
		try {
			HttpPost httpPost = new HttpPost(url);
			
			List<NameValuePair> formparams = null;
			if (paras != null && paras.size() > 0) {
				formparams = new ArrayList<>();
				for (Map.Entry<String, String> para : paras.entrySet()) {
					formparams.add(new BasicNameValuePair(para.getKey(), para.getValue()));
				}
				UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity(formparams, "UTF-8");
				httpPost.setEntity(urlEncodedFormEntity);
			}
			HttpResponse httpResponse = doAction(httpPost);
			HttpEntity entity = httpResponse.getEntity();
			String content = entityToString(entity);
			EntityUtils.consume(entity);
			return content;
		} catch (Exception e) {
			return null;
		}
	}
 
	private HttpResponse doAction(HttpRequestBase httpRequestBase) {
		try {
			httpRequestBase.addHeader(new BasicHeader("Origin", "https://kyfw.12306.cn"));
			httpRequestBase.addHeader(new BasicHeader(HttpHeaders.REFERER, "https://kyfw.12306.cn"));
			httpRequestBase.addHeader(new BasicHeader(HttpHeaders.HOST, "kyfw.12306.cn"));
			return httpClient.execute(httpRequestBase);
			
		} catch (IOException e) {
			RzLogger.error("请求异常：{},{}", httpRequestBase.getURI(), e.getMessage());
		}
		return null;
	}
	
 
	
	
	
	
	public static String entityToString(HttpEntity entity) {
		try {
			String result = null;
			if (entity == null) {
				return null;
			}
			long lenth = entity.getContentLength();
			if (lenth != -1 && lenth < 2048) {
				result = EntityUtils.toString(entity, StandardCharsets.UTF_8);
			} else {
				InputStreamReader reader1 = new InputStreamReader(entity.getContent(), StandardCharsets.UTF_8);
				CharArrayBuffer buffer = new CharArrayBuffer(2048);
				char[] tmp = new char[1024];
				int l;
				while ((l = reader1.read(tmp)) != -1) {
					buffer.append(tmp, 0, l);
				}
				result = buffer.toString();
			}
			return result;
		} catch (Exception ex) {
			RzLogger.error("转换失败", ex);
			return "";
		}
		
	}
}
