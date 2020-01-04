package com.rz.frame.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HttpResultDto {
	private Integer httpCode;
	private String httpContent;
}
