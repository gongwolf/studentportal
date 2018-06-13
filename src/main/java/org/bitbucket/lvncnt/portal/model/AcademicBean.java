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

import org.bitbucket.lvncnt.portal.service.ProgramCode;
import org.springframework.format.annotation.DateTimeFormat;

public class AcademicBean {

	private Integer userID;
	private Integer applicationID;
	private int schoolYear;
	private String schoolSemester;
	private String programNameAbbr;

	public String getProgramNameAbbr() {
		return programNameAbbr;
	}

	public void setProgramNameAbbr(String programNameAbbr) {
		this.programNameAbbr = programNameAbbr;
	}

	public int getSchoolYear() {
		return schoolYear;
	}

	public void setSchoolYear(int schoolYear) {
		this.schoolYear = schoolYear;
	}

	public String getSchoolSemester() {
		return schoolSemester;
	}

	public void setSchoolSemester(String schoolSemester) {
		this.schoolSemester = schoolSemester;
	}

	private String academicSchool;
	private String academicSchoolFullName;
	private String academicBannerID;
	private String academicMajor;
	private Float academicGPA;

	private String academicCredit;
	private String academicStatus;
	private String academicTransferSchool;

	@DateTimeFormat(pattern = "MM/dd/yyyy")
	private Date academicTransferDate;

	private String academicIntendedMajor;
	private String academicReferrer;

	private String academicYear;

	@DateTimeFormat(pattern = "MM/dd/yyyy")
	private Date academicGradDate;

	private String academicMinor;

	public AcademicBean() {
		super();
	}

	public AcademicBean(AcademicBean from) {
		this.userID = from.userID;
		this.applicationID = from.applicationID;
		this.programNameAbbr = from.programNameAbbr;
		this.schoolSemester = from.schoolSemester;
		this.schoolYear = from.schoolYear;

		this.academicSchool = from.academicSchool;
		this.academicBannerID = from.academicBannerID;
		this.academicMajor = from.academicMajor;
		this.academicGPA = from.academicGPA;
		this.academicCredit = from.academicCredit;
		this.academicStatus = from.academicStatus;
		this.academicTransferSchool = from.academicTransferSchool;
		this.setAcademicTransferDate(from.getAcademicTransferDate());
		this.academicIntendedMajor = from.academicIntendedMajor;
		this.academicReferrer = from.academicReferrer;
		this.academicYear = from.academicYear;
		this.setAcademicGradDate(from.getAcademicGradDate());
		this.academicMinor = from.academicMinor;
		this.academicSchoolFullName = from.getAcademicSchoolFullName(); // add by qixu
	}

	public String getAcademicYear() {
		return academicYear;
	}

	public void setAcademicYear(String academicYear) {
		this.academicYear = academicYear;
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

	public String getAcademicSchool() {
		return academicSchool;
	}

	public void setAcademicSchool(String academicSchool) {
		this.academicSchool = academicSchool;
	}

	public Date getAcademicGradDate() {
		if (academicGradDate == null)
			return null;
		return (Date) academicGradDate.clone();
	}

	public void setAcademicGradDate(Date academicGradDate) {
		if (academicGradDate == null)
			return;
		this.academicGradDate = (Date) academicGradDate.clone();
	}

	public String getAcademicBannerID() {
		return academicBannerID;
	}

	public void setAcademicBannerID(String academicBannerID) {
		this.academicBannerID = academicBannerID;
	}

	public String getAcademicMajor() {
		return academicMajor;
	}

	public void setAcademicMajor(String academicMajor) {
		this.academicMajor = academicMajor;
	}

	public String getAcademicMinor() {
		return academicMinor;
	}

	public void setAcademicMinor(String academicMinor) {
		this.academicMinor = academicMinor;
	}

	public Float getAcademicGPA() {
		return academicGPA;
	}

	public void setAcademicGPA(Float academicGPA) {
		this.academicGPA = academicGPA;
	}

	public String getAcademicCredit() {
		return academicCredit;
	}

	public void setAcademicCredit(String academicCredit) {
		this.academicCredit = academicCredit;
	}

	public String getAcademicStatus() {
		return academicStatus;
	}

	public void setAcademicStatus(String academicStatus) {
		this.academicStatus = academicStatus;
	}

	public String getAcademicTransferSchool() {
		return academicTransferSchool;
	}

	public void setAcademicTransferSchool(String academicTransferSchool) {
		this.academicTransferSchool = academicTransferSchool;
	}

	public Date getAcademicTransferDate() {
		if (academicTransferDate == null)
			return null;
		return (Date) academicTransferDate.clone();
	}

	public void setAcademicTransferDate(Date academicTransferDate) {
		if (academicTransferDate == null)
			return;
		this.academicTransferDate = (Date) academicTransferDate.clone();
	}

	public String getAcademicIntendedMajor() {
		return academicIntendedMajor;
	}

	public void setAcademicIntendedMajor(String academicIntendedMajor) {
		this.academicIntendedMajor = academicIntendedMajor;
	}

	public String getAcademicReferrer() {
		return academicReferrer;
	}

	public void setAcademicReferrer(String academicReferrer) {
		this.academicReferrer = academicReferrer;
	}

	// Add by qixu
	public String getAcademicSchoolFullName() {
		return academicSchoolFullName;
	}

	public void setAcademicSchoolFullName(String academicSchoolFullName) {
		this.academicSchoolFullName = academicSchoolFullName;
	}

	public void setAcademicSchoolFullName() {
		if (this.getAcademicSchool() != null) {
			this.academicSchoolFullName = ProgramCode.ACADEMIC_SCHOOL.get(this.getAcademicSchool());
		}
	}

}
