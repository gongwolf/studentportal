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
 
public class BiographyBean implements Serializable {
	private static final long serialVersionUID = 1L;

	private Integer userID;
	private String firstName;
	private String lastName;
	
	@DateTimeFormat(pattern="MM/dd/yyyy")
	private Date birthDate;
	
	private String middleName; 
	private String ssnLastFour; 
	private String gender;  
	private String birthCity; 
	private String birthState; 
	private Integer isNMResident; 
	private String citizenship;
	private Integer parentHasDegree; 
	public Integer getUserID() {
		return userID;
	}
	public void setUserID(Integer userID) {
		this.userID = userID;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public Date getBirthDate() {
		if(birthDate == null) return null; 
		return (Date)birthDate.clone();
	}

	public void setBirthDate(Date birthDate) {
		if(birthDate == null)return; 
		this.birthDate = (Date)birthDate.clone();
	}
	public String getMiddleName() {
		return middleName;
	}
	public void setMiddleName(String middleName) {
		this.middleName = middleName; 
	}
	public String getSsnLastFour() {
		return ssnLastFour;
	}
	public void setSsnLastFour(String ssnLastFour) {
		this.ssnLastFour = ssnLastFour;
	}
	public String getGender() {
		return gender;
	}
	public void setGender(String gender) {
		this.gender = gender;
	}
	public String getBirthCity() {
		return birthCity;
	}
	public void setBirthCity(String birthCity) {
		this.birthCity = birthCity;
	}
	public String getBirthState() {
		return birthState;
	}
	public void setBirthState(String birthState) {
		this.birthState = birthState;
	}
	public Integer getIsNMResident() {
		return isNMResident;
	}
	public void setIsNMResident(Integer isNMResident) {
		this.isNMResident = isNMResident;
	}
	public String getCitizenship() {
		return citizenship;
	}
	public void setCitizenship(String citizenship) {
		this.citizenship = citizenship;
	}
	public Integer getParentHasDegree() {
		return parentHasDegree;
	}
	public void setParentHasDegree(Integer parentHasDegree) {
		this.parentHasDegree = parentHasDegree;
	} 
	
}
