package com.rz.frame.biz;

import com.alibaba.fastjson.JSONObject;
import com.rz.frame.RzLogger;
import com.rz.frame.utils.*;
import com.rz.frame.dto.Constants;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DataUtils {
 
	@Autowired
	HttpTicketUtilManager httpTicketUtilManager;
	final HashMap<String, String> stationMap = new HashMap<>();
 
	@PostConstruct
	private void saveStationCode() {
		try {
			String stationFile = ClassLoader.getSystemResource("").getPath() + "/station" + LocalDate.now().toString();
			File file = new File(stationFile);
			if (!file.exists()) {
				RzLogger.info("saveStationCode", "三字码文件不存在，正在下载...");
				String stationListStr = getStationStr();
				initStationMap(stationListStr);
				RzLogger.info("saveStationCode", "存入本地");
				FileWriter fw = new FileWriter(stationFile);
				fw.write(JsonUtils.serializeObject(stationMap));
				fw.close();
				return;
			}
			StringBuilder stationCodeBuilder = new StringBuilder();
			FileReader fr = new FileReader(stationFile);
			BufferedReader bufferedReader = new BufferedReader(fr);
			String line = bufferedReader.readLine();
			while (line != null) {
				stationCodeBuilder.append(line);
				line = bufferedReader.readLine();
			}
			
			bufferedReader.close();
			fr.close();
			JSONObject stationObj = JsonUtils.toBean(stationCodeBuilder.toString());
			for (Map.Entry<String, Object> station : stationObj.entrySet()) {
				stationMap.put(station.getKey(), station.getValue().toString());
			}
			
			
		} catch (Exception ex) {
			RzLogger.error("getStationStr", ex);
		}
	}
	
	private void initStationMap(String stationListStr) {
		List<String> stationList = Arrays.asList(stationListStr.split("@"));
		for (String station : stationList) {
			if (StringUtils.isEmpty(station))
				continue;
			String[] stationNameArr = station.split("\\|");
			stationMap.put(stationNameArr[1], stationNameArr[2].toUpperCase());
			stationMap.put(stationNameArr[2].toUpperCase(), stationNameArr[1]);
		}
	}
	
	private String getStationStr() {
		try {
			RzLogger.info("getStationStr", "开始获得在线三字码数据");
			String stationNames = httpTicketUtilManager.getHttpClient(Constants.LoginInfo.UserName).doGet(Constants.QueryUrl.stationNameUrl);
			RzLogger.info("getStationStr", "获得在线三字码数据成功");
			stationNames = stationNames.replace("var station_names =", "");
			return stationNames.replace("'", "");
		} catch (Exception ex) {
			RzLogger.error("getStationStr", ex);
			return "";
		}
	}
	
	public String getStationCode(String name) {
		if (!stationMap.containsKey(name))
			return "";
		return stationMap.get(name);
	}
 
}
