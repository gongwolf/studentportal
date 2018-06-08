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

import org.springframework.format.annotation.DateTimeFormat;

public class EvaluationBean implements Serializable{
 
	private static final long serialVersionUID = 1L;
	
	private Integer applicationID; 
	private Integer menteeID; 
	private Integer mentorID; 
	private Integer evalYear;
	private String evalTerm;
	private String evalPoint;
	
	@NotNull(message="{NotNull.evaluationBean.evalDeadline}")
	@DateTimeFormat(pattern="MM/dd/yyyy")
	private Date evalDeadline;
	
	@DateTimeFormat(pattern="MM/dd/yyyy")
	private Date notifiedDate; 
	
	@DateTimeFormat(pattern="MM/dd/yyyy")
	private Date submitDate; 
 
	private String mentorName;
	private String menteeName;
	private String mentorEmail;
	private String menteeEmail;
	private String projectTitle;
 
	private String authToken; 
	private String mentorDept; 
	
	public Integer getApplicationID() {
		return applicationID;
	}
	public void setApplicationID(Integer applicationID) {
		this.applicationID = applicationID;
	}
	public Integer getEvalYear() {
		return evalYear;
	}
	public void setEvalYear(Integer evalYear) {
		this.evalYear = evalYear;
	}
	public String getEvalTerm() {
		return evalTerm;
	}
	public void setEvalTerm(String evalTerm) {
		this.evalTerm = evalTerm;
	}
	public Date getEvalDeadline() {
		return evalDeadline;
	}
	public void setEvalDeadline(Date evalDeadline) {
		this.evalDeadline = evalDeadline;
	}
	public String getEvalPoint() {
		return evalPoint;
	}
	public void setEvalPoint(String evalPoint) {
		this.evalPoint = evalPoint;
	}
	public String getMentorName() {
		return mentorName;
	}
	public void setMentorName(String mentorName) {
		this.mentorName = mentorName;
	}
	public String getMenteeName() {
		return menteeName;
	}
	public void setMenteeName(String menteeName) {
		this.menteeName = menteeName;
	}
	public String getMentorEmail() {
		return mentorEmail;
	}
	public void setMentorEmail(String mentorEmail) {
		this.mentorEmail = mentorEmail;
	}
	public String getMenteeEmail() {
		return menteeEmail;
	}
	public void setMenteeEmail(String menteeEmail) {
		this.menteeEmail = menteeEmail;
	}
	public String getProjectTitle() {
		return projectTitle;
	}
	public void setProjectTitle(String projectTitle) {
		this.projectTitle = projectTitle;
	}
	public Integer getMenteeID() {
		return menteeID;
	}
	public void setMenteeID(Integer menteeID) {
		this.menteeID = menteeID;
	}
	public Integer getMentorID() {
		return mentorID;
	}
	public void setMentorID(Integer mentorID) {
		this.mentorID = mentorID;
	}
	public String getMentorDept() {
		return mentorDept;
	}
	public void setMentorDept(String mentorDept) {
		this.mentorDept = mentorDept;
	}
	public Date getSubmitDate() {
		return submitDate;
	}
	public void setSubmitDate(Date submitDate) {
		this.submitDate = submitDate;
	}
	public String getAuthToken() {
		return authToken;
	}
	public void setAuthToken(String authToken) {
		this.authToken = authToken;
	}
	public Date getNotifiedDate() {
		return notifiedDate;
	}
	public void setNotifiedDate(Date notifiedDate) {
		this.notifiedDate = notifiedDate;
	}
	 
	
	
	

	
}
