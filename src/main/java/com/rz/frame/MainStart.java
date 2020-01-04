package com.rz.frame;

import com.rz.frame.biz.DataUtils;
import com.rz.frame.biz.MainBizImpl;
import com.rz.frame.biz.ScanTicketImp;
import com.rz.frame.dto.QueryTicketDto;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Set;


public class MainStart {
	
	public static void main(String[] args)  {
 
		AnnotationConfigApplicationContext configApplicationContext=new AnnotationConfigApplicationContext(GrabticketConfig.class);
		MainBizImpl mainBiz = configApplicationContext.getBean(MainBizImpl.class);
		mainBiz.doScan();
 
	}
}
