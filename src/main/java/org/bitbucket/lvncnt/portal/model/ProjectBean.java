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

import org.springframework.format.annotation.DateTimeFormat;

public class ProjectBean implements Serializable {
	 
	private static final long serialVersionUID = 1L;
	private Integer menteeID, applicationID, mentorID; 
	private String projectTitle; 
	private Integer externalProject; 
	private String externalAgency, externalTitle, externalDuration; 
	private String projectGoal, projectMethod, projectResult, projectTask;
	private String mentorSignature; 
	
	@DateTimeFormat(pattern="MM/dd/yyyy")
	private Date mentorSignatureDate; 
	
	private String menteeSignature;
	
	@DateTimeFormat(pattern="MM/dd/yyyy")
	private Date menteeSignatureDate;
	
	public ProjectBean() {
		super();
	}
	
	public ProjectBean(ProjectBean projectBean) {
		super();
		
		this.menteeID = projectBean.getMenteeID();
		this.applicationID = projectBean.getApplicationID();
		this.mentorID = projectBean.getMentorID();
		this.projectTitle = projectBean.getProjectTitle();
		this.externalProject = projectBean.getExternalProject();
		if(this.externalProject != null && this.externalProject == 1){
			this.externalAgency = projectBean.getExternalAgency();
			this.externalTitle = projectBean.getExternalTitle();
			this.externalDuration = projectBean.getExternalDuration();
		}
		this.projectGoal = projectBean.getProjectGoal();
		this.projectMethod = projectBean.getProjectMethod();
		this.projectResult = projectBean.getProjectResult();
		this.projectTask = projectBean.getProjectTask();
		this.mentorSignature = projectBean.getMentorSignature();
		this.mentorSignatureDate = projectBean.getMentorSignatureDate();
		this.menteeSignature = projectBean.getMenteeSignature();
		this.menteeSignatureDate = projectBean.getMenteeSignatureDate();
	}


	public Integer getMentorID() {
		return mentorID;
	}
	public void setMentorID(Integer mentorID) {
		this.mentorID = mentorID;
	}
	public Integer getMenteeID() {
		return menteeID;
	}
	public void setMenteeID(Integer menteeID) {
		this.menteeID = menteeID;
	}
	public Integer getApplicationID() {
		return applicationID;
	}
	public void setApplicationID(Integer applicationID) {
		this.applicationID = applicationID;
	}
	public String getProjectTitle() {
		return projectTitle;
	}
	public void setProjectTitle(String projectTitle) {
		this.projectTitle = projectTitle;
	}
	 
	public Integer getExternalProject() {
		return externalProject;
	}

	public void setExternalProject(Integer externalProject) {
		this.externalProject = externalProject;
	}

	public String getExternalAgency() {
		return externalAgency;
	}
	public void setExternalAgency(String externalAgency) {
		this.externalAgency = externalAgency;
	}
	public String getExternalTitle() {
		return externalTitle;
	}
	public void setExternalTitle(String externalTitle) {
		this.externalTitle = externalTitle;
	}
	public String getExternalDuration() {
		return externalDuration;
	}
	public void setExternalDuration(String externalDuration) {
		this.externalDuration = externalDuration;
	}
	public String getProjectGoal() {
		return projectGoal;
	}
	public void setProjectGoal(String projectGoal) {
		this.projectGoal = projectGoal;
	}
	public String getProjectMethod() {
		return projectMethod;
	}
	public void setProjectMethod(String projectMethod) {
		this.projectMethod = projectMethod;
	}
	public String getProjectResult() {
		return projectResult;
	}
	public void setProjectResult(String projectResult) {
		this.projectResult = projectResult;
	}
	public String getProjectTask() {
		return projectTask;
	}
	public void setProjectTask(String projectTask) {
		this.projectTask = projectTask;
	}
	
	public String getMentorSignature() {
		return mentorSignature;
	}
	public void setMentorSignature(String mentorSignature) {
		this.mentorSignature = mentorSignature;
	}
	public Date getMentorSignatureDate() {
		if(mentorSignatureDate == null) return null; 
		return (Date)mentorSignatureDate.clone();
	}
	public void setMentorSignatureDate(Date mentorSignatureDate) {
		if(mentorSignatureDate == null) return; 
		this.mentorSignatureDate = (Date)mentorSignatureDate.clone();
	}
	public String getMenteeSignature() {
		return menteeSignature;
	}
	public void setMenteeSignature(String menteeSignature) {
		this.menteeSignature = menteeSignature;
	}
	public Date getMenteeSignatureDate() {
		if(menteeSignatureDate == null) return null; 
		return (Date)menteeSignatureDate.clone();
	}
	public void setMenteeSignatureDate(Date menteeSignatureDate) {
		if(menteeSignatureDate == null) return; 
		this.menteeSignatureDate = (Date)menteeSignatureDate.clone();
	}
}
