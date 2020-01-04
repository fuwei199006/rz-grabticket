package com.rz.frame.biz;

import com.rz.frame.RzLogger;
import com.rz.frame.dto.*;

import com.rz.frame.utils.*;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.rz.frame.dto.Constants.OrderUrl.*;
import static com.rz.frame.dto.Constants.OrderUrl.checkOrderInfoUrl;

@Service
public class TicketUtils {
	@Autowired
	PassagerUtil passengerUtil;
	@Autowired
	HttpTicketUtilManager httpTicketUtilManager;
	
	public String buyTicket(BuyTicketModel buyTicketModel) {
		HttpTicketUtils httpTicketUtils = httpTicketUtilManager.getHttpClient(Constants.LoginInfo.UserName);
		HashMap<String, String> paras = new HashMap<>();
		paras.put("secretStr", buyTicketModel.getSecret());
		paras.put("train_date", buyTicketModel.getTicketDate());
		paras.put("back_train_date", "2019-12-20");
		paras.put("tour_flag", "dc");
		paras.put("purpose_codes", "ADULT");
		paras.put("query_from_station_name", buyTicketModel.getFrom());
		paras.put("query_to_station_name", buyTicketModel.getTo());
		
		String submitOrder = httpTicketUtils.doPost(submitUrl, paras);
		
		String initDcHtml = httpTicketUtils.doPost(initDcUrl, new HashMap<>());
		String token = "";
		String keyCheck = "";
		String regex = "globalRepeatSubmitToken \\= '(.*?)';";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(initDcHtml);
		while (m.find()) {
			token = m.group(1);
		}
		regex = "'key_check_isChange':'(.*?)',";
		Pattern p1 = Pattern.compile(regex);
		Matcher m1 = p1.matcher(initDcHtml);
		while (m1.find()) {
			keyCheck = m1.group(1);
		}
		List<String> nameList = buyTicketModel.getPassengerModelList().stream().map(x -> x.getPassengerName()).collect(Collectors.toList());
		List<String> passagerIdList = buyTicketModel.getPassengerModelList().stream().map(x -> x.getPassengerIdNo()).collect(Collectors.toList());
		List<PassengerModel> passengerModelList = passengerUtil.getPassengerModelList(token);
		if (CollectionUtils.isEmpty(passengerModelList)) {
			RzLogger.info("getPassengerModelList", "获得乘客失败");
			return null;
		}
		passengerModelList=passengerModelList.stream().filter(x -> nameList.contains(x.getPassengerName()) && passagerIdList.contains(x.getPassengerIdNo())).collect(Collectors.toList());
		if (CollectionUtils.isEmpty(passengerModelList)) {
			RzLogger.info("getPassengerModelList", "无此乘客，出票失败");
			return null;
		}
		buyTicketModel.setPassengerModelList(passengerModelList);
		SeatModel seatModel = buyTicketModel.getSeatModel();
		List<NameValuePair> formparams = new ArrayList<>();
		
		formparams.add(new BasicNameValuePair("bed_level_order_num", "000000000000000000000000000000"));
		formparams.add(new BasicNameValuePair("cancel_flag", "2"));
		formparams.add(new BasicNameValuePair("whatsSelect", "2"));
		formparams.add(new BasicNameValuePair("_json_att", ""));
		formparams.add(new BasicNameValuePair("tour_flag", "dc"));
		formparams.add(new BasicNameValuePair("randCode", ""));
		formparams.add(new BasicNameValuePair("passengerTicketStr", passengerUtil.getPassengerTicketStr(buyTicketModel.getPassengerModelList(), seatModel)));
		formparams.add(new BasicNameValuePair("REPEAT_SUBMIT_TOKEN", token));
		formparams.add(new BasicNameValuePair("getOldPassengerStr", passengerUtil.getOldPassengerStr(buyTicketModel.getPassengerModelList())));
		String checkOrder = httpTicketUtils.doPost(checkOrderInfoUrl, formparams);
		getQueueCount(buyTicketModel, token, seatModel);
		confirmSingleForQueue(buyTicketModel, keyCheck, buyTicketModel.getPassengerModelList(), seatModel, token);
		return "ok";
	}
	
	private void getQueueCount(BuyTicketModel buyTicketModel, String token, SeatModel seatModel) {
		List<NameValuePair> formparams = new ArrayList<>();
		
		formparams.add(new BasicNameValuePair("fromStationTelecode", buyTicketModel.getFrom()));
		formparams.add(new BasicNameValuePair("toStationTelecode", buyTicketModel.getTo()));
		formparams.add(new BasicNameValuePair("leftTicket", buyTicketModel.getLeftTicket()));
		formparams.add(new BasicNameValuePair("purpose_codes", "00"));
		formparams.add(new BasicNameValuePair("REPEAT_SUBMIT_TOKEN", token));
		formparams.add(new BasicNameValuePair("seatType", seatModel.getSeatLevel().getCode()));
		formparams.add(new BasicNameValuePair("stationTrainCode", buyTicketModel.getTrainNumber()));
		formparams.add(new BasicNameValuePair("train_date", DateUtils.getGMT(buyTicketModel.getTrainDate())));
		formparams.add(new BasicNameValuePair("train_location", buyTicketModel.getTrainLocation()));
		formparams.add(new BasicNameValuePair("train_no", buyTicketModel.getTrainCode()));
		formparams.add(new BasicNameValuePair("_json_att", ""));
		HttpTicketUtils httpClient = httpTicketUtilManager.getHttpClient("13661862134");
		String s = httpClient.doPost(getQueueCountUrl, formparams);
		RzLogger.info(s);
		
	}
	
	public void confirmSingleForQueue(BuyTicketModel buyTicketModel, String keyCheck, List<PassengerModel> passengerModel, SeatModel seatModel, String token) {
		
		
		List<NameValuePair> formparams = new ArrayList<>();
		formparams.add(new BasicNameValuePair("dwAll", "N"));
		formparams.add(new BasicNameValuePair("purpose_codes", "00"));
		formparams.add(new BasicNameValuePair("key_check_isChange", keyCheck));
		formparams.add(new BasicNameValuePair("_json_att", ""));
		formparams.add(new BasicNameValuePair("leftTicketStr", buyTicketModel.getLeftTicket()));
		formparams.add(new BasicNameValuePair("train_location", buyTicketModel.getTrainLocation()));
		formparams.add(new BasicNameValuePair("choose_seats", ""));
		formparams.add(new BasicNameValuePair("whatsSelect", "1"));
		formparams.add(new BasicNameValuePair("roomType", "00"));
		formparams.add(new BasicNameValuePair("seatDetailType", "000"));
		formparams.add(new BasicNameValuePair("randCode", ""));
		formparams.add(new BasicNameValuePair("passengerTicketStr", passengerUtil.getPassengerTicketStr(passengerModel, seatModel)));
		formparams.add(new BasicNameValuePair("REPEAT_SUBMIT_TOKEN", token));
		formparams.add(new BasicNameValuePair("getOldPassengerStr", passengerUtil.getOldPassengerStr(passengerModel)));
		HttpTicketUtils httpClient = httpTicketUtilManager.getHttpClient("13661862134");
		String s = httpClient.doPost(confirmSingleForQueueUrl, formparams);
		RzLogger.info(s);
	}
	
	
}
