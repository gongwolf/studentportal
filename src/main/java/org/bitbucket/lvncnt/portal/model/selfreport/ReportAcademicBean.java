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

public class ReportAcademicBean implements Serializable {
 
	private static final long serialVersionUID = 1L;
	private Integer windowID; 
	private Integer userID; 
	private String semester; 

	private String firstName; 
	private String lastName; 
	private String currentAddressLine1; 
	private String currentAddressLine2; 
	private String currentAddressCity; 
	private String currentAddressState; 
	private String currentAddressZip; 
	private String currentAddressCountry; 
	private String selectSchool; 
	private String major; 
	private String gpa;
	
	public String getSemester() {
		return semester;
	}

	public Integer getWindowID() {
		return windowID;
	}

	public void setWindowID(Integer windowID) {
		this.windowID = windowID;
	}

	public int getUserID() {
		return userID;
	}

	public void setUserID(int userID) {
		this.userID = userID;
	}

	public void setSemester(String semester) {
		this.semester = semester;
	}
 
	
	public ReportAcademicBean() {
		 
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
	public String getCurrentAddressLine1() {
		return currentAddressLine1;
	}
	public void setCurrentAddressLine1(String currentAddressLine1) {
		this.currentAddressLine1 = currentAddressLine1;
	}
	public String getCurrentAddressLine2() {
		return currentAddressLine2;
	}
	public void setCurrentAddressLine2(String currentAddressLine2) {
		this.currentAddressLine2 = currentAddressLine2;
	}
	public String getCurrentAddressCity() {
		return currentAddressCity;
	}
	public void setCurrentAddressCity(String currentAddressCity) {
		this.currentAddressCity = currentAddressCity;
	}
	public String getCurrentAddressState() {
		return currentAddressState;
	}
	public void setCurrentAddressState(String currentAddressState) {
		this.currentAddressState = currentAddressState;
	}
	public String getCurrentAddressZip() {
		return currentAddressZip;
	}
	public void setCurrentAddressZip(String currentAddressZip) {
		this.currentAddressZip = currentAddressZip;
	}
	public String getCurrentAddressCountry() {
		return currentAddressCountry;
	}
	public void setCurrentAddressCountry(String currentAddressCountry) {
		this.currentAddressCountry = currentAddressCountry;
	}
	public String getSelectSchool() {
		return selectSchool;
	}
	public void setSelectSchool(String selectSchool) {
		this.selectSchool = selectSchool;
	}
	public String getMajor() {
		return major;
	}
	public void setMajor(String major) {
		this.major = major;
	}
	public String getGpa() {
		return gpa;
	}
	public void setGpa(String gpa) {
		this.gpa = gpa;
	} 
 
}
