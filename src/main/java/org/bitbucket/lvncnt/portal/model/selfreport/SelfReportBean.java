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
package org.bitbucket.lvncnt.portal.model.selfreport;

import java.io.Serializable;
import java.util.Date;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;
import org.springframework.format.annotation.DateTimeFormat;

public class SelfReportBean implements Serializable {
 
	private static final long serialVersionUID = 1L;
	private Integer windowID; 
	private Integer userID; 
	private String email; 
	private Date submitDate;
	
	@NotBlank(message="{NotBlank.selfReportBean.semester}")
	private String semester; 
	
	@NotNull(message="{NotNull.selfReportBean.startDate}")
	@DateTimeFormat(pattern="MM/dd/yyyy")
	private Date startDate; 
	
	@NotNull(message="{NotNull.selfReportBean.endDate}")
	@DateTimeFormat(pattern="MM/dd/yyyy")
	private Date endDate;
	
	public String getSemester() {
		return semester;
	}

	public Integer getWindowID() {
		return windowID;
	}

	public void setWindowID(Integer windowID) {
		this.windowID = windowID;
	}

	public Integer getUserID() {
		return userID;
	}

	public void setUserID(int userID) {
		this.userID = userID;
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
 
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public SelfReportBean() {
		 
	}
	 
	
	 
	/**********************/ 
	private ReportAcademicBean reportAcademicBean; 
	private String reportInternJson; 
	private String reportTravelJson;
	private String reportConferenceJson; 
	private String reportPublicationJson; 
	private String reportAwardsJson; 
	private ReportOthersBean reportOthersBean; 
   
	public ReportAcademicBean getReportAcademicBean() {
		return reportAcademicBean;
	}

	public void setReportAcademicBean(ReportAcademicBean reportAcademicBean) {
		this.reportAcademicBean = reportAcademicBean;
	}
 
	public String getReportInternJson() {
		return reportInternJson;
	}

	public void setReportInternJson(String reportInternJson) {
		this.reportInternJson = reportInternJson;
	}

	public String getReportTravelJson() {
		return reportTravelJson;
	}

	public void setReportTravelJson(String reportTravelJson) {
		this.reportTravelJson = reportTravelJson;
	}

	public String getReportConferenceJson() {
		return reportConferenceJson;
	}

	public void setReportConferenceJson(String reportConferenceJson) {
		this.reportConferenceJson = reportConferenceJson;
	}
	
	public String getReportPublicationJson() {
		return reportPublicationJson;
	}

	public void setReportPublicationJson(String reportPublicationJson) {
		this.reportPublicationJson = reportPublicationJson;
	}
	
	public String getReportAwardsJson() {
		return reportAwardsJson;
	}

	public void setReportAwardsJson(String reportAwardsJson) {
		this.reportAwardsJson = reportAwardsJson;
	}
	
	public ReportOthersBean getReportOthersBean() {
		return reportOthersBean;
	}

	public void setReportOthersBean(ReportOthersBean reportOthersBean) {
		this.reportOthersBean = reportOthersBean;
	}
	

	public Date getSubmitDate() {
		if(submitDate == null) return null; 
		return (Date)submitDate.clone();
	}

	public void setSubmitDate(Date submitDate) {
		if(submitDate == null) return; 
		this.submitDate = (Date)submitDate.clone();
	} 
	

}
