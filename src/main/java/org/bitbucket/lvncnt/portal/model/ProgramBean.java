/*
 * Copyright (c) 2018 Feng Liu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.bitbucket.lvncnt.portal.model;

import java.io.Serializable;
import java.util.Date;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;
import org.springframework.format.annotation.DateTimeFormat;

public class ProgramBean implements Serializable {
 
	private static final long serialVersionUID = 1L;

	private Integer windowID; 
	private String programNameAbbr; 
	private String programName;
	
	@NotBlank(message="{NotBlank.programBean.year}")
	private String year; 
	
	@NotBlank(message="{NotBlank.programBean.semester}")
	private String semester; 
 
	@NotNull(message="{NotNull.programBean.startDate}")
	@DateTimeFormat(pattern="MM/dd/yyyy")
	private Date startDate; 
	
	@NotNull(message="{NotNull.programBean.endDate}")
	@DateTimeFormat(pattern="MM/dd/yyyy")
	private Date endDate;
	
	public ProgramBean(){
		
	}
	public ProgramBean(ProgramBean from) {
		this.windowID = from.windowID;
		this.programNameAbbr = from.programNameAbbr;
		this.programName = from.programName;
		this.year = from.year;
		this.semester = from.semester;
		this.startDate = from.startDate;
		this.endDate = from.endDate;
	}
	 
	public Integer getWindowID() {
		return windowID;
	}
	public void setWindowID(Integer windowID) {
		this.windowID = windowID;
	}
	public String getProgramNameAbbr() {
		return programNameAbbr;
	}
	public void setProgramNameAbbr(String programNameAbbr) {
		this.programNameAbbr = programNameAbbr;
	}
	public String getProgramName() {
		return programName;
	}
	public void setProgramName(String programName) {
		this.programName = programName;
	}
	public String getYear() {
		return year;
	}
	public void setYear(String year) {
		this.year = year;
	}
	public String getSemester() {
		return semester;
	}
	public void setSemester(String semester) {
		this.semester = semester;
	}
	public Date getStartDate() {
		if(startDate == null) return null; 
		return (Date)startDate.clone();
	}
	public void setStartDate(Date startDate) {
		if(startDate == null) return; 
		this.startDate = (Date)startDate.clone();
	}
	public Date getEndDate() {
		if(endDate == null) return null; 
		return (Date)endDate.clone();
	}
	public void setEndDate(Date endDate) {
		if(endDate == null) return; 
		this.endDate = (Date)endDate.clone();
	}
 
	
}
