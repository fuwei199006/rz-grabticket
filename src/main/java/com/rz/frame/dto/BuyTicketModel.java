package com.rz.frame.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BuyTicketModel {
	private String secret;
	private String ticketDate;
	private String from;
	private String to;
	private SeatModel seatModel;
	private List<PassengerModel> passengerModelList;
	private String leftTicket;
	private String trainNumber;
	private String trainDate;
	private String trainLocation;
	private String trainCode;
	
	public BuyTicketModel(TicketModel ticketModel, SeatModel seatModel, List<PassengerModel> passengerModels) {
		this.secret = ticketModel.getSecret();
		this.ticketDate = ticketModel.getSecret();
		this.from = ticketModel.getSecret();
		this.to=ticketModel.getTo();
		this.seatModel = seatModel;
		this.passengerModelList = passengerModels;
		this.leftTicket = ticketModel.getLeftTicket();
		this.trainNumber = ticketModel.getTrainNumber();
		this.trainDate = ticketModel.getTrainDate();
		this.trainCode = ticketModel.getTrainCode();
		this.trainLocation = ticketModel.getTrainLocation();
	}
}
