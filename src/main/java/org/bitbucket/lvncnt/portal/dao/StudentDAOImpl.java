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
package org.bitbucket.lvncnt.portal.dao;

import org.bitbucket.lvncnt.portal.model.*;
import org.bitbucket.lvncnt.portal.model.selfreport.ReportAcademicBean;
import org.bitbucket.lvncnt.portal.model.selfreport.ReportOthersBean;
import org.bitbucket.lvncnt.portal.model.selfreport.SelfReportBean;
import org.bitbucket.lvncnt.portal.service.ProgramCode;
import org.bitbucket.lvncnt.utilities.Parse;
import org.bitbucket.lvncnt.utilities.Security;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.*;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.text.ParseException;
import java.util.*;
import java.util.Date;
import java.util.stream.Collectors;

@Repository("studentDAO")
public class StudentDAOImpl implements Schemacode {

	@Autowired
	private JdbcTemplate jdbcTemplate;
	@Autowired
	private TransactionTemplate transactionTemplate;

	static {
		TimeZone.setDefault(TimeZone.getTimeZone("America/Denver"));
	}

	private HashMap<String, String> programMap = new HashMap<String, String>() {
		private static final long serialVersionUID = 1L;
		{
			put("CCCONF", "Community College Professional Development Workshop Stipend");
			put("SCCORE", "Summer Community College Opportunity for Research Experience");
			put("TRANS", "Transfer Scholarship");
			put("MESA", "MESA Scholarship");
			put("URS", "Undergraduate Research Scholars");
			put("IREP", "International Research and Education Participation");
		}
	};

	/***********************************
	 * Start application
	 ************************************/
	public int createApplication(int userID, String program, String semester, String year, int[] applicationID) {

		int result = 0;
		// If there is no application with same user, program, semester,year already
		// exists in application_list table, also its accept_status is not withdrew.
		String sql = "SELECT application_id FROM " + TABLE_APPLICATION_LIST
				+ " WHERE user_id = ? AND school_year = ? AND school_semester = ? AND program = ? AND accept_status<>2";
		try {
			result = jdbcTemplate.queryForObject(sql, new Object[] { userID, year, semester, program }, Integer.class);
		} catch (EmptyResultDataAccessException e) {
			sql = "INSERT INTO " + TABLE_APPLICATION_LIST
					+ " (application_id,user_id,school_year,school_semester,program,start_date) VALUES (?,?,?,?,?,?)";
			applicationID[0] = ProgramCode.newApplicationID(String.valueOf(userID), year);
			jdbcTemplate.update(sql, new Object[] { applicationID[0], userID, year, semester, program, new Date() });
		}

		return result;
	}

	public int saveAcademicSchool(int userID, int applicationID, String program, String school) {
		String table = ProgramCode.TABLE_APPLICATION_DETAIL.get(program);
		String sql = "INSERT INTO " + table + " (user_id, application_id, academic_school" + ") VALUES (?,?,?)"
				+ " ON DUPLICATE KEY UPDATE " + "academic_school = VALUES(academic_school)";

		return jdbcTemplate.update(sql, new Object[] { userID, applicationID, school });
	}

	/***********************************
	 * Get application List & Detail
	 ************************************/
	public List<ApplicationBean> getApplicationList(int userID) {
		// the last 3 years
		String sql = "SELECT * FROM " + TABLE_APPLICATION_LIST + " WHERE user_id = ? ORDER BY start_date DESC";
		return jdbcTemplate.query(sql, new Object[] { userID }, new RowMapper<ApplicationBean>() {

			@Override
			public ApplicationBean mapRow(ResultSet reSet, int rowNum) throws SQLException {
				ApplicationBean bean = new ApplicationBean();
				bean.setUserID(reSet.getInt("user_id"));
				bean.setApplicationID(reSet.getInt("application_id"));
				bean.setSchoolYear(reSet.getInt("school_year"));
				bean.setSchoolSemester(reSet.getString("school_semester"));
				bean.setProgramNameAbbr(reSet.getString("program"));
				bean.setProgramNameFull(programMap.get(bean.getProgramNameAbbr()));

				bean.setStartDate(reSet.getDate("start_date"));
				bean.setApplicantSubmitDate(reSet.getDate("applicant_submit_date"));
				bean.setTranscriptDate(reSet.getDate("transcript_date"));
				bean.setMentorSubmitDate(reSet.getDate("mentor_submit_date"));
				bean.setRecommenderSubmitDate(reSet.getDate("recommender_submit_date"));
				bean.setMedicalSubmitDate(reSet.getDate("medical_submit_date"));
				bean.setMentorInfoDate(reSet.getDate("mentor_info_date"));
				bean.setCompleteDate(reSet.getDate("complete_date"));

				bean.setDecision(reSet.getString("decision"));

				return bean;
			}
		});
	}

	public ApplicationBean getApplicationBean(Integer userID, Integer applicationID, String program) {

		String table = ProgramCode.TABLE_APPLICATION_DETAIL.get(program);
		switch (program) {
		// programsTwoYr
		case ProgramCode.CCCONF:
			return getApplicationBeanCCCONF(userID, applicationID, table);

		case ProgramCode.SCCORE:
			return getApplicationBeanSCCORE(userID, applicationID, table);

		case ProgramCode.TRANS:
			return getApplicationBeanTRANS(userID, applicationID, table);

		// programsFourYr
		case ProgramCode.MESA:
			return getApplicationBeanMESA(userID, applicationID, table);

		case ProgramCode.URS:
			return getApplicationBeanURS(userID, applicationID, table);

		case ProgramCode.IREP:
			return getApplicationBeanIREP(userID, applicationID, table);
		}
		return null;
	}

