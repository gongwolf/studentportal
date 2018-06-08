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

public class EssayBean {
	private Integer userID; 
	private Integer applicationID; 
	private String programNameAbbr; 
	
	// CCCONF Short Essay 
	private String essayEducationalGoal;

	// SCCORE Application Questionnaire 
	private String sccoreSchoolAttendPref;
	private String sccoreSchoolAttendAltn;
	private String essayFieldOfStudy;
	private String essayPreferredResearch;
	private String essayStrengthBring;
	private String essayAmpGain;

	// MESA Essay Questions 
	private String essayProfesionalGoal;
	private String essayAcademicPathway;
	private String essayCriticalEvent;
	 
	// TRANS Essay Questions 
	private String essayFieldOfInterest;
	private String essayMentor;

	public Integer getUserID() {
		return userID;
	}
	
	public String getProgramNameAbbr() {
		return programNameAbbr;
	}

	public void setProgramNameAbbr(String programNameAbbr) {
		this.programNameAbbr = programNameAbbr;
	}

	public EssayBean() {
		super();
	}

	public EssayBean(EssayBean from) {
		this.userID = from.userID; 
		this.applicationID = from.applicationID; 
		this.programNameAbbr = from.programNameAbbr; 
		
		this.essayEducationalGoal = from.essayEducationalGoal;
		this.sccoreSchoolAttendPref = from.sccoreSchoolAttendPref;
		this.sccoreSchoolAttendAltn = from.sccoreSchoolAttendAltn;
		this.essayFieldOfStudy = from.essayFieldOfStudy;
		this.essayPreferredResearch = from.essayPreferredResearch;
		this.essayStrengthBring = from.essayStrengthBring;
		this.essayAmpGain = from.essayAmpGain;
		this.essayProfesionalGoal = from.essayProfesionalGoal;
		this.essayAcademicPathway = from.essayAcademicPathway;
		this.essayCriticalEvent = from.essayCriticalEvent;
		this.essayFieldOfInterest = from.essayFieldOfInterest;
		this.essayMentor = from.essayMentor;
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
	public String getEssayEducationalGoal() {
		return essayEducationalGoal;
	}
	public void setEssayEducationalGoal(String essayEducationalGoal) {
		this.essayEducationalGoal = essayEducationalGoal;
	}
	public String getSccoreSchoolAttendPref() {
		return sccoreSchoolAttendPref;
	}
	public void setSccoreSchoolAttendPref(String sccoreSchoolAttendPref) {
		this.sccoreSchoolAttendPref = sccoreSchoolAttendPref;
	}
	public String getSccoreSchoolAttendAltn() {
		return sccoreSchoolAttendAltn;
	}
	public void setSccoreSchoolAttendAltn(String sccoreSchoolAttendAltn) {
		this.sccoreSchoolAttendAltn = sccoreSchoolAttendAltn;
	}
	public String getEssayFieldOfStudy() {
		return essayFieldOfStudy;
	}
	public void setEssayFieldOfStudy(String essayFieldOfStudy) {
		this.essayFieldOfStudy = essayFieldOfStudy;
	}
	public String getEssayPreferredResearch() {
		return essayPreferredResearch;
	}
	public void setEssayPreferredResearch(String essayPreferredResearch) {
		this.essayPreferredResearch = essayPreferredResearch;
	}
	public String getEssayStrengthBring() {
		return essayStrengthBring;
	}
	public void setEssayStrengthBring(String essayStrengthBring) {
		this.essayStrengthBring = essayStrengthBring;
	}
	public String getEssayAmpGain() {
		return essayAmpGain;
	}
	public void setEssayAmpGain(String essayAmpGain) {
		this.essayAmpGain = essayAmpGain;
	}
	public String getEssayProfesionalGoal() {
		return essayProfesionalGoal;
	}
	public void setEssayProfesionalGoal(String essayProfesionalGoal) {
		this.essayProfesionalGoal = essayProfesionalGoal;
	}
	public String getEssayAcademicPathway() {
		return essayAcademicPathway;
	}
	public void setEssayAcademicPathway(String essayAcademicPathway) {
		this.essayAcademicPathway = essayAcademicPathway;
	}
	public String getEssayCriticalEvent() {
		return essayCriticalEvent;
	}
	public void setEssayCriticalEvent(String essayCriticalEvent) {
		this.essayCriticalEvent = essayCriticalEvent;
	}
	public String getEssayFieldOfInterest() {
		return essayFieldOfInterest;
	}
	public void setEssayFieldOfInterest(String essayFieldOfInterest) {
		this.essayFieldOfInterest = essayFieldOfInterest;
	}
	public String getEssayMentor() {
		return essayMentor;
	}
	public void setEssayMentor(String essayMentor) {
		this.essayMentor = essayMentor;
	}
	
	

}
