package com.rz.frame.biz;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.rz.frame.*;
import com.rz.frame.dto.BaseResultDto;
import com.rz.frame.dto.UserLoginDto;

import com.rz.frame.utils.*;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.rz.frame.dto.Constants.LoginUrl.*;
import static com.rz.frame.dto.Constants.LoginUrl.uamauthclient;

@Service
public class LoginUtils {
	@Autowired
	HttpTicketUtilManager httpTicketUtilManager;
	
	public UserLoginDto loginInfo(String userName, String password) {
		try {
			HttpTicketUtils httpTicketUtils = httpTicketUtilManager.getHttpClient(userName);
			List<NameValuePair> uamtkStatic = new ArrayList<>();
			uamtkStatic.add(new BasicNameValuePair("appid", "otn"));
			
			String result = httpTicketUtils.doPost(uamtk_static, uamtkStatic);
			BaseResultDto baseResultDto = JsonUtils.toBean(result, BaseResultDto.class);
			if (baseResultDto.getResult_code() != 1) {
				RzLogger.info("loginInfo", "用户已经登录", result);
				return null;
			}
			RzLogger.info("loginInfo", "1. 检测登录结果：", result);
			//1. https://kyfw.12306.cn/otn/login/conf
			String confHttpResult = httpTicketUtils.doPost(conf, new HashMap<String, String>());
			BaseResultDto confHttpResultDto = JsonUtils.toBean(confHttpResult, BaseResultDto.class);
			RzLogger.info("loginInfo", "2. conf:", confHttpResult);
			
			String position = getCacheImage(httpTicketUtils);
			RzLogger.info("loginInfo", "打码结果：{}", position);
			List<NameValuePair> loginParas = new ArrayList<>();
			loginParas.add(new BasicNameValuePair("username", userName));
			loginParas.add(new BasicNameValuePair("password", password));
			loginParas.add(new BasicNameValuePair("appid", "otn"));
			loginParas.add(new BasicNameValuePair("answer", position));
			
			String loginResult = httpTicketUtils.doPost(webLogin, loginParas);
			BaseResultDto loginDto = JsonUtils.toBean(loginResult, BaseResultDto.class);
			if (loginDto.getResult_code() != 0) {
				RzLogger.error("loginInfo", "登录失败:{}", loginDto.getResult_message());
				return null;
			}
			RzLogger.info("loginInfo", "登陆结果:{}", loginDto.getResult_message());
			
			String userLoginResult = httpTicketUtils.doGet(userLogin);
			
			List<NameValuePair> uamtkformparams = new ArrayList<>();
			uamtkformparams.add(new BasicNameValuePair("appid", "otn"));
			
			String uamtkResult = httpTicketUtils.doPost(uamtk, uamtkformparams);
			BaseResultDto uamtkResultDto = JsonUtils.toBean(uamtkResult, BaseResultDto.class);
			
			if (uamtkResultDto.getResult_code() != 0) {
				RzLogger.error("loginInfo", "uamtk登录失败", loginDto.getResult_message());
				return null;
			}
			RzLogger.info("loginInfo", "uamtk结果：{}", uamtkResultDto.getResult_message());
			
			JSONObject uamtk = JsonUtils.toBean(uamtkResult);
			
			List<NameValuePair> uamauthclientformparams = new ArrayList<>();
			uamauthclientformparams.add(new BasicNameValuePair("tk", uamtk.getString("newapptk")));
			
			String uamauthResult = httpTicketUtils.doPost(uamauthclient, uamauthclientformparams);
			UserLoginDto userLoginDto = JsonUtils.toBean(uamauthResult, UserLoginDto.class);
			RzLogger.info("loginInfo", "uamauth获得登录名：{}", userLoginDto.getUserName());
			if (userLoginDto.getResult_code() == 0) {
				userLoginDto.setStatus(true);
			}
			return userLoginDto;
		} catch (Exception ex) {
			RzLogger.error("loginInfo", "登录失败");
		}
		return null;
	}
	
	String getPosition(JSONObject imgObj) {
		try {
			String markURL = "https://www.markcaptcha.com/Mark12306Captcha/mark/captcha";
			HttpPost captchaHttp = new HttpPost(markURL);
			Map<String, String> captchaDataMap = new HashMap<>();
			captchaDataMap.put("CaptchaBase64Str", imgObj.getString("image"));
			InputStreamEntity printCodeParaEntity = new InputStreamEntity(new ByteArrayInputStream(JsonUtils.serializeObject(captchaDataMap).getBytes()));
			captchaHttp.setEntity(printCodeParaEntity);
			CloseableHttpResponse printResult = HttpClients.createDefault().execute(captchaHttp);
			
			JSONObject json = JsonUtils.toBean(HttpTicketUtils.entityToString(printResult.getEntity()));
			JSONArray pArray = json.getJSONArray("result");
			StringBuilder position = new StringBuilder();
			for (int i = 0; i < pArray.size(); i++) {
				position.append(pArray.get(i).toString().replace("[", "").replace("]", ""));
				if (i != pArray.size() - 1) {
					position.append(",");
				}
			}
			return position.toString();
		} catch (IOException e) {
			RzLogger.error("getPosition", e);
		}
		return "";
	}
	
	String getCacheImage(HttpTicketUtils httpTicketUtils) {
		RzLogger.info("loginInfo", "开始获得验证码");
		//4. 打码captcha-image64
		for (int i = 0; i < 10; i++) {
			String imgStr = httpTicketUtils.doGet(String.format(captchaImg, Math.random()));
			String imgFormat = RegexUtils.matchOne("\\{.+\\}", imgStr);
			JSONObject imgObj = JsonUtils.toBean(imgFormat);
			RzLogger.info("loginInfo", "验证码获取成功，开始打码");
			//5、获得打码结果
			String position = getPosition(imgObj);
			String checkResult = httpTicketUtils.doGet(String.format(captchaCheck, position, Math.random()));
			String checkFormat = RegexUtils.matchOne("\\{.+\\}", checkResult);
			JSONObject checkObj = (JSONObject) JsonUtils.toBean(checkFormat);
			if (!checkObj.getString("result_code").equals("4")) {
				RzLogger.info("loginInfo", "验证码校验失败，重新尝试{}次", i);
				continue;
			}
			RzLogger.info("loginInfo", "验证码校验成功，开始进行登录");
			return position;
		}
		return "";
	}
}
