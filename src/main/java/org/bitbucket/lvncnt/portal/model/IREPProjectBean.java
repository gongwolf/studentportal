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

import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

public class IREPProjectBean {
	private Integer userID; 
	private Integer applicationID; 
	private String schoolYear; 
	private String schoolSemester; 
 
	public String getSchoolYear() {
		return schoolYear;
	}
	public void setSchoolYear(String schoolYear) {
		this.schoolYear = schoolYear;
	}
	public String getSchoolSemester() {
		return schoolSemester;
	}
	public void setSchoolSemester(String schoolSemester) {
		this.schoolSemester = schoolSemester;
	}
	@DateTimeFormat(pattern="MM/dd/yyyy")
	private Date researchDate; 
	
	@DateTimeFormat(pattern="MM/dd/yyyy")
	private Date leaveDate; 
	
	@DateTimeFormat(pattern="MM/dd/yyyy")
	private Date returnDate; 
	
	private Integer everFundAmp;
	private String listProgram;
	private String projectAbstract;
	public Date getResearchDate() {
		if(this.researchDate == null) return null; 
		return (Date)this.researchDate.clone();
	}
	public void setResearchDate(Date researchDate) {
		if(researchDate == null) return; 
		this.researchDate = (Date)researchDate.clone();
	}
	public Date getLeaveDate() {
		if(this.leaveDate == null) return null; 
		return (Date)this.leaveDate.clone();
	}
	public void setLeaveDate(Date leaveDate) {
		if(leaveDate == null) return; 
		this.leaveDate = (Date)leaveDate.clone();
	}
	public Date getReturnDate() {
		if(this.returnDate == null) return null; 
		return (Date)this.returnDate.clone();
	}
	public void setReturnDate(Date returnDate) {
		if(returnDate == null) return; 
		this.returnDate = (Date)returnDate.clone();
	}
	public Integer getEverFundAmp() {
		return everFundAmp;
	}
	public void setEverFundAmp(Integer everFundAmp) {
		this.everFundAmp = everFundAmp;
	}
	public String getListProgram() {
		return listProgram;
	}
	public void setListProgram(String listProgram) {
		this.listProgram = listProgram;
	}
	public String getProjectAbstract() {
		return projectAbstract;
	}
	public void setProjectAbstract(String projectAbstract) {
		this.projectAbstract = projectAbstract;
	}
	public Integer getUserID() {
		return userID;
	}
	public void setUserID(Integer userID) {
		this.userID = userID;
	}
	public Integer getApplicationID() {
		return applicationID;
	}
	public void setApplicationID(Integer applicationID) {
		this.applicationID = applicationID;
	}
 

}