	private ApplicationBean getApplicationBeanCCCONF(int userID, int applicationID, String table) {

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT * \n");
		sql.append("FROM ").append(TABLE_APPLICATION_LIST).append(" a \n");
		sql.append("LEFT JOIN ").append(table).append(" b\n");
		sql.append("ON a.application_id = b.application_id\n");
		sql.append("WHERE a.user_id = ? AND a.application_id=? AND a.program = 'CCCONF'");

		try {
			ApplicationBean applicationBean = jdbcTemplate.queryForObject(sql.toString(),
					new Object[] { userID, applicationID }, new RowMapper<ApplicationBean>() {
						@Override
						public ApplicationBean mapRow(ResultSet reSet, int rowNum) throws SQLException {

							ApplicationBean bean = new ApplicationBean();
							bean.setApplicationID(applicationID);
							bean.setSchoolYear(reSet.getInt("school_year"));
							bean.setSchoolSemester(reSet.getString("school_semester"));

							// get academicInfo
							AcademicBean academicBean = null;
							String academicSchool = reSet.getString("academic_school");
							Date transfer_date = reSet.getDate("transfer_date");
							if (transfer_date != null) {
								academicBean = new AcademicBean();
								academicBean.setApplicationID(applicationID);
								academicBean.setSchoolSemester(reSet.getString("school_semester"));
								academicBean.setSchoolYear(reSet.getInt("school_year"));

								academicBean.setAcademicSchool(academicSchool);
								academicBean.setAcademicBannerID(reSet.getString("academic_banner_id"));
								academicBean.setAcademicMajor(reSet.getString("academic_major"));
								academicBean.setAcademicStatus(reSet.getString("academic_status"));
								academicBean.setAcademicCredit(reSet.getString("academic_credit"));
								academicBean.setAcademicGPA(Parse.tryParseFloat(reSet.getString("academic_gpa")));
								academicBean.setAcademicTransferDate(reSet.getDate("transfer_date"));
								academicBean.setAcademicTransferSchool(reSet.getString("academic_transfer_school"));
								academicBean.setAcademicIntendedMajor(reSet.getString("academic_intended_major"));
								academicBean.setAcademicReferrer(reSet.getString("academic_referrer"));
								bean.setAcademicBean(academicBean);
							}

							// get invInfo
							InvolvementBean involvementBean = null;
							String ampScholarship = reSet.getString("amp_scholarship");
							if (ampScholarship != null) { // *
								involvementBean = new InvolvementBean();
								involvementBean.setAmpScholarship(Parse.tryParseInteger(ampScholarship));
								if (ampScholarship.equals("1")) {
									involvementBean.setAmpScholarshipSchool(reSet.getString("amp_scholarship_school"));
									involvementBean.setAmpScholarshipType(reSet.getString("amp_scholarship_type"));
									involvementBean
											.setAmpScholarshipSemester(reSet.getString("amp_scholarship_semester"));
									involvementBean.setAmpScholarshipYear(reSet.getString("amp_scholarship_year"));
									involvementBean.setAmpScholarshipAmount(reSet.getFloat("amp_scholarship_amount"));
								}
								involvementBean.setOtherScholarship(
										Parse.tryParseInteger(reSet.getString("other_scholarship")));
								involvementBean.setListOtherScholarship(reSet.getString("list_other_scholarship"));

								String isCurrentEmploy = reSet.getString("is_current_employ");
								involvementBean.setIsCurrentEmploy(Parse.tryParseInteger(isCurrentEmploy));
								if (isCurrentEmploy.equals("1")) {
									involvementBean.setListEmployCampus(reSet.getString("list_employ_campus"));
									involvementBean.setListEmployDept(reSet.getString("list_employ_dept"));
									involvementBean.setListEmploySupervisor(reSet.getString("list_employ_supervisor"));
									involvementBean.setListEmployStart(reSet.getDate("list_employ_start"));
									involvementBean.setListEmployEnd(reSet.getDate("list_employ_end"));
								}

								involvementBean.setEverInResearch(reSet.getInt("ever_in_research"));
								involvementBean.setDescribeResearch(reSet.getString("describe_research"));

								involvementBean.setEverAttendConference(reSet.getInt("ever_attend_conference"));
								involvementBean.setProgramEverIn(reSet.getInt("program_ever_in"));
								involvementBean.setProgramEverInYear(reSet.getInt("program_ever_in_year"));
								bean.setInvolvementBean(involvementBean);
							}

							// get essayBean
							EssayBean essayBean = null;
							String essayCriticalEvent = reSet.getString("essay_critical_event");
							if (essayCriticalEvent != null) { // *
								essayBean = new EssayBean();
								essayBean.setEssayCriticalEvent(essayCriticalEvent);
								essayBean.setEssayEducationalGoal(reSet.getString("essay_educational_goal"));
								essayBean.setEssayProfesionalGoal(reSet.getString("essay_profesional_goal"));
								essayBean.setEssayAmpGain(reSet.getString("essay_amp_gain"));
								bean.setEssayBean(essayBean);
							}

							// get Transcript
							FileBucket fileBucket = null;
							Blob transcriptBlob = reSet.getBlob("transcript_content");
							if (transcriptBlob != null && transcriptBlob.length() != 0) {
								fileBucket = new FileBucket();
								fileBucket.setFileExist(1);
								bean.setFileBucket(fileBucket);
							}

							// get submission info
							String applicantSubmitDate = reSet.getString("applicant_submit_date");
							if (applicantSubmitDate != null) {
								String applicantSignature = reSet.getString("applicant_signature");
								bean.setApplicantSignature(applicantSignature);
								Date date = null;
								try {
									date = Parse.FORMAT_DATETIME.parse(applicantSubmitDate);
									applicantSubmitDate = Parse.FORMAT_DATE_MDY.format(date);
									bean.setApplicantSubmitDate(Parse.tryParseDateMDY(applicantSubmitDate));

									String completeDate = reSet.getString("complete_date");
									if (completeDate != null) {
										date = Parse.FORMAT_DATETIME.parse(completeDate);
										completeDate = Parse.FORMAT_DATE_MDY.format(date);
										bean.setCompleteDate(Parse.tryParseDateMDY(completeDate));
									}
								} catch (NullPointerException | ParseException e) {
									e.printStackTrace();
								}
							}

							return bean;
						}
					});
			return applicationBean;
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	private ApplicationBean getApplicationBeanMESA(int userID, int applicationID, String table) {

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT * \n");
		sql.append("FROM ").append(TABLE_APPLICATION_LIST).append(" a \n");
		sql.append("LEFT JOIN ").append(table).append(" b\n");
		sql.append("ON a.application_id = b.application_id\n");
		sql.append("WHERE a.user_id = ? AND a.application_id=? AND a.program = 'MESA'");

		try {
			ApplicationBean applicationBean = jdbcTemplate.queryForObject(sql.toString(),
					new Object[] { userID, applicationID }, new RowMapper<ApplicationBean>() {
						@Override
						public ApplicationBean mapRow(ResultSet reSet, int rowNum) throws SQLException {

							ApplicationBean bean = new ApplicationBean();
							bean.setApplicationID(applicationID);
							bean.setSchoolYear(reSet.getInt("school_year"));
							bean.setSchoolSemester(reSet.getString("school_semester"));

							// get academicInfo
							AcademicBean academicBean = null;
							String academicSchool = reSet.getString("academic_school");
							Date academic_grad_date = reSet.getDate("academic_grad_date");
							if (academic_grad_date != null) {
								academicBean = new AcademicBean();
								academicBean.setApplicationID(applicationID);
								academicBean.setSchoolSemester(reSet.getString("school_semester"));
								academicBean.setSchoolYear(reSet.getInt("school_year"));

								academicBean.setAcademicSchool(academicSchool);
								academicBean.setAcademicYear(reSet.getString("academic_year"));
								academicBean.setAcademicGradDate(reSet.getDate("academic_grad_date"));
								academicBean.setAcademicGPA(Parse.tryParseFloat(reSet.getString("academic_gpa")));

								academicBean.setAcademicTransferDate(reSet.getDate("transfer_date"));
								academicBean.setAcademicTransferSchool(reSet.getString("academic_transfer_school"));
								academicBean.setAcademicIntendedMajor(reSet.getString("academic_intended_major"));
								academicBean.setAcademicReferrer(reSet.getString("academic_referrer"));
								bean.setAcademicBean(academicBean);
							}

							// get invInfo
							InvolvementBean involvementBean = null;
							String ampScholarship = reSet.getString("amp_scholarship");
							if (ampScholarship != null) { // *
								involvementBean = new InvolvementBean();
								involvementBean.setAmpScholarship(Parse.tryParseInteger(ampScholarship));

								if (ampScholarship.equals("1")) {
									involvementBean.setAmpScholarshipSchool(reSet.getString("amp_scholarship_school"));
									involvementBean.setAmpScholarshipType(reSet.getString("amp_scholarship_type"));
									involvementBean
											.setAmpScholarshipSemester(reSet.getString("amp_scholarship_semester"));
									involvementBean.setAmpScholarshipYear(reSet.getString("amp_scholarship_year"));
									involvementBean.setAmpScholarshipAmount(reSet.getFloat("amp_scholarship_amount"));
								}
								involvementBean.setOtherScholarship(reSet.getInt("other_scholarship"));
								involvementBean.setListOtherScholarship(reSet.getString("list_other_scholarship"));
								bean.setInvolvementBean(involvementBean);
							}

							// get essayBean
							EssayBean essayBean = null;
							String essayProfesionalGoal = reSet.getString("essay_profesional_goal");
							if (essayProfesionalGoal != null) { // *
								essayBean = new EssayBean();
								essayBean.setEssayProfesionalGoal(essayProfesionalGoal);
								essayBean.setEssayAcademicPathway(reSet.getString("essay_academic_pathway"));
								essayBean.setEssayCriticalEvent(reSet.getString("essay_critical_event"));
								bean.setEssayBean(essayBean);
							}

							// get Reference
							FileBucket referenceBucket = null;
							Blob referenceBlob = reSet.getBlob("reference_content");
							if (referenceBlob != null && referenceBlob.length() != 0) {
								referenceBucket = new FileBucket();
								referenceBucket.setFileExist(1);
								bean.setReferenceBucket(referenceBucket);
							}

							// get Transcript
							FileBucket fileBucket = null;
							Blob transcriptBlob = reSet.getBlob("transcript_content");
							if (transcriptBlob != null && transcriptBlob.length() != 0) {
								fileBucket = new FileBucket();
								fileBucket.setFileExist(1);
								bean.setFileBucket(fileBucket);
							}

							// get submission info
							String applicantSubmitDate = reSet.getString("applicant_submit_date");
							if (applicantSubmitDate != null) {
								String applicantSignature = reSet.getString("applicant_signature");
								bean.setApplicantSignature(applicantSignature);
								Date date = null;
								try {
									date = Parse.FORMAT_DATETIME.parse(applicantSubmitDate);
									applicantSubmitDate = Parse.FORMAT_DATE_MDY.format(date);
									bean.setApplicantSubmitDate(Parse.tryParseDateMDY(applicantSubmitDate));

									String completeDate = reSet.getString("complete_date");
									if (completeDate != null) {
										date = Parse.FORMAT_DATETIME.parse(completeDate);
										completeDate = Parse.FORMAT_DATE_MDY.format(date);
										bean.setCompleteDate(Parse.tryParseDateMDY(completeDate));
									}
								} catch (NullPointerException | ParseException e) {
									e.printStackTrace();
								}
							}

							return bean;
						}
					});
			return applicationBean;
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	private ApplicationBean getApplicationBeanSCCORE(int userID, int applicationID, String table) {

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT * \n");
		sql.append("FROM ").append(TABLE_APPLICATION_LIST).append(" a \n");
		sql.append("LEFT JOIN ").append(table).append(" b\n");
		sql.append("ON a.application_id = b.application_id\n");
		sql.append("WHERE a.user_id = ? AND a.application_id=? AND a.program = 'SCCORE'");

		try {
			ApplicationBean applicationBean = jdbcTemplate.queryForObject(sql.toString(),
					new Object[] { userID, applicationID }, new RowMapper<ApplicationBean>() {
						@Override
						public ApplicationBean mapRow(ResultSet reSet, int rowNum) throws SQLException {

							ApplicationBean bean = new ApplicationBean();
							bean.setApplicationID(applicationID);
							bean.setSchoolYear(reSet.getInt("school_year"));
							bean.setSchoolSemester(reSet.getString("school_semester"));

							// get academicInfo
							AcademicBean academicBean = null;
							String academicSchool = reSet.getString("academic_school");
							Date academic_grad_date = reSet.getDate("academic_grad_date");
							if (academic_grad_date != null) {
								academicBean = new AcademicBean();
								academicBean.setApplicationID(applicationID);
								academicBean.setSchoolSemester(reSet.getString("school_semester"));
								academicBean.setSchoolYear(reSet.getInt("school_year"));

								academicBean.setAcademicSchool(academicSchool);
								academicBean.setAcademicYear(reSet.getString("academic_year"));
								academicBean.setAcademicGradDate(reSet.getDate("academic_grad_date"));
								academicBean.setAcademicBannerID(reSet.getString("academic_banner_id"));
								academicBean.setAcademicMajor(reSet.getString("academic_major"));
								academicBean.setAcademicMinor(reSet.getString("academic_minor"));
								academicBean.setAcademicGPA(reSet.getFloat("academic_gpa"));
								bean.setAcademicBean(academicBean);
							}

							// get invInfo
							InvolvementBean involvementBean = null;
							String programEverIn = reSet.getString("program_ever_in");
							if (programEverIn != null) { // *
								involvementBean = new InvolvementBean();
								involvementBean.setProgramEverIn(Parse.tryParseInteger(programEverIn));
								involvementBean.setProgramEverInYear(reSet.getInt("program_ever_in_year"));
								String ampScholarship = reSet.getString("amp_scholarship");
								involvementBean.setAmpScholarship(Parse.tryParseInteger(ampScholarship));
								if (ampScholarship.equals("1")) {
									involvementBean.setAmpScholarshipSchool(reSet.getString("amp_scholarship_school"));
									involvementBean.setAmpScholarshipType(reSet.getString("amp_scholarship_type"));
									involvementBean
											.setAmpScholarshipSemester(reSet.getString("amp_scholarship_semester"));
									involvementBean.setAmpScholarshipYear(reSet.getString("amp_scholarship_year"));
									involvementBean.setAmpScholarshipAmount(reSet.getFloat("amp_scholarship_amount"));
								}

								involvementBean.setOtherScholarship(reSet.getInt("other_scholarship"));
								involvementBean.setListOtherScholarship(reSet.getString("list_other_scholarship"));
								bean.setInvolvementBean(involvementBean);
							}

							// get essayBean
							EssayBean essayBean = null;
							String sccoreSchoolAttendPref = reSet.getString("sccore_school_attend_pref");
							if (sccoreSchoolAttendPref != null) {
								essayBean = new EssayBean();
								essayBean.setSccoreSchoolAttendPref(sccoreSchoolAttendPref);
								essayBean.setSccoreSchoolAttendAltn(reSet.getString("sccore_school_attend_altn"));
								essayBean.setEssayFieldOfStudy(reSet.getString("essay_field_of_study"));
								essayBean.setEssayEducationalGoal(reSet.getString("essay_educational_goal"));
								essayBean.setEssayPreferredResearch(reSet.getString("essay_preferred_research"));
								essayBean.setEssayStrengthBring(reSet.getString("essay_strengths_bring"));
								essayBean.setEssayAmpGain(reSet.getString("essay_amp_gain"));
								bean.setEssayBean(essayBean);
							}

							// get Medical
							FileBucket referenceBucket = null;
							Blob referenceBlob = reSet.getBlob("medical_content");
							if (referenceBlob != null && referenceBlob.length() != 0) {
								referenceBucket = new FileBucket();
								referenceBucket.setFileExist(1);
								bean.setReferenceBucket(referenceBucket);
							}

							// get Transcript
							FileBucket fileBucket = null;
							Blob transcriptBlob = reSet.getBlob("transcript_content");
							if (transcriptBlob != null && transcriptBlob.length() != 0) {
								fileBucket = new FileBucket();
								fileBucket.setFileExist(1);
								bean.setFileBucket(fileBucket);
							}

							// get submission info
							String applicantSubmitDate = reSet.getString("applicant_submit_date");
							if (applicantSubmitDate != null) {
								String applicantSignature = reSet.getString("applicant_signature");
								bean.setApplicantSignature(applicantSignature);
								Date date = null;
								try {
									date = Parse.FORMAT_DATETIME.parse(applicantSubmitDate);
									applicantSubmitDate = Parse.FORMAT_DATE_MDY.format(date);
									bean.setApplicantSubmitDate(Parse.tryParseDateMDY(applicantSubmitDate));

									String completeDate = reSet.getString("complete_date");
									if (completeDate != null) {
										date = Parse.FORMAT_DATETIME.parse(completeDate);
										completeDate = Parse.FORMAT_DATE_MDY.format(date);
										bean.setCompleteDate(Parse.tryParseDateMDY(completeDate));
									}
								} catch (NullPointerException | ParseException e) {
									e.printStackTrace();
								}
							}

							return bean;
						}
					});
			return applicationBean;
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	private ApplicationBean getApplicationBeanTRANS(int userID, int applicationID, String table) {

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT * \n");
		sql.append("FROM ").append(TABLE_APPLICATION_LIST).append(" a \n");
		sql.append("LEFT JOIN ").append(table).append(" b\n");
		sql.append("ON a.application_id = b.application_id\n");
		sql.append("WHERE a.user_id = ? AND a.application_id=? AND a.program = 'TRANS'");

		try {
			ApplicationBean applicationBean = jdbcTemplate.queryForObject(sql.toString(),
					new Object[] { userID, applicationID }, new RowMapper<ApplicationBean>() {
						@Override
						public ApplicationBean mapRow(ResultSet reSet, int rowNum) throws SQLException {

							ApplicationBean bean = new ApplicationBean();
							bean.setApplicationID(applicationID);
							bean.setSchoolYear(reSet.getInt("school_year"));
							bean.setSchoolSemester(reSet.getString("school_semester"));

							// get academicInfo
							AcademicBean academicBean = null;
							String academicSchool = reSet.getString("academic_school");
							Date academic_grad_date = reSet.getDate("academic_grad_date");
							if (academic_grad_date != null) {
								academicBean = new AcademicBean();
								academicBean.setApplicationID(applicationID);
								academicBean.setSchoolSemester(reSet.getString("school_semester"));
								academicBean.setSchoolYear(reSet.getInt("school_year"));

								academicBean.setAcademicSchool(academicSchool);
								academicBean.setAcademicYear(reSet.getString("academic_year"));
								academicBean.setAcademicGradDate(reSet.getDate("academic_grad_date"));
								academicBean.setAcademicBannerID(reSet.getString("academic_banner_id"));
								academicBean.setAcademicMajor(reSet.getString("academic_major"));
								academicBean.setAcademicMinor(reSet.getString("academic_minor"));
								academicBean.setAcademicGPA(reSet.getFloat("academic_gpa"));
								academicBean.setAcademicTransferDate(reSet.getDate("transfer_date"));
								academicBean.setAcademicTransferSchool(reSet.getString("academic_transfer_school"));
								academicBean.setAcademicIntendedMajor(reSet.getString("academic_intended_major"));
								academicBean.setAcademicReferrer(reSet.getString("academic_referrer"));
								bean.setAcademicBean(academicBean);
							}

							// get invInfo
							InvolvementBean involvementBean = null;
							String ampScholarship = reSet.getString("amp_scholarship");
							if (ampScholarship != null) { // *
								involvementBean = new InvolvementBean();
								involvementBean.setAmpScholarship(Parse.tryParseInteger(ampScholarship));

								if (ampScholarship.equals("1")) {
									involvementBean.setAmpScholarshipSchool(reSet.getString("amp_scholarship_school"));
									involvementBean.setAmpScholarshipType(reSet.getString("amp_scholarship_type"));
									involvementBean
											.setAmpScholarshipSemester(reSet.getString("amp_scholarship_semester"));
									involvementBean.setAmpScholarshipYear(reSet.getString("amp_scholarship_year"));
									involvementBean.setAmpScholarshipAmount(reSet.getFloat("amp_scholarship_amount"));
								}
								involvementBean.setOtherScholarship(reSet.getInt("other_scholarship"));
								involvementBean.setListOtherScholarship(reSet.getString("list_other_scholarship"));
								bean.setInvolvementBean(involvementBean);
							}

							// get essayBean
							EssayBean essayBean = null;
							String essayProfesionalGoal = reSet.getString("essay_profesional_goal");
							if (essayProfesionalGoal != null) {
								essayBean = new EssayBean();
								essayBean.setEssayProfesionalGoal(essayProfesionalGoal);
								essayBean.setEssayFieldOfInterest(reSet.getString("essay_field_of_interest"));
								essayBean.setEssayCriticalEvent(reSet.getString("essay_critical_event"));
								essayBean.setEssayMentor(reSet.getString("essay_mentor"));
								essayBean.setEssayAmpGain(reSet.getString("essay_amp_gain"));
								bean.setEssayBean(essayBean);
							}

							// get Transcript
							FileBucket fileBucket = null;
							Blob transcriptBlob = reSet.getBlob("transcript_content");
							if (transcriptBlob != null && transcriptBlob.length() != 0) {
								fileBucket = new FileBucket();
								fileBucket.setFileExist(1);
								bean.setFileBucket(fileBucket);
							}

							// get submission info
							String applicantSubmitDate = reSet.getString("applicant_submit_date");
							if (applicantSubmitDate != null) {
								String applicantSignature = reSet.getString("applicant_signature");
								bean.setApplicantSignature(applicantSignature);
								Date date = null;
								try {
									date = Parse.FORMAT_DATETIME.parse(applicantSubmitDate);
									applicantSubmitDate = Parse.FORMAT_DATE_MDY.format(date);
									bean.setApplicantSubmitDate(Parse.tryParseDateMDY(applicantSubmitDate));

									String completeDate = reSet.getString("complete_date");
									if (completeDate != null) {
										date = Parse.FORMAT_DATETIME.parse(completeDate);
										completeDate = Parse.FORMAT_DATE_MDY.format(date);
										bean.setCompleteDate(Parse.tryParseDateMDY(completeDate));
									}
								} catch (NullPointerException | ParseException e) {
									e.printStackTrace();
								}
							}
							return bean;
						}
					});
			return applicationBean;
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	private ApplicationBean getApplicationBeanURS(int userID, int applicationID, String table) {

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT * \n");
		sql.append("FROM ").append(TABLE_APPLICATION_LIST).append(" a \n");
		sql.append("LEFT JOIN ").append(table).append(" b\n");
		sql.append("ON a.application_id = b.application_id\n");
		sql.append("WHERE a.user_id = ? AND a.application_id=? AND a.program = 'URS'");

		try {
			ApplicationBean applicationBean = jdbcTemplate.queryForObject(sql.toString(),
					new Object[] { userID, applicationID }, new RowMapper<ApplicationBean>() {
						@Override
						public ApplicationBean mapRow(ResultSet reSet, int rowNum) throws SQLException {

							ApplicationBean bean = new ApplicationBean();
							bean.setApplicationID(applicationID);
							bean.setSchoolYear(reSet.getInt("school_year"));
							bean.setSchoolSemester(reSet.getString("school_semester"));

							// get academicInfo
							AcademicBean academicBean = null;
							String academicSchool = reSet.getString("academic_school");
							Date academic_grad_date = reSet.getDate("academic_grad_date");
							if (academic_grad_date != null) {
								academicBean = new AcademicBean();
								academicBean.setApplicationID(applicationID);
								academicBean.setSchoolSemester(reSet.getString("school_semester"));
								academicBean.setSchoolYear(reSet.getInt("school_year"));

								academicBean.setAcademicSchool(academicSchool);
								academicBean.setAcademicYear(reSet.getString("academic_year"));
								academicBean.setAcademicGradDate(reSet.getDate("academic_grad_date"));
								academicBean.setAcademicBannerID(reSet.getString("academic_banner_id"));
								academicBean.setAcademicMajor(reSet.getString("academic_major"));
								academicBean.setAcademicMinor(reSet.getString("academic_minor"));
								academicBean.setAcademicGPA(reSet.getFloat("academic_gpa"));
								bean.setAcademicBean(academicBean);
							}

							// get invInfo
							InvolvementBean involvementBean = null;
							String programEverIn = reSet.getString("program_ever_in");
							if (programEverIn != null) {
								involvementBean = new InvolvementBean();
								involvementBean.setProgramEverIn(Parse.tryParseInteger(programEverIn));
								involvementBean.setProgramEverInSemesters(reSet.getString("program_ever_in_semesters"));

								String ampScholarship = reSet.getString("amp_scholarship");
								involvementBean.setAmpScholarship(Parse.tryParseInteger(ampScholarship));
								if ("1".equals(ampScholarship)) {
									involvementBean.setAmpScholarshipSchool(reSet.getString("amp_scholarship_school"));
									involvementBean.setAmpScholarshipType(reSet.getString("amp_scholarship_type"));
									involvementBean
											.setAmpScholarshipSemester(reSet.getString("amp_scholarship_semester"));
									involvementBean.setAmpScholarshipYear(reSet.getString("amp_scholarship_year"));
									involvementBean.setAmpScholarshipAmount(
											Parse.tryParseFloat(reSet.getString("amp_scholarship_amount")));
								}

								involvementBean.setOtherScholarship(
										Parse.tryParseInteger(reSet.getString("other_scholarship")));
								involvementBean.setListOtherScholarship(reSet.getString("list_other_scholarship"));
								bean.setInvolvementBean(involvementBean);
							}

							// get essayBean
							EssayBean essayBean = null;
							String essayCriticalEvent = reSet.getString("essay_educational_goal");
							if (essayCriticalEvent != null) {
								essayBean = new EssayBean();
								essayBean.setEssayCriticalEvent(essayCriticalEvent);
								essayBean.setEssayEducationalGoal(reSet.getString("essay_educational_goal"));
								bean.setEssayBean(essayBean);
							}

							// get mentor info
							MentorBean mentorBean = null;
							String mentorFirstName = reSet.getString("mentor_first_name");
							if (mentorFirstName != null) {
								mentorBean = new MentorBean();
								mentorBean.setMentorFirstName(mentorFirstName);
								mentorBean.setMentorLastName(reSet.getString("mentor_last_name"));
								mentorBean.setMentorPrefix(reSet.getString("mentor_prefix"));
								mentorBean.setMentorEmail(reSet.getString("mentor_email"));
								bean.setMentorBean(mentorBean);
							}

							// get project info
							ProjectBean projectBean = null;
							String projectTitle = reSet.getString("project_title");
							if (projectTitle != null) {
								projectBean = new ProjectBean();
								projectBean.setProjectTitle(projectTitle);
								Integer isExternal = reSet.getInt("project_is_external");
								projectBean.setExternalProject(isExternal);
								;
								if (isExternal != null && isExternal == 1) {
									projectBean.setExternalAgency(reSet.getString("project_external_agency"));
									projectBean.setExternalTitle(reSet.getString("project_external_title"));
									projectBean.setExternalDuration(reSet.getString("project_external_duration"));
								}
								projectBean.setProjectGoal(reSet.getString("project_goal"));
								projectBean.setProjectMethod(reSet.getString("project_method"));
								projectBean.setProjectResult(reSet.getString("project_result"));
								projectBean.setProjectTask(reSet.getString("project_task"));
								bean.setProjectBean(projectBean);
							}

							// get Transcript
							FileBucket fileBucket = null;
							Blob transcriptBlob = reSet.getBlob("transcript_content");
							if (transcriptBlob != null && transcriptBlob.length() != 0) {
								fileBucket = new FileBucket();
								fileBucket.setFileExist(1);
								bean.setFileBucket(fileBucket);
							}

							// get submission info
							String applicantSubmitDate = reSet.getString("applicant_submit_date");
							if (applicantSubmitDate != null) {
								String applicantSignature = reSet.getString("applicant_signature");
								bean.setApplicantSignature(applicantSignature);
								Date date = null;
								try {
									date = Parse.FORMAT_DATETIME.parse(applicantSubmitDate);
									applicantSubmitDate = Parse.FORMAT_DATE_MDY.format(date);
									bean.setApplicantSubmitDate(Parse.tryParseDateMDY(applicantSubmitDate));

									String completeDate = reSet.getString("complete_date");
									if (completeDate != null) {
										date = Parse.FORMAT_DATETIME.parse(completeDate);
										completeDate = Parse.FORMAT_DATE_MDY.format(date);
										bean.setCompleteDate(Parse.tryParseDateMDY(completeDate));
									}
								} catch (NullPointerException | ParseException e) {
									e.printStackTrace();
								}
							}
							return bean;
						}
					});
			return applicationBean;
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	private ApplicationBean getApplicationBeanIREP(int userID, int applicationID, String table) {

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT * \n");
		sql.append("FROM ").append(TABLE_APPLICATION_LIST).append(" a \n");
		sql.append("LEFT JOIN ").append(table).append(" b\n");
		sql.append("ON a.application_id = b.application_id\n");
		sql.append("WHERE a.user_id = ? AND a.application_id=? AND a.program = 'IREP'");

		try {
			ApplicationBean applicationBean = jdbcTemplate.queryForObject(sql.toString(),
					new Object[] { userID, applicationID }, new RowMapper<ApplicationBean>() {
						@Override
						public ApplicationBean mapRow(ResultSet reSet, int rowNum) throws SQLException {

							ApplicationBean bean = new ApplicationBean();
							bean.setApplicationID(applicationID);
							bean.setSchoolYear(reSet.getInt("school_year"));
							bean.setSchoolSemester(reSet.getString("school_semester"));

							// get academicInfo
							AcademicBean academicBean = null;
							String academicSchool = reSet.getString("academic_school");
							Date academic_grad_date = reSet.getDate("academic_grad_date");
							if (academic_grad_date != null) {
								academicBean = new AcademicBean();
								academicBean.setApplicationID(applicationID);
								academicBean.setSchoolSemester(reSet.getString("school_semester"));
								academicBean.setSchoolYear(reSet.getInt("school_year"));

								academicBean.setAcademicSchool(academicSchool);
								academicBean.setAcademicYear(reSet.getString("academic_year"));
								academicBean.setAcademicGradDate(reSet.getDate("academic_grad_date"));
								academicBean.setAcademicBannerID(reSet.getString("academic_banner_id"));
								academicBean.setAcademicMajor(reSet.getString("academic_major"));
								academicBean.setAcademicMinor(reSet.getString("academic_minor"));
								academicBean.setAcademicGPA(reSet.getFloat("academic_gpa"));
								bean.setAcademicBean(academicBean);
							}

							// get mentor info
							MentorBean mentorBean = null;
							String mentorFirstName = reSet.getString("mentor_first_name");
							if (mentorFirstName != null) {
								mentorBean = new MentorBean();
								mentorBean.setMentorFirstName(mentorFirstName);
								mentorBean.setMentorLastName(reSet.getString("mentor_last_name"));
								mentorBean.setMentorInstitution(reSet.getString("mentor_institution"));
								mentorBean.setMentorPhone(reSet.getString("mentor_phone"));
								mentorBean.setMentorEmail(reSet.getString("mentor_email"));

								mentorBean.setIntlMentorFirstName(reSet.getString("intl_mentor_first_name"));
								mentorBean.setIntlMentorLastName(reSet.getString("intl_mentor_last_name"));
								mentorBean.setIntlMentorInstitution(reSet.getString("intl_mentor_institution"));
								mentorBean.setIntlMentorPhone(reSet.getString("intl_mentor_phone"));
								mentorBean.setIntlMentorEmail(reSet.getString("intl_mentor_email"));
								mentorBean.setIntlMentorCountry(reSet.getString("intl_mentor_country"));
								bean.setMentorBean(mentorBean);
							}

							// get project info for IREP
							IREPProjectBean irepProjectBean = null;
							Date researchDate = reSet.getDate("research_date");
							if (researchDate != null) {
								irepProjectBean = new IREPProjectBean();
								irepProjectBean.setResearchDate(researchDate);
								irepProjectBean.setLeaveDate(reSet.getDate("leave_date"));
								irepProjectBean.setReturnDate(reSet.getDate("return_date"));
								Integer everFundAmp = reSet.getInt("ever_fund_amp");
								irepProjectBean.setEverFundAmp(everFundAmp);
								if (everFundAmp != null && everFundAmp == 1) {
									irepProjectBean.setListProgram(reSet.getString("list_program"));
								}
								irepProjectBean.setProjectAbstract(reSet.getString("project_abstract"));
								bean.setIrepProjectBean(irepProjectBean);
							}

							// get BudgetBean
							BudgetBean budgetBean = null;
							BigDecimal totalDomesticTravel = reSet.getBigDecimal("budget_total_domestictravel");
							if (totalDomesticTravel != null) {
								budgetBean = new BudgetBean();

								budgetBean.setTotalDomesticTravel(totalDomesticTravel);
								budgetBean.setTotalRoundTrip(reSet.getBigDecimal("budget_total_roundtrip"));
								budgetBean.setTotalVisa(reSet.getBigDecimal("budget_total_visa"));
								budgetBean.setTotalPassport(reSet.getBigDecimal("budget_total_passport"));
								budgetBean.setTotalImmunization(reSet.getBigDecimal("budget_total_immunization"));
								budgetBean.setTotalHousing(reSet.getBigDecimal("budget_total_housing"));
								budgetBean.setTotalCommunication(reSet.getBigDecimal("budget_total_communication"));
								budgetBean.setTotalMeal(reSet.getBigDecimal("budget_total_meal"));
								budgetBean.setTotalMiscellaneous(reSet.getBigDecimal("budget_total_miscellaneous"));

								budgetBean
										.setCurrentDomesticTravel(reSet.getBigDecimal("budget_current_domestictravel"));
								budgetBean.setCurrentRoundTrip(reSet.getBigDecimal("budget_current_roundtrip"));
								budgetBean.setCurrentVisa(reSet.getBigDecimal("budget_current_visa"));
								budgetBean.setCurrentPassport(reSet.getBigDecimal("budget_current_passport"));
								budgetBean.setCurrentImmunization(reSet.getBigDecimal("budget_current_immunization"));
								budgetBean.setCurrentHousing(reSet.getBigDecimal("budget_current_housing"));
								budgetBean.setCurrentCommunication(reSet.getBigDecimal("budget_current_communication"));
								budgetBean.setCurrentMeal(reSet.getBigDecimal("budget_current_meal"));
								budgetBean.setCurrentMiscellaneous(reSet.getBigDecimal("budget_current_miscellaneous"));

								budgetBean.setMiscellaneousDescribe(reSet.getString("budget_miscellaneous_describe"));
								budgetBean.setFundingSource(reSet.getString("budget_funding_source"));

								bean.setBudgetBean(budgetBean);
							}

							// get Transcript
							FileBucket fileBucket = null;
							Blob transcriptBlob = reSet.getBlob("transcript_content");
							if (transcriptBlob != null && transcriptBlob.length() != 0) {
								fileBucket = new FileBucket();
								fileBucket.setFileExist(1);
								bean.setFileBucket(fileBucket);
							}

							// get submission info
							String applicantSubmitDate = reSet.getString("applicant_submit_date");
							if (applicantSubmitDate != null) {
								String applicantSignature = reSet.getString("applicant_signature");
								bean.setApplicantSignature(applicantSignature);
								Date date = null;
								try {
									date = Parse.FORMAT_DATETIME.parse(applicantSubmitDate);
									applicantSubmitDate = Parse.FORMAT_DATE_MDY.format(date);
									bean.setApplicantSubmitDate(Parse.tryParseDateMDY(applicantSubmitDate));

									String completeDate = reSet.getString("complete_date");
									if (completeDate != null) {
										date = Parse.FORMAT_DATETIME.parse(completeDate);
										completeDate = Parse.FORMAT_DATE_MDY.format(date);
										bean.setCompleteDate(Parse.tryParseDateMDY(completeDate));
									}
								} catch (ParseException e) {
									e.printStackTrace();
								}
							}
							return bean;
						}
					});
			return applicationBean;
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	/***********************************
	 * Update Academic Info
	 ************************************/
	public int updateAcademicBean(int userID, int applicationID, String program, AcademicBean academicBean) {

		// check if application has submitted

		String table = ProgramCode.TABLE_APPLICATION_DETAIL.get(program);

		switch (program) {
		// programsTwoYr
		case ProgramCode.CCCONF:
			return updateAcademicCCCONF(userID, applicationID, table, academicBean);

		case ProgramCode.SCCORE:
			return updateAcademicSCCORE(userID, applicationID, table, academicBean);

		case ProgramCode.TRANS:
			return updateAcademicTRANS(userID, applicationID, table, academicBean);

		// programsFourYr
		case ProgramCode.URS:
		case ProgramCode.IREP:
			return updateAcademicSCCORE(userID, applicationID, table, academicBean);

		case ProgramCode.MESA:
			return updateAcademicMESA(userID, applicationID, table, academicBean);
		}

		return 0;
	}

	private int updateAcademicCCCONF(int userID, int applicationID, String table, AcademicBean bean) {
		String sql = "INSERT INTO " + table + " (user_id, application_id, "
				+ "academic_school, academic_banner_id, academic_major, academic_status,"
				+ "academic_credit,academic_gpa,academic_transfer_school,"
				+ "transfer_date,academic_intended_major,academic_referrer" + ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?)"
				+ " ON DUPLICATE KEY UPDATE " + "academic_school = VALUES(academic_school), "
				+ "academic_banner_id = VALUES(academic_banner_id), " + "academic_major = VALUES(academic_major), "
				+ "academic_status = VALUES(academic_status), " + "academic_credit = VALUES(academic_credit), "
				+ "academic_gpa = VALUES(academic_gpa), "
				+ "academic_transfer_school = VALUES(academic_transfer_school), "
				+ "transfer_date = VALUES(transfer_date), "
				+ "academic_intended_major = VALUES(academic_intended_major), "
				+ "academic_referrer = VALUES(academic_referrer)";

		return jdbcTemplate.update(sql,
				new Object[] { userID, applicationID, bean.getAcademicSchool(), bean.getAcademicBannerID(),
						bean.getAcademicMajor(), bean.getAcademicStatus(), bean.getAcademicCredit(),
						bean.getAcademicGPA(), bean.getAcademicTransferSchool(), bean.getAcademicTransferDate(),
						bean.getAcademicIntendedMajor(), bean.getAcademicReferrer() });
	}

	private int updateAcademicMESA(int userID, int applicationID, String table, AcademicBean bean) {
		String sql = "INSERT INTO " + table + " (user_id, application_id, "
				+ "academic_school, academic_year, academic_grad_date," + "academic_gpa,"
				+ "academic_transfer_school,transfer_date,academic_intended_major,academic_referrer"
				+ ") VALUES (?,?,?,?,?,?,?,?,?,?)" + " ON DUPLICATE KEY UPDATE "
				+ "academic_school = VALUES(academic_school), " + "academic_year = VALUES(academic_year), "
				+ "academic_grad_date = VALUES(academic_grad_date), " + "academic_gpa = VALUES(academic_gpa), "
				+ "academic_transfer_school = VALUES(academic_transfer_school), "
				+ "transfer_date = VALUES(transfer_date), "
				+ "academic_intended_major = VALUES(academic_intended_major), "
				+ "academic_referrer = VALUES(academic_referrer)";
		return jdbcTemplate.update(sql,
				new Object[] { userID, applicationID, bean.getAcademicSchool(), bean.getAcademicYear(),
						bean.getAcademicGradDate(), bean.getAcademicGPA(), bean.getAcademicTransferSchool(),
						bean.getAcademicTransferDate(), bean.getAcademicIntendedMajor(), bean.getAcademicReferrer() });
	}

	private int updateAcademicSCCORE(int userID, int applicationID, String table, AcademicBean bean) {
		String sql = "INSERT INTO " + table + " (user_id, application_id, "
				+ "academic_school, academic_year, academic_grad_date, academic_banner_id,"
				+ "academic_major,academic_minor,academic_gpa" + ") VALUES (?,?,?,?,?,?,?,?,?)"
				+ " ON DUPLICATE KEY UPDATE " + "academic_school = VALUES(academic_school), "
				+ "academic_year = VALUES(academic_year), " + "academic_grad_date = VALUES(academic_grad_date), "
				+ "academic_banner_id = VALUES(academic_banner_id), " + "academic_major = VALUES(academic_major), "
				+ "academic_minor = VALUES(academic_minor), " + "academic_gpa = VALUES(academic_gpa)";
		return jdbcTemplate.update(sql,
				new Object[] { userID, applicationID, bean.getAcademicSchool(), bean.getAcademicYear(),
						bean.getAcademicGradDate(), bean.getAcademicBannerID(), bean.getAcademicMajor(),
						bean.getAcademicMinor(), bean.getAcademicGPA() });
	}

	private int updateAcademicTRANS(int userID, int applicationID, String table, AcademicBean bean) {
		String sql = "INSERT INTO " + table + " (user_id, application_id, "
				+ "academic_school, academic_year, academic_grad_date, academic_banner_id,"
				+ "academic_major,academic_minor,academic_gpa,"
				+ "academic_transfer_school,transfer_date,academic_intended_major,academic_referrer"
				+ ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)" + " ON DUPLICATE KEY UPDATE "
				+ "academic_school = VALUES(academic_school), " + "academic_year = VALUES(academic_year), "
				+ "academic_grad_date = VALUES(academic_grad_date), "
				+ "academic_banner_id = VALUES(academic_banner_id), " + "academic_major = VALUES(academic_major), "
				+ "academic_minor = VALUES(academic_minor), " + "academic_gpa = VALUES(academic_gpa), "
				+ "academic_transfer_school = VALUES(academic_transfer_school), "
				+ "transfer_date = VALUES(transfer_date), "
				+ "academic_intended_major = VALUES(academic_intended_major), "
				+ "academic_referrer = VALUES(academic_referrer)";
		return jdbcTemplate.update(sql,
				new Object[] { userID, applicationID, bean.getAcademicSchool(), bean.getAcademicYear(),
						bean.getAcademicGradDate(), bean.getAcademicBannerID(), bean.getAcademicMajor(),
						bean.getAcademicMinor(), bean.getAcademicGPA(), bean.getAcademicTransferSchool(),
						bean.getAcademicTransferDate(), bean.getAcademicIntendedMajor(), bean.getAcademicReferrer(), });
	}

	/*********************************
	 * Update Involvement Info
	 *********************************/
	public int updateInvolvementBean(int userID, int applicationID, String program, InvolvementBean involvementBean) {

		String table = ProgramCode.TABLE_APPLICATION_DETAIL.get(program);
		switch (program) {
		// programsTwoYr
		case ProgramCode.CCCONF:
			return updateInvolvementCCCONF(userID, applicationID, table, involvementBean);

		case ProgramCode.SCCORE:
			return updateInvolvementSCCORE(userID, applicationID, table, involvementBean);

		case ProgramCode.TRANS:
			return updateInvolvementMESA(userID, applicationID, table, involvementBean);

		// programsFourYr
		case ProgramCode.URS:
			return updateInvolvementURS(userID, applicationID, table, involvementBean);

		case ProgramCode.MESA:
			return updateInvolvementMESA(userID, applicationID, table, involvementBean);
		}
		return 0;
	}

	private int updateInvolvementCCCONF(int userID, int applicationID, String table, InvolvementBean bean) {

		// INSERTs a record if it does not exists otherwise UPDATEs it.
		StringBuilder sql = new StringBuilder();
		sql.append("INSERT INTO " + table).append(" (user_id, application_id, ")
				.append("amp_scholarship,amp_scholarship_school,amp_scholarship_type,"
						+ "amp_scholarship_semester,amp_scholarship_year,amp_scholarship_amount, ")
				.append("other_scholarship,list_other_scholarship,")
				.append("is_current_employ,list_employ_campus,list_employ_dept,list_employ_supervisor,")
				.append("list_employ_start,list_employ_end,").append("ever_in_research,describe_research,")
				.append("ever_attend_conference,program_ever_in,program_ever_in_year")
				.append(") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE ")
				.append("amp_scholarship = VALUES(amp_scholarship)")
				.append(", amp_scholarship_school = VALUES(amp_scholarship_school)")
				.append(", amp_scholarship_type = VALUES(amp_scholarship_type)")
				.append(", amp_scholarship_semester = VALUES(amp_scholarship_semester)")
				.append(", amp_scholarship_year = VALUES(amp_scholarship_year)")
				.append(", amp_scholarship_amount = VALUES(amp_scholarship_amount)")
				.append(", other_scholarship = VALUES(other_scholarship)")
				.append(", list_other_scholarship = VALUES(list_other_scholarship)")
				.append(", is_current_employ = VALUES(is_current_employ)")
				.append(", list_employ_campus = VALUES(list_employ_campus)")
				.append(", list_employ_dept = VALUES(list_employ_dept)")
				.append(", list_employ_supervisor = VALUES(list_employ_supervisor)")
				.append(", list_employ_start = VALUES(list_employ_start)")
				.append(", list_employ_end = VALUES(list_employ_end)")
				.append(", ever_in_research = VALUES(ever_in_research)")
				.append(", describe_research = VALUES(describe_research)")
				.append(", ever_attend_conference = VALUES(ever_attend_conference)")
				.append(", program_ever_in = VALUES(program_ever_in)")
				.append(", program_ever_in_year = VALUES(program_ever_in_year)");

		return jdbcTemplate.update(sql.toString(),
				new Object[] { userID, applicationID, bean.getAmpScholarship(), bean.getAmpScholarshipSchool(),
						bean.getAmpScholarshipType(), bean.getAmpScholarshipSemester(), bean.getAmpScholarshipYear(),
						bean.getAmpScholarshipAmount() == null ? null : String.valueOf(bean.getAmpScholarshipAmount()),
						bean.getOtherScholarship(), bean.getListOtherScholarship(), bean.getIsCurrentEmploy(),
						bean.getListEmployCampus(), bean.getListEmployDept(), bean.getListEmploySupervisor(),
						bean.getListEmployStart(), bean.getListEmployEnd(), bean.getEverInResearch(),
						bean.getDescribeResearch(), bean.getEverAttendConference(), bean.getProgramEverIn(),
						bean.getProgramEverInYear() == null ? null : String.valueOf(bean.getProgramEverInYear()) });

	}

	private int updateInvolvementMESA(int userID, int applicationID, String table, InvolvementBean bean) {
		StringBuilder sql = new StringBuilder();
		sql.append("INSERT INTO " + table).append(" (user_id, application_id, ")
				.append("amp_scholarship,amp_scholarship_school,amp_scholarship_type,"
						+ "amp_scholarship_semester,amp_scholarship_year,amp_scholarship_amount, ")
				.append("other_scholarship,list_other_scholarship")
				.append(") VALUES (?,?,?,?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE ")
				.append("amp_scholarship = VALUES(amp_scholarship)")
				.append(", amp_scholarship_school = VALUES(amp_scholarship_school)")
				.append(", amp_scholarship_type = VALUES(amp_scholarship_type)")
				.append(", amp_scholarship_semester = VALUES(amp_scholarship_semester)")
				.append(", amp_scholarship_year = VALUES(amp_scholarship_year)")
				.append(", amp_scholarship_amount = VALUES(amp_scholarship_amount)")
				.append(", other_scholarship = VALUES(other_scholarship)")
				.append(", list_other_scholarship = VALUES(list_other_scholarship)");

		return jdbcTemplate.update(sql.toString(),
				new Object[] { userID, applicationID, bean.getAmpScholarship(), bean.getAmpScholarshipSchool(),
						bean.getAmpScholarshipType(), bean.getAmpScholarshipSemester(), bean.getAmpScholarshipYear(),
						bean.getAmpScholarshipAmount() == null ? null : String.valueOf(bean.getAmpScholarshipAmount()),
						bean.getOtherScholarship(), bean.getListOtherScholarship() });
	}

	private int updateInvolvementSCCORE(int userID, int applicationID, String table, InvolvementBean bean) {
		StringBuilder sql = new StringBuilder();
		sql.append("INSERT INTO " + table).append(" (user_id, application_id, ")
				.append("program_ever_in,program_ever_in_year, ")
				.append("amp_scholarship,amp_scholarship_school,amp_scholarship_type,"
						+ "amp_scholarship_semester,amp_scholarship_year,amp_scholarship_amount, ")
				.append("other_scholarship,list_other_scholarship")
				.append(") VALUES (?,?,?,?,?,?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE ")
				.append("program_ever_in = VALUES(program_ever_in)")
				.append(", program_ever_in_year = VALUES(program_ever_in_year)")
				.append(", amp_scholarship = VALUES(amp_scholarship)")
				.append(", amp_scholarship_school = VALUES(amp_scholarship_school)")
				.append(", amp_scholarship_type = VALUES(amp_scholarship_type)")
				.append(", amp_scholarship_semester = VALUES(amp_scholarship_semester)")
				.append(", amp_scholarship_year = VALUES(amp_scholarship_year)")
				.append(", amp_scholarship_amount = VALUES(amp_scholarship_amount)")
				.append(", other_scholarship = VALUES(other_scholarship)")
				.append(", list_other_scholarship = VALUES(list_other_scholarship)");

		return jdbcTemplate.update(sql.toString(),
				new Object[] { userID, applicationID, bean.getProgramEverIn(),
						bean.getProgramEverInYear() == null ? null : String.valueOf(bean.getProgramEverInYear()),
						bean.getAmpScholarship(), bean.getAmpScholarshipSchool(), bean.getAmpScholarshipType(),
						bean.getAmpScholarshipSemester(), bean.getAmpScholarshipYear(),
						bean.getAmpScholarshipAmount() == null ? null : String.valueOf(bean.getAmpScholarshipAmount()),
						bean.getOtherScholarship(), bean.getListOtherScholarship() });
	}

	private int updateInvolvementURS(int userID, int applicationID, String table, InvolvementBean bean) {
		StringBuilder sql = new StringBuilder();
		sql.append("INSERT INTO " + table).append(" (user_id, application_id, ")
				.append("program_ever_in,program_ever_in_semesters, ")
				.append("amp_scholarship,amp_scholarship_school,amp_scholarship_type,"
						+ "amp_scholarship_semester,amp_scholarship_year,amp_scholarship_amount, ")
				.append("other_scholarship,list_other_scholarship")
				.append(") VALUES (?,?,?,?,?,?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE ")
				.append("program_ever_in = VALUES(program_ever_in)")
				.append(", program_ever_in_semesters = VALUES(program_ever_in_semesters)")
				.append(", amp_scholarship = VALUES(amp_scholarship)")
				.append(", amp_scholarship_school = VALUES(amp_scholarship_school)")
				.append(", amp_scholarship_type = VALUES(amp_scholarship_type)")
				.append(", amp_scholarship_semester = VALUES(amp_scholarship_semester)")
				.append(", amp_scholarship_year = VALUES(amp_scholarship_year)")
				.append(", amp_scholarship_amount = VALUES(amp_scholarship_amount)")
				.append(", other_scholarship = VALUES(other_scholarship)")
				.append(", list_other_scholarship = VALUES(list_other_scholarship)");

		return jdbcTemplate.update(sql.toString(),
				new Object[] { userID, applicationID, bean.getProgramEverIn(), bean.getProgramEverInSemesters(),
						bean.getAmpScholarship(), bean.getAmpScholarshipSchool(), bean.getAmpScholarshipType(),
						bean.getAmpScholarshipSemester(), bean.getAmpScholarshipYear(),
						bean.getAmpScholarshipAmount() == null ? null : String.valueOf(bean.getAmpScholarshipAmount()),
						bean.getOtherScholarship(), bean.getListOtherScholarship() });
	}

	/**
	 * Submit Application
	 */
	public int submitApplication(int userID, String applicationID, String program, SignatureBean signatureBean) {
		// When all required information are completed, Update tableAppList
		switch (program) {
		// programsTwoYr
		case ProgramCode.CCCONF:
		case ProgramCode.MESA:
		case ProgramCode.SCCORE:
			return submitApplicationCCCONF(userID, applicationID, signatureBean);
		case ProgramCode.TRANS:
			return submitApplicationTRANS(userID, applicationID, signatureBean);

		// programsFourYr
		case ProgramCode.URS:
			return submitApplicationCCCONF(userID, applicationID, signatureBean);
		case ProgramCode.IREP:
			return submitApplicationIREP(userID, applicationID, signatureBean);
		}
		return 0;
	}

	private int submitApplicationCCCONF(int userID, String applicationID, SignatureBean signatureBean) {
		String sql = "UPDATE " + TABLE_APPLICATION_LIST
				+ " SET applicant_submit_date=?,applicant_signature=?,complete_date=? "
				+ "WHERE application_id = ? AND user_id = ?";
		return jdbcTemplate.update(sql, new Object[] { signatureBean.getSignatureDate(), signatureBean.getSignature(),
				new Date(), applicationID, userID });
	}

	private int submitApplicationTRANS(int userID, String applicationID, SignatureBean signatureBean) {

		StringBuilder sql = new StringBuilder();
		Object[] params;
		sql.append("UPDATE \n").append(TABLE_APPLICATION_LIST).append("\n");
		// complete date: check recommendation has submitted:
		// if so, set complete date
		if (!hasRecommenderSubmitted(userID, applicationID)) {
			sql.append("SET applicant_submit_date=?,applicant_signature=? ");
			params = new Object[] { signatureBean.getSignatureDate(), signatureBean.getSignature(), applicationID,
					userID };
		} else {
			sql.append("SET applicant_submit_date=?,applicant_signature=?,complete_date=? ");
			params = new Object[] { signatureBean.getSignatureDate(), signatureBean.getSignature(), new Date(),
					applicationID, userID };
		}
		sql.append("WHERE application_id = ? AND user_id = ?");

		return jdbcTemplate.update(sql.toString(), params);
	}

	private int submitApplicationIREP(int userID, String applicationID, SignatureBean signatureBean) {
		String sql = "UPDATE " + TABLE_APPLICATION_LIST + " SET applicant_submit_date=?,applicant_signature=? "
				+ "WHERE application_id = ? AND user_id = ?";
		return jdbcTemplate.update(sql,
				new Object[] { signatureBean.getSignatureDate(), signatureBean.getSignature(), applicationID, userID });
	}

	/*********************************
	 * Update Essay Info
	 *********************************/
	public int updateEssayBean(int userID, int applicationID, String program, EssayBean essayBean) {
		String table = ProgramCode.TABLE_APPLICATION_DETAIL.get(program);
		switch (program) {
		// programsTwoYr
		case ProgramCode.CCCONF:
			return updateEssayCCCONF(userID, applicationID, table, essayBean);

		case ProgramCode.SCCORE:
			return updateEssaySCCORE(userID, applicationID, table, essayBean);

		case ProgramCode.TRANS:
			return updateEssayTRANS(userID, applicationID, table, essayBean);

		// programsFourYr
		case ProgramCode.URS:
			return updateEssayURS(userID, applicationID, table, essayBean);

		case ProgramCode.MESA:
			return updateEssayMESA(userID, applicationID, table, essayBean);
		}
		return 0;
	}

	private int updateEssayCCCONF(int userID, int applicationID, String table, EssayBean bean) {
		StringBuilder sql = new StringBuilder();
		sql.append("INSERT INTO " + table).append(" (user_id, application_id, ")
				.append("essay_critical_event,essay_educational_goal,essay_profesional_goal,essay_amp_gain")
				.append(") VALUES (?,?,?,?,?,?) ON DUPLICATE KEY UPDATE ")
				.append("essay_critical_event = VALUES(essay_critical_event)")
				.append(", essay_educational_goal = VALUES(essay_educational_goal)")
				.append(", essay_profesional_goal = VALUES(essay_profesional_goal)")
				.append(", essay_amp_gain = VALUES(essay_amp_gain)");

		return jdbcTemplate.update(sql.toString(), new Object[] { userID, applicationID, bean.getEssayCriticalEvent(),
				bean.getEssayEducationalGoal(), bean.getEssayProfesionalGoal(), bean.getEssayAmpGain() });
	}

	private int updateEssayMESA(int userID, int applicationID, String table, EssayBean bean) {
		StringBuilder sql = new StringBuilder();
		sql.append("INSERT INTO " + table).append(" (user_id, application_id, ")
				.append("essay_profesional_goal,essay_academic_pathway,essay_critical_event")
				.append(") VALUES (?,?,?,?,?) ON DUPLICATE KEY UPDATE ")
				.append("essay_profesional_goal = VALUES(essay_profesional_goal)")
				.append(", essay_academic_pathway = VALUES(essay_academic_pathway)")
				.append(", essay_critical_event = VALUES(essay_critical_event)");

		return jdbcTemplate.update(sql.toString(), new Object[] { userID, applicationID, bean.getEssayProfesionalGoal(),
				bean.getEssayAcademicPathway(), bean.getEssayCriticalEvent() });
	}

	private int updateEssaySCCORE(int userID, int applicationID, String table, EssayBean bean) {
		StringBuilder sql = new StringBuilder();
		sql.append("INSERT INTO " + table).append(" (user_id, application_id, ")
				.append("sccore_school_attend_pref,sccore_school_attend_altn,")
				.append("essay_field_of_study,essay_educational_goal,")
				.append("essay_preferred_research,essay_strengths_bring,essay_amp_gain")
				.append(") VALUES (?,?,?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE ")
				.append("sccore_school_attend_pref = VALUES(sccore_school_attend_pref)")
				.append(", sccore_school_attend_altn = VALUES(sccore_school_attend_altn)")
				.append(", essay_field_of_study = VALUES(essay_field_of_study)")
				.append(", essay_educational_goal = VALUES(essay_educational_goal)")
				.append(", essay_preferred_research = VALUES(essay_preferred_research)")
				.append(", essay_strengths_bring = VALUES(essay_strengths_bring)")
				.append(", essay_amp_gain = VALUES(essay_amp_gain)");

		return jdbcTemplate.update(sql.toString(),
				new Object[] { userID, applicationID, bean.getSccoreSchoolAttendPref(),
						bean.getSccoreSchoolAttendAltn(), bean.getEssayFieldOfStudy(), bean.getEssayEducationalGoal(),
						bean.getEssayPreferredResearch(), bean.getEssayStrengthBring(), bean.getEssayAmpGain() });
	}

	private int updateEssayTRANS(int userID, int applicationID, String table, EssayBean bean) {
		StringBuilder sql = new StringBuilder();
		sql.append("INSERT INTO " + table).append(" (user_id, application_id, ")
				.append("essay_profesional_goal,essay_field_of_interest,essay_critical_event,")
				.append("essay_mentor,essay_amp_gain").append(") VALUES (?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE ")
				.append("essay_profesional_goal = VALUES(essay_profesional_goal)")
				.append(", essay_field_of_interest = VALUES(essay_field_of_interest)")
				.append(", essay_critical_event = VALUES(essay_critical_event)")
				.append(", essay_mentor = VALUES(essay_mentor)").append(", essay_amp_gain = VALUES(essay_amp_gain)");

		return jdbcTemplate.update(sql.toString(),
				new Object[] { userID, applicationID, bean.getEssayProfesionalGoal(), bean.getEssayFieldOfInterest(),
						bean.getEssayCriticalEvent(), bean.getEssayMentor(), bean.getEssayAmpGain() });
	}

	private int updateEssayURS(int userID, int applicationID, String table, EssayBean bean) {
		StringBuilder sql = new StringBuilder();
		sql.append("INSERT INTO " + table).append(" (user_id, application_id, ").append("essay_educational_goal")
				.append(") VALUES (?,?,?) ON DUPLICATE KEY UPDATE ")
				.append("essay_educational_goal = VALUES(essay_educational_goal)");

		return jdbcTemplate.update(sql.toString(),
				new Object[] { userID, applicationID, bean.getEssayEducationalGoal() });
	}

	/*********************************
	 * Update Mentor Info
	 *********************************/
	// save MentorInfo in application_detail table,
	// when applicant finally submit the application
	// create a user account in users table, and add created mentor_id
	// (same as mentor's user id) to application_list table
	// when the mentor first log in, the mentor will need to
	// fill in his/her info, which will then be save in mentors table.
	public int updateMentorBean(int userID, int applicationID, MentorBean mentorBean) {
		String table = ProgramCode.TABLE_APPLICATION_DETAIL.get(ProgramCode.URS);
		String sql = "INSERT INTO " + table + " (user_id, application_id, "
				+ "mentor_first_name,mentor_last_name,mentor_prefix,mentor_email" + ") VALUES (?,?,?,?,?,?)"
				+ " ON DUPLICATE KEY UPDATE " + "mentor_first_name = VALUES(mentor_first_name), "
				+ "mentor_last_name = VALUES(mentor_last_name), " + "mentor_prefix = VALUES(mentor_prefix), "
				+ "mentor_email = VALUES(mentor_email)";
		return jdbcTemplate.update(sql,
				new Object[] { userID, applicationID, mentorBean.getMentorFirstName().trim(),
						mentorBean.getMentorLastName().trim(), mentorBean.getMentorPrefix(),
						mentorBean.getMentorEmail().trim() });
	}

	public int updateMentorInfoDate(int userID, String applicationID) {
		String sql = "UPDATE " + TABLE_APPLICATION_LIST + " SET mentor_info_date = ? "
				+ "WHERE application_id = ? AND user_id = ?";
		return jdbcTemplate.update(sql, new Object[] { new Date(), applicationID, userID });
	}

	/**
	 * Get Mentor Info
	 */
	public MentorBean getMentorBean(int userID, int applicationID) {
		String sql = "SELECT mentor_id FROM " + TABLE_APPLICATION_LIST + " WHERE user_id = ? AND application_id = ?";
		int mentorID = 0;
		try {
			mentorID = jdbcTemplate.queryForObject(sql, new Object[] { userID, applicationID }, Integer.class);
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
		sql = "SELECT mentor_first_name,mentor_last_name,mentor_prefix,mentor_email FROM " + TABLE_PROFILE_MENTOR
				+ " WHERE mentor_id = ?";
		try {
			return jdbcTemplate.queryForObject(sql, new Object[] { mentorID }, new RowMapper<MentorBean>() {
				@Override
				public MentorBean mapRow(ResultSet reSet, int rowNum) throws SQLException {
					MentorBean bean = new MentorBean();
					bean.setMentorFirstName(reSet.getString("mentor_first_name"));
					bean.setMentorLastName(reSet.getString("mentor_last_name"));
					bean.setMentorPrefix(reSet.getString("mentor_prefix"));
					bean.setMentorEmail(reSet.getString("mentor_email"));
					return bean;
				}
			});
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	public int setMentorID4URS(int menteeID, int applicationID, int mentorID) {
		String sql = "UPDATE " + TABLE_APPLICATION_LIST + " SET mentor_id = ? "
				+ "WHERE application_id = ? AND user_id = ?";
		return jdbcTemplate.update(sql, new Object[] { mentorID, applicationID, menteeID });

	}

	public MentorBean isMentorAndProjectFilled(int userID, int applicationID) {
		String table = ProgramCode.TABLE_APPLICATION_DETAIL.get(ProgramCode.URS);
		String sql = "SELECT mentor_first_name,mentor_last_name,mentor_prefix,mentor_email,project_title,project_goal,project_method,project_result,project_task FROM "
				+ table + " WHERE user_id = ? AND application_id = ?";

		try {
			return jdbcTemplate.queryForObject(sql, new Object[] { userID, applicationID },
					new RowMapper<MentorBean>() {
						@Override
						public MentorBean mapRow(ResultSet reSet, int rowNum) throws SQLException {
							if (reSet.getString("project_title") == null || reSet.getString("mentor_prefix") == null
									|| reSet.getString("mentor_email") == null
									|| reSet.getString("project_goal") == null
									|| reSet.getString("project_method") == null
									|| reSet.getString("project_result") == null
									|| reSet.getString("project_task") == null) {
								return null;
							}
							MentorBean bean = new MentorBean();
							bean.setMentorFirstName(reSet.getString("mentor_first_name"));
							bean.setMentorLastName(reSet.getString("mentor_last_name"));
							bean.setMentorPrefix(reSet.getString("mentor_prefix"));
							bean.setMentorEmail(reSet.getString("mentor_email"));
							return bean;
						}
					});
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	/*********************************
	 * Update Mentor Info (IREP)
	 *********************************/
	public int updateIREPMentorBean(int userID, String applicationID, MentorBean mentorBean) {
		String table = ProgramCode.TABLE_APPLICATION_DETAIL.get(ProgramCode.IREP);
		String sql = "INSERT INTO " + table + " (user_id, application_id, "
				+ "mentor_first_name,mentor_last_name,mentor_institution,mentor_phone,mentor_email,"
				+ "intl_mentor_first_name,intl_mentor_last_name,intl_mentor_institution,"
				+ "intl_mentor_phone,intl_mentor_email,intl_mentor_country" + ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)"
				+ " ON DUPLICATE KEY UPDATE " + "mentor_first_name = VALUES(mentor_first_name), "
				+ "mentor_last_name = VALUES(mentor_last_name), " + "mentor_institution = VALUES(mentor_institution), "
				+ "mentor_phone = VALUES(mentor_phone), " + "mentor_email = VALUES(mentor_email), "
				+ "intl_mentor_first_name = VALUES(intl_mentor_first_name), "
				+ "intl_mentor_last_name = VALUES(intl_mentor_last_name), "
				+ "intl_mentor_institution = VALUES(intl_mentor_institution), "
				+ "intl_mentor_phone = VALUES(intl_mentor_phone), " + "intl_mentor_email = VALUES(intl_mentor_email), "
				+ "intl_mentor_country = VALUES(intl_mentor_country)";
		return jdbcTemplate.update(sql, new Object[] { userID, applicationID, mentorBean.getMentorFirstName(),
				mentorBean.getMentorLastName(), mentorBean.getMentorInstitution(), mentorBean.getMentorPhone(),
				mentorBean.getMentorEmail().trim(), mentorBean.getIntlMentorFirstName(),
				mentorBean.getIntlMentorLastName(), mentorBean.getIntlMentorInstitution(),
				mentorBean.getIntlMentorPhone(), mentorBean.getIntlMentorEmail(), mentorBean.getIntlMentorCountry() });
	}

	/*********************************
	 * Update/Submit Project Proposal
	 *********************************/
	public int updateProjectBean(int userID, int applicationID, ProjectBean projectBean) {
		String table = ProgramCode.TABLE_APPLICATION_DETAIL.get(ProgramCode.URS);
		String sql = "INSERT INTO " + table + " (user_id,application_id," + "project_title,project_is_external,"
				+ "project_external_agency,project_external_title,project_external_duration,"
				+ "project_goal,project_method,project_result,project_task" + ") VALUES (?,?,?,?,?,?,?,?,?,?,?)"
				+ " ON DUPLICATE KEY UPDATE " + "project_title = VALUES(project_title), "
				+ "project_is_external = VALUES(project_is_external), "
				+ "project_external_agency = VALUES(project_external_agency), "
				+ "project_external_title = VALUES(project_external_title), "
				+ "project_external_duration = VALUES(project_external_duration), "
				+ "project_goal = VALUES(project_goal), " + "project_method = VALUES(project_method), "
				+ "project_result = VALUES(project_result), " + "project_task = VALUES(project_task) ";
		return jdbcTemplate.update(sql,
				new Object[] { userID, applicationID, projectBean.getProjectTitle(), projectBean.getExternalProject(),
						projectBean.getExternalAgency(), projectBean.getExternalTitle(),
						projectBean.getExternalDuration(), projectBean.getProjectGoal(), projectBean.getProjectMethod(),
						projectBean.getProjectResult(), projectBean.getProjectTask() });
	}

	public ProjectBean getProjectBean(int userID, int applicationID) {
		String table = ProgramCode.TABLE_APPLICATION_DETAIL.get(ProgramCode.URS);
		String sql = "SELECT * FROM " + table + " WHERE user_id = ? AND application_id = ?";

		try {
			return jdbcTemplate.queryForObject(sql, new Object[] { userID, applicationID },
					new RowMapper<ProjectBean>() {

						@Override
						public ProjectBean mapRow(ResultSet reSet, int rowNum) throws SQLException {
							ProjectBean projectBean = null;
							String projectTitle = reSet.getString("project_title");
							if (projectTitle != null) {
								projectBean = new ProjectBean();
								projectBean.setProjectTitle(projectTitle);
								Integer isExternal = reSet.getInt("project_is_external");
								projectBean.setExternalProject(isExternal);
								;
								if (isExternal != null && isExternal == 1) {
									projectBean.setExternalAgency(reSet.getString("project_external_agency"));
									projectBean.setExternalTitle(reSet.getString("project_external_title"));
									projectBean.setExternalDuration(reSet.getString("project_external_duration"));
								}
								projectBean.setProjectGoal(reSet.getString("project_goal"));
								projectBean.setProjectMethod(reSet.getString("project_method"));
								projectBean.setProjectResult(reSet.getString("project_result"));
								projectBean.setProjectTask(reSet.getString("project_task"));
								projectBean.setMenteeSignature(reSet.getString("project_mentee_signature")); // *

								try {
									Date date = null;
									String start = reSet.getString("project_mentee_signature_date"); // *menteee
									if (start != null) {
										date = Parse.FORMAT_DATETIME.parse(start);
										start = Parse.FORMAT_DATE_MDY.format(date);
										projectBean.setMenteeSignatureDate(Parse.tryParseDateMDY(start));
									}
								} catch (NullPointerException | ParseException e) {
									e.printStackTrace();
								}

							}
							return projectBean;
						}

					});
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	/*********************************
	 * Update/Submit Project Proposal (IREP)
	 *********************************/
	public int updateIREPProjectBean(int userID, int applicationID, IREPProjectBean irepProjectBean) {
		String table = ProgramCode.TABLE_APPLICATION_DETAIL.get(ProgramCode.IREP);
		String sql = "INSERT INTO " + table + " (user_id,application_id," + "research_date,leave_date,return_date,"
				+ "ever_fund_amp,list_program,project_abstract" + ") VALUES (?,?,?,?,?,?,?,?)"
				+ " ON DUPLICATE KEY UPDATE " + "research_date = VALUES(research_date), "
				+ "leave_date = VALUES(leave_date), " + "return_date = VALUES(return_date), "
				+ "ever_fund_amp = VALUES(ever_fund_amp), " + "list_program = VALUES(list_program), "
				+ "project_abstract = VALUES(project_abstract) ";
		return jdbcTemplate.update(sql,
				new Object[] { userID, applicationID, irepProjectBean.getResearchDate(), irepProjectBean.getLeaveDate(),
						irepProjectBean.getReturnDate(), irepProjectBean.getEverFundAmp(),
						irepProjectBean.getListProgram(), irepProjectBean.getProjectAbstract() });
	}

	public MentorBean isMentorAndProjectFilledIREP(int userID, String applicationID) {
		String table = ProgramCode.TABLE_APPLICATION_DETAIL.get(ProgramCode.IREP);
		String sql = "SELECT mentor_first_name,mentor_last_name,mentor_email,research_date,leave_date,return_date,ever_fund_amp,project_abstract FROM "
				+ table + " WHERE user_id = ? AND application_id = ?";
		try {
			return jdbcTemplate.queryForObject(sql, new Object[] { userID, applicationID },
					new RowMapper<MentorBean>() {
						@Override
						public MentorBean mapRow(ResultSet reSet, int rowNum) throws SQLException {
							if (reSet.getString("mentor_first_name") == null || reSet.getString("mentor_email") == null
									|| reSet.getString("mentor_last_name") == null
									|| reSet.getDate("research_date") == null || reSet.getDate("leave_date") == null
									|| reSet.getDate("return_date") == null || reSet.getString("ever_fund_amp") == null
									|| reSet.getString("project_abstract") == null) {
								return null;
							}
							MentorBean bean = new MentorBean();
							bean.setMentorFirstName(reSet.getString("mentor_first_name"));
							bean.setMentorLastName(reSet.getString("mentor_last_name"));
							bean.setMentorEmail(reSet.getString("mentor_email"));
							return bean;
						}
					});
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	public String updateProjectKeyIREP(int userID, String applicationID) {
		String table = ProgramCode.TABLE_APPLICATION_DETAIL.get(ProgramCode.IREP);
		String key = null;
		// check if key already set
		String sql = "SELECT project_key FROM " + table
				+ " WHERE user_id = ? AND application_id = ? AND project_key IS NOT NULL";
		try {
			key = jdbcTemplate.queryForObject(sql, new Object[] { userID, applicationID }, String.class);
		} catch (EmptyResultDataAccessException e) {
			// set new project key
			key = getRecommendationKey(userID + "" + applicationID)[0];
			sql = "UPDATE " + table + " SET project_key = ? WHERE user_id = ? AND application_id = ?";
			jdbcTemplate.update(sql, new Object[] { key, userID, applicationID });
			return key;
		}
		return key;
	}

	public IREPProjectBean getProjectAbstractIREP(String projectKey) {
		String table = ProgramCode.TABLE_APPLICATION_DETAIL.get(ProgramCode.IREP);
		if (projectKey == null || projectKey.length() == 0) {
			return null;
		}

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT a.user_id,a.application_id,a.mentor_first_name,a.mentor_first_name,"
				+ "a.research_date,a.leave_date,a.return_date,a.ever_fund_amp,a.list_program,a.project_abstract,"
				+ "b.school_year,b.school_semester,b.complete_date\n");
		sql.append("FROM ").append(table).append(" a \n");
		sql.append("LEFT JOIN ").append(TABLE_APPLICATION_LIST).append(" b\n");
		sql.append("ON a.application_id = b.application_id\n");
		sql.append("WHERE a.project_key=?");

		try {
			return jdbcTemplate.queryForObject(sql.toString(), new Object[] { projectKey },
					new RowMapper<IREPProjectBean>() {

						@Override
						public IREPProjectBean mapRow(ResultSet reSet, int rowNum) throws SQLException {
							IREPProjectBean bean = new IREPProjectBean();
							bean.setSchoolSemester(reSet.getString("school_semester"));
							bean.setSchoolYear(String.valueOf(reSet.getInt("school_year")));
							bean.setUserID(reSet.getInt("user_id"));
							bean.setApplicationID(reSet.getInt("application_id"));
							bean.setResearchDate(reSet.getDate("research_date"));
							bean.setLeaveDate(reSet.getDate("leave_date"));
							bean.setReturnDate(reSet.getDate("return_date"));
							Integer everFundAmp = reSet.getInt("ever_fund_amp");
							bean.setEverFundAmp(everFundAmp);
							if (everFundAmp != null && everFundAmp == 1) {
								bean.setListProgram(reSet.getString("list_program"));
							}
							bean.setProjectAbstract(reSet.getString("project_abstract"));
							return bean;
						}

					});
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	public int submitProjectIREP(int userID, int applicationID, SignatureBean bean) {
		String table = ProgramCode.TABLE_APPLICATION_DETAIL.get(ProgramCode.IREP);
		String sql1 = "UPDATE " + table + " SET project_mentor_signature = ?, project_mentor_signature_date = ? "
				+ "WHERE user_id = ? AND application_id = ?";
		jdbcTemplate.update(sql1, new Object[] { bean.getSignature(), bean.getSignatureDate(), userID, applicationID });
		String sql2 = "UPDATE " + TABLE_APPLICATION_LIST
				+ " SET mentor_submit_date = ? WHERE user_id = ? AND application_id = ?";
		return jdbcTemplate.update(sql2, new Object[] { new Date(), userID, applicationID });
	}

	public int markIREPComplete(int userID, int applicationID) {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT applicant_submit_date, mentor_submit_date \n");
		sql.append("FROM ").append(TABLE_APPLICATION_LIST).append("\n");
		sql.append("WHERE user_id = ? AND application_id=? AND program = 'IREP'");
		try {
			boolean isComplete = jdbcTemplate.query(sql.toString(), new Object[] { userID, applicationID },
					new ResultSetExtractor<Boolean>() {
						@Override
						public Boolean extractData(ResultSet reSet) throws SQLException, DataAccessException {
							if (reSet.next()) {
								return reSet.getString("applicant_submit_date") != null
										&& reSet.getString("mentor_submit_date") != null;
							}
							return false;
						}
					});

			if (isComplete) {
				String sql2 = "UPDATE " + TABLE_APPLICATION_LIST + " SET complete_date = ? " // CURRENT_TIMESTAMP()
						+ "WHERE user_id = ? AND application_id = ? AND program = 'IREP'";
				return jdbcTemplate.update(sql2, new Object[] { new Date(), userID, applicationID });
			}

		} catch (EmptyResultDataAccessException e) {
			return 0;
		}
		return 0;
	}

	/*********************************
	 * Upload/Download Reference
	 *********************************/
	public int uploadReference(int userID, String applicationID, String program, FileBucket fileBucket) {

		String table = ProgramCode.TABLE_APPLICATION_DETAIL.get(program);
		String sql = "INSERT INTO " + table
				+ " (user_id,application_id,reference_name,reference_type,reference_size,reference_content"
				+ ") VALUES (?,?,?,?,?,?)" + " ON DUPLICATE KEY UPDATE "
				+ "reference_name = VALUES(reference_name), reference_type = VALUES(reference_type), "
				+ "reference_size = VALUES(reference_size), reference_content = VALUES(reference_content)";

		MultipartFile file = fileBucket.getFile();

		int result = jdbcTemplate.update(sql, new PreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps) throws SQLException {
				ps.setInt(1, userID);
				ps.setString(2, applicationID);
				ps.setString(3, file.getOriginalFilename());
				ps.setString(4, file.getContentType());
				ps.setLong(5, file.getSize());
				try {
					ps.setBytes(6, file.getBytes());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});

		if (result == 0) {
			return 0;
		}

		/*
		 * update recommender_submit_date in the application_list (student do the
		 * submission for MESA)
		 */
		sql = "UPDATE " + TABLE_APPLICATION_LIST + " SET recommender_submit_date = ? "
				+ "WHERE user_id = ? AND application_id = ? ";
		return jdbcTemplate.update(sql,
				new Object[] { Parse.FORMAT_DATETIME.format(new Date()), userID, applicationID });
	}

	public byte[] downloadReference(int userID, String applicationID, String program) {
		String table = ProgramCode.TABLE_APPLICATION_DETAIL.get(program);
		String sql = "SELECT reference_content FROM " + table + " WHERE user_id = ? AND application_id = ?";
		try {
			return jdbcTemplate.queryForObject(sql, byte[].class, new Object[] { userID, applicationID });
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	/*********************************
	 * Upload/Download Medical Authorization
	 *********************************/
	public int uploadMedicalAuthorization(int userID, String applicationID, String program, FileBucket fileBucket) {

		String table = ProgramCode.TABLE_APPLICATION_DETAIL.get(program);
		String sql = "INSERT INTO " + table
				+ " (user_id,application_id,medical_name,medical_type,medical_size,medical_content"
				+ ") VALUES (?,?,?,?,?,?)" + " ON DUPLICATE KEY UPDATE "
				+ "medical_name = VALUES(medical_name), medical_type = VALUES(medical_type), "
				+ "medical_size = VALUES(medical_size), medical_content = VALUES(medical_content)";

		MultipartFile file = fileBucket.getFile();
		int result = jdbcTemplate.update(sql, new PreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps) throws SQLException {
				ps.setInt(1, userID);
				ps.setString(2, applicationID);
				ps.setString(3, file.getOriginalFilename());
				ps.setString(4, file.getContentType());
				ps.setLong(5, file.getSize());
				try {
					ps.setBytes(6, file.getBytes());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});

		if (result == 0) {
			return 0;
		}

		sql = "UPDATE " + TABLE_APPLICATION_LIST + " SET medical_submit_date = ? "
				+ "WHERE user_id = ? AND application_id = ? ";
		return jdbcTemplate.update(sql,
				new Object[] { Parse.FORMAT_DATETIME.format(new Date()), userID, applicationID });
	}

	public byte[] downloadMedicalAuthorization(int userID, String applicationID, String program) {
		String table = ProgramCode.TABLE_APPLICATION_DETAIL.get(program);
		String sql = "SELECT medical_content FROM " + table + " WHERE user_id = ? AND application_id = ?";
		try {
			return jdbcTemplate.queryForObject(sql, byte[].class, new Object[] { userID, applicationID });
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	/*********************************
	 * Upload/Download Transcript
	 *********************************/
	public int uploadTranscript(int userID, int applicationID, String program, FileBucket fileBucket) {

		String table = ProgramCode.TABLE_APPLICATION_DETAIL.get(program);
		String sql = "INSERT INTO " + table
				+ " (user_id,application_id,transcript_name,transcript_type,transcript_size,transcript_content"
				+ ") VALUES (?,?,?,?,?,?)" + " ON DUPLICATE KEY UPDATE "
				+ "transcript_name = VALUES(transcript_name), transcript_type = VALUES(transcript_type), "
				+ "transcript_size = VALUES(transcript_size), transcript_content = VALUES(transcript_content)";

		MultipartFile file = fileBucket.getFile();

		int result = jdbcTemplate.update(sql, new PreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps) throws SQLException {
				ps.setInt(1, userID);
				ps.setInt(2, applicationID);
				ps.setString(3, file.getOriginalFilename());
				ps.setString(4, file.getContentType());
				ps.setLong(5, file.getSize());
				try {
					ps.setBytes(6, file.getBytes());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});

		if (result == 0) {
			return 0;
		}

		/* update transcript submit date in the application_list */
		sql = "UPDATE " + TABLE_APPLICATION_LIST + " SET transcript_date = ? "
				+ "WHERE user_id = ? AND application_id = ? ";
		return jdbcTemplate.update(sql,
				new Object[] { Parse.FORMAT_DATETIME.format(new Date()), userID, applicationID });
	}

	public byte[] downloadTranscript(int userID, String applicationID, String program) {
		String table = ProgramCode.TABLE_APPLICATION_DETAIL.get(program);
		String sql = "SELECT transcript_content FROM " + table + " WHERE user_id = ? AND application_id = ?";
		try {
			return jdbcTemplate.queryForObject(sql, byte[].class, new Object[] { userID, applicationID });
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	/***********************************
	 * Update Budget Info (IREP)
	 ************************************/
	public int updateBudgetBean(int userID, String applicationID, BudgetBean budgetBean) {
		String table = ProgramCode.TABLE_APPLICATION_DETAIL.get(ProgramCode.IREP);
		String sql = "INSERT INTO " + table + " (user_id,application_id,"
				+ "budget_total_domestictravel,budget_total_roundtrip,budget_total_visa,"
				+ "budget_total_passport,budget_total_immunization,budget_total_housing,"
				+ "budget_total_communication,budget_total_meal,budget_total_miscellaneous,"
				+ "budget_current_domestictravel,budget_current_roundtrip,budget_current_visa,"
				+ "budget_current_passport,budget_current_immunization,budget_current_housing,"
				+ "budget_current_communication,budget_current_meal,budget_current_miscellaneous,"
				+ "budget_miscellaneous_describe,budget_funding_source"
				+ ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)" + " ON DUPLICATE KEY UPDATE "
				+ "budget_total_domestictravel = VALUES(budget_total_domestictravel), "
				+ "budget_total_roundtrip = VALUES(budget_total_roundtrip), "
				+ "budget_total_visa = VALUES(budget_total_visa), "
				+ "budget_total_passport = VALUES(budget_total_passport), "
				+ "budget_total_immunization = VALUES(budget_total_immunization), "
				+ "budget_total_housing = VALUES(budget_total_housing), "
				+ "budget_total_communication = VALUES(budget_total_communication), "
				+ "budget_total_meal = VALUES(budget_total_meal), "
				+ "budget_total_miscellaneous = VALUES(budget_total_miscellaneous), "
				+ "budget_current_domestictravel = VALUES(budget_current_domestictravel), "
				+ "budget_current_roundtrip = VALUES(budget_current_roundtrip), "
				+ "budget_current_visa = VALUES(budget_current_visa), "
				+ "budget_current_passport = VALUES(budget_current_passport), "
				+ "budget_current_immunization = VALUES(budget_current_immunization), "
				+ "budget_current_housing = VALUES(budget_current_housing), "
				+ "budget_current_communication = VALUES(budget_current_communication), "
				+ "budget_current_meal = VALUES(budget_current_meal), "
				+ "budget_current_miscellaneous = VALUES(budget_current_miscellaneous), "
				+ "budget_miscellaneous_describe = VALUES(budget_miscellaneous_describe), "
				+ "budget_funding_source = VALUES(budget_funding_source) ";

		return jdbcTemplate.update(sql, new Object[] { userID, applicationID, budgetBean.getTotalDomesticTravel(),
				budgetBean.getTotalRoundTrip(), budgetBean.getTotalVisa(), budgetBean.getTotalPassport(),
				budgetBean.getTotalImmunization(), budgetBean.getTotalHousing(), budgetBean.getTotalCommunication(),
				budgetBean.getTotalMeal(), budgetBean.getTotalMiscellaneous(), budgetBean.getCurrentDomesticTravel(),
				budgetBean.getCurrentRoundTrip(), budgetBean.getCurrentVisa(), budgetBean.getCurrentPassport(),
				budgetBean.getCurrentImmunization(), budgetBean.getCurrentHousing(),
				budgetBean.getCurrentCommunication(), budgetBean.getCurrentMeal(), budgetBean.getCurrentMiscellaneous(),
				budgetBean.getMiscellaneousDescribe(), budgetBean.getFundingSource() });
	}

	/*********************************
	 * Self-report
	 *********************************/
	public int createSelfReport(int userID, int windowID) {

		int result = 0;
		String sql = "SELECT window_id FROM " + TABLE_SELFREPORT_DATA + " WHERE user_id = ? AND window_id = ?";

		try {
			result = jdbcTemplate.queryForObject(sql, new Object[] { userID, windowID }, Integer.class);
		} catch (EmptyResultDataAccessException e) {
			sql = String.format(
					"INSERT INTO %s (user_id,window_id,semester) SELECT %s,window_id,semester FROM %s WHERE window_id = ?",
					TABLE_SELFREPORT_DATA, userID, TABLE_SELFREPORT_MANAGE);
			jdbcTemplate.update(sql, new Object[] { windowID });
		}

		return result;
	}

	public SelfReportBean getSelfreportBean(int windowID, int userID) {
		String sql = "SELECT * FROM " + TABLE_SELFREPORT_DATA + " WHERE window_id = ? AND user_id = ?";

		try {
			SelfReportBean selfReportBean = jdbcTemplate.queryForObject(sql, new Object[] { windowID, userID },
					new RowMapper<SelfReportBean>() {
						@Override
						public SelfReportBean mapRow(ResultSet reSet, int rowNum) throws SQLException {

							SelfReportBean selfReportBean = new SelfReportBean();
							selfReportBean.setWindowID(reSet.getInt("window_id"));
							selfReportBean.setSemester(reSet.getString("semester"));

							ReportAcademicBean reportAcademicBean = null;
							if (reSet.getString("first_name") != null) {
								reportAcademicBean = new ReportAcademicBean();
								reportAcademicBean.setFirstName(reSet.getString("first_name"));
								reportAcademicBean.setLastName(reSet.getString("last_name"));
								reportAcademicBean.setCurrentAddressLine1(reSet.getString("current_address_line1"));
								reportAcademicBean.setCurrentAddressLine2(reSet.getString("current_address_line2"));
								reportAcademicBean.setCurrentAddressCity(reSet.getString("current_address_city"));
								reportAcademicBean.setCurrentAddressState(reSet.getString("current_address_state"));
								reportAcademicBean.setCurrentAddressZip(reSet.getString("current_address_zip"));
								reportAcademicBean.setSelectSchool(reSet.getString("select_school"));
								reportAcademicBean.setMajor(reSet.getString("major"));
								reportAcademicBean.setGpa(reSet.getString("gpa"));
							}
							selfReportBean.setReportAcademicBean(reportAcademicBean);
							selfReportBean.setReportInternJson(reSet.getString("intern_json"));
							selfReportBean.setReportTravelJson(reSet.getString("travel_json"));
							selfReportBean.setReportConferenceJson(reSet.getString("conference_json"));
							selfReportBean.setReportPublicationJson(reSet.getString("publication_json"));
							selfReportBean.setReportAwardsJson(reSet.getString("awards_json"));

							ReportOthersBean reportOthersBean = new ReportOthersBean();
							reportOthersBean.setOtherActivities(reSet.getString("other_activities"));
							selfReportBean.setReportOthersBean(reportOthersBean);
							return selfReportBean;
						}
					});
			return selfReportBean;
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	public List<SelfReportBean> getSelfreportSubmitDates(int userID, boolean onlySubmitted) {
		String sql = null;
		if (onlySubmitted) {
			sql = "SELECT window_id,semester,submit_date FROM " + TABLE_SELFREPORT_DATA
					+ " WHERE user_id = ? AND submit_date IS NOT NULL ORDER BY window_id DESC";
		} else {
			sql = "SELECT window_id,semester,submit_date FROM " + TABLE_SELFREPORT_DATA
					+ " WHERE user_id = ? ORDER BY window_id DESC";
		}

		return jdbcTemplate.query(sql, new Object[] { userID }, new ResultSetExtractor<List<SelfReportBean>>() {
			@Override
			public List<SelfReportBean> extractData(ResultSet reSet) throws SQLException, DataAccessException {
				List<SelfReportBean> list = new ArrayList<>();

				while (reSet.next()) {
					SelfReportBean bean = new SelfReportBean();
					bean.setWindowID(reSet.getInt("window_id"));
					bean.setSemester(reSet.getString("semester"));
					bean.setSubmitDate(reSet.getDate("submit_date"));
					list.add(bean);
				}
				return list;
			}
		});
	}

	public List<SelfReportBean> getSelectedSelfReport(int userID, List<String> idList) {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT * \n");
		sql.append("FROM ").append(TABLE_SELFREPORT_DATA);
		sql.append(" WHERE user_id = ? AND window_id IN (");
		sql.append(idList.stream().collect(Collectors.joining(",")));
		sql.append(") ORDER BY semester DESC\n");

		return jdbcTemplate.query(sql.toString(), new Object[] { userID }, new RowMapper<SelfReportBean>() {

			@Override
			public SelfReportBean mapRow(ResultSet reSet, int rowNum) throws SQLException {
				SelfReportBean selfReportBean = new SelfReportBean();
				selfReportBean.setSemester(reSet.getString("semester"));
				selfReportBean.setUserID(reSet.getInt("user_id"));

				ReportAcademicBean reportAcademicBean = new ReportAcademicBean();
				if (reSet.getString("first_name") != null) {
					reportAcademicBean.setFirstName(reSet.getString("first_name"));
					reportAcademicBean.setLastName(reSet.getString("last_name"));
					reportAcademicBean.setCurrentAddressLine1(reSet.getString("current_address_line1"));
					reportAcademicBean.setCurrentAddressLine2(reSet.getString("current_address_line2"));
					reportAcademicBean.setCurrentAddressCity(reSet.getString("current_address_city"));
					reportAcademicBean.setCurrentAddressState(reSet.getString("current_address_state"));
					reportAcademicBean.setCurrentAddressZip(reSet.getString("current_address_zip"));
					reportAcademicBean.setCurrentAddressCountry(reSet.getString("current_address_country"));
					reportAcademicBean.setSelectSchool(reSet.getString("select_school"));
					reportAcademicBean.setMajor(reSet.getString("major"));
					reportAcademicBean.setGpa(reSet.getString("gpa"));
				}
				selfReportBean.setReportAcademicBean(reportAcademicBean);

				selfReportBean.setReportInternJson(reSet.getString("intern_json"));
				selfReportBean.setReportTravelJson(reSet.getString("travel_json"));
				selfReportBean.setReportConferenceJson(reSet.getString("conference_json"));
				selfReportBean.setReportPublicationJson(reSet.getString("publication_json"));
				selfReportBean.setReportAwardsJson(reSet.getString("awards_json"));
				return selfReportBean;
			}
		});
	}

	public int updateReportAcademic(int userID, int windowID, String semester, ReportAcademicBean selfReportBean) {
		String sql = "INSERT INTO " + TABLE_SELFREPORT_DATA + " (window_id, user_id,semester,first_name,last_name,"
				+ "current_address_line1,current_address_line2,current_address_city,current_address_state,"
				+ "current_address_zip,select_school,major,gpa" + ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)"
				+ " ON DUPLICATE KEY UPDATE " + "first_name = VALUES(first_name), " + "last_name = VALUES(last_name), "
				+ "current_address_line1 = VALUES(current_address_line1), "
				+ "current_address_line2 = VALUES(current_address_line2), "
				+ "current_address_city = VALUES(current_address_city), "
				+ "current_address_state = VALUES(current_address_state), "
				+ "current_address_zip = VALUES(current_address_zip), " + "select_school = VALUES(select_school), "
				+ "major = VALUES(major), " + "gpa = VALUES(gpa) ";
		return jdbcTemplate.update(sql,
				new Object[] { windowID, userID, semester, selfReportBean.getFirstName(), selfReportBean.getLastName(),
						selfReportBean.getCurrentAddressLine1(), selfReportBean.getCurrentAddressLine2(),
						selfReportBean.getCurrentAddressCity(), selfReportBean.getCurrentAddressState(),
						selfReportBean.getCurrentAddressZip(), selfReportBean.getSelectSchool(),
						selfReportBean.getMajor(), selfReportBean.getGpa() });
	}

	public int updateReportInternship(int userID, int windowID, String semester, String internJson) {
		StringBuilder sql = new StringBuilder();
		sql.append("INSERT INTO \n").append(TABLE_SELFREPORT_DATA);
		sql.append(" (window_id, user_id,semester,intern_json) ");
		sql.append("VALUES (?,?,?,?) ON DUPLICATE KEY UPDATE ");
		sql.append("intern_json=VALUES(intern_json) ");
		return jdbcTemplate.update(sql.toString(), new Object[] { windowID, userID, semester, internJson });
	}

	public int updateReportTravel(int userID, int windowID, String semester, String travelJson) {
		StringBuilder sql = new StringBuilder();
		sql.append("INSERT INTO \n").append(TABLE_SELFREPORT_DATA);
		sql.append(" (window_id, user_id,semester,travel_json) ");
		sql.append(" VALUES (?,?,?,?) ON DUPLICATE KEY UPDATE ");
		sql.append("travel_json=VALUES(travel_json) ");
		return jdbcTemplate.update(sql.toString(), new Object[] { windowID, userID, semester, travelJson });
	}

	public int updateReportConferenceBean(int userID, int windowID, String semester, String conferenceJson) {
		StringBuilder sql = new StringBuilder();
		sql.append("INSERT INTO \n").append(TABLE_SELFREPORT_DATA);
		sql.append(" (window_id, user_id,semester,conference_json) ");
		sql.append(" VALUES (?,?,?,?) ON DUPLICATE KEY UPDATE ");
		sql.append("conference_json=VALUES(conference_json) ");
		return jdbcTemplate.update(sql.toString(), new Object[] { windowID, userID, semester, conferenceJson, });
	}

	public int updateReportPublicationBean(int userID, int windowID, String semester, String publicationJson) {
		StringBuilder sql = new StringBuilder();
		sql.append("INSERT INTO \n").append(TABLE_SELFREPORT_DATA);
		sql.append(" (window_id, user_id,semester,publication_json) ");
		sql.append("VALUES (?,?,?,?) ON DUPLICATE KEY UPDATE ");
		sql.append("publication_json=VALUES(publication_json) ");

		return jdbcTemplate.update(sql.toString(), new Object[] { windowID, userID, semester, publicationJson });
	}

	public int updateReportAwardsBean(int userID, int windowID, String semester, String awardsJson) {
		StringBuilder sql = new StringBuilder();
		sql.append("INSERT INTO \n").append(TABLE_SELFREPORT_DATA);
		sql.append(" (window_id, user_id,semester,awards_json) ");
		sql.append("VALUES (?,?,?,?) ON DUPLICATE KEY UPDATE ");
		sql.append("awards_json=VALUES(awards_json) ");
		return jdbcTemplate.update(sql.toString(), new Object[] { windowID, userID, semester, awardsJson });
	}

	public int updateReportOthersBean(int userID, int windowID, String semester, ReportOthersBean bean) {

		StringBuilder sql = new StringBuilder();
		sql.append("INSERT INTO \n").append(TABLE_SELFREPORT_DATA);
		sql.append(" (window_id, user_id,semester,other_activities ");
		sql.append(") VALUES (?,?,?,?) ON DUPLICATE KEY UPDATE ");
		sql.append("other_activities=VALUES(other_activities) ");

		return jdbcTemplate.update(sql.toString(),
				new Object[] { windowID, userID, semester, bean.getOtherActivities() });
	}

	public int updateReportSubmit(int userID, int windowID, String semester) {

		String sql = "SELECT first_name,current_address_line1,major,gpa FROM " + TABLE_SELFREPORT_DATA
				+ " WHERE window_id=? AND user_id=? AND semester=?";
		boolean isComplete = false;
		try {
			isComplete = jdbcTemplate.query(sql, new Object[] { windowID, userID, semester },
					new ResultSetExtractor<Boolean>() {
						@Override
						public Boolean extractData(ResultSet reSet) throws SQLException, DataAccessException {
							if (reSet.next()) {
								if (reSet.getString("first_name") != null
										&& reSet.getString("current_address_line1") != null
										&& reSet.getString("major") != null && reSet.getString("gpa") != null) {
									return true;
								}
							}
							return false;
						}
					});
		} catch (EmptyResultDataAccessException e) {
			isComplete = false;
		}

		if (!isComplete) {
			return 0;
		}

		sql = "UPDATE " + TABLE_SELFREPORT_DATA + " SET submit_date = ? WHERE window_id=? AND user_id=? AND semester=?";
		return jdbcTemplate.update(sql, new Object[] { new Date(), windowID, userID, semester });
	}

	/***********************************
	 * Recommendation (for TRANS)
	 ************************************/
	public List<RecommenderBean> getRecommenderList(int userID, int applicationID) {

		String table = ProgramCode.TABLE_APPLICATION_DETAIL.get(ProgramCode.TRANS);

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT a.recommender_first_name,a.recommender_last_name,"
				+ "a.recommender_email,a.recommender_institution, " + "b.recommender_submit_date\n");
		sql.append("FROM ").append(table).append(" a \n");
		sql.append("LEFT JOIN ").append(TABLE_APPLICATION_LIST).append(" b\n");
		sql.append("ON a.application_id = b.application_id\n");
		sql.append("WHERE a.user_id = ? AND a.application_id=?");

		return jdbcTemplate.query(sql.toString(), new Object[] { userID, applicationID },
				new ResultSetExtractor<List<RecommenderBean>>() {

					@Override
					public List<RecommenderBean> extractData(ResultSet reSet) throws SQLException, DataAccessException {
						List<RecommenderBean> recommenderList = new ArrayList<RecommenderBean>();
						while (reSet.next()) {
							if (reSet.getString("recommender_email") == null) {
								continue;
							}
							RecommenderBean bean = new RecommenderBean();
							bean.setApplicationID(applicationID);
							bean.setFirstName(reSet.getString("recommender_first_name"));
							bean.setLastName(reSet.getString("recommender_last_name"));
							bean.setEmail(reSet.getString("recommender_email"));
							bean.setInstitution(reSet.getString("recommender_institution"));
							bean.setSubmitDate(reSet.getDate("recommender_submit_date"));
							recommenderList.add(bean);
						}
						return recommenderList;
					}
				});
	}

	public int updateRecommenderBean(int userID, int applicationID, RecommenderBean bean) {
		String table = ProgramCode.TABLE_APPLICATION_DETAIL.get(ProgramCode.TRANS);

		boolean alreadyHave = false;
		String sql1 = "SELECT recommender_email FROM " + table + " WHERE user_id = ? AND application_id = ?";
		try {
			alreadyHave = jdbcTemplate.query(sql1, new Object[] { userID, applicationID },
					new ResultSetExtractor<Boolean>() {
						@Override
						public Boolean extractData(ResultSet reSet) throws SQLException, DataAccessException {
							if (reSet.next()) {
								if (reSet.getString("recommender_email") != null) {
									return true;
								}
							}
							return false;
						}
					});
		} catch (EmptyResultDataAccessException e) {
			alreadyHave = false;
		}

		if (alreadyHave) {
			return -1;
		}

		String SQL_INSERT = "INSERT INTO " + table + " (user_id, application_id, "
				+ "recommender_first_name,recommender_last_name,recommender_prefix,"
				+ "recommender_email,recommender_institution,recommender_phone,"
				+ "recommender_address_line1,recommender_address_line2,recommender_address_city,"
				+ "recommender_address_state,recommender_address_zip,recommender_address_country,"
				+ "recommendation_key,recommendation_timeout" + ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"
				+ " ON DUPLICATE KEY UPDATE " + "recommender_first_name = VALUES(recommender_first_name), "
				+ "recommender_last_name = VALUES(recommender_last_name), "
				+ "recommender_prefix = VALUES(recommender_prefix), "
				+ "recommender_email = VALUES(recommender_email), "
				+ "recommender_institution = VALUES(recommender_institution), "
				+ "recommender_phone = VALUES(recommender_phone), "
				+ "recommender_address_line1 = VALUES(recommender_address_line1), "
				+ "recommender_address_line2 = VALUES(recommender_address_line2), "
				+ "recommender_address_city = VALUES(recommender_address_city), "
				+ "recommender_address_state = VALUES(recommender_address_state), "
				+ "recommender_address_zip = VALUES(recommender_address_zip), "
				+ "recommender_address_country = VALUES(recommender_address_country), "
				+ "recommendation_key = VALUES(recommendation_key), "
				+ "recommendation_timeout = VALUES(recommendation_timeout)";
		String[] key = getRecommendationKey(bean.getEmail());
		int result = 0;
		if (key != null) {
			result = jdbcTemplate.update(SQL_INSERT,
					new Object[] { userID, applicationID, bean.getFirstName(), bean.getLastName(), bean.getPrefix(),
							bean.getEmail().trim(), bean.getInstitution(), bean.getPhone(), bean.getAddressLine1(),
							bean.getAddressLine2(), bean.getAddressCity(), bean.getAddressState(), bean.getAddressZip(),
							bean.getAddressCountry(), key[0], Long.parseLong(key[1]) });
		}
		if (result != 0) {
			bean.setRecommendationKey(key[0]);
			bean.setRecommendationTimeout(Long.parseLong(key[1]));
		}

		return result;
	}

	private static String[] getRecommendationKey(String email) {
		String randomStr = UUID.randomUUID().toString();
		long time = 10 * 24 * 60 * 60 * 1000L;
		Timestamp outDate = new Timestamp(System.currentTimeMillis() + time);
		long recommendationTimeout = outDate.getTime();
		String str = email + "$" + recommendationTimeout + "$" + randomStr;
		String recommendationKey = Security.digest(str, Security.SHA256);
		if (recommendationKey != null) {
			return new String[] { recommendationKey, String.valueOf(recommendationTimeout) };
		}
		return null;
	}

	public int removeRecommender(int userID, String applicationID) {
		String table = ProgramCode.TABLE_APPLICATION_DETAIL.get(ProgramCode.TRANS);

		boolean alreadySubmitted = false;
		String sql = "SELECT recommender_submit_date FROM " + TABLE_APPLICATION_LIST
				+ " WHERE user_id = ? AND application_id = ?";
		try {
			alreadySubmitted = jdbcTemplate.query(sql, new Object[] { userID, applicationID },
					new ResultSetExtractor<Boolean>() {
						@Override
						public Boolean extractData(ResultSet reSet) throws SQLException, DataAccessException {
							if (reSet.next()) {
								if (reSet.getString("recommender_submit_date") != null) {
									return true;
								}
							}
							return false;
						}
					});
		} catch (EmptyResultDataAccessException e) {
			alreadySubmitted = false;
		}

		if (alreadySubmitted) {
			return -1;
		}

		return jdbcTemplate.update(new PreparedStatementCreator() {
			@Override
			public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
				String s = "UPDATE " + table
						+ " SET recommender_first_name=?,recommender_last_name=?,recommender_prefix=?,"
						+ "recommender_email=?,recommender_institution=?,recommender_phone=?,"
						+ "recommender_address_line1=?,recommender_address_line2=?,recommender_address_city=?,"
						+ "recommender_address_state=?,recommender_address_zip=?,recommender_address_country=?,"
						+ "recommendation_key=?,recommendation_timeout=?" + " WHERE user_id = ? AND application_id = ?";
				PreparedStatement query = con.prepareStatement(s);
				int index = 1;
				for (int i = 1; i <= 13; i++) {
					query.setNull(index++, java.sql.Types.VARCHAR);
				}
				query.setNull(index++, java.sql.Types.BIGINT);
				query.setInt(index++, userID);
				query.setString(index++, applicationID);
				return query;
			}
		});

	}

	public RecommenderBean resendRecommender(int userID, String applicationID) {
		String table = ProgramCode.TABLE_APPLICATION_DETAIL.get(ProgramCode.TRANS);

		boolean alreadySubmitted = false;
		String sql = "SELECT recommender_submit_date FROM " + TABLE_APPLICATION_LIST
				+ " WHERE user_id = ? AND application_id = ?";
		try {
			alreadySubmitted = jdbcTemplate.query(sql, new Object[] { userID, applicationID },
					new ResultSetExtractor<Boolean>() {
						@Override
						public Boolean extractData(ResultSet reSet) throws SQLException, DataAccessException {
							if (reSet.next()) {
								if (reSet.getString("recommender_submit_date") != null) {
									return true;
								}
							}
							return false;
						}
					});
		} catch (EmptyResultDataAccessException e) {
			alreadySubmitted = false;
		}

		if (alreadySubmitted) {
			return null;
		}

		sql = "SELECT recommender_first_name,recommender_last_name,recommender_email FROM " + table
				+ " WHERE user_id = ? AND application_id = ?";
		RecommenderBean recommenderBean = null;
		try {
			recommenderBean = jdbcTemplate.queryForObject(sql, new Object[] { userID, applicationID },
					new RowMapper<RecommenderBean>() {

						@Override
						public RecommenderBean mapRow(ResultSet reSet, int rowNum) throws SQLException {
							RecommenderBean bean = new RecommenderBean();
							if (reSet.getString("recommender_email") == null)
								return null;
							bean.setEmail(reSet.getString("recommender_email"));
							bean.setFirstName(reSet.getString("recommender_first_name"));
							bean.setLastName(reSet.getString("recommender_last_name"));
							return bean;
						}

					});
		} catch (EmptyResultDataAccessException e) {
			return null;
		}

		if (recommenderBean == null)
			return null;

		String[] key = getRecommendationKey(recommenderBean.getEmail());
		if (key != null) {
			sql = "UPDATE " + table + " SET recommendation_key = ?, recommendation_timeout = ?"
					+ " WHERE user_id = ? AND application_id = ?";
			int result = jdbcTemplate.update(sql,
					new Object[] { key[0], Long.parseLong(key[1]), userID, applicationID });
			if (result != 0) {
				recommenderBean.setRecommendationKey(key[0]);
				recommenderBean.setRecommendationTimeout(Long.parseLong(key[1]));
			}
		}
		return recommenderBean;

	}

	public RecommenderBean getRecommendationBean(String recommendationKey) {
		String table = ProgramCode.TABLE_APPLICATION_DETAIL.get(ProgramCode.TRANS);
		if (recommendationKey == null || recommendationKey.length() == 0) {
			return null;
		}

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT a.recommender_first_name,a.recommender_last_name,"
				+ "a.recommender_email,a.recommender_institution,a.recommendation_timeout,"
				+ "b.user_id,b.application_id,b.school_year,b.school_semester\n");

		sql.append("FROM ").append(table).append(" a \n");
		sql.append("LEFT JOIN ").append(TABLE_APPLICATION_LIST).append(" b\n");
		sql.append("ON a.application_id = b.application_id\n");
		sql.append("WHERE a.recommendation_key=?");
		try {
			return jdbcTemplate.queryForObject(sql.toString(), new Object[] { recommendationKey },
					new RowMapper<RecommenderBean>() {

						@Override
						public RecommenderBean mapRow(ResultSet reSet, int rowNum) throws SQLException {
							// check if expired
							long recommendationTimeout = reSet.getLong("recommendation_timeout");

							if (System.currentTimeMillis() > recommendationTimeout) {
								return null;
							}
							RecommenderBean bean = new RecommenderBean();
							int userID = reSet.getInt("user_id");
							bean.setUserID(userID);
							bean.setApplicationID(reSet.getInt("application_id"));
							bean.setSchoolYear(String.valueOf(reSet.getInt("school_year")));
							bean.setSchoolSemester(reSet.getString("school_semester"));
							bean.setFirstName(reSet.getString("recommender_first_name"));
							bean.setLastName(reSet.getString("recommender_last_name"));
							bean.setEmail(reSet.getString("recommender_email"));
							bean.setInstitution(reSet.getString("recommender_institution"));
							return bean;
						}

					});
		} catch (EmptyResultDataAccessException e) {
			return null;
		}

	}

	public int uploadRecommandation(String recommendationKey, FileBucket fileBucket) {
		String table = ProgramCode.TABLE_APPLICATION_DETAIL.get(ProgramCode.TRANS);
		String sql = "UPDATE " + table + " SET recommendation_file_name = ?, recommendation_file_type = ?,"
				+ "recommendation_file_size=?,recommendation_file_content=?" + " WHERE recommendation_key = ?";

		MultipartFile file = fileBucket.getFile();
		int result = jdbcTemplate.update(sql, new PreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps) throws SQLException {

				ps.setString(1, file.getOriginalFilename());
				ps.setString(2, file.getContentType());
				ps.setLong(3, file.getSize());
				try {
					ps.setBytes(4, file.getBytes());
				} catch (IOException e) {
					return;
				}
				ps.setString(5, recommendationKey);
			}
		});
		if (result == 0)
			return 0;

		// set recommender_submit_date in tableAppList
		// in recommendation upload: check submission of application
		// if so, mark application as complete
		StringBuilder sqlSelect = new StringBuilder();
		sqlSelect.append("SELECT a.application_id,b.applicant_submit_date\n");
		sqlSelect.append("FROM ").append(table).append(" a \n");
		sqlSelect.append("LEFT JOIN ").append(TABLE_APPLICATION_LIST).append(" b\n");
		sqlSelect.append("ON a.application_id = b.application_id\n");
		sqlSelect.append("WHERE a.recommendation_key=?");
		ApplicationBean applicationBean = null;
		try {
			applicationBean = jdbcTemplate.queryForObject(sqlSelect.toString(), new Object[] { recommendationKey },
					new RowMapper<ApplicationBean>() {

						@Override
						public ApplicationBean mapRow(ResultSet reSet, int rowNum) throws SQLException {
							ApplicationBean bean = new ApplicationBean();
							bean.setApplicationID(reSet.getInt("application_id"));
							bean.setApplicantSubmitDate(reSet.getDate("applicant_submit_date"));
							return bean;
						}
					});
		} catch (EmptyResultDataAccessException e) {
			applicationBean = null;
		}

		if (applicationBean == null)
			return 0;

		if (applicationBean.getApplicantSubmitDate() != null) {
			sql = "UPDATE " + TABLE_APPLICATION_LIST
					+ " SET recommender_submit_date=?,complete_date=? WHERE application_id = ?";
			return jdbcTemplate.update(sql,
					new Object[] { new Date(), new Date(), applicationBean.getApplicationID() });
		} else {
			sql = "UPDATE " + TABLE_APPLICATION_LIST + " SET recommender_submit_date=? WHERE application_id = ?";
			return jdbcTemplate.update(sql, new Object[] { new Date(), applicationBean.getApplicationID() });
		}

	}

	public boolean hasRecommenderSubmitted(int userID, String applicationID) {
		String sql = "SELECT recommender_submit_date FROM " + TABLE_APPLICATION_LIST
				+ " WHERE application_id = ? AND user_id = ?";
		try {
			return jdbcTemplate.query(sql, new Object[] { applicationID, userID }, new ResultSetExtractor<Boolean>() {
				@Override
				public Boolean extractData(ResultSet reSet) throws SQLException, DataAccessException {
					if (reSet.next()) {
						if (reSet.getString("recommender_submit_date") != null) {
							return true;
						}
					}
					return false;
				}
			});

		} catch (EmptyResultDataAccessException e) {
			return false;
		}

	}

	/**
	 * Check completion of application
	 */
	public boolean isApplicationCompleteByCompleteDate(int menteeID, int applicationID) {
		String sql = "SELECT complete_date FROM " + TABLE_APPLICATION_LIST
				+ " WHERE application_id = ? AND user_id = ?";
		try {
			return jdbcTemplate.query(sql, new Object[] { applicationID, menteeID }, new ResultSetExtractor<Boolean>() {
				@Override
				public Boolean extractData(ResultSet reSet) throws SQLException, DataAccessException {
					if (reSet.next()) {
						if (reSet.getString("complete_date") != null) {
							return true;
						}
					}
					return false;
				}
			});

		} catch (EmptyResultDataAccessException e) {
			return false;
		}
	}

	public boolean isApplicationIComplete(int userID, String applicationID, String program) {
		String table = ProgramCode.TABLE_APPLICATION_DETAIL.get(program);

		StringBuilder sql = new StringBuilder();
		Object[] params = new Object[] { userID, applicationID };
		switch (program) {
		// programsTwoYr
		case ProgramCode.CCCONF:
			sql.append(
					"SELECT academic_school,academic_major,amp_scholarship,essay_educational_goal,transcript_name \n");
			sql.append("FROM ").append(table).append(" \n");
			sql.append("WHERE user_id = ? AND application_id=?");
			return isApplicationICompleteCCCONF(params, sql);
		case ProgramCode.SCCORE:
			sql.append(
					"SELECT academic_school,academic_major,amp_scholarship,essay_educational_goal,transcript_name,medical_name \n");
			sql.append("FROM ").append(table).append(" \n");
			sql.append("WHERE user_id = ? AND application_id=?");
			return isApplicationICompleteSCCORE(params, sql);

		case ProgramCode.MESA:
			sql.append(
					"SELECT academic_school,academic_grad_date,amp_scholarship,essay_profesional_goal,transcript_name,reference_name \n");
			sql.append("FROM ").append(table).append(" \n");
			sql.append("WHERE user_id = ? AND application_id=?");
			return isApplicationICompleteMESA(params, sql);

		case ProgramCode.TRANS: // DO NOT check recommendation info
			sql.append(
					"SELECT academic_school,academic_grad_date,amp_scholarship,essay_profesional_goal,transcript_name \n");
			sql.append("FROM ").append(table).append(" \n");
			sql.append("WHERE user_id = ? AND application_id=?");
			return isApplicationICompleteTRANS(params, sql);

		// programsFourYr
		case ProgramCode.URS:
			sql.append(
					"SELECT academic_school,academic_grad_date,amp_scholarship,transcript_name,mentor_email,essay_educational_goal \n");
			sql.append("FROM ").append(table).append(" \n");
			sql.append("WHERE user_id = ? AND application_id=?");
			return isApplicationICompleteURS(params, sql);
		case ProgramCode.IREP:
			sql.append(
					"SELECT academic_school,academic_grad_date,mentor_email,transcript_name,budget_total_domestictravel \n");
			sql.append("FROM ").append(table).append(" \n");
			sql.append("WHERE user_id = ? AND application_id=?");
			return isApplicationICompleteIREP(params, sql);
		}

		return false;
	}

	private boolean isApplicationICompleteCCCONF(Object[] params, StringBuilder sql) {
		try {
			return jdbcTemplate.query(sql.toString(), params, new ResultSetExtractor<Boolean>() {
				@Override
				public Boolean extractData(ResultSet reSet) throws SQLException, DataAccessException {
					if (reSet.next()) {
						String academicInfo = reSet.getString("academic_school");
						String invInfo = reSet.getString("amp_scholarship");
						String essayInfo = reSet.getString("essay_educational_goal");
						String transcriptInfo = reSet.getString("transcript_name");
						if (academicInfo != null && reSet.getString("academic_major") != null && invInfo != null
								&& transcriptInfo != null && essayInfo != null) {
							return true;
						}
					}
					return false;
				}
			});

		} catch (EmptyResultDataAccessException e) {
			return false;
		}
	}

	private boolean isApplicationICompleteSCCORE(Object[] params, StringBuilder sql) {
		try {
			return jdbcTemplate.query(sql.toString(), params, new ResultSetExtractor<Boolean>() {
				@Override
				public Boolean extractData(ResultSet reSet) throws SQLException, DataAccessException {
					if (reSet.next()) {
						String academicInfo = reSet.getString("academic_school");
						String invInfo = reSet.getString("amp_scholarship");
						String essayInfo = reSet.getString("essay_educational_goal");
						String transcriptInfo = reSet.getString("transcript_name");
						String medicalInfo = reSet.getString("medical_name");
						if (academicInfo != null && reSet.getString("academic_major") != null && invInfo != null
								&& transcriptInfo != null && essayInfo != null && medicalInfo != null) {
							return true;
						}
					}
					return false;
				}
			});

		} catch (EmptyResultDataAccessException e) {
			return false;
		}
	}

	private boolean isApplicationICompleteMESA(Object[] params, StringBuilder sql) {
		try {
			return jdbcTemplate.query(sql.toString(), params, new ResultSetExtractor<Boolean>() {
				@Override
				public Boolean extractData(ResultSet reSet) throws SQLException, DataAccessException {
					if (reSet.next()) {
						String academicInfo = reSet.getString("academic_school");
						String invInfo = reSet.getString("amp_scholarship");
						String essayInfo = reSet.getString("essay_profesional_goal");
						String transcriptInfo = reSet.getString("transcript_name");
						String referenceInfo = reSet.getString("reference_name");
						if (academicInfo != null && reSet.getDate("academic_grad_date") != null && invInfo != null
								&& transcriptInfo != null && essayInfo != null & referenceInfo != null) {
							return true;
						}
					}
					return false;
				}
			});

		} catch (EmptyResultDataAccessException e) {
			return false;
		}
	}

	private boolean isApplicationICompleteTRANS(Object[] params, StringBuilder sql) {
		try {
			return jdbcTemplate.query(sql.toString(), params, new ResultSetExtractor<Boolean>() {
				@Override
				public Boolean extractData(ResultSet reSet) throws SQLException, DataAccessException {
					if (reSet.next()) {
						String academicInfo = reSet.getString("academic_school");
						String invInfo = reSet.getString("amp_scholarship");
						String essayInfo = reSet.getString("essay_profesional_goal");
						String transcriptInfo = reSet.getString("transcript_name");
						if (academicInfo != null && reSet.getDate("academic_grad_date") != null && invInfo != null
								&& transcriptInfo != null && essayInfo != null) {
							return true;
						}
					}
					return false;
				}
			});

		} catch (EmptyResultDataAccessException e) {
			return false;
		}
	}

	private boolean isApplicationICompleteURS(Object[] params, StringBuilder sql) {
		try {
			return jdbcTemplate.query(sql.toString(), params, new ResultSetExtractor<Boolean>() {
				@Override
				public Boolean extractData(ResultSet reSet) throws SQLException, DataAccessException {
					if (reSet.next()) {
						String academicInfo = reSet.getString("academic_school");
						String invInfo = reSet.getString("amp_scholarship");
						String transcriptInfo = reSet.getString("transcript_name");
						String mentorInfo = reSet.getString("mentor_email");
						String essayInfo = reSet.getString("essay_educational_goal");
						if (academicInfo != null && reSet.getDate("academic_grad_date") != null && invInfo != null
								&& transcriptInfo != null && mentorInfo != null && essayInfo != null) {
							return true;
						}
					}
					return false;
				}
			});

		} catch (EmptyResultDataAccessException e) {
			return false;
		}
	}

	private boolean isApplicationICompleteIREP(Object[] params, StringBuilder sql) {
		try {
			return jdbcTemplate.query(sql.toString(), params, new ResultSetExtractor<Boolean>() {
				@Override
				public Boolean extractData(ResultSet reSet) throws SQLException, DataAccessException {
					if (reSet.next()) {
						String academicInfo = reSet.getString("academic_school");
						String mentorInfo = reSet.getString("mentor_email");
						String transcriptInfo = reSet.getString("transcript_name");
						String budgetInfo = reSet.getString("budget_total_domestictravel");
						if (academicInfo != null && reSet.getDate("academic_grad_date") != null && budgetInfo != null
								&& transcriptInfo != null && mentorInfo != null) {
							return true;
						}
					}
					return false;
				}
			});

		} catch (EmptyResultDataAccessException e) {
			return false;
		}
	}

	public boolean isApplicationIICompleteURS(int userID, String applicationID) {
		String sql = "SELECT mentor_submit_date FROM " + TABLE_APPLICATION_LIST
				+ " WHERE application_id = ? AND user_id = ?";
		try {
			return jdbcTemplate.query(sql, new Object[] { applicationID, userID }, new ResultSetExtractor<Boolean>() {
				@Override
				public Boolean extractData(ResultSet reSet) throws SQLException, DataAccessException {
					if (reSet.next()) {
						return reSet.getString("mentor_submit_date") != null;
					}
					return false;
				}
			});

		} catch (EmptyResultDataAccessException e) {
			return false;
		}
	}

	/************************
	 * Student Evaluations
	 ************************/
	public List<EvaluationBean> getEvaluations(int menteeID) {

		StringBuilder sql = new StringBuilder();
		sql.append(
				"(SELECT application_id,eval_year,eval_semester,deadline,submit_date,notified_date,'Mid-Term' as eval_point,b.first_name,b.last_name \n");
		sql.append("FROM ").append(TABLE_EVAL_MIDTERM_STU).append(" AS a\n").append("LEFT JOIN ")
				.append(TABLE_PROFILE_STUDENT).append(" AS b\n").append("ON a.mentee_id = b.user_id")
				.append(" WHERE a.mentee_id=? )\n");
		return jdbcTemplate.query(sql.toString(), new Object[] { menteeID }, new RowMapper<EvaluationBean>() {
			@Override
			public EvaluationBean mapRow(ResultSet reSet, int rowNum) throws SQLException {
				EvaluationBean bean = new EvaluationBean();
				bean.setApplicationID(reSet.getInt("application_id"));
				bean.setEvalPoint(reSet.getString("eval_point"));
				bean.setEvalYear(reSet.getInt("eval_year"));
				bean.setEvalTerm(reSet.getString("eval_semester"));
				bean.setSubmitDate(reSet.getDate("submit_date"));
				bean.setEvalDeadline(Parse.tryParseDateYMD(reSet.getString("deadline")));
				bean.setMenteeName(reSet.getString("first_name") + " " + reSet.getString("last_name"));
				return bean;
			}
		});
	}

	public int setEvaluationMidTerm(String applicationID, String evalYear, String evalSemester,
			HashMap<String, Object> map) {
		StringBuilder sql = new StringBuilder();
		sql.append("UPDATE ").append(TABLE_EVAL_MIDTERM_STU)
				.append(" SET question_1=?,question_2=?,question_3=?,submit_date=? ")
				.append("WHERE application_id = ? AND eval_year = ? AND eval_semester = ? ");

		return jdbcTemplate.update(sql.toString(), new Object[] { map.get("quest-1"), map.get("quest-2"),
				map.get("quest-3"), new Date(), applicationID, evalYear, evalSemester });
	}

	/************************* add by qixu ***************************/
	/****************************************************
	 * Batch Update Application Decision
	 * 
	 * @return *
	 ****************************************************/
	public int UpdateSingleApplicationDecision(String program, String appIDs, String decision, String schoolTarget) {
		String sql = "UPDATE " + TABLE_APPLICATION_LIST + " SET decision=?, accept_school = ?, accept_status = ? "
				+ "WHERE application_id = ? AND program = ?";
		if (decision.equalsIgnoreCase("withdrew")) {
//			System.out.println(
//					"Update sql \n" + sql + "\n" + program + " " + appIDs + " " + decision + " " + schoolTarget);
			try {
				return jdbcTemplate.update(sql, decision, null, 2, appIDs, program);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return 0;

		// jdbcTemplate.update(sql, new BatchPreparedStatementSetter() {
		//
		// @Override
		// public void setValues(PreparedStatement ps, int i) throws SQLException {
		// if (decision == null || decision.length() < 1) {
		// ps.setNull(1, java.sql.Types.VARCHAR);
		// ps.setNull(2, java.sql.Types.VARCHAR);
		// ps.setInt(3, 0);
		// } else {
		// ps.setString(1, decision);
		// if (decision.equalsIgnoreCase("admit")) {
		// ps.setString(2, schoolTarget);
		// ps.setInt(3, 1);
		// } else if (decision.equalsIgnoreCase("withdrew")) {
		// ps.setNull(2, java.sql.Types.VARCHAR);
		// ps.setInt(3, 2);
		// } else {
		// ps.setNull(2, java.sql.Types.VARCHAR);
		// ps.setInt(3, 0);
		// }
		// }
		// ps.setInt(4, Integer.parseInt(appIDs));
		// ps.setString(5, program);
		// }
		//
		// @Override
		// public int getBatchSize() {
		// return 1;
		// }
		// });

	}

}
