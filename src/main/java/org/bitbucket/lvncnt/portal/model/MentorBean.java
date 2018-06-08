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

public class MentorBean implements Serializable{
	private static final long serialVersionUID = 1L;
	private Integer menteeID; 
	private Integer applicationID; 
	private Integer mentorID; 

	private String mentorFirstName; 
	private String mentorLastName; 
	private String mentorPrefix; 
	private String mentorEmail;
	
	private String mentorMiddleName; 
	private String mentorTitle; 
	private String mentorInstitution; 
	private String mentorDept; 
	private String mentorPhone;
	private String mentorBuilding;
	private String mentorFax;
	
	private String intlMentorFirstName; 
    private String intlMentorLastName; 
    private String intlMentorInstitution; 
    private String intlMentorPhone;
    private String intlMentorEmail;
    private String intlMentorCountry;
	
	public MentorBean(MentorBean from) {
		if(from != null){
			this.menteeID = from.getMenteeID();
			this.applicationID = from.getApplicationID();
			this.mentorID = from.getMentorID();
			this.mentorFirstName = from.getMentorFirstName();
			this.mentorLastName = from.getMentorLastName();
			this.mentorPrefix = from.getMentorPrefix();
			this.mentorEmail = from.getMentorEmail();
			this.mentorMiddleName = from.getMentorMiddleName();
			this.mentorTitle = from.getMentorTitle();
			this.mentorDept = from.getMentorDept();
			this.mentorPhone = from.getMentorPhone();
			this.mentorBuilding = from.getMentorBuilding();
			this.mentorFax = from.getMentorFax();
			this.mentorInstitution = from.getMentorInstitution(); 
			
			this.intlMentorFirstName = from.getIntlMentorFirstName(); 
		    this.intlMentorLastName = from.getIntlMentorLastName(); 
		    this.intlMentorInstitution = from.getIntlMentorInstitution(); 
		    this.intlMentorPhone = from.getIntlMentorPhone();
		    this.intlMentorEmail = from.getIntlMentorEmail();
		    this.intlMentorCountry = from.getIntlMentorCountry();
		}
	}
	public MentorBean() {
		super();
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
	public Integer getMentorID() {
		return mentorID;
	}
	public void setMentorID(Integer mentorID) {
		this.mentorID = mentorID;
	}
	public String getMentorFirstName() {
		return mentorFirstName;
	}
	public void setMentorFirstName(String mentorFirstName) {
		this.mentorFirstName = mentorFirstName;
	}
	public String getMentorLastName() {
		return mentorLastName;
	}
	public void setMentorLastName(String mentorLastName) {
		this.mentorLastName = mentorLastName;
	}
	public String getMentorPrefix() {
		return mentorPrefix;
	}
	public void setMentorPrefix(String mentorPrefix) {
		this.mentorPrefix = mentorPrefix;
	}
	public String getMentorEmail() {
		return mentorEmail;
	}
	public void setMentorEmail(String mentorEmail) {
		this.mentorEmail = mentorEmail;
	}
	public String getMentorMiddleName() {
		return mentorMiddleName;
	}
	public void setMentorMiddleName(String mentorMiddleName) {
		this.mentorMiddleName = mentorMiddleName;
	}
	public String getMentorTitle() {
		return mentorTitle;
	}
	public void setMentorTitle(String mentorTitle) {
		this.mentorTitle = mentorTitle;
	}
	public String getMentorDept() {
		return mentorDept;
	}
	public void setMentorDept(String mentorDept) {
		this.mentorDept = mentorDept;
	}
	public String getMentorPhone() {
		return mentorPhone;
	}
	public void setMentorPhone(String mentorPhone) {
		this.mentorPhone = mentorPhone;
	}
	public String getMentorBuilding() {
		return mentorBuilding;
	}
	public void setMentorBuilding(String mentorBuilding) {
		this.mentorBuilding = mentorBuilding;
	}
	public String getMentorFax() {
		return mentorFax;
	}
	public void setMentorFax(String mentorFax) {
		this.mentorFax = mentorFax;
	}
	public String getMentorInstitution() {
		return mentorInstitution;
	}
	public void setMentorInstitution(String mentorInstitution) {
		this.mentorInstitution = mentorInstitution;
	}
	public String getIntlMentorFirstName() {
		return intlMentorFirstName;
	}
	public void setIntlMentorFirstName(String intlMentorFirstName) {
		this.intlMentorFirstName = intlMentorFirstName;
	}
	public String getIntlMentorLastName() {
		return intlMentorLastName;
	}
	public void setIntlMentorLastName(String intlMentorLastName) {
		this.intlMentorLastName = intlMentorLastName;
	}
	public String getIntlMentorInstitution() {
		return intlMentorInstitution;
	}
	public void setIntlMentorInstitution(String intlMentorInstitution) {
		this.intlMentorInstitution = intlMentorInstitution;
	}
	public String getIntlMentorPhone() {
		return intlMentorPhone;
	}
	public void setIntlMentorPhone(String intlMentorPhone) {
		this.intlMentorPhone = intlMentorPhone;
	}
	public String getIntlMentorEmail() {
		return intlMentorEmail;
	}
	public void setIntlMentorEmail(String intlMentorEmail) {
		this.intlMentorEmail = intlMentorEmail;
	}
	public String getIntlMentorCountry() {
		return intlMentorCountry;
	}
	public void setIntlMentorCountry(String intlMentorCountry) {
		this.intlMentorCountry = intlMentorCountry;
	}
	
	

}
