package com.rz.frame.biz;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.rz.frame.RzLogger;
import com.rz.frame.utils.HttpTicketUtils;
import com.rz.frame.utils.JsonUtils;
import com.rz.frame.dto.Constants;
import com.rz.frame.dto.PassengerModel;
import com.rz.frame.dto.SeatModel;

import com.rz.frame.utils.HttpTicketUtilManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.rz.frame.dto.Constants.OrderUrl.passengerDTOsUrl;

@Service
public class PassagerUtil {
	@Autowired
	HttpTicketUtilManager httpTicketUtilManager;
	public String getPassengerTicketStr(PassengerModel passengerModel, SeatModel seatModel) {
		
		return seatModel.getSeatLevel().getCode() + "," + "0,1," + passengerModel.getPassengerName() + "," + passengerModel.getPassengerIdTypeCode() + "," + passengerModel.getPassengerIdNo() + "," + passengerModel.getMobileNo() + "," + "N," + passengerModel.getAllEncStr();
	}
	
	public String getPassengerTicketStr(List<PassengerModel> passengerModel, SeatModel seatModel) {
		
		StringBuilder passagerDto = new StringBuilder();
		for (PassengerModel passager : passengerModel) {
			passagerDto.append(getPassengerTicketStr(passager, seatModel));
			passagerDto.append("_");
		}
		return passagerDto.deleteCharAt(passagerDto.length() - 1).toString();
	}
	
	public String getOldPassengerStr(List<PassengerModel> passengerModel) {
		
		StringBuilder passagerDto = new StringBuilder();
		for (PassengerModel passager : passengerModel) {
			passagerDto.append(getOldPassengerStr(passager));
			passagerDto.append("_");
		}
		return passagerDto.toString();
	}
	
	
	public String getOldPassengerStr(PassengerModel passengerModel) {
		return passengerModel.getPassengerName() + "," + passengerModel.getPassengerIdTypeCode() + "," + passengerModel.getPassengerIdNo() + "," + passengerModel.getPassengerType() + "_";
	}
	
	public List<PassengerModel> getPassengerModelList(String token) {
		HttpTicketUtils httpTicketUtils = httpTicketUtilManager.getHttpClient(Constants.LoginInfo.UserName);
		HashMap<String, String> passagerParas = new HashMap<>();
		passagerParas.put("_json_att", "");
		passagerParas.put("REPEAT_SUBMIT_TOKEN", token);
		String passagerStr = httpTicketUtils.doPost(passengerDTOsUrl, passagerParas);
		JSONObject passagerObj = JsonUtils.toBean(passagerStr);
		JSONArray data = passagerObj.getJSONObject("data").getJSONArray("normal_passengers");
		List<PassengerModel> passengerModelList = new ArrayList<>();
		if(data==null){
			RzLogger.info("getPassengerModelList","获得乘客失败");
			return null;
		}
		for (Object entry : data) {
			JSONObject jsonObject = (JSONObject) entry;
			HashMap<String, String> passengerModelMap = new HashMap<>();
			for (Map.Entry<String, Object> mapObj : jsonObject.entrySet()) {
				passengerModelMap.put(mapObj.getKey(), mapObj.getValue().toString());
			}
			PassengerModel passengerModel = new PassengerModel(passengerModelMap);
			passengerModelList.add(passengerModel);
		}
		return passengerModelList;
		
	}
}
