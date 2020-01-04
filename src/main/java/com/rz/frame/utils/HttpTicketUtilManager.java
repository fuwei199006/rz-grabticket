package com.rz.frame.utils;


import com.rz.frame.RzLogger;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class HttpTicketUtilManager {
	private ConcurrentHashMap<String, HttpTicketUtils> concurrentHashMap;
	public HttpTicketUtilManager(){
		basicCookieStore=new BasicCookieStore();
		updateBasicCookieStore();
		concurrentHashMap=new ConcurrentHashMap<>();
	}
	private BasicCookieStore basicCookieStore;
	
	public HttpTicketUtils getHttpClient(String userName) {
		if (StringUtils.isEmpty(userName)) {
			return new HttpTicketUtils();
		}
		if (concurrentHashMap.containsKey(userName)) {
			return concurrentHashMap.get(userName);
		}
		CloseableHttpClient httpClient = HttpClients.custom().setDefaultCookieStore(basicCookieStore).build();
		HttpTicketUtils httpTicketUtils = new HttpTicketUtils();
		httpTicketUtils.setHttpClient(httpClient);
		concurrentHashMap.put(userName, httpTicketUtils);
		return httpTicketUtils;
	}
	private void updateBasicCookieStore() {
		RzLogger.info("updateBasicCookieStore","更新Cookie");
		System.setProperty("webdriver.chrome.driver", ClassLoader.getSystemResource("").getPath() + "\\chromedriver.exe");
		//谷歌浏览器
		WebDriver driver = new ChromeDriver();
		driver.get("https://www.12306.cn/index/index.html");
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		String RAIL_DEVICEID = driver.manage().getCookieNamed("RAIL_DEVICEID").getValue();
		String RAIL_EXPIRATION = driver.manage().getCookieNamed("RAIL_EXPIRATION").getValue();
		RzLogger.info("updateBasicCookieStore","获得RAIL_DEVICEID：{},RAIL_EXPIRATION:{}",RAIL_DEVICEID,RAIL_EXPIRATION);
		//关闭浏览器
		driver.close();
		driver.quit();
		
		BasicClientCookie expCookie = new BasicClientCookie("RAIL_EXPIRATION", RAIL_EXPIRATION);
		expCookie.setDomain("kyfw.12306.cn");
		expCookie.setPath("/");
		basicCookieStore.addCookie(expCookie);
		
		BasicClientCookie dfCookie = new BasicClientCookie("RAIL_DEVICEID", RAIL_DEVICEID);
		dfCookie.setDomain("kyfw.12306.cn");
		dfCookie.setPath("/");
		basicCookieStore.addCookie(dfCookie);
		
	}
}
