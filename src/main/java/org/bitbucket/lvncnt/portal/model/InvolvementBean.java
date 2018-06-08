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

public class InvolvementBean {
	private Integer userID; 
	private Integer applicationID; 
	private String programNameAbbr;
 
	private Integer programEverIn;
	private Integer programEverInYear;
	private String programEverInSemesters; 

	private Integer ampScholarship; 
	private String ampScholarshipSchool; 
	private String ampScholarshipType; 
	private String ampScholarshipSemester;   
	private String ampScholarshipYear;  
	private Float ampScholarshipAmount; 
	
	private Integer otherScholarship; 
	private String listOtherScholarship;
	
	// CCCONF program 
	private Integer isCurrentEmploy; 
	private String listEmployCampus; 
	private String listEmployDept; 
	private String listEmploySupervisor; 
	
	@DateTimeFormat(pattern="MM/dd/yyyy")
	private Date listEmployStart;
	
	@DateTimeFormat(pattern="MM/dd/yyyy")
	private Date listEmployEnd;
	
	private Integer everInResearch;
	private String describeResearch;
	
	private Integer everAttendConference;

	public InvolvementBean() {
		super();
	}

	public InvolvementBean(InvolvementBean from) {
		if(from != null){
			this.userID = from.userID; 
			this.applicationID = from.applicationID; 
			this.programNameAbbr = from.programNameAbbr; 
			
			this.programEverIn = from.programEverIn;
			this.programEverInYear = from.programEverInYear;
			this.programEverInSemesters = from.programEverInSemesters;
			this.ampScholarship = from.ampScholarship;
			this.ampScholarshipSchool = from.ampScholarshipSchool;
			this.ampScholarshipType = from.ampScholarshipType;
			this.ampScholarshipSemester = from.ampScholarshipSemester;
			this.ampScholarshipYear = from.ampScholarshipYear;
			this.ampScholarshipAmount = from.ampScholarshipAmount;
			this.otherScholarship = from.otherScholarship;
			this.listOtherScholarship = from.listOtherScholarship;
			this.isCurrentEmploy = from.isCurrentEmploy;
			this.listEmployCampus = from.listEmployCampus;
			this.listEmployDept = from.listEmployDept;
			this.listEmploySupervisor = from.listEmploySupervisor;
			this.setListEmployStart(from.getListEmployStart());
			this.setListEmployEnd(from.getListEmployEnd());
			this.everInResearch = from.everInResearch;
			this.describeResearch = from.describeResearch;
			this.everAttendConference = from.everAttendConference;
		}
	}
	 
	public String getProgramNameAbbr() {
		return programNameAbbr;
	}

	public void setProgramNameAbbr(String programNameAbbr) {
		this.programNameAbbr = programNameAbbr;
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
	 
	public Integer getProgramEverIn() {
		return programEverIn;
	}
	public void setProgramEverIn(Integer programEverIn) {
		this.programEverIn = programEverIn;
	}
	public Integer getProgramEverInYear() {
		return programEverInYear;
	}
	public void setProgramEverInYear(Integer programEverInYear) {
		this.programEverInYear = programEverInYear;
	}
	public String getProgramEverInSemesters() {
		return programEverInSemesters;
	}
	public void setProgramEverInSemesters(String programEverInSemesters) {
		this.programEverInSemesters = programEverInSemesters;
	}
	public Integer getAmpScholarship() {
		return ampScholarship;
	}
	public void setAmpScholarship(Integer ampScholarship) {
		this.ampScholarship = ampScholarship;
	}
	public String getAmpScholarshipSchool() {
		return ampScholarshipSchool;
	}
	public void setAmpScholarshipSchool(String ampScholarshipSchool) {
		this.ampScholarshipSchool = ampScholarshipSchool;
	}
	public String getAmpScholarshipType() {
		return ampScholarshipType;
	}
	public void setAmpScholarshipType(String ampScholarshipType) {
		this.ampScholarshipType = ampScholarshipType;
	}
	public String getAmpScholarshipSemester() {
		return ampScholarshipSemester;
	}
	public void setAmpScholarshipSemester(String ampScholarshipSemester) {
		this.ampScholarshipSemester = ampScholarshipSemester;
	}
	public String getAmpScholarshipYear() {
		return ampScholarshipYear;
	}
	public void setAmpScholarshipYear(String ampScholarshipYear) {
		this.ampScholarshipYear = ampScholarshipYear;
	}
	public Float getAmpScholarshipAmount() {
		return ampScholarshipAmount;
	}
	public void setAmpScholarshipAmount(Float ampScholarshipAmount) {
		this.ampScholarshipAmount = ampScholarshipAmount;
	}
	public Integer getOtherScholarship() {
		return otherScholarship;
	}
	public void setOtherScholarship(Integer otherScholarship) {
		this.otherScholarship = otherScholarship;
	}
	public String getListOtherScholarship() {
		return listOtherScholarship;
	}
	public void setListOtherScholarship(String listOtherScholarship) {
		this.listOtherScholarship = listOtherScholarship;
	}
	
	public Integer getIsCurrentEmploy() {
		return isCurrentEmploy;
	}
	public void setIsCurrentEmploy(Integer isCurrentEmploy) {
		this.isCurrentEmploy = isCurrentEmploy;
	}
	public String getListEmployDept() {
		return listEmployDept;
	}
	public void setListEmployDept(String listEmployDept) {
		this.listEmployDept = listEmployDept;
	}
	public String getListEmploySupervisor() {
		return listEmploySupervisor;
	}
	public void setListEmploySupervisor(String listEmploySupervisor) {
		this.listEmploySupervisor = listEmploySupervisor;
	}
	public Date getListEmployStart() {
		if(listEmployStart == null) return null; 
		return (Date)listEmployStart.clone();
	}
	public void setListEmployStart(Date listEmployStart) {
		if(listEmployStart == null) return; 
		this.listEmployStart = (Date)listEmployStart.clone();
	}
	public Date getListEmployEnd() {
		if(listEmployEnd == null) return null; 
		return (Date)listEmployEnd.clone();
	}
	public void setListEmployEnd(Date listEmployEnd) {
		if(listEmployEnd == null) return; 
		this.listEmployEnd = (Date)listEmployEnd.clone();
	}
	public Integer getEverInResearch() {
		return everInResearch;
	}
	public void setEverInResearch(Integer everInResearch) {
		this.everInResearch = everInResearch;
	}
	public String getDescribeResearch() {
		return describeResearch;
	}
	public void setDescribeResearch(String describeResearch) {
		this.describeResearch = describeResearch;
	}
	public Integer getEverAttendConference() {
		return everAttendConference;
	}
	public void setEverAttendConference(Integer everAttendConference) {
		this.everAttendConference = everAttendConference;
	}
	public String getListEmployCampus() {
		return listEmployCampus;
	}
	public void setListEmployCampus(String listEmployCampus) {
		this.listEmployCampus = listEmployCampus;
	}
 
}
