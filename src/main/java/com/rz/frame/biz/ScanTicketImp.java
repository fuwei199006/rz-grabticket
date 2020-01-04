package com.rz.frame.biz;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.rz.frame.RzLogger;
import com.rz.frame.dto.*;

import com.rz.frame.utils.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;

import java.util.List;

import java.util.stream.Collectors;

import static com.rz.frame.dto.Constants.QueryUrl.queryUrl;

@Service
public class ScanTicketImp {
	@Autowired
	HttpTicketUtilManager httpTicketUtilManager;
	
	public List<TicketModel>  queryTicket(QueryTicketDto queryTicketDto) {
		String defalutUrl = queryUrl();
		if (StringUtils.isEmpty(defalutUrl)) {
			RzLogger.info("queryTicket", "查询12306余票Url失败");
			return null;
		}
		HttpTicketUtils httpTicketUtils = httpTicketUtilManager.getHttpClient(Constants.LoginInfo.UserName);
		JSONObject queryObj = null;
		for (int i = 0; i < 1000; i++) {
			HttpResultDto queryResult = httpTicketUtils.doGetResult(String.format(queryUrl, defalutUrl, queryTicketDto.getDepartDate(), queryTicketDto.getDepartStationCode(), queryTicketDto.getArriveStaionCode(), queryTicketDto.getDepartStation(), queryTicketDto.getArriveStaion()));
			if (queryResult == null || queryResult.getHttpCode() != 200) {
				RzLogger.info("queryTicket", "查询12306余票失败");
				continue;
			}
			queryObj = JsonUtils.toBean(queryResult.getHttpContent());
			if (queryObj.getInteger("httpstatus") != 200) {
				RzLogger.info("queryTicket", "查询12306余票失败,返回code:{}", queryObj.getInteger("httpstatus"));
				continue;
			}
			JSONArray jsonArray = queryObj.getJSONObject("data").getJSONArray("result");
			if (jsonArray.size() == 0) {
				RzLogger.info("queryTicket", "查询12306余票失败,继续查询：{}次", i);
				continue;
			}
			
			JSONObject data = queryObj.getJSONObject("data");
			JSONArray result = data.getJSONArray("result");
			List<TicketModel> ticketModels = new ArrayList<>();
			for (int j = 0; j < result.size(); j++) {
				TicketModel ticketModel = new TicketModel();
				ticketModel.setInfo(result.get(j).toString().split("\\|"));
				ticketModel.setTrainDate(queryTicketDto.getDepartDate());
				ticketModels.add(ticketModel);
			}
			
			ticketModels = ticketModels.stream().filter(x -> StringUtils.isContain(queryTicketDto.getTrainNos(), x.getTrainNumber())).collect(Collectors.toList());
			if (CollectionUtils.isEmpty(ticketModels)) {
				RzLogger.info("queryTicket", "未查询到符合车次的车票，请检查行程");
				return null;
			}
			
			for (TicketModel ticket : ticketModels) {
				List<SeatModel> seatModels = ticket.getSeat().stream().filter(x -> queryTicketDto.getSeatNames().contains(x.getSeatLevel().getName()) && !x.getCount().equals("0") && !x.getCount().equals("无") && !x.getCount().equals("")).collect(Collectors.toList());
				
				if (CollectionUtils.isEmpty(seatModels)) {
					RzLogger.info("queryTicket", "未查询到余票，重试{}次", i);
					SleepUtils.Sleep(10);
					continue;
				}
				
				ticket.setSeat(seatModels);
				return ticketModels;
			}
			
		}
		return null;
	}
	
	private String queryUrl() {
		RzLogger.info("查询扫余票的url");
		String defalutUrl = "queryA";
		HttpTicketUtils httpTicketUtils = httpTicketUtilManager.getHttpClient(Constants.LoginInfo.UserName);
		String queryResult = httpTicketUtils.doGet(String.format(queryUrl, defalutUrl, LocalDate.now().plusDays(1).toString(), "SHH", "IMH"));
		if (StringUtils.isEmpty(queryResult)) {
			RzLogger.info("获得查余票的Url出错");
			return null;
		}
		JSONObject queryJson = JsonUtils.toBean(queryResult);
		Boolean status = queryJson.getBoolean("status");
		if (!status) {
			String c_url = queryJson.getString("c_url");
			String[] urls = c_url.split("/");
			defalutUrl = urls[1];
		}
		RzLogger.info("获得查余票的Url:", defalutUrl);
		return defalutUrl;
		
	}
}
