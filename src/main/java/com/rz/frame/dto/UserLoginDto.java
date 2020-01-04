package com.rz.frame.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserLoginDto extends BaseResultDto{
	private boolean status;
	private String userName;
	
	
}
