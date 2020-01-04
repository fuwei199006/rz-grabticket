package com.rz.frame.dto;


import com.rz.frame.utils.StringUtils;
import lombok.Getter;
import lombok.Setter;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zhangbin
 * @date 2019-05-31 10:46
 * @description: TODO
 */
@Getter
@Setter
public class TicketModel {
	
	private String trainDate;
	private String secret;
	private String trainCode;
	private String trainNumber;
	private String from;
	private String to;
	private String trainLocation;
	private String departTime;
	private String arriveTime;
	private String interval;
	private String leftTicket;
	
	private List<SeatModel> seat = new ArrayList<>();
	
	public void setInfo(String[] info) {
		this.secret = URLDecoder.decode(info[0]);
		this.trainCode = info[2];
		this.trainNumber = info[3];
		this.from = info[6];
		this.to = info[7];
		this.trainLocation = info[15];
		this.departTime = info[8];
		this.arriveTime = info[9];
		this.interval = info[10];
		this.leftTicket = info[12];
		
		for (SeatLevelEnum seatLevelEnum : SeatLevelEnum.values()) {
			SeatModel seatModel = new SeatModel(seatLevelEnum, info);
			if (StringUtils.isEmpty(seatModel.getCount()) || seatModel.getCount().equals("0") || seatModel.getCount().equals("无")) {
				continue;
			}
			seat.add(seatModel);
		}
	}
	
	//    @Override
	//    public String toString(){
	//        return "✅车次[" + trainNumber + "][" + Station.getNameByCode(from) + "-" + Station.getNameByCode(to) + "][" + departDate + "-" + arriveDate + "]:" + seat.toString();
	//    }
}
