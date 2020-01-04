package com.rz.frame.biz;

import com.rz.frame.RzLogger;
import com.rz.frame.dto.*;
import com.rz.frame.utils.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MainBizImpl {
	@Autowired
	DataUtils dataUtils;
	@Autowired
	ScanTicketImp scanTicketImp;
	@Autowired
	TicketUtils ticketUtils;
	@Autowired
	LoginUtils loginUtils;
	
	public void doScan() {
		QueryTicketDto queryTicketDto = new QueryTicketDto();
		//		queryTicketDto.setDepartStation("上海虹桥");
		//		queryTicketDto.setArriveStaion("松江南");
		queryTicketDto.setDepartStationCode(dataUtils.getStationCode("上海虹桥"));
		queryTicketDto.setArriveStaionCode(dataUtils.getStationCode("松江南"));
		queryTicketDto.setDepartDate("2020-01-23");
		queryTicketDto.setSeatNames("二等座");
		queryTicketDto.setTrainNos("D3201");
		List<TicketModel> ticketModels = scanTicketImp.queryTicket(queryTicketDto);
		List<PassengerModel> passagerUtilList = new ArrayList<>();
		PassengerModel passagerUtil1 = new PassengerModel();
		passagerUtil1.setPassengerIdNo("3412***********452");
		passagerUtil1.setPassengerName("xxx");
		passagerUtilList.add(passagerUtil1);
		PassengerModel passagerUtil2 = new PassengerModel();
		passagerUtil2.setPassengerIdNo("3412***********584");
		passagerUtil2.setPassengerName("xxx");
		passagerUtilList.add(passagerUtil2);
		
		if (CollectionUtils.isNotEmpty(ticketModels)) {
			RzLogger.info("doScan", "查询余票成功，开始尝试登录预订");
			UserLoginDto userLoginDto = loginUtils.loginInfo(Constants.LoginInfo.UserName, Constants.LoginInfo.Password);
			if (userLoginDto == null || !userLoginDto.isStatus()) {
				RzLogger.info("doScan", "登录失败");
				return;
			}
			for (TicketModel ticketModel : ticketModels) {
				for (SeatModel seatModel : ticketModel.getSeat()) {
					BuyTicketModel buyTicketModel = new BuyTicketModel(ticketModel, seatModel, passagerUtilList);
					String s = ticketUtils.buyTicket(buyTicketModel);
					
				}
				
			}
		}
		
	}
}
