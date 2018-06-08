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

 
public class ContactBean implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private Integer userID; 
 
	public Integer getUserID() {
		return userID;
	}
	public void setUserID(Integer userID) {
		this.userID = userID;
	}
 
	// Email and Phone
	private String emailPref; 
	private String emailAltn; 
	private String phoneNum1;
	private String phoneType1; 
	private String phoneNum2;
	private String phoneType2; 
	Integer receiveSMS;
		 	
	// Permanent address
	private String permanentAddressLine1; 
	private String permanentAddressLine2; 
	private String permanentAddressCity; 
	private String permanentAddressCounty; 
	private String permanentAddressState; 
	private String permanentAddressZip; 
	
	// Current Address
	private String currentAddressLine1; 
	private String currentAddressLine2; 
	private String currentAddressCity; 
	private String currentAddressCounty; 
	private String currentAddressState; 
	private String currentAddressZip; 
	
	// Emergency Contact
	private String ecFirstName; 
	private String ecLastName; 
	private String ecRelationship; 
	private String ecPhoneNum1;
	private String ecPhoneType1; 
	private String ecPhoneNum2;
	private String ecPhoneType2;
	private String ecAddressLine1; 
	private String ecAddressLine2; 
	private String ecAddressCity; 
	private String ecAddressCounty;
	private String ecAddressState; 
	private String ecAddressZip; 
	
	public String getEmailPref() {
		return emailPref;
	}
	public void setEmailPref(String emailPref) {
		this.emailPref = emailPref;
	}
	public String getEmailAltn() {
		return emailAltn;
	}
	public void setEmailAltn(String emailAltn) {
		this.emailAltn = emailAltn;
	}
	public String getPhoneNum1() {
		return phoneNum1;
	}
	public void setPhoneNum1(String phoneNum1) {
		this.phoneNum1 = phoneNum1;
	}
	public String getPhoneType1() {
		return phoneType1;
	}
	public void setPhoneType1(String phoneType1) {
		this.phoneType1 = phoneType1;
	}
	public String getPhoneNum2() {
		return phoneNum2;
	}
	public void setPhoneNum2(String phoneNum2) {
		this.phoneNum2 = phoneNum2;
	}
	public String getPhoneType2() {
		return phoneType2;
	}
	public void setPhoneType2(String phoneType2) {
		this.phoneType2 = phoneType2;
	}
	public Integer getReceiveSMS() {
		return receiveSMS;
	}
	public void setReceiveSMS(Integer receiveSMS) {
		this.receiveSMS = receiveSMS;
	}
	public String getPermanentAddressLine1() {
		return permanentAddressLine1;
	}
	public void setPermanentAddressLine1(String permanentAddressLine1) {
		this.permanentAddressLine1 = permanentAddressLine1;
	}
	public String getPermanentAddressLine2() {
		return permanentAddressLine2;
	}
	public void setPermanentAddressLine2(String permanentAddressLine2) {
		this.permanentAddressLine2 = permanentAddressLine2;
	}
	public String getPermanentAddressCity() {
		return permanentAddressCity;
	}
	public void setPermanentAddressCity(String permanentAddressCity) {
		this.permanentAddressCity = permanentAddressCity;
	}
	public String getPermanentAddressState() {
		return permanentAddressState;
	}
	public void setPermanentAddressState(String permanentAddressState) {
		this.permanentAddressState = permanentAddressState;
	}
	public String getPermanentAddressZip() {
		return permanentAddressZip;
	}
	public void setPermanentAddressZip(String permanentAddressZip) {
		this.permanentAddressZip = permanentAddressZip;
	}
	public String getPermanentAddressCounty() {
		return permanentAddressCounty;
	}
	public void setPermanentAddressCounty(String permanentAddressCounty) {
		this.permanentAddressCounty = permanentAddressCounty;
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
	public String getCurrentAddressCounty() {
		return currentAddressCounty;
	}
	public void setCurrentAddressCounty(String currentAddressCounty) {
		this.currentAddressCounty = currentAddressCounty;
	}
	public String getEcFirstName() {
		return ecFirstName;
	}
	public void setEcFirstName(String ecFirstName) {
		this.ecFirstName = ecFirstName;
	}
	public String getEcLastName() {
		return ecLastName;
	}
	public void setEcLastName(String ecLastName) {
		this.ecLastName = ecLastName;
	}
	public String getEcRelationship() {
		return ecRelationship;
	}
	public void setEcRelationship(String ecRelationship) {
		this.ecRelationship = ecRelationship;
	}
	public String getEcPhoneNum1() {
		return ecPhoneNum1;
	}
	public void setEcPhoneNum1(String ecPhoneNum1) {
		this.ecPhoneNum1 = ecPhoneNum1;
	}
	public String getEcPhoneType1() {
		return ecPhoneType1;
	}
	public void setEcPhoneType1(String ecPhoneType1) {
		this.ecPhoneType1 = ecPhoneType1;
	}
	public String getEcPhoneNum2() {
		return ecPhoneNum2;
	}
	public void setEcPhoneNum2(String ecPhoneNum2) {
		this.ecPhoneNum2 = ecPhoneNum2;
	}
	public String getEcPhoneType2() {
		return ecPhoneType2;
	}
	public void setEcPhoneType2(String ecPhoneType2) {
		this.ecPhoneType2 = ecPhoneType2;
	}
	public String getEcAddressLine1() {
		return ecAddressLine1;
	}
	public void setEcAddressLine1(String ecAddressLine1) {
		this.ecAddressLine1 = ecAddressLine1;
	}
	public String getEcAddressLine2() {
		return ecAddressLine2;
	}
	public void setEcAddressLine2(String ecAddressLine2) {
		this.ecAddressLine2 = ecAddressLine2;
	}
	public String getEcAddressCity() {
		return ecAddressCity;
	}
	public void setEcAddressCity(String ecAddressCity) {
		this.ecAddressCity = ecAddressCity;
	}
	public String getEcAddressState() {
		return ecAddressState;
	}
	public void setEcAddressState(String ecAddressState) {
		this.ecAddressState = ecAddressState;
	}
	public String getEcAddressZip() {
		return ecAddressZip;
	}
	public void setEcAddressZip(String ecAddressZip) {
		this.ecAddressZip = ecAddressZip;
	}
	public String getEcAddressCounty() {
		return ecAddressCounty;
	}
	public void setEcAddressCounty(String ecAddressCounty) {
		this.ecAddressCounty = ecAddressCounty;
	}
 
}
