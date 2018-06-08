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
 
public class RecommenderBean {
	private Integer userID; 
	private Integer applicationID; 
	private String recommendationKey; 
	private Long recommendationTimeout; 
	
	private String applicantFirstName; 
	private String applicantLastName; 
	private String schoolYear; 
	private String schoolSemester; 
	
	private String firstName;
	private String lastName;
	private String email;
	private String prefix;
	private String institution;
	private String phone;
	private String addressLine1;
	private String addressLine2;
	private String addressCity;
	private String addressState;
	private String addressZip;
	private String addressCountry;
	
	private Date submitDate; 
	
	private String url; 
	private String programNameFull; 
	
	private byte[] recommendationContent; 
	
	public byte[] getRecommendationContent() {
		return recommendationContent;
	}

	public void setRecommendationContent(byte[] recommendationContent) {
		this.recommendationContent = recommendationContent;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getProgramNameFull() {
		return programNameFull;
	}

	public void setProgramNameFull(String programNameFull) {
		this.programNameFull = programNameFull;
	}
 
	public RecommenderBean() {
	}
	
	public RecommenderBean(RecommenderBean from) {
		this.userID = from.userID;
		this.applicationID = from.applicationID;
		this.recommendationKey = from.recommendationKey;
		this.recommendationTimeout = from.recommendationTimeout;
		this.firstName = from.firstName;
		this.lastName = from.lastName;
		this.email = from.email;
		this.prefix = from.prefix;
		this.institution = from.institution;
		this.phone = from.phone;
		this.addressLine1 = from.addressLine1;
		this.addressLine2 = from.addressLine2;
		this.addressCity = from.addressCity;
		this.addressState = from.addressState;
		this.addressZip = from.addressZip;
		this.addressCountry = from.addressCountry;
		this.submitDate = from.submitDate;
		
		this.applicantFirstName = from.applicantFirstName;
		this.applicantLastName = from.applicantLastName;
		this.schoolYear = from.schoolYear;
		this.schoolSemester = from.schoolSemester;
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

	public String getRecommendationKey() {
		return recommendationKey;
	}

	public void setRecommendationKey(String recommendationKey) {
		this.recommendationKey = recommendationKey;
	}

	public Long getRecommendationTimeout() {
		return recommendationTimeout;
	}

	public void setRecommendationTimeout(Long recommendationTimeout) {
		this.recommendationTimeout = recommendationTimeout;
	}

	public String getApplicantFirstName() {
		return applicantFirstName;
	}

	public void setApplicantFirstName(String applicantFirstName) {
		this.applicantFirstName = applicantFirstName;
	}

	public String getApplicantLastName() {
		return applicantLastName;
	}

	public void setApplicantLastName(String applicantLastName) {
		this.applicantLastName = applicantLastName;
	}

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

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public String getInstitution() {
		return institution;
	}

	public void setInstitution(String institution) {
		this.institution = institution;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getAddressLine1() {
		return addressLine1;
	}

	public void setAddressLine1(String addressLine1) {
		this.addressLine1 = addressLine1;
	}

	public String getAddressLine2() {
		return addressLine2;
	}

	public void setAddressLine2(String addressLine2) {
		this.addressLine2 = addressLine2;
	}

	public String getAddressCity() {
		return addressCity;
	}

	public void setAddressCity(String addressCity) {
		this.addressCity = addressCity;
	}

	public String getAddressState() {
		return addressState;
	}

	public void setAddressState(String addressState) {
		this.addressState = addressState;
	}

	public String getAddressZip() {
		return addressZip;
	}

	public void setAddressZip(String addressZip) {
		this.addressZip = addressZip;
	}

	public String getAddressCountry() {
		return addressCountry;
	}

	public void setAddressCountry(String addressCountry) {
		this.addressCountry = addressCountry;
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
