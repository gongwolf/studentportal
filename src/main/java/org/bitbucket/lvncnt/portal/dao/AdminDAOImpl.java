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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

@Repository("adminDAO")
public class AdminDAOImpl implements Schemacode {

	@Autowired
	private TransactionTemplate transactionTemplate;
	@Autowired
	private JdbcTemplate jdbcTemplate;

	static {
		TimeZone.setDefault(TimeZone.getTimeZone("America/Denver"));
	}

	/****************************************************
	 * Manage Program *
	 ****************************************************/
	public int setApplicationWindow(int windowID, String program, ProgramBean programBean) {

		StringBuilder sql = new StringBuilder();
		sql.append("UPDATE ").append(TABLE_PROGRAM).append(" SET start=?,end=? WHERE window_id = ? AND program=?");

		String startDate = Parse.FORMAT_DATETIME.format(programBean.getStartDate());
		startDate = startDate.split("\\s+")[0] + " 00:00:00";
		String endDate = Parse.FORMAT_DATETIME.format(programBean.getEndDate());
		endDate = endDate.split("\\s+")[0] + " 23:59:59";
		int result = 0;
		result = jdbcTemplate.update(sql.toString(), new Object[] { startDate, endDate, windowID, program });

		return result;
	}

	public List<ProgramBean> getApplicationWindows() {
		String sql = "SELECT * FROM " + TABLE_PROGRAM;

		return jdbcTemplate.query(sql, new RowMapper<ProgramBean>() {
			@Override
			public ProgramBean mapRow(ResultSet reSet, int rowNum) throws SQLException {

				ProgramBean programBean = new ProgramBean();
				programBean.setWindowID(reSet.getInt("window_id"));
				String name = reSet.getString("program");
				programBean.setProgramNameAbbr(name);
				programBean.setProgramName(ProgramCode.PROGRAMS.get(name));

				programBean.setSemester(reSet.getString("semester"));
				programBean.setYear(String.valueOf(reSet.getInt("year")));
				try {
					programBean.setStartDate(Parse.FORMAT_DATETIME.parse(reSet.getString("start")));
					programBean.setEndDate(Parse.FORMAT_DATETIME.parse(reSet.getString("end")));
				} catch (ParseException e) {
					e.printStackTrace();
				}
				return programBean;
			}
		});

	}

	public ProgramBean getApplicationWindow(int windowID, String program) {
		String sql = "SELECT * FROM " + TABLE_PROGRAM + " WHERE window_id = ? AND program = ?";

		try {
			ProgramBean programBean = jdbcTemplate.queryForObject(sql, new Object[] { windowID, program },
					new RowMapper<ProgramBean>() {
						@Override
						public ProgramBean mapRow(ResultSet reSet, int rowNum) throws SQLException {
							ProgramBean programBean = new ProgramBean();
							programBean.setWindowID(reSet.getInt("window_id"));
							String name = reSet.getString("program");
							programBean.setProgramNameAbbr(name);
							programBean.setProgramName(ProgramCode.PROGRAMS.get(name));

							programBean.setSemester(reSet.getString("semester"));
							programBean.setYear(String.valueOf(reSet.getInt("year")));
							try {
								programBean.setStartDate(Parse.FORMAT_DATETIME.parse(reSet.getString("start")));
								programBean.setEndDate(Parse.FORMAT_DATETIME.parse(reSet.getString("end")));
							} catch (ParseException e) {
								e.printStackTrace();
							}
							return programBean;
						}
					});
			return programBean;
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	public ProgramBean getApplicationWindow(String program, String semester, String year) {
		String sql = "SELECT * FROM " + TABLE_PROGRAM + " WHERE program = ? AND semester = ? AND year = ?";

		try {
			ProgramBean programBean = jdbcTemplate.queryForObject(sql, new Object[] { program, semester, year },
					new RowMapper<ProgramBean>() {
						@Override
						public ProgramBean mapRow(ResultSet reSet, int rowNum) throws SQLException {
							ProgramBean programBean = new ProgramBean();
							programBean.setSemester(reSet.getString("semester"));
							programBean.setYear(String.valueOf(reSet.getInt("year")));
							try {
								programBean.setStartDate(Parse.FORMAT_DATETIME.parse(reSet.getString("start")));
								programBean.setEndDate(Parse.FORMAT_DATETIME.parse(reSet.getString("end")));
							} catch (ParseException e) {
								e.printStackTrace();
							}
							return programBean;
						}
					});
			return programBean;
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	public int addApplicationWindow(ProgramBean programBean) {

		int windowID = 0;
		try {
			windowID = jdbcTemplate.queryForObject(
					"SELECT window_id FROM " + TABLE_PROGRAM + " WHERE program = ? AND year = ? AND semester = ?",
					new Object[] { programBean.getProgramNameAbbr(), programBean.getYear(), programBean.getSemester() },
					Integer.class);
		} catch (EmptyResultDataAccessException e) {
			// insert
			String startDate = Parse.FORMAT_DATETIME.format(programBean.getStartDate());
			startDate = startDate.split("\\s+")[0] + " 00:00:00";
			String endDate = Parse.FORMAT_DATETIME.format(programBean.getEndDate());
			endDate = endDate.split("\\s+")[0] + " 23:59:59";
			String sql;
			Object[] params;
			sql = "INSERT INTO " + TABLE_PROGRAM + " (program, year, semester, start, end)" + " VALUES (?, ?, ?, ?, ?)";
			params = new Object[] { programBean.getProgramNameAbbr(), programBean.getYear(), programBean.getSemester(),
					startDate, endDate };
			return jdbcTemplate.update(sql, params);
		}

		return windowID;
	}

	public int deleteApplicationWindow(int windowID, String program) {
		return jdbcTemplate.update("DELETE from " + TABLE_PROGRAM + " WHERE window_id=? AND program = ?",
				new Object[] { windowID, program });
	}

	// get latest deadline for each application
	public Map<String, List<ProgramBean>> getProgramDeadlines() {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT * FROM ").append(TABLE_PROGRAM).append(" AS p1 ")
				.append("where p1.start = (select max(p2.start) from ").append(TABLE_PROGRAM).append(" AS p2 ")
				.append("where p1.program = p2.program)");
		return jdbcTemplate.query(sql.toString(), new ResultSetExtractor<Map<String, List<ProgramBean>>>() {
			@Override
			public Map<String, List<ProgramBean>> extractData(ResultSet rs) throws SQLException, DataAccessException {

				Map<String, List<ProgramBean>> mapSet = new HashMap<>();
				while (rs.next()) {
					String name = rs.getString("program");
					ProgramBean bean = new ProgramBean();
					bean.setWindowID(rs.getInt("window_id"));
					bean.setSemester(rs.getString("semester"));
					bean.setYear(String.valueOf(rs.getInt("year")));
					try {
						bean.setStartDate(Parse.FORMAT_DATETIME.parse(rs.getString("start")));
						bean.setEndDate(Parse.FORMAT_DATETIME.parse(rs.getString("end")));
					} catch (ParseException e) {
						e.printStackTrace();
					}
					mapSet.computeIfAbsent(name, List -> new ArrayList<>()).add(bean);
				}
				return mapSet;
			}
		});
	}

	/****************************************************
	 * Search Applicant *
	 ****************************************************/
	public List<ApplicationBean> getApplicantsByProgram(String program) {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT user_id, first_name, last_name, birth_date, email \n");

		sql.append("FROM ").append(TABLE_PROFILE_STUDENT).append(" \n");
		sql.append("WHERE user_id in (SELECT user_id FROM ").append(TABLE_APPLICATION_LIST);

		Object[] params;
		if (program != null && program.length() > 1) {
			sql.append(" WHERE program=?)\n");
			params = new Object[] { program };
		} else {
			sql.append(" )\n");
			params = new Object[] {};
		}

		return jdbcTemplate.query(sql.toString(), params, new RowMapper<ApplicationBean>() {

			@Override
			public ApplicationBean mapRow(ResultSet reSet, int rowNum) throws SQLException {
				ApplicationBean applicationBean = new ApplicationBean();
				applicationBean.setUserID(reSet.getInt("user_id"));
				applicationBean.setFirstName(reSet.getString("first_name"));
				applicationBean.setLastName(reSet.getString("last_name"));
				Date date = null;
				String birthDate = reSet.getString("birth_date");
				if (birthDate != null) {
					try {
						date = Parse.FORMAT_DATE_YMD.parse(birthDate);
						birthDate = Parse.FORMAT_DATE_MDY.format(date);
						applicationBean.setBirthDate(Parse.FORMAT_DATE_MDY.parse(birthDate));
					} catch (ParseException e) {
						e.printStackTrace();
					}
				}

				applicationBean.setEmail(reSet.getString("email"));
				return applicationBean;
			}
		});
	}

	public List<ApplicationBean> getApplicantsByPerson(String firstName, String lastName, String birthDate,
			String email) {

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT user_id, first_name, last_name, birth_date, email \n");
		sql.append("FROM ").append(TABLE_PROFILE_STUDENT).append(" \n");

		if (!firstName.isEmpty() || !lastName.isEmpty() || !birthDate.isEmpty() || !email.isEmpty()) {

			sql.append("WHERE");

			if (!firstName.isEmpty()) {
				sql.append(" first_name LIKE '%" + firstName + "%' AND");
			}

			if (!lastName.isEmpty()) {
				sql.append(" last_name LIKE '%" + lastName + "%' AND");
			}

			if (!email.isEmpty()) {
				sql.append(" email LIKE '%" + email + "%' AND");
			}

			if (!birthDate.isEmpty()) {
				sql.append(" DATE_FORMAT(birth_date, '%m/%d/%Y')='" + birthDate + "' AND");
			}

			sql.delete(sql.length() - 3, sql.length());
		}

		return jdbcTemplate.query(sql.toString(), new RowMapper<ApplicationBean>() {

			@Override
			public ApplicationBean mapRow(ResultSet reSet, int rowNum) throws SQLException {
				ApplicationBean applicationBean = new ApplicationBean();
				applicationBean.setUserID(reSet.getInt("user_id"));
				applicationBean.setFirstName(reSet.getString("first_name"));
				applicationBean.setLastName(reSet.getString("last_name"));
				Date date = null;
				String birthDate = reSet.getString("birth_date");
				if (birthDate != null) {
					try {
						date = Parse.FORMAT_DATE_YMD.parse(birthDate);
						birthDate = Parse.FORMAT_DATE_MDY.format(date);
						applicationBean.setBirthDate(Parse.FORMAT_DATE_MDY.parse(birthDate));
					} catch (ParseException e) {
						e.printStackTrace();
					}
				}
				applicationBean.setEmail(reSet.getString("email"));

				return applicationBean;
			}
		});
	}

	public List<ProfileBean> getStudentProfile(String[] userIDList) {
		List<String> ids = new ArrayList<>(Arrays.asList(userIDList));
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT  * \n");
		sql.append("FROM ").append(TABLE_PROFILE_STUDENT).append(" \n");
		sql.append("WHERE user_id IN (");
		sql.append(ids.stream().collect(Collectors.joining(",")));
		sql.append(")\n");

		return jdbcTemplate.query(sql.toString(), new ResultSetExtractor<List<ProfileBean>>() {

			@Override
			public List<ProfileBean> extractData(ResultSet reSet) throws SQLException, DataAccessException {
				List<ProfileBean> profileList = new ArrayList<>();
				while (reSet.next()) {
					ProfileBean bean = new ProfileBean();
					// biography info
					bean.setUserID(reSet.getInt("user_id"));
					BiographyBean biographyBean = new BiographyBean();
					biographyBean.setFirstName(reSet.getString("first_name"));
					biographyBean.setLastName(reSet.getString("last_name"));
					biographyBean.setMiddleName(reSet.getString("middle_name"));
					biographyBean.setBirthDate(reSet.getDate("birth_date"));

					if (reSet.getString("ssn_last_four") != null) {
						biographyBean.setSsnLastFour(reSet.getString("ssn_last_four"));
						if (!Parse.isBlankOrNull(reSet.getString("gender"))) {
							biographyBean.setGender(reSet.getString("gender"));
						}
						if (!Parse.isBlankOrNull(reSet.getString("is_nm_resident"))) {
							biographyBean.setIsNMResident(reSet.getInt("is_nm_resident"));
						}
						if (!Parse.isBlankOrNull(reSet.getString("citizenship"))) {
							biographyBean.setCitizenship(reSet.getString("citizenship"));
						}
						if (!Parse.isBlankOrNull(reSet.getString("parent_has_degree"))) {
							biographyBean.setParentHasDegree(reSet.getInt("parent_has_degree"));
						}
					}
					bean.setBiographyBean(biographyBean);

					// contact info
					ContactBean contactBean = null;
					if (reSet.getString("email") != null) {
						contactBean = new ContactBean();
						// Email and Phone
						contactBean.setEmailPref(reSet.getString("email"));
						contactBean.setEmailAltn(reSet.getString("email_alternate"));
						contactBean.setPhoneNum1(reSet.getString("phone_num1"));
						contactBean.setPhoneType1(reSet.getString("phone_type1"));
						contactBean.setPhoneNum2(reSet.getString("phone_num2"));
						contactBean.setPhoneType2(reSet.getString("phone_type2"));
						contactBean.setReceiveSMS(reSet.getInt("receive_sms"));

						// Permanent address
						contactBean.setPermanentAddressLine1(reSet.getString("permanent_address_line1"));
						contactBean.setPermanentAddressLine2(reSet.getString("permanent_address_line2"));
						contactBean.setPermanentAddressCity(reSet.getString("permanent_address_city"));
						contactBean.setPermanentAddressState(reSet.getString("permanent_address_state"));
						contactBean.setPermanentAddressZip(reSet.getString("permanent_address_zip"));
						contactBean.setPermanentAddressCounty(reSet.getString("permanent_address_county"));

						// Current Address
						contactBean.setCurrentAddressLine1(reSet.getString("current_address_line1"));
						contactBean.setCurrentAddressLine2(reSet.getString("current_address_line2"));
						contactBean.setCurrentAddressCity(reSet.getString("current_address_city"));
						contactBean.setCurrentAddressState(reSet.getString("current_address_state"));
						contactBean.setCurrentAddressZip(reSet.getString("current_address_zip"));
						contactBean.setCurrentAddressCounty(reSet.getString("current_address_county"));

						// Emergency Contact
						contactBean.setEcFirstName(reSet.getString("ec_first_name"));
						contactBean.setEcLastName(reSet.getString("ec_last_name"));
						contactBean.setEcRelationship(reSet.getString("ec_relationship"));
						contactBean.setEcPhoneNum1(reSet.getString("ec_phone_num1"));
						contactBean.setEcPhoneType1(reSet.getString("ec_phone_type1"));
						contactBean.setEcPhoneNum2(reSet.getString("ec_phone_num2"));
						contactBean.setEcPhoneType2(reSet.getString("ec_phone_type2"));
						contactBean.setEcAddressLine1(reSet.getString("ec_address_line1"));
						contactBean.setEcAddressLine2(reSet.getString("ec_address_line2"));
						contactBean.setEcAddressCity(reSet.getString("ec_address_city"));
						contactBean.setEcAddressState(reSet.getString("ec_address_state"));
						contactBean.setEcAddressZip(reSet.getString("ec_address_zip"));
						contactBean.setEcAddressCounty(reSet.getString("ec_address_county"));
						bean.setContactBean(contactBean);
					}

					// Ethnicity
					EthnicityBean ethnicityBean = null;
					String isHispanic = reSet.getString("is_hispanic");
					if (isHispanic != null) {
						ethnicityBean = new EthnicityBean();
						ethnicityBean.setIsHispanic(Integer.parseInt(isHispanic));
						ethnicityBean.setDisability(reSet.getString("disability"));
						String race = reSet.getString("race");
						if (race != null) {
							ethnicityBean.setRace(Arrays.asList(race.trim().split("\\s*,\\s*")));
						}
						bean.setEthnicityBean(ethnicityBean);
					}

					profileList.add(bean);
				}
				return profileList;
			}
		});
	}

	/****************************************************
	 * Manage Selfreport *
	 ****************************************************/
	public List<SelfReportBean> getSelfreportWindows(boolean isActive) {

		String sql = "SELECT * FROM " + TABLE_SELFREPORT_MANAGE;

		if (isActive) {
			sql += " WHERE start <= curdate() AND end >= curdate()";
		}

		return jdbcTemplate.query(sql, new RowMapper<SelfReportBean>() {
			@Override
			public SelfReportBean mapRow(ResultSet reSet, int rowNum) throws SQLException {

				SelfReportBean selfReportBean = new SelfReportBean();
				selfReportBean.setWindowID(reSet.getInt("window_id"));
				selfReportBean.setSemester(reSet.getString("semester"));

				try {
					Date date = null;
					String start = reSet.getString("start");
					if (start != null) {
						date = Parse.FORMAT_DATETIME.parse(start);
						start = Parse.FORMAT_DATE_MDY.format(date);
						selfReportBean.setStartDate(Parse.FORMAT_DATE_MDY.parse(start));
					}

					String end = reSet.getString("end");
					if (end != null) {
						date = Parse.FORMAT_DATETIME.parse(end);
						end = Parse.FORMAT_DATE_MDY.format(date);
						selfReportBean.setEndDate(Parse.FORMAT_DATE_MDY.parse(end));
					}

				} catch (ParseException e) {
					e.printStackTrace();
				}
				return selfReportBean;
			}
		});
	}

	public SelfReportBean getSelfreportWindow(int windowID) {
		String sql = "SELECT * FROM " + TABLE_SELFREPORT_MANAGE + " WHERE window_id = ?";

		try {
			SelfReportBean selfReportBean = jdbcTemplate.queryForObject(sql, new Object[] { windowID },
					new RowMapper<SelfReportBean>() {
						@Override
						public SelfReportBean mapRow(ResultSet reSet, int rowNum) throws SQLException {

							SelfReportBean selfReportBean = new SelfReportBean();
							selfReportBean.setWindowID(reSet.getInt("window_id"));
							selfReportBean.setSemester(reSet.getString("semester"));

							try {
								Date date = null;
								String start = reSet.getString("start");
								if (start != null) {
									date = Parse.FORMAT_DATETIME.parse(start);
									start = Parse.FORMAT_DATE_MDY.format(date);
									selfReportBean.setStartDate(Parse.FORMAT_DATE_MDY.parse(start));
								}

								String end = reSet.getString("end");
								if (end != null) {
									date = Parse.FORMAT_DATETIME.parse(end);
									end = Parse.FORMAT_DATE_MDY.format(date);
									selfReportBean.setEndDate(Parse.FORMAT_DATE_MDY.parse(end));
								}

							} catch (ParseException e) {
								e.printStackTrace();
							}

							return selfReportBean;
						}
					});
			return selfReportBean;
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	public int addSelfreportWindow(SelfReportBean selfReportBean) {
		try {
			jdbcTemplate.queryForObject("SELECT window_id FROM " + TABLE_SELFREPORT_MANAGE + " WHERE semester = ?",
					new Object[] { selfReportBean.getSemester() }, Integer.class);
		} catch (EmptyResultDataAccessException e) {
			// insert
			String sql = "INSERT INTO " + TABLE_SELFREPORT_MANAGE + " (semester, start, end)" + " VALUES (?, ?, ?)";
			return jdbcTemplate.update(sql, new Object[] { selfReportBean.getSemester(), selfReportBean.getStartDate(),
					selfReportBean.getEndDate() });
		}
		return 0;
	}

	public int deleteSelfreportWindow(int windowID) {
		return jdbcTemplate.update("DELETE from " + TABLE_SELFREPORT_MANAGE + " WHERE window_id=?",
				new Object[] { windowID });
	}

	public int setSelfreportWindow(int windowID, SelfReportBean selfReportBean) {
		StringBuilder sql = new StringBuilder();
		sql.append("UPDATE ").append(TABLE_SELFREPORT_MANAGE).append(" SET start=?,end=? ")
				.append("WHERE window_id=? AND semester=?");

		return jdbcTemplate.update(sql.toString(), new Object[] { selfReportBean.getStartDate(),
				selfReportBean.getEndDate(), windowID, selfReportBean.getSemester() });
	}

	public boolean isSelfreportWindowExist(int windowID, String semester) {
		try {
			String sql = "SELECT window_id FROM " + TABLE_SELFREPORT_MANAGE + " WHERE window_id = ? AND semester = ?";
			return jdbcTemplate.query(sql, new Object[] { windowID, semester }, new ResultSetExtractor<Boolean>() {
				@Override
				public Boolean extractData(ResultSet reSet) throws SQLException, DataAccessException {
					return reSet.next();
				}
			});

		} catch (EmptyResultDataAccessException e) {
			return false;
		}
	}

	/**
	 * Only select submitted selfreport
	 */
	public List<SelfReportBean> getSubmittedSelfReport(String windowID, String[] userIDs) {
		List<String> ids = new ArrayList<>(Arrays.asList(userIDs));
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT * \n");
		sql.append("FROM ").append(TABLE_SELFREPORT_DATA).append(" a \n");
		sql.append("LEFT JOIN ").append(TABLE_USER).append(" b\n");
		sql.append("ON a.user_id = b.user_id \n");
		sql.append("WHERE window_id = ? AND a.user_id IN (");
		sql.append(ids.stream().collect(Collectors.joining(",")));
		sql.append(")\n");

		return jdbcTemplate.query(sql.toString(), new Object[] { windowID }, new RowMapper<SelfReportBean>() {

			@Override
			public SelfReportBean mapRow(ResultSet reSet, int rowNum) throws SQLException {
				SelfReportBean selfReportBean = new SelfReportBean();
				selfReportBean.setSemester(reSet.getString("semester"));
				selfReportBean.setUserID(reSet.getInt("user_id"));
				selfReportBean.setEmail(reSet.getString("email"));

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

				ReportOthersBean reportOthersBean = new ReportOthersBean();
				reportOthersBean.setOtherActivities(reSet.getString("other_activities"));
				selfReportBean.setReportOthersBean(reportOthersBean);

				selfReportBean.setSubmitDate(reSet.getDate("submit_date"));
				return selfReportBean;
			}
		});
	}

	/****************************************************
	 * Manage Application (Select) *
	 ****************************************************/
	public List<ApplicationBean> getApplicationsBySchoolFromTo(String schoolFrom, String schoolTarget, int year,
			String semester, String program, boolean completedOnly) {

		// String institution = "NMSU";
		String tableAppDetail = ProgramCode.TABLE_APPLICATION_DETAIL.get(program);
		StringBuilder sql = new StringBuilder();
		Object[] params = null;
		/*
		 * if(schoolTarget.equalsIgnoreCase("ALL")){ params = new Object[]{program,
		 * year, semester}; }else{ params = new Object[]{program, year, semester,
		 * schoolTarget}; }
		 */

		switch (program) {
		// programsTwoYr
		case ProgramCode.CCCONF:
		case ProgramCode.MESA:
		case ProgramCode.TRANS:
			sql.append("SELECT a.*, b.academic_transfer_school FROM \n");
			sql.append("(SELECT * FROM ").append(TABLE_APPLICATION_LIST)
					.append(" WHERE program = ? AND school_year=? AND school_semester=?) AS a\n");
			sql.append("LEFT JOIN ").append(tableAppDetail).append(" b\n");
			sql.append("ON a.application_id = b.application_id\n");
			if (!schoolTarget.equalsIgnoreCase("ALL")) {
				sql.append("WHERE b.academic_transfer_school=? ");
				if (completedOnly) {
					sql.append("AND a.complete_date IS NOT NULL \n");
				} else {
					sql.append("\n");
				}
				if (!schoolFrom.equalsIgnoreCase("ALL")) {
					sql.append("AND b.academic_school=? \n");
					params = new Object[] { program, year, semester, schoolTarget, schoolFrom };
				} else {
					params = new Object[] { program, year, semester, schoolTarget };
				}
			} else {
				if (completedOnly) {
					sql.append("WHERE a.complete_date IS NOT NULL \n");
					if (!schoolFrom.equalsIgnoreCase("ALL")) {
						sql.append("AND b.academic_school=? \n");
						params = new Object[] { program, year, semester, schoolFrom };
					} else {
						params = new Object[] { program, year, semester };
					}
				} else {
					if (!schoolFrom.equalsIgnoreCase("ALL")) {
						sql.append("WHERE b.academic_school=? \n");
						params = new Object[] { program, year, semester, schoolFrom };
					} else {
						params = new Object[] { program, year, semester };
					}
				}
			}
			return getApplicationsNMSUCCCONF(sql, params);

		// programsFourYr
		case ProgramCode.URS:
		case ProgramCode.IREP:
			sql.append("SELECT a.*, b.academic_school FROM \n");
			sql.append("(SELECT * FROM ").append(TABLE_APPLICATION_LIST)
					.append(" WHERE program = ? AND school_year=? AND school_semester=?) AS a\n");
			sql.append("LEFT JOIN ").append(tableAppDetail).append(" b\n");
			sql.append("ON a.application_id = b.application_id\n");
			if (!schoolTarget.equalsIgnoreCase("ALL")) {
				sql.append("WHERE b.academic_school=? ");
				if (completedOnly) {
					sql.append("AND a.complete_date IS NOT NULL \n");
				} else {
					sql.append("\n");
				}
				// apply to same school
				params = new Object[] { program, year, semester, schoolTarget };
			} else {
				if (completedOnly) {
					sql.append("WHERE a.complete_date IS NOT NULL \n");
					if (!schoolFrom.equalsIgnoreCase("ALL")) {
						sql.append("AND b.academic_school=? \n");
						params = new Object[] { program, year, semester, schoolFrom };
					} else {
						params = new Object[] { program, year, semester };
					}
				} else {
					if (!schoolFrom.equalsIgnoreCase("ALL")) {
						sql.append("WHERE b.academic_school=? \n");
						params = new Object[] { program, year, semester, schoolFrom };
					} else {
						params = new Object[] { program, year, semester };
					}
				}
			}
			return getApplicationsNMSUCCCONF(sql, params);

		case ProgramCode.SCCORE:
			// target school: in application questionaire (either pref or alt is ok)
			sql.append("SELECT a.*, b.sccore_school_attend_pref, b.sccore_school_attend_altn FROM \n");
			sql.append("(SELECT * FROM ").append(TABLE_APPLICATION_LIST)
					.append(" WHERE program = ? AND school_year=? AND school_semester=?) AS a\n");
			sql.append("LEFT JOIN ").append(tableAppDetail).append(" b\n");
			sql.append("ON a.application_id = b.application_id\n");
			if (!schoolTarget.equalsIgnoreCase("ALL")) {
				sql.append("WHERE (b.sccore_school_attend_pref=? OR b.sccore_school_attend_altn=?) ");
				if (completedOnly) {
					sql.append("AND a.complete_date IS NOT NULL \n");
				} else {
					sql.append("\n");
				}

				if (!schoolFrom.equalsIgnoreCase("ALL")) {
					sql.append("AND b.academic_school=? \n");
					params = new Object[] { program, year, semester, schoolTarget, schoolTarget, schoolFrom };
				} else {
					params = new Object[] { program, year, semester, schoolTarget, schoolTarget };
				}
			} else {
				if (completedOnly) {
					sql.append("WHERE a.complete_date IS NOT NULL \n");
					if (!schoolFrom.equalsIgnoreCase("ALL")) {
						sql.append("AND b.academic_school=? \n");
						params = new Object[] { program, year, semester, schoolFrom };
					} else {
						params = new Object[] { program, year, semester };
					}
				} else {
					if (!schoolFrom.equalsIgnoreCase("ALL")) {
						sql.append("WHERE b.academic_school=? \n");
						params = new Object[] { program, year, semester, schoolFrom };
					} else {
						params = new Object[] { program, year, semester };
					}
				}
			}

			return getApplicationsNMSUCCCONF(sql, params);
		}
		return null;
	}

	private List<ApplicationBean> getApplicationsNMSUCCCONF(StringBuilder sql, Object[] params) {
		return jdbcTemplate.query(sql.toString(), params, new ResultSetExtractor<List<ApplicationBean>>() {

			@Override
			public List<ApplicationBean> extractData(ResultSet reSet) throws SQLException, DataAccessException {
				List<ApplicationBean> applicationList = new ArrayList<>();
				while (reSet.next()) {
					/* export both completed and incomplete application */
					ApplicationBean bean = new ApplicationBean();
					bean.setUserID(reSet.getInt("user_id"));
					bean.setApplicationID(reSet.getInt("application_id"));
					bean.setSchoolYear(reSet.getInt("school_year"));
					bean.setSchoolSemester(reSet.getString("school_semester"));
					String program = reSet.getString("program");
					bean.setProgramNameAbbr(program);

					String school = null;
					switch (program) {
					case ProgramCode.URS:
					case ProgramCode.IREP:
						school = reSet.getString("academic_school");
						break;
					case ProgramCode.CCCONF:
					case ProgramCode.MESA:
					case ProgramCode.TRANS:
						school = reSet.getString("academic_transfer_school");
						break;
					case ProgramCode.SCCORE:
						school = reSet.getString("sccore_school_attend_pref");
						break;
					}
					if (school != null) {
						bean.setSchoolTarget(ProgramCode.ACADEMIC_SCHOOL.get(school));
					}

					// get submission info
					try {
						String startDate = reSet.getString("start_date");
						if (startDate != null) {
							Date date = Parse.FORMAT_DATETIME.parse(startDate);
							startDate = Parse.FORMAT_DATE_MDY.format(date);
							bean.setStartDate(Parse.FORMAT_DATE_MDY.parse(startDate));
						}

						String transcriptDate = reSet.getString("transcript_date");
						if (transcriptDate != null) {
							Date date = Parse.FORMAT_DATETIME.parse(transcriptDate);
							transcriptDate = Parse.FORMAT_DATE_MDY.format(date);
							bean.setTranscriptDate(Parse.FORMAT_DATE_MDY.parse(transcriptDate));
						}

						String applicantSubmitDate = reSet.getString("applicant_submit_date");
						if (applicantSubmitDate != null) {
							Date date = Parse.FORMAT_DATETIME.parse(applicantSubmitDate);
							applicantSubmitDate = Parse.FORMAT_DATE_MDY.format(date);
							bean.setApplicantSubmitDate(Parse.FORMAT_DATE_MDY.parse(applicantSubmitDate));
						}

						String mentorSubmitDate = reSet.getString("mentor_submit_date");
						if (mentorSubmitDate != null) {
							Date date = Parse.FORMAT_DATETIME.parse(mentorSubmitDate);
							mentorSubmitDate = Parse.FORMAT_DATE_MDY.format(date);
							bean.setMentorSubmitDate(Parse.FORMAT_DATE_MDY.parse(mentorSubmitDate));
						}

						String recommenderSubmitDate = reSet.getString("recommender_submit_date");
						if (recommenderSubmitDate != null) {
							Date date = Parse.FORMAT_DATETIME.parse(recommenderSubmitDate);
							recommenderSubmitDate = Parse.FORMAT_DATE_MDY.format(date);
							bean.setRecommenderSubmitDate(Parse.FORMAT_DATE_MDY.parse(recommenderSubmitDate));
						}

						String medicalSubmitDate = reSet.getString("medical_submit_date");
						if (medicalSubmitDate != null) {
							Date date = Parse.FORMAT_DATETIME.parse(medicalSubmitDate);
							medicalSubmitDate = Parse.FORMAT_DATE_MDY.format(date);
							bean.setMedicalSubmitDate(Parse.FORMAT_DATE_MDY.parse(medicalSubmitDate));
						}

						String completeDate = reSet.getString("complete_date");
						if (completeDate != null) {
							Date date = Parse.FORMAT_DATETIME.parse(completeDate);
							completeDate = Parse.FORMAT_DATE_MDY.format(date);
							bean.setCompleteDate(Parse.FORMAT_DATE_MDY.parse(completeDate));
						}

						String mentorInfoDate = reSet.getString("mentor_info_date");
						if (mentorInfoDate != null) {
							Date date = Parse.FORMAT_DATETIME.parse(mentorInfoDate);
							mentorInfoDate = Parse.FORMAT_DATE_MDY.format(date);
							bean.setMentorInfoDate(Parse.FORMAT_DATE_MDY.parse(mentorInfoDate));
						}

						String notifiedDate = reSet.getString("notified_date");
						if (notifiedDate != null) {
							Date date = Parse.FORMAT_DATETIME.parse(notifiedDate);
							notifiedDate = Parse.FORMAT_DATE_MDY.format(date);
							bean.setNotifiedDate(Parse.FORMAT_DATE_MDY.parse(notifiedDate));
						}
					} catch (ParseException e) {
						e.printStackTrace();
					}

					bean.setDecision(reSet.getString("decision"));
					applicationList.add(bean);
				}

				return applicationList;
			}
		});
	}

	/****************************************************
	 * Manage Application (Export) *
	 ****************************************************/
	public List<ApplicationBean> getApplicationForZip(String program, List<String> ids) {

		String tableAppDetail = ProgramCode.TABLE_APPLICATION_DETAIL.get(program);
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT * \n");
		sql.append("FROM ").append(TABLE_APPLICATION_LIST).append(" a \n");
		sql.append("LEFT JOIN ").append(tableAppDetail).append(" b\n");
		sql.append("ON a.application_id = b.application_id \n");
		sql.append("WHERE a.application_id IN (");
		sql.append(ids.stream().collect(Collectors.joining(",")));
		sql.append(")\n");

		Object[] params = new Object[] {};

		switch (program) {
		// programsTwoYr
		case ProgramCode.CCCONF:
			return getApplicationByInstitutionCCCONF(sql, params);
		case ProgramCode.MESA:
			return getApplicationByInstitutionMESA(sql, params);
		case ProgramCode.SCCORE:
			// target school: in application questionaire
			return getApplicationByInstitutionSCCORE(sql, params);
		case ProgramCode.TRANS:
			return getApplicationByInstitutionTRANS(sql, params);
		// programsFourYr
		case ProgramCode.URS:
			return getApplicationByInstitutionURS(sql, params);
		case ProgramCode.IREP:
			return getApplicationByInstitutionIREP(sql, params);
		}
		return null;

	}

	private List<ApplicationBean> getApplicationByInstitutionCCCONF(StringBuilder sql, Object[] params) {
		return jdbcTemplate.query(sql.toString(), params, new ResultSetExtractor<List<ApplicationBean>>() {

			@Override
			public List<ApplicationBean> extractData(ResultSet reSet) throws SQLException, DataAccessException {
				List<ApplicationBean> applicationList = new ArrayList<>();
				while (reSet.next()) {
					ApplicationBean bean = new ApplicationBean();
					bean.setUserID(reSet.getInt("user_id"));
					bean.setApplicationID(reSet.getInt("application_id"));
					bean.setSchoolYear(reSet.getInt("school_year"));
					bean.setSchoolSemester(reSet.getString("school_semester"));
					bean.setSchoolTarget(reSet.getString("academic_transfer_school"));
					bean.setProgramNameAbbr(reSet.getString("program"));

					// get academicInfo
					AcademicBean academicBean = null;
					String academicSchool = reSet.getString("academic_school");
					Date transfer_date = reSet.getDate("transfer_date");
					if (transfer_date != null) {
						academicBean = new AcademicBean();
						academicBean.setAcademicSchool(academicSchool);
						academicBean.setAcademicBannerID(reSet.getString("academic_banner_id"));
						academicBean.setAcademicMajor(reSet.getString("academic_major"));
						academicBean.setAcademicStatus(reSet.getString("academic_status"));
						academicBean.setAcademicCredit(reSet.getString("academic_credit"));
						academicBean.setAcademicGPA(Float.parseFloat(reSet.getString("academic_gpa")));
						academicBean.setAcademicTransferDate(reSet.getDate("transfer_date"));
						academicBean.setAcademicTransferSchool(reSet.getString("academic_transfer_school"));
						academicBean.setAcademicIntendedMajor(reSet.getString("academic_intended_major"));
						academicBean.setAcademicReferrer(reSet.getString("academic_referrer"));
						bean.setAcademicBean(academicBean);
					}

					// get invInfo
					InvolvementBean involvementBean = null;
					String ampScholarship = reSet.getString("amp_scholarship");
					if (ampScholarship != null) {
						involvementBean = new InvolvementBean();
						involvementBean.setAmpScholarship(Integer.parseInt(ampScholarship));
						if (ampScholarship.equals("1")) {
							involvementBean.setAmpScholarshipSchool(reSet.getString("amp_scholarship_school"));
							involvementBean.setAmpScholarshipType(reSet.getString("amp_scholarship_type"));
							involvementBean.setAmpScholarshipSemester(reSet.getString("amp_scholarship_semester"));
							involvementBean.setAmpScholarshipYear(reSet.getString("amp_scholarship_year"));
							involvementBean.setAmpScholarshipAmount(reSet.getFloat("amp_scholarship_amount"));
						}
						involvementBean.setOtherScholarship(Integer.parseInt(reSet.getString("other_scholarship")));
						involvementBean.setListOtherScholarship(reSet.getString("list_other_scholarship"));

						String isCurrentEmploy = reSet.getString("is_current_employ");
						involvementBean.setIsCurrentEmploy(Integer.parseInt(isCurrentEmploy));
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

					/* Transcript */
					Blob blob = reSet.getBlob("transcript_content");
					if (blob != null && blob.length() != 0) {
						byte[] content = blob.getBytes(1, (int) blob.length());
						blob.free();
						FileBucket fileBucket = new FileBucket();
						fileBucket.setFileContent(content);
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
							bean.setApplicantSubmitDate(Parse.FORMAT_DATE_MDY.parse(applicantSubmitDate));

							String completeDate = reSet.getString("complete_date");
							if (completeDate != null) {
								date = Parse.FORMAT_DATETIME.parse(completeDate);
								completeDate = Parse.FORMAT_DATE_MDY.format(date);
								bean.setCompleteDate(Parse.FORMAT_DATE_MDY.parse(completeDate));
							}
						} catch (ParseException e) {
							e.printStackTrace();
						}
					}
					applicationList.add(bean);
				}
				return applicationList;
			}
		});
	}

	private List<ApplicationBean> getApplicationByInstitutionMESA(StringBuilder sql, Object[] params) {
		return jdbcTemplate.query(sql.toString(), params, new ResultSetExtractor<List<ApplicationBean>>() {

			@Override
			public List<ApplicationBean> extractData(ResultSet reSet) throws SQLException, DataAccessException {
				List<ApplicationBean> applicationList = new ArrayList<>();
				while (reSet.next()) {
					ApplicationBean bean = new ApplicationBean();
					bean.setUserID(reSet.getInt("user_id"));
					bean.setApplicationID(reSet.getInt("application_id"));
					bean.setSchoolYear(reSet.getInt("school_year"));
					bean.setSchoolSemester(reSet.getString("school_semester"));
					bean.setSchoolTarget(reSet.getString("academic_transfer_school"));
					bean.setProgramNameAbbr(reSet.getString("program"));

					// get academicInfo
					AcademicBean academicBean = null;
					String academicSchool = reSet.getString("academic_school");
					Date academicGradDate = reSet.getDate("academic_grad_date");
					if (academicGradDate != null) {
						academicBean = new AcademicBean();

						academicBean.setAcademicSchool(academicSchool);
						academicBean.setAcademicYear(reSet.getString("academic_year"));
						academicBean.setAcademicGradDate(reSet.getDate("academic_grad_date"));
						academicBean.setAcademicGPA(Float.parseFloat(reSet.getString("academic_gpa")));

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
						involvementBean.setAmpScholarship(Integer.parseInt(ampScholarship));

						if (ampScholarship.equals("1")) {
							involvementBean.setAmpScholarshipSchool(reSet.getString("amp_scholarship_school"));
							involvementBean.setAmpScholarshipType(reSet.getString("amp_scholarship_type"));
							involvementBean.setAmpScholarshipSemester(reSet.getString("amp_scholarship_semester"));
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

					/* Reference */
					Blob referenceblob = reSet.getBlob("reference_content");
					if (referenceblob != null && referenceblob.length() != 0) {
						byte[] content = referenceblob.getBytes(1, (int) referenceblob.length());
						referenceblob.free();
						FileBucket reference = new FileBucket();
						reference.setFileContent(content);
						bean.setReferenceBucket(reference);
					}

					/* Transcript */
					Blob blob = reSet.getBlob("transcript_content");
					if (blob != null && blob.length() != 0) {
						byte[] content = blob.getBytes(1, (int) blob.length());
						blob.free();
						FileBucket fileBucket = new FileBucket();
						fileBucket.setFileContent(content);
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
							bean.setApplicantSubmitDate(Parse.FORMAT_DATE_MDY.parse(applicantSubmitDate));

							String completeDate = reSet.getString("complete_date");
							if (completeDate != null) {
								date = Parse.FORMAT_DATETIME.parse(completeDate);
								completeDate = Parse.FORMAT_DATE_MDY.format(date);
								bean.setCompleteDate(Parse.FORMAT_DATE_MDY.parse(completeDate));
							}
						} catch (ParseException e) {
							e.printStackTrace();
						}
					}
					applicationList.add(bean);
				}

				return applicationList;
			}
		});
	}

	private List<ApplicationBean> getApplicationByInstitutionSCCORE(StringBuilder sql, Object[] params) {
		return jdbcTemplate.query(sql.toString(), params, new ResultSetExtractor<List<ApplicationBean>>() {

			@Override
			public List<ApplicationBean> extractData(ResultSet reSet) throws SQLException, DataAccessException {
				List<ApplicationBean> applicationList = new ArrayList<>();
				while (reSet.next()) {
					ApplicationBean bean = new ApplicationBean();
					bean.setUserID(reSet.getInt("user_id"));
					bean.setApplicationID(reSet.getInt("application_id"));
					bean.setSchoolYear(reSet.getInt("school_year"));
					bean.setSchoolSemester(reSet.getString("school_semester"));
					bean.setSchoolTarget(reSet.getString("sccore_school_attend_pref")); // choose first choice
					bean.setProgramNameAbbr(reSet.getString("program"));

					// get academicInfo
					AcademicBean academicBean = null;
					String academicSchool = reSet.getString("academic_school");
					Date academicGradDate = reSet.getDate("academic_grad_date");
					if (academicGradDate != null) {
						academicBean = new AcademicBean();

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
						involvementBean.setProgramEverIn(Integer.parseInt(programEverIn));
						involvementBean.setProgramEverInYear(reSet.getInt("program_ever_in_year"));
						String ampScholarship = reSet.getString("amp_scholarship");
						involvementBean.setAmpScholarship(Integer.parseInt(ampScholarship));
						if (ampScholarship.equals("1")) {
							involvementBean.setAmpScholarshipSchool(reSet.getString("amp_scholarship_school"));
							involvementBean.setAmpScholarshipType(reSet.getString("amp_scholarship_type"));
							involvementBean.setAmpScholarshipSemester(reSet.getString("amp_scholarship_semester"));
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
					if (sccoreSchoolAttendPref != null) { // *
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

					/* Transcript */
					Blob blob = reSet.getBlob("transcript_content");
					if (blob != null && blob.length() != 0) {
						byte[] content = blob.getBytes(1, (int) blob.length());
						blob.free();
						FileBucket fileBucket = new FileBucket();
						fileBucket.setFileContent(content);
						bean.setFileBucket(fileBucket);
					}

					// get Medical
					Blob referenceblob = reSet.getBlob("medical_content");
					if (referenceblob != null && referenceblob.length() != 0) {
						byte[] content = referenceblob.getBytes(1, (int) referenceblob.length());
						referenceblob.free();
						FileBucket reference = new FileBucket();
						reference.setFileContent(content);
						bean.setReferenceBucket(reference);
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
							bean.setApplicantSubmitDate(Parse.FORMAT_DATE_MDY.parse(applicantSubmitDate));

							String completeDate = reSet.getString("complete_date");
							if (completeDate != null) {
								date = Parse.FORMAT_DATETIME.parse(completeDate);
								completeDate = Parse.FORMAT_DATE_MDY.format(date);
								bean.setCompleteDate(Parse.FORMAT_DATE_MDY.parse(completeDate));
							}
						} catch (ParseException e) {
							e.printStackTrace();
						}
					}
					applicationList.add(bean);
				}

				return applicationList;
			}
		});
	}

	private List<ApplicationBean> getApplicationByInstitutionTRANS(StringBuilder sql, Object[] params) {
		return jdbcTemplate.query(sql.toString(), params, new ResultSetExtractor<List<ApplicationBean>>() {

			@Override
			public List<ApplicationBean> extractData(ResultSet reSet) throws SQLException, DataAccessException {
				List<ApplicationBean> applicationList = new ArrayList<>();
				while (reSet.next()) {
					ApplicationBean bean = new ApplicationBean();
					bean.setUserID(reSet.getInt("user_id"));
					bean.setApplicationID(reSet.getInt("application_id"));
					bean.setSchoolYear(reSet.getInt("school_year"));
					bean.setSchoolSemester(reSet.getString("school_semester"));
					bean.setSchoolTarget(reSet.getString("academic_transfer_school"));
					bean.setProgramNameAbbr(reSet.getString("program"));

					// get academicInfo
					AcademicBean academicBean = null;
					String academicSchool = reSet.getString("academic_school");
					Date academicGradDate = reSet.getDate("academic_grad_date");
					if (academicGradDate != null) {
						academicBean = new AcademicBean();

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
						involvementBean.setAmpScholarship(Integer.parseInt(ampScholarship));

						if (ampScholarship.equals("1")) {
							involvementBean.setAmpScholarshipSchool(reSet.getString("amp_scholarship_school"));
							involvementBean.setAmpScholarshipType(reSet.getString("amp_scholarship_type"));
							involvementBean.setAmpScholarshipSemester(reSet.getString("amp_scholarship_semester"));
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
						essayBean.setEssayFieldOfInterest(reSet.getString("essay_field_of_interest"));
						essayBean.setEssayCriticalEvent(reSet.getString("essay_critical_event"));
						essayBean.setEssayMentor(reSet.getString("essay_mentor"));
						essayBean.setEssayAmpGain(reSet.getString("essay_amp_gain"));
						bean.setEssayBean(essayBean);
					}

					/* Transcript */
					Blob blob = reSet.getBlob("transcript_content");
					if (blob != null && blob.length() != 0) {
						byte[] content = blob.getBytes(1, (int) blob.length());
						blob.free();
						FileBucket fileBucket = new FileBucket();
						fileBucket.setFileContent(content);
						bean.setFileBucket(fileBucket);
					}

					/* Recommendation */
					blob = reSet.getBlob("recommendation_file_content");
					RecommenderBean recommenderBean = null;
					if (blob != null && blob.length() != 0) {
						recommenderBean = new RecommenderBean();

						byte[] content = blob.getBytes(1, (int) blob.length());
						recommenderBean.setRecommendationContent(content);
						blob.free();

						recommenderBean.setPrefix(reSet.getString("recommender_prefix"));
						recommenderBean.setFirstName(reSet.getString("recommender_first_name"));
						recommenderBean.setLastName(reSet.getString("recommender_last_name"));
						recommenderBean.setEmail(reSet.getString("recommender_email"));
						recommenderBean.setPhone(reSet.getString("recommender_phone"));
						recommenderBean.setInstitution(reSet.getString("recommender_institution"));
						recommenderBean.setAddressLine1(reSet.getString("recommender_address_line1"));
						recommenderBean.setAddressLine2(reSet.getString("recommender_address_line2"));
						recommenderBean.setAddressCity(reSet.getString("recommender_address_city"));
						recommenderBean.setAddressState(reSet.getString("recommender_address_state"));
						recommenderBean.setAddressZip(reSet.getString("recommender_address_zip"));
						recommenderBean.setAddressCountry(reSet.getString("recommender_address_country"));
						bean.setRecommenderBean(recommenderBean);
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
							bean.setApplicantSubmitDate(Parse.FORMAT_DATE_MDY.parse(applicantSubmitDate));

							String completeDate = reSet.getString("complete_date");
							if (completeDate != null) {
								date = Parse.FORMAT_DATETIME.parse(completeDate);
								completeDate = Parse.FORMAT_DATE_MDY.format(date);
								bean.setCompleteDate(Parse.FORMAT_DATE_MDY.parse(completeDate));
							}
						} catch (ParseException e) {
							e.printStackTrace();
						}
					}
					applicationList.add(bean);
				}

				return applicationList;
			}
		});
	}

	private List<ApplicationBean> getApplicationByInstitutionURS(StringBuilder sql, Object[] params) {
		return jdbcTemplate.query(sql.toString(), params, new ResultSetExtractor<List<ApplicationBean>>() {

			@Override
			public List<ApplicationBean> extractData(ResultSet reSet) throws SQLException, DataAccessException {
				List<ApplicationBean> applicationList = new ArrayList<>();
				while (reSet.next()) {
					ApplicationBean bean = new ApplicationBean();
					bean.setUserID(reSet.getInt("user_id"));
					bean.setApplicationID(reSet.getInt("application_id"));
					bean.setSchoolYear(reSet.getInt("school_year"));
					bean.setSchoolSemester(reSet.getString("school_semester"));
					bean.setSchoolTarget(reSet.getString("academic_school")); // for 4yr: apply to same school
					bean.setProgramNameAbbr(reSet.getString("program"));

					// get academicInfo
					AcademicBean academicBean = null;
					String academicSchool = reSet.getString("academic_school");
					Date academicGradDate = reSet.getDate("academic_grad_date");
					if (academicGradDate != null) {
						academicBean = new AcademicBean();

						academicBean.setAcademicSchool(academicSchool);
						academicBean.setAcademicYear(reSet.getString("academic_year"));
						academicBean.setAcademicGradDate(reSet.getDate("academic_grad_date"));
						academicBean.setAcademicBannerID(reSet.getString("academic_banner_id"));
						academicBean.setAcademicMajor(reSet.getString("academic_major"));
						academicBean.setAcademicMinor(reSet.getString("academic_minor"));
						academicBean.setAcademicGPA(reSet.getFloat("academic_gpa"));
						bean.setAcademicBean(academicBean);
					}

					// get essayBean
					EssayBean essayBean = null;
					String essayEducationalGoal = reSet.getString("essay_educational_goal");
					if (essayEducationalGoal != null) { // *
						essayBean = new EssayBean();
						essayBean.setEssayEducationalGoal(essayEducationalGoal);
						bean.setEssayBean(essayBean);
					}

					// get invInfo
					InvolvementBean involvementBean = null;
					String programEverIn = reSet.getString("program_ever_in");
					if (programEverIn != null) { // *
						involvementBean = new InvolvementBean();
						involvementBean.setProgramEverIn(Integer.parseInt(programEverIn));
						involvementBean.setProgramEverInSemesters(reSet.getString("program_ever_in_semesters"));

						String ampScholarship = reSet.getString("amp_scholarship");
						involvementBean.setAmpScholarship(Integer.parseInt(ampScholarship));
						if (ampScholarship.equals("1")) {
							involvementBean.setAmpScholarshipSchool(reSet.getString("amp_scholarship_school"));
							involvementBean.setAmpScholarshipType(reSet.getString("amp_scholarship_type"));
							involvementBean.setAmpScholarshipSemester(reSet.getString("amp_scholarship_semester"));
							involvementBean.setAmpScholarshipYear(reSet.getString("amp_scholarship_year"));
							involvementBean.setAmpScholarshipAmount(
									Float.parseFloat(reSet.getString("amp_scholarship_amount")));
						}

						involvementBean.setOtherScholarship(Integer.parseInt(reSet.getString("other_scholarship")));
						involvementBean.setListOtherScholarship(reSet.getString("list_other_scholarship"));
						bean.setInvolvementBean(involvementBean);
					}

					// get mentor info : mentorID => get mentor profile
					// MentorBean mentorBean = null;
					String mentorID = reSet.getString("mentor_id");
					if (mentorID != null) {
						bean.setMentorID(Integer.parseInt(mentorID));
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

					/* Transcript */
					Blob blob = reSet.getBlob("transcript_content");
					if (blob != null && blob.length() > 0) {
						byte[] content = blob.getBytes(1, (int) blob.length());
						blob.free();
						FileBucket fileBucket = new FileBucket();
						fileBucket.setFileContent(content);
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
							bean.setApplicantSubmitDate(Parse.FORMAT_DATE_MDY.parse(applicantSubmitDate));

							String completeDate = reSet.getString("complete_date");
							if (completeDate != null) {
								date = Parse.FORMAT_DATETIME.parse(completeDate);
								completeDate = Parse.FORMAT_DATE_MDY.format(date);
								bean.setCompleteDate(Parse.FORMAT_DATE_MDY.parse(completeDate));
							}
						} catch (ParseException e) {
							e.printStackTrace();
						}
					}
					applicationList.add(bean);
				}

				return applicationList;
			}
		});
	}

	private List<ApplicationBean> getApplicationByInstitutionIREP(StringBuilder sql, Object[] params) {
		return jdbcTemplate.query(sql.toString(), params, new ResultSetExtractor<List<ApplicationBean>>() {

			@Override
			public List<ApplicationBean> extractData(ResultSet reSet) throws SQLException, DataAccessException {
				List<ApplicationBean> applicationList = new ArrayList<>();
				while (reSet.next()) {
					ApplicationBean bean = new ApplicationBean();
					bean.setUserID(reSet.getInt("user_id"));
					bean.setApplicationID(reSet.getInt("application_id"));
					bean.setSchoolYear(reSet.getInt("school_year"));
					bean.setSchoolSemester(reSet.getString("school_semester"));
					bean.setSchoolTarget(reSet.getString("academic_school")); // for 4yr: apply to same school
					bean.setProgramNameAbbr(reSet.getString("program"));

					// get academicInfo
					AcademicBean academicBean = null;
					String academicSchool = reSet.getString("academic_school");
					Date academicGradDate = reSet.getDate("academic_grad_date");
					if (academicGradDate != null) {
						academicBean = new AcademicBean();
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
					String mentorEmail = reSet.getString("mentor_email");
					if (mentorEmail != null) {
						MentorBean mentorBean = new MentorBean();
						mentorBean.setMentorFirstName(reSet.getString("mentor_first_name"));
						mentorBean.setMentorLastName(reSet.getString("mentor_last_name"));
						mentorBean.setMentorInstitution(reSet.getString("mentor_institution"));
						mentorBean.setMentorPhone(reSet.getString("mentor_phone"));
						mentorBean.setMentorEmail(mentorEmail);

						mentorBean.setIntlMentorFirstName(reSet.getString("intl_mentor_first_name"));
						mentorBean.setIntlMentorLastName(reSet.getString("intl_mentor_last_name"));
						mentorBean.setIntlMentorInstitution(reSet.getString("intl_mentor_institution"));
						mentorBean.setIntlMentorPhone(reSet.getString("intl_mentor_phone"));
						mentorBean.setIntlMentorEmail(reSet.getString("intl_mentor_email"));
						mentorBean.setIntlMentorCountry(reSet.getString("intl_mentor_country"));
						bean.setMentorBean(mentorBean);
					}

					// get project info
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

						budgetBean.setCurrentDomesticTravel(reSet.getBigDecimal("budget_current_domestictravel"));
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

					/* Transcript */
					Blob blob = reSet.getBlob("transcript_content");
					if (blob != null && blob.length() > 0) {
						byte[] content = blob.getBytes(1, (int) blob.length());
						blob.free();
						FileBucket fileBucket = new FileBucket();
						fileBucket.setFileContent(content);
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
							bean.setApplicantSubmitDate(Parse.FORMAT_DATE_MDY.parse(applicantSubmitDate));

							String completeDate = reSet.getString("complete_date");
							if (completeDate != null) {
								date = Parse.FORMAT_DATETIME.parse(completeDate);
								completeDate = Parse.FORMAT_DATE_MDY.format(date);
								bean.setCompleteDate(Parse.FORMAT_DATE_MDY.parse(completeDate));
							}
						} catch (ParseException e) {
							e.printStackTrace();
						}
					}
					applicationList.add(bean);
				}

				return applicationList;
			}
		});
	}

	public List<ApplicationBean> getSelectedApplicationForExcel(int year, String semester, String program,
			List<String> ids) {
		// List<String> ids = new ArrayList<>(Arrays.asList(appIDList));
		String tableAppDetail = ProgramCode.TABLE_APPLICATION_DETAIL.get(program);
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT * FROM \n");
		sql.append("(SELECT a.user_id as new_user_id,a.application_id as new_application_id, b.* FROM  ")
				.append(TABLE_APPLICATION_LIST).append(" a \n");
		sql.append("LEFT JOIN ").append(tableAppDetail).append(" b\n");
		sql.append("ON a.application_id = b.application_id \n");
		sql.append("WHERE a.application_id IN (");
		sql.append(ids.stream().collect(Collectors.joining(",")));
		sql.append(")) AS c \n");
		sql.append("LEFT JOIN ").append(TABLE_PROFILE_STUDENT).append(" AS d\n");
		sql.append("ON c.new_user_id = d.user_id \n");

		switch (program) {
		// programsTwoYr
		case ProgramCode.CCCONF:
			return getSelectedApplicationCCCONF(sql, year, semester, program);

		case ProgramCode.MESA:
			return getSelectedApplicationMESA(sql, year, semester, program);

		case ProgramCode.TRANS:
			return getSelectedApplicationTRANS(sql, year, semester, program);

		// programsFourYr
		case ProgramCode.URS:
			return getSelectedApplicationURS(sql, year, semester, program);

		case ProgramCode.SCCORE:
			return getSelectedApplicationSCCORE(sql, year, semester, program);

		case ProgramCode.IREP:
			return getSelectedApplicationIREP(sql, year, semester, program);
		}
		return null;
	}

	private List<ApplicationBean> getSelectedApplicationCCCONF(StringBuilder sql, int year, String semester,
			String program) {
		return jdbcTemplate.query(sql.toString(), new ResultSetExtractor<List<ApplicationBean>>() {

			@Override
			public List<ApplicationBean> extractData(ResultSet reSet) throws SQLException, DataAccessException {
				List<ApplicationBean> applicationList = new ArrayList<>();
				while (reSet.next()) {
					ApplicationBean bean = new ApplicationBean();
					bean.setUserID(reSet.getInt("new_user_id"));// *
					bean.setApplicationID(reSet.getInt("new_application_id"));
					bean.setFirstName(reSet.getString("first_name"));
					bean.setLastName(reSet.getString("last_name"));
					bean.setEmail(reSet.getString("email"));
					bean.setBirthDate(reSet.getDate("birth_date"));

					bean.setSchoolYear(year);
					bean.setSchoolSemester(semester);
					bean.setSchoolTarget(reSet.getString("academic_school"));
					bean.setProgramNameAbbr(program);

					// get academicInfo
					AcademicBean academicBean = null;
					String academicSchool = reSet.getString("academic_school");
					Date transfer_date = reSet.getDate("transfer_date"); // * diff from other program
					if (transfer_date != null) {
						academicBean = new AcademicBean();
						academicBean.setAcademicSchool(academicSchool);
						academicBean.setAcademicBannerID(reSet.getString("academic_banner_id"));
						academicBean.setAcademicMajor(reSet.getString("academic_major"));
						academicBean.setAcademicStatus(reSet.getString("academic_status"));
						academicBean.setAcademicCredit(reSet.getString("academic_credit"));
						academicBean.setAcademicGPA(Float.parseFloat(reSet.getString("academic_gpa")));
						academicBean.setAcademicTransferDate(reSet.getDate("transfer_date"));
						academicBean.setAcademicTransferSchool(reSet.getString("academic_transfer_school"));
						academicBean.setAcademicIntendedMajor(reSet.getString("academic_intended_major"));
						academicBean.setAcademicReferrer(reSet.getString("academic_referrer"));
						bean.setAcademicBean(academicBean);
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

					applicationList.add(bean);
				}
				return applicationList;
			}
		});
	}

	private List<ApplicationBean> getSelectedApplicationMESA(StringBuilder sql, int year, String semester,
			String program) {
		return jdbcTemplate.query(sql.toString(), new ResultSetExtractor<List<ApplicationBean>>() {

			@Override
			public List<ApplicationBean> extractData(ResultSet reSet) throws SQLException, DataAccessException {
				List<ApplicationBean> applicationList = new ArrayList<>();
				while (reSet.next()) {
					ApplicationBean bean = new ApplicationBean();
					bean.setUserID(reSet.getInt("new_user_id")); // *
					bean.setApplicationID(reSet.getInt("new_application_id"));
					bean.setFirstName(reSet.getString("first_name"));
					bean.setLastName(reSet.getString("last_name"));
					bean.setEmail(reSet.getString("email"));
					bean.setBirthDate(reSet.getDate("birth_date"));

					bean.setSchoolYear(year);
					bean.setSchoolSemester(semester);
					bean.setSchoolTarget(reSet.getString("academic_school"));
					bean.setProgramNameAbbr(program);

					// get academicInfo
					AcademicBean academicBean = null;
					Date academicGradDate = reSet.getDate("academic_grad_date");
					String academicSchool = reSet.getString("academic_school");
					if (academicGradDate != null) {
						academicBean = new AcademicBean();
						academicBean.setAcademicSchool(academicSchool);
						academicBean.setAcademicYear(reSet.getString("academic_year"));
						academicBean.setAcademicGradDate(reSet.getDate("academic_grad_date"));
						academicBean.setAcademicGPA(Float.parseFloat(reSet.getString("academic_gpa")));
						academicBean.setAcademicTransferDate(reSet.getDate("transfer_date"));
						academicBean.setAcademicTransferSchool(reSet.getString("academic_transfer_school"));
						academicBean.setAcademicIntendedMajor(reSet.getString("academic_intended_major"));
						academicBean.setAcademicReferrer(reSet.getString("academic_referrer"));
						bean.setAcademicBean(academicBean);
					}

					// get essayBean
					EssayBean essayBean = null;
					String essayProfesionalGoal = reSet.getString("essay_profesional_goal");
					if (essayProfesionalGoal != null) {
						essayBean = new EssayBean();
						essayBean.setEssayProfesionalGoal(essayProfesionalGoal);
						essayBean.setEssayAcademicPathway(reSet.getString("essay_academic_pathway"));
						essayBean.setEssayCriticalEvent(reSet.getString("essay_critical_event"));
						bean.setEssayBean(essayBean);
					}

					applicationList.add(bean);
				}
				return applicationList;
			}
		});
	}

	private List<ApplicationBean> getSelectedApplicationTRANS(StringBuilder sql, int year, String semester,
			String program) {
		return jdbcTemplate.query(sql.toString(), new ResultSetExtractor<List<ApplicationBean>>() {

			@Override
			public List<ApplicationBean> extractData(ResultSet reSet) throws SQLException, DataAccessException {
				List<ApplicationBean> applicationList = new ArrayList<>();
				while (reSet.next()) {
					ApplicationBean bean = new ApplicationBean();
					bean.setUserID(reSet.getInt("new_user_id"));
					bean.setApplicationID(reSet.getInt("new_application_id"));
					bean.setFirstName(reSet.getString("first_name"));
					bean.setLastName(reSet.getString("last_name"));
					bean.setEmail(reSet.getString("email"));
					bean.setBirthDate(reSet.getDate("birth_date"));

					bean.setSchoolYear(year);
					bean.setSchoolSemester(semester);
					bean.setSchoolTarget(reSet.getString("academic_school"));
					bean.setProgramNameAbbr(program);

					// get academicInfo
					AcademicBean academicBean = null;
					Date academicGradDate = reSet.getDate("academic_grad_date");
					String academicSchool = reSet.getString("academic_school");
					if (academicGradDate != null) {
						academicBean = new AcademicBean();
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

					// get essayBean
					EssayBean essayBean = null;
					String essayProfesionalGoal = reSet.getString("essay_profesional_goal");
					if (essayProfesionalGoal != null) { // *
						essayBean = new EssayBean();
						essayBean.setEssayProfesionalGoal(essayProfesionalGoal);
						essayBean.setEssayFieldOfInterest(reSet.getString("essay_field_of_interest"));
						essayBean.setEssayCriticalEvent(reSet.getString("essay_critical_event"));
						essayBean.setEssayMentor(reSet.getString("essay_mentor"));
						essayBean.setEssayAmpGain(reSet.getString("essay_amp_gain"));
						bean.setEssayBean(essayBean);
					}

					/* Recommendation */
					RecommenderBean recommenderBean = null;
					if (reSet.getString("recommender_email") != null) {
						recommenderBean = new RecommenderBean();
						recommenderBean.setFirstName(reSet.getString("recommender_first_name"));
						recommenderBean.setLastName(reSet.getString("recommender_last_name"));
						recommenderBean.setEmail(reSet.getString("recommender_email"));
						bean.setRecommenderBean(recommenderBean);
					}
					applicationList.add(bean);
				}
				return applicationList;
			}
		});
	}

	private List<ApplicationBean> getSelectedApplicationSCCORE(StringBuilder sql, int year, String semester,
			String program) {
		return jdbcTemplate.query(sql.toString(), new ResultSetExtractor<List<ApplicationBean>>() {

			@Override
			public List<ApplicationBean> extractData(ResultSet reSet) throws SQLException, DataAccessException {
				List<ApplicationBean> applicationList = new ArrayList<>();
				while (reSet.next()) {
					ApplicationBean bean = new ApplicationBean();
					bean.setUserID(reSet.getInt("new_user_id"));
					bean.setApplicationID(reSet.getInt("new_application_id"));
					bean.setFirstName(reSet.getString("first_name"));
					bean.setLastName(reSet.getString("last_name"));
					bean.setEmail(reSet.getString("email"));
					bean.setBirthDate(reSet.getDate("birth_date"));

					bean.setSchoolYear(year);
					bean.setSchoolSemester(semester);
					bean.setProgramNameAbbr(program);

					// get academicInfo
					AcademicBean academicBean = null;
					// ** don't use academic_school to check null, as it is filled as first step of
					// application
					Date academicGradDate = reSet.getDate("academic_grad_date");
					if (academicGradDate != null) {
						academicBean = new AcademicBean();
						String academicSchool = reSet.getString("academic_school");
						academicBean.setAcademicSchool(academicSchool);
						academicBean.setAcademicYear(reSet.getString("academic_year"));
						academicBean.setAcademicGradDate(academicGradDate);
						academicBean.setAcademicBannerID(reSet.getString("academic_banner_id"));
						academicBean.setAcademicMajor(reSet.getString("academic_major"));
						academicBean.setAcademicMinor(reSet.getString("academic_minor"));
						academicBean.setAcademicGPA(reSet.getFloat("academic_gpa"));
						bean.setAcademicBean(academicBean);
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
					applicationList.add(bean);
				}
				return applicationList;
			}
		});
	}

	private List<ApplicationBean> getSelectedApplicationURS(StringBuilder sql, int year, String semester,
			String program) {
		return jdbcTemplate.query(sql.toString(), new ResultSetExtractor<List<ApplicationBean>>() {

			@Override
			public List<ApplicationBean> extractData(ResultSet reSet) throws SQLException, DataAccessException {
				List<ApplicationBean> applicationList = new ArrayList<>();
				while (reSet.next()) {
					ApplicationBean bean = new ApplicationBean();
					bean.setUserID(reSet.getInt("new_user_id"));
					bean.setApplicationID(reSet.getInt("new_application_id"));
					bean.setFirstName(reSet.getString("first_name"));
					bean.setLastName(reSet.getString("last_name"));
					bean.setEmail(reSet.getString("email"));
					bean.setBirthDate(reSet.getDate("birth_date"));

					bean.setSchoolYear(year);
					bean.setSchoolSemester(semester);
					bean.setSchoolTarget(reSet.getString("academic_school"));
					bean.setProgramNameAbbr(program);

					// get academicInfo
					AcademicBean academicBean = null;
					Date academicGradDate = reSet.getDate("academic_grad_date");
					if (academicGradDate != null) {
						academicBean = new AcademicBean();
						String academicSchool = reSet.getString("academic_school");
						academicBean.setAcademicSchool(academicSchool);
						academicBean.setAcademicYear(reSet.getString("academic_year"));
						academicBean.setAcademicGradDate(academicGradDate);
						academicBean.setAcademicBannerID(reSet.getString("academic_banner_id"));
						academicBean.setAcademicMajor(reSet.getString("academic_major"));
						academicBean.setAcademicMinor(reSet.getString("academic_minor"));
						academicBean.setAcademicGPA(reSet.getFloat("academic_gpa"));
						bean.setAcademicBean(academicBean);
					}

					// get essayBean
					EssayBean essayBean = null;
					String essayEducationalGoal = reSet.getString("essay_educational_goal");
					if (essayEducationalGoal != null) {
						essayBean = new EssayBean();
						essayBean.setEssayEducationalGoal(essayEducationalGoal);
						bean.setEssayBean(essayBean);
					}

					// get invInfo
					InvolvementBean involvementBean = null;
					String programEverIn = reSet.getString("program_ever_in");
					if (programEverIn != null) {
						involvementBean = new InvolvementBean();
						involvementBean.setProgramEverIn(Integer.parseInt(programEverIn));
						involvementBean.setProgramEverInSemesters(reSet.getString("program_ever_in_semesters"));

						String ampScholarship = reSet.getString("amp_scholarship");
						involvementBean.setAmpScholarship(Integer.parseInt(ampScholarship));
						if (ampScholarship.equals("1")) {
							involvementBean.setAmpScholarshipSchool(reSet.getString("amp_scholarship_school"));
							involvementBean.setAmpScholarshipType(reSet.getString("amp_scholarship_type"));
							involvementBean.setAmpScholarshipSemester(reSet.getString("amp_scholarship_semester"));
							involvementBean.setAmpScholarshipYear(reSet.getString("amp_scholarship_year"));
							involvementBean.setAmpScholarshipAmount(
									Float.parseFloat(reSet.getString("amp_scholarship_amount")));
						}

						involvementBean.setOtherScholarship(Integer.parseInt(reSet.getString("other_scholarship")));
						involvementBean.setListOtherScholarship(reSet.getString("list_other_scholarship"));
						bean.setInvolvementBean(involvementBean);
					}

					// get mentor info
					// MentorBean mentorBean = null;
					String mentorEmail = reSet.getString("mentor_email");
					if (mentorEmail != null) {
						MentorBean mentorBean = new MentorBean();
						mentorBean.setMentorFirstName(reSet.getString("mentor_first_name"));
						mentorBean.setMentorLastName(reSet.getString("mentor_last_name"));
						mentorBean.setMentorEmail(mentorEmail);
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
					applicationList.add(bean);
				}
				return applicationList;
			}
		});
	}

	private List<ApplicationBean> getSelectedApplicationIREP(StringBuilder sql, int year, String semester,
			String program) {
		return jdbcTemplate.query(sql.toString(), new ResultSetExtractor<List<ApplicationBean>>() {

			@Override
			public List<ApplicationBean> extractData(ResultSet reSet) throws SQLException, DataAccessException {
				List<ApplicationBean> applicationList = new ArrayList<>();
				while (reSet.next()) {
					ApplicationBean bean = new ApplicationBean();
					bean.setUserID(reSet.getInt("new_user_id"));
					bean.setApplicationID(reSet.getInt("new_application_id"));
					bean.setFirstName(reSet.getString("first_name"));
					bean.setLastName(reSet.getString("last_name"));
					bean.setEmail(reSet.getString("email"));
					bean.setBirthDate(reSet.getDate("birth_date"));

					bean.setSchoolYear(year);
					bean.setSchoolSemester(semester);
					bean.setSchoolTarget(reSet.getString("academic_school"));
					bean.setProgramNameAbbr(program);

					// get academicInfo
					AcademicBean academicBean = null;
					Date academicGradDate = reSet.getDate("academic_grad_date");
					if (academicGradDate != null) {
						academicBean = new AcademicBean();
						String academicSchool = reSet.getString("academic_school");
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
					String mentorEmail = reSet.getString("mentor_email");
					if (mentorEmail != null) {
						MentorBean mentorBean = new MentorBean();
						mentorBean.setMentorFirstName(reSet.getString("mentor_first_name"));
						mentorBean.setMentorLastName(reSet.getString("mentor_last_name"));
						mentorBean.setMentorInstitution(reSet.getString("mentor_institution"));
						mentorBean.setMentorPhone(reSet.getString("mentor_phone"));
						mentorBean.setMentorEmail(mentorEmail);

						mentorBean.setIntlMentorFirstName(reSet.getString("intl_mentor_first_name"));
						mentorBean.setIntlMentorLastName(reSet.getString("intl_mentor_last_name"));
						mentorBean.setIntlMentorInstitution(reSet.getString("intl_mentor_institution"));
						mentorBean.setIntlMentorPhone(reSet.getString("intl_mentor_phone"));
						mentorBean.setIntlMentorEmail(reSet.getString("intl_mentor_email"));
						mentorBean.setIntlMentorCountry(reSet.getString("intl_mentor_country"));
						bean.setMentorBean(mentorBean);
					}

					// get project info
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

						budgetBean.setCurrentDomesticTravel(reSet.getBigDecimal("budget_current_domestictravel"));
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
					applicationList.add(bean);
				}
				return applicationList;
			}
		});
	}

	/****************************************************
	 * Get Application Attachments, i.e., transcripts *
	 ****************************************************/
	public byte[] downloadTranscript(int applicationID, String program) {
		String table = ProgramCode.TABLE_APPLICATION_DETAIL.get(program);
		String sql = "SELECT transcript_content FROM " + table + " WHERE application_id = ?";
		try {
			return jdbcTemplate.queryForObject(sql, byte[].class, new Object[] { applicationID });
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	/****************************************************
	 * Delete Application
	 ****************************************************/
	public boolean deleteApplication(String program, String applicationID) {
		String tableAppDetail = ProgramCode.TABLE_APPLICATION_DETAIL.get(program);
		String sql1 = "DELETE FROM " + tableAppDetail + " WHERE application_id = ?";
		String sql2 = "DELETE FROM " + TABLE_APPLICATION_LIST + " WHERE program = ? AND application_id = ?";

		return transactionTemplate.execute(new TransactionCallback<Boolean>() {
			@Override
			public Boolean doInTransaction(TransactionStatus status) {
				try {
					jdbcTemplate.update(sql1, new Object[] { applicationID });
					jdbcTemplate.update(sql2, new Object[] { program, applicationID });
				} catch (Exception e) {
					status.setRollbackOnly();
					e.printStackTrace();
					return false;
				}
				return true;
			}
		});

	}

	/****************************************************
	 * Batch Update Application Decision *
	 ****************************************************/
	public void batchUpdateApplicationDecision(String program, List<String> appIDs, String decision,
			String schoolTarget) {
		String sql = "UPDATE " + TABLE_APPLICATION_LIST + " SET decision=?, accept_school = ?, accept_status = ? "
				+ "WHERE application_id = ? AND program = ?";

		jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {

			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				if (decision == null || decision.length() < 1) {
					ps.setNull(1, java.sql.Types.VARCHAR);
					ps.setNull(2, java.sql.Types.VARCHAR);
					ps.setInt(3, 0);
				} else {
					ps.setString(1, decision);
					if (decision.equalsIgnoreCase("admit")) {
						ps.setString(2, schoolTarget);
						ps.setInt(3, 1);
					} else if (decision.equalsIgnoreCase("withdrew")) {
						ps.setNull(2, java.sql.Types.VARCHAR);
						ps.setInt(3, 2);
					} else {
						ps.setNull(2, java.sql.Types.VARCHAR);
						ps.setInt(3, 0);
					}
				}
				ps.setInt(4, Integer.parseInt(appIDs.get(i)));
				ps.setString(5, program);
			}

			@Override
			public int getBatchSize() {
				return appIDs.size();
			}
		});

	}

	/****************************************************
	 * Get Application Profile/Decision *
	 ****************************************************/
	public List<ApplicationBean> getSelectedApplicationDecision(int year, String semester, String program,
			List<String> ids) {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT a.user_id, a.application_id,a.decision, b.email,b.first_name,b.last_name \n");
		sql.append("FROM ").append(TABLE_APPLICATION_LIST).append(" a \n");
		sql.append("LEFT JOIN ").append(TABLE_PROFILE_STUDENT).append(" b\n");
		sql.append("ON a.user_id = b.user_id \n");
		sql.append("WHERE a.school_year = ? AND a.school_semester = ? AND a.program = ? AND a.application_id IN (");
		sql.append(ids.stream().collect(Collectors.joining(",")));
		sql.append(")\n");

		Object[] params = new Object[] { year, semester, program };
		return jdbcTemplate.query(sql.toString(), params, new ResultSetExtractor<List<ApplicationBean>>() {

			@Override
			public List<ApplicationBean> extractData(ResultSet reSet) throws SQLException, DataAccessException {
				List<ApplicationBean> applicationList = new ArrayList<>();
				while (reSet.next()) {
					ApplicationBean bean = new ApplicationBean();
					bean.setUserID(reSet.getInt("user_id"));
					bean.setApplicationID(reSet.getInt("application_id"));
					bean.setFirstName(reSet.getString("first_name"));
					bean.setLastName(reSet.getString("last_name"));
					bean.setEmail(reSet.getString("email"));
					String decision = reSet.getString("decision");
					bean.setDecision(decision);
					applicationList.add(bean);
				}
				return applicationList;
			}
		});
	}

	public List<ApplicationBean> getDecisionProfile(int year, String semester, String program, String[] appIDList) {
		List<String> ids = new ArrayList<>(Arrays.asList(appIDList));
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT a.application_id,b.email,b.first_name,b.last_name,a.decision,"
				+ "b.current_address_line1,b.current_address_line2,b.current_address_city,b.current_address_state,b.current_address_zip\n");
		sql.append("FROM ").append(TABLE_APPLICATION_LIST).append(" a \n");
		sql.append("LEFT JOIN ").append(TABLE_PROFILE_STUDENT).append(" b\n");
		sql.append("ON a.user_id = b.user_id \n");
		sql.append("WHERE a.school_year = ? AND a.school_semester = ? AND a.program = ? AND a.application_id IN (");
		sql.append(ids.stream().collect(Collectors.joining(",")));
		sql.append(")\n");

		Object[] params = new Object[] { year, semester, program };
		return jdbcTemplate.query(sql.toString(), params, new ResultSetExtractor<List<ApplicationBean>>() {

			@Override
			public List<ApplicationBean> extractData(ResultSet reSet) throws SQLException, DataAccessException {
				List<ApplicationBean> applicationList = new ArrayList<>();
				while (reSet.next()) {
					ApplicationBean bean = new ApplicationBean();
					String decision = reSet.getString("decision");
					if (decision == null)
						return null;
					bean.setDecision(decision);

					bean.setApplicationID(reSet.getInt("application_id"));
					bean.setFirstName(reSet.getString("first_name"));
					bean.setLastName(reSet.getString("last_name"));
					bean.setEmail(reSet.getString("email"));
					bean.setSchoolYear(year);
					bean.setSchoolSemester(semester);
					bean.setProgramNameAbbr(program);
					// bean.setProgramNameFull(ProgramCode.PROGRAMS.get(reSet.getString("program")));

					bean.setAddressLine1(reSet.getString("current_address_line1"));
					// bean.setAddressLine2(reSet.getString("current_address_line2"));
					bean.setAddressCity(reSet.getString("current_address_city"));
					bean.setAddressState(reSet.getString("current_address_state"));
					bean.setAddressZip(reSet.getString("current_address_zip"));
					applicationList.add(bean);
				}
				return applicationList;
			}
		});
	}

	public List<ApplicationBean> getApplicationsByUserID(int userID) {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT application_id,school_year,school_semester,program,decision,accept_school \n");
		sql.append("FROM ").append(TABLE_APPLICATION_LIST).append("\n");
		sql.append("WHERE user_id = ? \n");

		Object[] params = new Object[] { userID };
		return jdbcTemplate.query(sql.toString(), params, new ResultSetExtractor<List<ApplicationBean>>() {

			@Override
			public List<ApplicationBean> extractData(ResultSet reSet) throws SQLException, DataAccessException {
				List<ApplicationBean> applicationList = new ArrayList<>();
				while (reSet.next()) {
					ApplicationBean bean = new ApplicationBean();
					bean.setApplicationID(reSet.getInt("application_id"));
					bean.setSchoolYear(reSet.getInt("school_year"));
					bean.setSchoolSemester(reSet.getString("school_semester"));
					bean.setProgramNameFull(ProgramCode.PROGRAMS_SUB.get(reSet.getString("program")));
					String schoolTarget = "";
					if (reSet.getString("accept_school") != null) {
						schoolTarget = ProgramCode.ACADEMIC_SCHOOL.get(reSet.getString("accept_school"));
					}
					bean.setSchoolTarget(schoolTarget);
					bean.setDecision(reSet.getString("decision"));
					applicationList.add(bean);
				}
				return applicationList;
			}
		});
	}

	/****************************************************
	 * Batch Update Decision Notified Date *
	 ****************************************************/
	public void batchUpdateDecisionNotifiedDate(int year, String semester, String program, String[] appIDList) {
		String sql = "UPDATE " + TABLE_APPLICATION_LIST
				+ " SET notified_date=? WHERE school_year=? AND school_semester=? AND program=? AND application_id=? ";
		TimeZone.setDefault(TimeZone.getTimeZone("America/Denver"));
		jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				ps.setString(1, Parse.FORMAT_DATETIME.format(new Date()));
				ps.setInt(2, year);
				ps.setString(3, semester);
				ps.setString(4, program);
				ps.setInt(5, Integer.parseInt(appIDList[i]));
			}

			@Override
			public int getBatchSize() {
				return appIDList.length;
			}
		});

	}

	/****************************************************
	 * Get Project for URS *
	 ****************************************************/
	public List<ApplicationBean> getAcceptedURSProject(String schoolSemester, int schoolYear, String schoolTarget) {

		/* only select admitted application AND a.complete_date IS NOT NULL */
		String table = ProgramCode.TABLE_APPLICATION_DETAIL.get(ProgramCode.URS);
		StringBuilder sql = new StringBuilder();
		sql.append(
				"SELECT a.application_id,school_year,school_semester,mentor_id,mentor_first_name,mentor_last_name,c.first_name,c.last_name,project_title \n");
		sql.append("FROM ").append(TABLE_APPLICATION_LIST).append(" a \n");
		sql.append("LEFT JOIN ").append(table).append(" b\n");
		sql.append("ON a.application_id = b.application_id\n");
		sql.append("LEFT JOIN ").append(Schemacode.TABLE_PROFILE_STUDENT).append(" c\n");
		sql.append("ON a.user_id = c.user_id\n");
		sql.append("WHERE a.program='URS' AND a.decision = 'Admit' AND school_semester = ? AND school_year = ? ");

		Object[] params = null;
		if (!schoolTarget.equalsIgnoreCase("ALL")) {
			sql.append(" AND accept_school=? \n");
			params = new Object[] { schoolSemester, schoolYear, schoolTarget };
		} else {
			sql.append("\n");
			params = new Object[] { schoolSemester, schoolYear };
		}

		return jdbcTemplate.query(sql.toString(), params, new ResultSetExtractor<List<ApplicationBean>>() {

			@Override
			public List<ApplicationBean> extractData(ResultSet reSet) throws SQLException, DataAccessException {
				List<ApplicationBean> applicationList = new ArrayList<>();
				while (reSet.next()) {
					ApplicationBean bean = new ApplicationBean();

					bean.setApplicationID(reSet.getInt("application_id"));
					bean.setSchoolYear(reSet.getInt("school_year"));
					bean.setSchoolSemester(reSet.getString("school_semester"));
					bean.setFirstName(reSet.getString("first_name"));
					bean.setLastName(reSet.getString("last_name"));

					String mentorID = reSet.getString("mentor_id");
					if (mentorID != null) {
						MentorBean mentorBean = new MentorBean();
						mentorBean.setMenteeID(Parse.tryParseInteger(mentorID));
						mentorBean.setMentorFirstName(reSet.getString("mentor_first_name"));
						mentorBean.setMentorLastName(reSet.getString("mentor_last_name"));
						// mentorBean.setMentorEmail(reSet.getString("mentor_email"));
						bean.setMentorBean(mentorBean);
					}

					// get project info
					ProjectBean projectBean = null;
					String projectTitle = reSet.getString("project_title");
					if (projectTitle != null) {
						projectBean = new ProjectBean();
						projectBean.setProjectTitle(projectTitle);
						bean.setProjectBean(projectBean);
					}

					applicationList.add(bean);
				}

				return applicationList;
			}
		});
	}

	public ApplicationBean getURSProjectByApplicationID(int applicationID) {
		String table = ProgramCode.TABLE_APPLICATION_DETAIL.get(ProgramCode.URS);
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT * \n");
		sql.append("FROM ").append(TABLE_APPLICATION_LIST).append(" a \n");
		sql.append("LEFT JOIN ").append(table).append(" b\n");
		sql.append("ON a.application_id = b.application_id\n");
		sql.append("LEFT JOIN ").append(TABLE_USER).append(" c\n");
		sql.append("ON a.user_id = c.user_id\n");
		sql.append("WHERE a.program='URS' AND a.application_id = ? \n");

		try {
			ApplicationBean applicationBean = jdbcTemplate.queryForObject(sql.toString(),
					new Object[] { applicationID }, new RowMapper<ApplicationBean>() {
						@Override
						public ApplicationBean mapRow(ResultSet reSet, int rowNum) throws SQLException {

							ApplicationBean bean = new ApplicationBean();
							bean.setApplicationID(applicationID);
							bean.setSchoolYear(reSet.getInt("school_year"));
							bean.setSchoolSemester(reSet.getString("school_semester"));
							bean.setFirstName(reSet.getString("first_name"));
							bean.setLastName(reSet.getString("last_name"));
							bean.setEmail(reSet.getString("email"));
							bean.setUserID(reSet.getInt("user_id"));
							bean.setMentorID(reSet.getInt("mentor_id"));

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
								bean.setProjectBean(projectBean);
							}

							return bean;
						}
					});
			return applicationBean;
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	public List<ApplicationBean> getApplicationNMSUforCommitteeReview(String program, List<String> ids) {

		String tableAppDetail = ProgramCode.TABLE_APPLICATION_DETAIL.get(program);
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT * \n");
		sql.append("FROM ").append(TABLE_APPLICATION_LIST).append(" a \n");
		sql.append("LEFT JOIN ").append(tableAppDetail).append(" b\n");
		sql.append("ON a.application_id = b.application_id\n");
		sql.append("WHERE a.application_id IN (");
		sql.append(ids.stream().collect(Collectors.joining(",")));
		sql.append(")\n");

		switch (program) {
		// programsTwoYr
		case ProgramCode.CCCONF:
			return getApplicationforCommitteeReviewCCCONF(sql);

		case ProgramCode.MESA:
			return getApplicationforCommitteeReviewMESA(sql);

		case ProgramCode.TRANS:
			return getApplicationforCommitteeReviewTRANS(sql);

		// programsFourYr
		case ProgramCode.URS:
			return getApplicationforCommitteeReviewURS(sql);

		case ProgramCode.SCCORE:
			return getApplicationforCommitteeReviewSCCORE(sql);

		case ProgramCode.IREP:
			return getApplicationforCommitteeReviewIREP(sql);
		}
		return null;
	}

	private List<ApplicationBean> getApplicationforCommitteeReviewCCCONF(StringBuilder sql) {
		return jdbcTemplate.query(sql.toString(), new RowMapper<ApplicationBean>() {
			@Override
			public ApplicationBean mapRow(ResultSet reSet, int rowNum) throws SQLException {

				ApplicationBean bean = new ApplicationBean();
				bean.setUserID(reSet.getInt("user_id"));
				bean.setApplicationID(reSet.getInt("application_id"));
				bean.setSchoolYear(reSet.getInt("school_year"));
				bean.setSchoolSemester(reSet.getString("school_semester"));
				bean.setSchoolTarget(reSet.getString("academic_school"));
				bean.setProgramNameAbbr(reSet.getString("program"));

				// get academicInfo
				AcademicBean academicBean = null;
				String academicSchool = reSet.getString("academic_school");
				Date transfer_date = reSet.getDate("transfer_date");
				if (transfer_date != null) {
					academicBean = new AcademicBean();
					academicBean.setAcademicSchool(academicSchool);
					academicBean.setAcademicMajor(reSet.getString("academic_major"));
					academicBean.setAcademicGPA(Float.parseFloat(reSet.getString("academic_gpa")));
					academicBean.setAcademicTransferDate(reSet.getDate("transfer_date"));
					academicBean.setAcademicTransferSchool(reSet.getString("academic_transfer_school"));
					academicBean.setAcademicIntendedMajor(reSet.getString("academic_intended_major"));
					academicBean.setAcademicReferrer(reSet.getString("academic_referrer"));
					bean.setAcademicBean(academicBean);
				}

				// get essayBean
				EssayBean essayBean = null;
				String essayCriticalEvent = reSet.getString("essay_critical_event");
				if (essayCriticalEvent != null) {
					essayBean = new EssayBean();
					essayBean.setEssayCriticalEvent(essayCriticalEvent);
					essayBean.setEssayEducationalGoal(reSet.getString("essay_educational_goal"));
					essayBean.setEssayProfesionalGoal(reSet.getString("essay_profesional_goal"));
					essayBean.setEssayAmpGain(reSet.getString("essay_amp_gain"));
					bean.setEssayBean(essayBean);
				}

				/* Transcript */
				Blob blob = reSet.getBlob("transcript_content");
				if (blob != null && blob.length() != 0) {
					byte[] content = blob.getBytes(1, (int) blob.length());
					blob.free();
					FileBucket fileBucket = new FileBucket();
					fileBucket.setFileContent(content);
					bean.setFileBucket(fileBucket);
				}
				return bean;
			}
		});
	}

	private List<ApplicationBean> getApplicationforCommitteeReviewMESA(StringBuilder sql) {
		return jdbcTemplate.query(sql.toString(), new RowMapper<ApplicationBean>() {
			@Override
			public ApplicationBean mapRow(ResultSet reSet, int rowNum) throws SQLException {

				ApplicationBean bean = new ApplicationBean();
				bean.setUserID(reSet.getInt("user_id"));
				bean.setApplicationID(reSet.getInt("application_id"));
				bean.setSchoolYear(reSet.getInt("school_year"));
				bean.setSchoolSemester(reSet.getString("school_semester"));
				bean.setSchoolTarget(reSet.getString("academic_school"));
				bean.setProgramNameAbbr(reSet.getString("program"));

				// get academicInfo
				AcademicBean academicBean = null;
				String academicSchool = reSet.getString("academic_school");
				Date academic_grad_date = reSet.getDate("academic_grad_date");
				if (academic_grad_date != null) {
					academicBean = new AcademicBean();
					academicBean.setAcademicSchool(academicSchool);
					academicBean.setAcademicGradDate(reSet.getDate("academic_grad_date"));
					academicBean.setAcademicTransferDate(reSet.getDate("transfer_date"));
					academicBean.setAcademicTransferSchool(reSet.getString("academic_transfer_school"));
					academicBean.setAcademicIntendedMajor(reSet.getString("academic_intended_major"));
					academicBean.setAcademicReferrer(reSet.getString("academic_referrer"));
					academicBean.setAcademicGPA(reSet.getFloat("academic_gpa"));
					bean.setAcademicBean(academicBean);
				}

				// get essayBean
				EssayBean essayBean = null;
				String essayProfesionalGoal = reSet.getString("essay_profesional_goal");
				if (essayProfesionalGoal != null) {
					essayBean = new EssayBean();
					essayBean.setEssayProfesionalGoal(essayProfesionalGoal);
					essayBean.setEssayAcademicPathway(reSet.getString("essay_academic_pathway"));
					essayBean.setEssayCriticalEvent(reSet.getString("essay_critical_event"));
					bean.setEssayBean(essayBean);
				}

				/* Reference */
				Blob referenceblob = reSet.getBlob("reference_content");
				if (referenceblob != null && referenceblob.length() != 0) {
					byte[] content = referenceblob.getBytes(1, (int) referenceblob.length());
					referenceblob.free();
					FileBucket reference = new FileBucket();
					reference.setFileContent(content);
					bean.setReferenceBucket(reference);
				}

				/* Transcript */
				Blob blob = reSet.getBlob("transcript_content");
				if (blob != null && blob.length() != 0) {
					byte[] content = blob.getBytes(1, (int) blob.length());
					blob.free();
					FileBucket fileBucket = new FileBucket();
					fileBucket.setFileContent(content);
					bean.setFileBucket(fileBucket);
				}
				return bean;
			}
		});
	}

	private List<ApplicationBean> getApplicationforCommitteeReviewSCCORE(StringBuilder sql) {
		return jdbcTemplate.query(sql.toString(), new RowMapper<ApplicationBean>() {
			@Override
			public ApplicationBean mapRow(ResultSet reSet, int rowNum) throws SQLException {

				ApplicationBean bean = new ApplicationBean();
				bean.setUserID(reSet.getInt("user_id"));
				bean.setApplicationID(reSet.getInt("application_id"));
				bean.setSchoolYear(reSet.getInt("school_year"));
				bean.setSchoolSemester(reSet.getString("school_semester"));
				bean.setSchoolTarget(reSet.getString("academic_school"));
				bean.setProgramNameAbbr(reSet.getString("program"));

				// get academicInfo
				AcademicBean academicBean = null;
				String academicSchool = reSet.getString("academic_school");
				Date academic_grad_date = reSet.getDate("academic_grad_date");
				if (academic_grad_date != null) {
					academicBean = new AcademicBean();
					academicBean.setAcademicSchool(academicSchool);
					academicBean.setAcademicGradDate(reSet.getDate("academic_grad_date"));
					academicBean.setAcademicMajor(reSet.getString("academic_major"));
					academicBean.setAcademicGPA(reSet.getFloat("academic_gpa"));
					bean.setAcademicBean(academicBean);
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

				/* Transcript */
				Blob blob = reSet.getBlob("transcript_content");
				if (blob != null && blob.length() != 0) {
					byte[] content = blob.getBytes(1, (int) blob.length());
					blob.free();
					FileBucket fileBucket = new FileBucket();
					fileBucket.setFileContent(content);
					bean.setFileBucket(fileBucket);
				}

				// get Medical
				Blob referenceblob = reSet.getBlob("medical_content");
				if (referenceblob != null && referenceblob.length() != 0) {
					byte[] content = referenceblob.getBytes(1, (int) referenceblob.length());
					referenceblob.free();
					FileBucket reference = new FileBucket();
					reference.setFileContent(content);
					bean.setReferenceBucket(reference);
				}
				return bean;
			}
		});
	}

	private List<ApplicationBean> getApplicationforCommitteeReviewTRANS(StringBuilder sql) {
		return jdbcTemplate.query(sql.toString(), new RowMapper<ApplicationBean>() {
			@Override
			public ApplicationBean mapRow(ResultSet reSet, int rowNum) throws SQLException {

				ApplicationBean bean = new ApplicationBean();
				bean.setUserID(reSet.getInt("user_id"));
				bean.setApplicationID(reSet.getInt("application_id"));
				bean.setSchoolYear(reSet.getInt("school_year"));
				bean.setSchoolSemester(reSet.getString("school_semester"));
				bean.setSchoolTarget(reSet.getString("academic_school"));
				bean.setProgramNameAbbr(reSet.getString("program"));

				// get academicInfo
				AcademicBean academicBean = null;
				String academicSchool = reSet.getString("academic_school");
				Date academic_grad_date = reSet.getDate("academic_grad_date");
				if (academic_grad_date != null) {
					academicBean = new AcademicBean();
					academicBean.setAcademicGradDate(reSet.getDate("academic_grad_date"));
					academicBean.setAcademicSchool(academicSchool);
					academicBean.setAcademicMajor(reSet.getString("academic_major"));
					academicBean.setAcademicGPA(Float.parseFloat(reSet.getString("academic_gpa")));
					academicBean.setAcademicTransferDate(reSet.getDate("transfer_date"));
					academicBean.setAcademicTransferSchool(reSet.getString("academic_transfer_school"));
					academicBean.setAcademicIntendedMajor(reSet.getString("academic_intended_major"));
					academicBean.setAcademicReferrer(reSet.getString("academic_referrer"));
					bean.setAcademicBean(academicBean);
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

				/* Recommendation */
				Blob blobRec = reSet.getBlob("recommendation_file_content");
				RecommenderBean recommenderBean = null;
				if (blobRec != null && blobRec.length() != 0) {
					recommenderBean = new RecommenderBean();
					byte[] content = blobRec.getBytes(1, (int) blobRec.length());
					recommenderBean.setRecommendationContent(content);
					blobRec.free();

					recommenderBean.setPrefix(reSet.getString("recommender_prefix"));
					recommenderBean.setFirstName(reSet.getString("recommender_first_name"));
					recommenderBean.setLastName(reSet.getString("recommender_last_name"));
					recommenderBean.setEmail(reSet.getString("recommender_email"));
					recommenderBean.setPhone(reSet.getString("recommender_phone"));
					recommenderBean.setInstitution(reSet.getString("recommender_institution"));
					recommenderBean.setAddressLine1(reSet.getString("recommender_address_line1"));
					recommenderBean.setAddressLine2(reSet.getString("recommender_address_line2"));
					recommenderBean.setAddressCity(reSet.getString("recommender_address_city"));
					recommenderBean.setAddressState(reSet.getString("recommender_address_state"));
					recommenderBean.setAddressZip(reSet.getString("recommender_address_zip"));
					recommenderBean.setAddressCountry(reSet.getString("recommender_address_country"));
					bean.setRecommenderBean(recommenderBean);
				}

				/* Transcript */
				Blob blob = reSet.getBlob("transcript_content");
				if (blob != null && blob.length() != 0) {
					byte[] content = blob.getBytes(1, (int) blob.length());
					blob.free();
					FileBucket fileBucket = new FileBucket();
					fileBucket.setFileContent(content);
					bean.setFileBucket(fileBucket);
				}
				return bean;
			}
		});
	}

	private List<ApplicationBean> getApplicationforCommitteeReviewURS(StringBuilder sql) {
		return jdbcTemplate.query(sql.toString(), new RowMapper<ApplicationBean>() {
			@Override
			public ApplicationBean mapRow(ResultSet reSet, int rowNum) throws SQLException {

				ApplicationBean bean = new ApplicationBean();
				bean.setUserID(reSet.getInt("user_id"));
				bean.setApplicationID(reSet.getInt("application_id"));
				bean.setSchoolYear(reSet.getInt("school_year"));
				bean.setSchoolSemester(reSet.getString("school_semester"));
				bean.setSchoolTarget(reSet.getString("academic_school"));
				bean.setProgramNameAbbr(reSet.getString("program"));

				// get academicInfo
				AcademicBean academicBean = null;
				String academicSchool = reSet.getString("academic_school");
				Date academic_grad_date = reSet.getDate("academic_grad_date");
				if (academic_grad_date != null) {
					academicBean = new AcademicBean();
					academicBean.setAcademicSchool(academicSchool);
					academicBean.setAcademicGradDate(reSet.getDate("academic_grad_date"));
					academicBean.setAcademicMajor(reSet.getString("academic_major"));
					academicBean.setAcademicGPA(reSet.getFloat("academic_gpa"));
					bean.setAcademicBean(academicBean);
				}

				// get essayBean
				EssayBean essayBean = null;
				String essayEducationalGoal = reSet.getString("essay_educational_goal");
				if (essayEducationalGoal != null) {
					essayBean = new EssayBean();
					essayBean.setEssayEducationalGoal(essayEducationalGoal);
					bean.setEssayBean(essayBean);
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

				/* Transcript */
				Blob blob = reSet.getBlob("transcript_content");
				if (blob != null && blob.length() != 0) {
					byte[] content = blob.getBytes(1, (int) blob.length());
					blob.free();
					FileBucket fileBucket = new FileBucket();
					fileBucket.setFileContent(content);
					bean.setFileBucket(fileBucket);
				}
				return bean;
			}
		});
	}

	private List<ApplicationBean> getApplicationforCommitteeReviewIREP(StringBuilder sql) {
		return jdbcTemplate.query(sql.toString(), new RowMapper<ApplicationBean>() {
			@Override
			public ApplicationBean mapRow(ResultSet reSet, int rowNum) throws SQLException {

				ApplicationBean bean = new ApplicationBean();
				bean.setUserID(reSet.getInt("user_id"));
				bean.setApplicationID(reSet.getInt("application_id"));
				bean.setSchoolYear(reSet.getInt("school_year"));
				bean.setSchoolSemester(reSet.getString("school_semester"));
				bean.setSchoolTarget(reSet.getString("academic_school"));
				bean.setProgramNameAbbr(reSet.getString("program"));

				// get academicInfo
				AcademicBean academicBean = null;
				String academicSchool = reSet.getString("academic_school");
				Date academic_grad_date = reSet.getDate("academic_grad_date");
				if (academic_grad_date != null) {
					academicBean = new AcademicBean();
					academicBean.setAcademicSchool(academicSchool);
					academicBean.setAcademicGradDate(reSet.getDate("academic_grad_date"));
					academicBean.setAcademicMajor(reSet.getString("academic_major"));
					academicBean.setAcademicGPA(reSet.getFloat("academic_gpa"));
					bean.setAcademicBean(academicBean);
				}

				// get mentor info
				String mentorEmail = reSet.getString("mentor_email");
				if (mentorEmail != null) {
					MentorBean mentorBean = new MentorBean();
					mentorBean.setMentorFirstName(reSet.getString("mentor_first_name"));
					mentorBean.setMentorLastName(reSet.getString("mentor_last_name"));
					mentorBean.setMentorInstitution(reSet.getString("mentor_institution"));
					mentorBean.setMentorPhone(reSet.getString("mentor_phone"));
					mentorBean.setMentorEmail(mentorEmail);

					mentorBean.setIntlMentorFirstName(reSet.getString("intl_mentor_first_name"));
					mentorBean.setIntlMentorLastName(reSet.getString("intl_mentor_last_name"));
					mentorBean.setIntlMentorInstitution(reSet.getString("intl_mentor_institution"));
					mentorBean.setIntlMentorPhone(reSet.getString("intl_mentor_phone"));
					mentorBean.setIntlMentorEmail(reSet.getString("intl_mentor_email"));
					mentorBean.setIntlMentorCountry(reSet.getString("intl_mentor_country"));
					bean.setMentorBean(mentorBean);
				}

				// get project info
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

					budgetBean.setCurrentDomesticTravel(reSet.getBigDecimal("budget_current_domestictravel"));
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

				/* Transcript */
				Blob blob = reSet.getBlob("transcript_content");
				if (blob != null && blob.length() != 0) {
					byte[] content = blob.getBytes(1, (int) blob.length());
					blob.free();
					FileBucket fileBucket = new FileBucket();
					fileBucket.setFileContent(content);
					bean.setFileBucket(fileBucket);
				}
				return bean;
			}
		});
	}

	/****************************************************
	 * Get Accepted Students *
	 ****************************************************/
	public List<ApplicationBean> getAcceptedStudents(String schoolSemester, int schoolYear, String schoolTarget) {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT distinct(a.user_id),b.first_name,b.last_name,b.email,a.school_year,a.school_semester \n");
		sql.append("FROM ").append(TABLE_APPLICATION_LIST).append(" a \n");
		sql.append("LEFT JOIN ").append(TABLE_USER).append(" b\n");
		sql.append("ON a.user_id = b.user_id\n");
		sql.append("WHERE a.decision = 'Admit' AND school_semester = ? AND school_year = ? ");
		Object[] params = null;
		if (!schoolTarget.equalsIgnoreCase("ALL")) {
			sql.append(" AND accept_school=? \n");
			params = new Object[] { schoolSemester, schoolYear, schoolTarget };
		} else {
			sql.append("\n");
			params = new Object[] { schoolSemester, schoolYear };
		}

		return jdbcTemplate.query(sql.toString(), params, new ResultSetExtractor<List<ApplicationBean>>() {

			@Override
			public List<ApplicationBean> extractData(ResultSet reSet) throws SQLException, DataAccessException {
				List<ApplicationBean> applicationList = new ArrayList<>();
				while (reSet.next()) {
					ApplicationBean bean = new ApplicationBean();
					bean.setUserID(reSet.getInt("user_id"));
					bean.setFirstName(reSet.getString("first_name"));
					bean.setLastName(reSet.getString("last_name"));
					bean.setEmail(reSet.getString("email"));
					bean.setSchoolYear(reSet.getInt("school_year"));
					bean.setSchoolSemester(reSet.getString("school_semester"));
					applicationList.add(bean);
				}

				return applicationList;
			}
		});
	}

	/****************************************************
	 * Manage Mentor Evaluation *
	 ****************************************************/
	public int addEvaluation(String applicationID, EvaluationBean evaluationBean) {

		String table = evaluationBean.getEvalPoint().equalsIgnoreCase(ProgramCode.EVALUATION_POINT.MIDTERM.toString())
				? TABLE_EVAL_MIDTERM
				: TABLE_EVAL_END;

		try {
			jdbcTemplate.queryForObject(
					"SELECT application_id FROM " + table
							+ " WHERE application_id = ? AND eval_year = ? AND eval_semester = ?",
					new Object[] { applicationID, evaluationBean.getEvalYear(), evaluationBean.getEvalTerm() },
					Integer.class);
		} catch (EmptyResultDataAccessException e) {
			// insert
			String endDate = Parse.FORMAT_DATETIME.format(evaluationBean.getEvalDeadline());
			endDate = endDate.split("\\s+")[0] + " 23:59:59";
			String sql = "INSERT INTO " + table
					+ " (application_id, eval_year, eval_semester, mentee_id, mentor_id, deadline)"
					+ " VALUES (?, ?, ?, ?, ?, ?)";
			Object[] params = new Object[] { applicationID, evaluationBean.getEvalYear(), evaluationBean.getEvalTerm(),
					evaluationBean.getMenteeID(), evaluationBean.getMentorID(), endDate };
			return jdbcTemplate.update(sql, params);
		}

		return 0;
	}

	public List<EvaluationBean> getEvaluations(int applicationID) {

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT eval_year,eval_semester,deadline,notified_date,submit_date, 'Mid-Term' as eval_point \n");
		sql.append("FROM ").append(TABLE_EVAL_MIDTERM).append(" WHERE application_id=? \n");
		sql.append("UNION ALL \n");
		sql.append(
				"SELECT eval_year,eval_semester,deadline,notified_date,submit_date, 'End-Of-Semester' as eval_point \n");
		sql.append("FROM ").append(TABLE_EVAL_END).append(" WHERE application_id=? \n");

		return jdbcTemplate.query(sql.toString(), new Object[] { applicationID, applicationID },
				new RowMapper<EvaluationBean>() {
					@Override
					public EvaluationBean mapRow(ResultSet reSet, int rowNum) throws SQLException {
						EvaluationBean bean = new EvaluationBean();
						bean.setApplicationID(applicationID);
						bean.setNotifiedDate(reSet.getDate("notified_date"));
						bean.setEvalPoint(reSet.getString("eval_point"));
						bean.setEvalYear(reSet.getInt("eval_year"));
						bean.setEvalTerm(reSet.getString("eval_semester"));
						bean.setSubmitDate(reSet.getDate("submit_date"));
						bean.setEvalDeadline(reSet.getDate("deadline"));
						return bean;
					}
				});
	}

	public EvaluationBean getEvalutionBasic(String applicationID, String evalYear, String evalSemester,
			String evalPoint) {
		String table = ProgramCode.TABLE_EVALUATION.get(evalPoint);
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT a.*,b.first_name,b.last_name,c.mentor_first_name,c.mentor_email,c.mentor_last_name \n");
		sql.append("FROM \n");
		sql.append("(SELECT * FROM ").append(table).append("\n");
		sql.append("where application_id = ? AND eval_year = ? AND eval_semester = ?) AS a \n");
		sql.append("LEFT JOIN ").append(TABLE_PROFILE_STUDENT).append(" b\n");
		sql.append("ON a.mentee_id = b.user_id\n");
		sql.append("LEFT JOIN ").append(TABLE_PROFILE_MENTOR).append(" c\n");
		sql.append("ON a.mentor_id = c.mentor_id\n");

		try {
			EvaluationBean evaluationBean = jdbcTemplate.queryForObject(sql.toString(),
					new Object[] { applicationID, evalYear, evalSemester }, new RowMapper<EvaluationBean>() {
						@Override
						public EvaluationBean mapRow(ResultSet reSet, int rowNum) throws SQLException {
							EvaluationBean bean = new EvaluationBean();
							bean.setEvalPoint(evalPoint);
							bean.setEvalYear(reSet.getInt("eval_year"));
							bean.setEvalTerm(reSet.getString("eval_semester"));
							bean.setMenteeName(reSet.getString("first_name") + " " + reSet.getString("last_name"));
							bean.setMentorName(
									reSet.getString("mentor_first_name") + " " + reSet.getString("mentor_last_name"));
							bean.setMentorEmail(reSet.getString("mentor_email"));
							bean.setEvalDeadline(reSet.getDate("deadline"));
							return bean;
						}
					});
			return evaluationBean;
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	public int setEvaluationNotifiedDate(String applicationID, String evalYear, String evalSemester, String evalPoint) {
		String table = ProgramCode.TABLE_EVALUATION.get(evalPoint);
		StringBuilder sql = new StringBuilder();
		sql.append("UPDATE ").append(table).append(" SET notified_date=? ")
				.append("where application_id = ? AND eval_year = ? AND eval_semester = ? \n");
		return jdbcTemplate.update(sql.toString(), new Object[] { new Date(), applicationID, evalYear, evalSemester });
	}

	public int setEvaluationDeadline(String applicationID, String evalYear, String evalSemester, String evalPoint,
			Date deadline) {
		String table = ProgramCode.TABLE_EVALUATION.get(evalPoint);
		StringBuilder sql = new StringBuilder();
		sql.append("UPDATE ").append(table).append(" SET deadline=? ")
				.append("where application_id = ? AND eval_year = ? AND eval_semester = ? \n");
		return jdbcTemplate.update(sql.toString(), new Object[] { deadline, applicationID, evalYear, evalSemester });
	}

	public List<List<String>> getEvaluationsMidTerm(List<String> ids) {
		String table = ProgramCode.TABLE_APPLICATION_DETAIL.get(ProgramCode.URS);
		StringBuilder sql = new StringBuilder();
		sql.append(
				"SELECT a.*, b.project_title,c.first_name,c.last_name,d.mentor_first_name,d.mentor_last_name,d.mentor_email,d.mentor_department \n");
		sql.append("FROM ").append(TABLE_EVAL_MIDTERM).append(" a \n");
		sql.append("LEFT JOIN ").append(table).append(" b\n");
		sql.append("ON a.application_id = b.application_id\n");
		sql.append("LEFT JOIN ").append(TABLE_PROFILE_STUDENT).append(" c\n");
		sql.append("ON a.mentee_id = c.user_id\n");
		sql.append("LEFT JOIN ").append(TABLE_PROFILE_MENTOR).append(" d\n");
		sql.append("ON a.mentor_id = d.mentor_id\n");
		sql.append("WHERE a.application_id IN (");
		sql.append(ids.stream().collect(Collectors.joining(",")));
		sql.append(")\n");

		return jdbcTemplate.query(sql.toString(), new Object[] {}, new RowMapper<List<String>>() {
			@Override
			public List<String> mapRow(ResultSet reSet, int rowNum) throws SQLException {
				List<String> list = new ArrayList<String>();
				list.add(reSet.getString("eval_semester") + " " + reSet.getInt("eval_year"));
				list.add(reSet.getString("application_id"));
				list.add(reSet.getString("project_title"));
				list.add(reSet.getString("mentor_first_name"));
				list.add(reSet.getString("mentor_last_name"));
				list.add(reSet.getString("mentor_email"));
				list.add(reSet.getString("mentor_department"));
				list.add(reSet.getString("first_name"));
				list.add(reSet.getString("last_name"));
				list.add(reSet.getString("rating_report"));
				list.add(reSet.getString("rating_has"));
				list.add(reSet.getString("rating_complete"));
				list.add(reSet.getString("rating_demonstrate"));
				list.add(reSet.getString("rating_gain"));
				list.add(reSet.getString("radio_meet"));
				list.add(reSet.getString("quest_assessment"));
				list.add(reSet.getString("quest_conference"));
				list.add(reSet.getString("quest_concern"));
				list.add(reSet.getString("quest_assist"));
				list.add(reSet.getString("submit_date"));
				return list;
			}
		});

	}

	public List<List<String>> getEvaluationsEnd(List<String> ids) {
		String table = ProgramCode.TABLE_APPLICATION_DETAIL.get(ProgramCode.URS);
		StringBuilder sql = new StringBuilder();
		sql.append(
				"SELECT a.*, b.project_title,c.first_name,c.last_name,d.mentor_first_name,d.mentor_last_name,d.mentor_email,d.mentor_department \n");
		sql.append("FROM ").append(TABLE_EVAL_END).append(" a \n");
		sql.append("LEFT JOIN ").append(table).append(" b\n");
		sql.append("ON a.application_id = b.application_id\n");
		sql.append("LEFT JOIN ").append(TABLE_PROFILE_STUDENT).append(" c\n");
		sql.append("ON a.mentee_id = c.user_id\n");
		sql.append("LEFT JOIN ").append(TABLE_PROFILE_MENTOR).append(" d\n");
		sql.append("ON a.mentor_id = d.mentor_id\n");
		sql.append("WHERE a.application_id IN (");
		sql.append(ids.stream().collect(Collectors.joining(",")));
		sql.append(")\n");

		return jdbcTemplate.query(sql.toString(), new Object[] {}, new RowMapper<List<String>>() {
			@Override
			public List<String> mapRow(ResultSet reSet, int rowNum) throws SQLException {
				List<String> list = new ArrayList<String>();
				list.add(reSet.getString("eval_semester") + " " + reSet.getInt("eval_year"));
				list.add(reSet.getString("application_id"));
				list.add(reSet.getString("project_title"));
				list.add(reSet.getString("mentor_first_name"));
				list.add(reSet.getString("mentor_last_name"));
				list.add(reSet.getString("mentor_email"));
				list.add(reSet.getString("mentor_department"));
				list.add(reSet.getString("first_name"));
				list.add(reSet.getString("last_name"));

				list.add(reSet.getString("rating_lab_1"));
				list.add(reSet.getString("rating_lab_2"));
				list.add(reSet.getString("rating_lab_3"));
				list.add(reSet.getString("rating_organization_1"));
				list.add(reSet.getString("rating_organization_2"));
				list.add(reSet.getString("rating_organization_3"));
				list.add(reSet.getString("rating_organization_4"));
				list.add(reSet.getString("rating_independent_1"));
				list.add(reSet.getString("rating_independent_2"));
				list.add(reSet.getString("rating_writing_1"));
				list.add(reSet.getString("rating_writing_2"));

				list.add(reSet.getString("quest_techniques"));
				list.add(reSet.getString("quest_publications"));
				list.add(reSet.getString("quest_conferences"));
				list.add(reSet.getString("quest_assessment"));
				list.add(reSet.getString("submit_date"));
				return list;
			}
		});
	}

	public List<String> getEvaluationMidTerm(String applicationID, String evalYear, String evalSemester,
			String evalPoint) {
		String table = ProgramCode.TABLE_APPLICATION_DETAIL.get(ProgramCode.URS);

		StringBuilder sql = new StringBuilder();
		sql.append(
				"SELECT a.*, b.project_title,c.first_name,c.last_name,d.mentor_first_name,d.mentor_last_name,d.mentor_email,d.mentor_department \n");
		sql.append("FROM ").append(TABLE_EVAL_MIDTERM).append(" a \n");
		sql.append("LEFT JOIN ").append(table).append(" b\n");
		sql.append("ON a.application_id = b.application_id\n");
		sql.append("LEFT JOIN ").append(TABLE_PROFILE_STUDENT).append(" c\n");
		sql.append("ON a.mentee_id = c.user_id\n");
		sql.append("LEFT JOIN ").append(TABLE_PROFILE_MENTOR).append(" d\n");
		sql.append("ON a.mentor_id = d.mentor_id\n");
		sql.append("WHERE a.application_id = ? AND a.eval_year = ? AND a.eval_semester = ? \n");

		try {
			List<String> list = jdbcTemplate.queryForObject(sql.toString(),
					new Object[] { applicationID, evalYear, evalSemester }, new RowMapper<List<String>>() {
						@Override
						public List<String> mapRow(ResultSet reSet, int rowNum) throws SQLException {
							if (reSet.getString("submit_date") == null) {
								return null;
							}
							List<String> list = new ArrayList<String>();
							list.add(reSet.getString("eval_semester") + " " + reSet.getInt("eval_year"));
							list.add(reSet.getString("application_id"));
							list.add(reSet.getString("project_title"));
							list.add(reSet.getString("mentor_first_name"));
							list.add(reSet.getString("mentor_last_name"));
							list.add(reSet.getString("mentor_email"));
							list.add(reSet.getString("mentor_department"));
							list.add(reSet.getString("first_name"));
							list.add(reSet.getString("last_name"));
							list.add(reSet.getString("rating_report"));
							list.add(reSet.getString("rating_has"));
							list.add(reSet.getString("rating_complete"));
							list.add(reSet.getString("rating_demonstrate"));
							list.add(reSet.getString("rating_gain"));
							list.add(reSet.getString("radio_meet"));
							list.add(reSet.getString("quest_assessment"));
							list.add(reSet.getString("quest_conference"));
							list.add(reSet.getString("quest_concern"));
							list.add(reSet.getString("quest_assist"));
							list.add(reSet.getString("submit_date"));
							return list;
						}
					});
			return list;
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	public List<String> getEvalutionEnd(String applicationID, String evalYear, String evalSemester, String evalPoint) {
		String table = ProgramCode.TABLE_APPLICATION_DETAIL.get(ProgramCode.URS);

		StringBuilder sql = new StringBuilder();
		sql.append(
				"SELECT a.*, b.project_title,c.first_name,c.last_name,d.mentor_first_name,d.mentor_last_name,d.mentor_email,d.mentor_department \n");
		sql.append("FROM ").append(TABLE_EVAL_END).append(" a \n");
		sql.append("LEFT JOIN ").append(table).append(" b\n");
		sql.append("ON a.application_id = b.application_id\n");
		sql.append("LEFT JOIN ").append(TABLE_PROFILE_STUDENT).append(" c\n");
		sql.append("ON a.mentee_id = c.user_id\n");
		sql.append("LEFT JOIN ").append(TABLE_PROFILE_MENTOR).append(" d\n");
		sql.append("ON a.mentor_id = d.mentor_id\n");
		sql.append("WHERE a.application_id = ? AND a.eval_year = ? AND a.eval_semester = ? \n");

		try {
			List<String> list = jdbcTemplate.queryForObject(sql.toString(),
					new Object[] { applicationID, evalYear, evalSemester }, new RowMapper<List<String>>() {
						@Override
						public List<String> mapRow(ResultSet reSet, int rowNum) throws SQLException {
							if (reSet.getString("submit_date") == null) {
								return null;
							}
							List<String> list = new ArrayList<String>();
							list.add(reSet.getString("eval_semester") + " " + reSet.getInt("eval_year"));
							list.add(reSet.getString("application_id"));
							list.add(reSet.getString("project_title"));
							list.add(reSet.getString("mentor_first_name"));
							list.add(reSet.getString("mentor_last_name"));
							list.add(reSet.getString("mentor_email"));
							list.add(reSet.getString("mentor_department"));
							list.add(reSet.getString("first_name"));
							list.add(reSet.getString("last_name"));

							list.add(reSet.getString("rating_lab_1"));
							list.add(reSet.getString("rating_lab_2"));
							list.add(reSet.getString("rating_lab_3"));
							list.add(reSet.getString("rating_organization_1"));
							list.add(reSet.getString("rating_organization_2"));
							list.add(reSet.getString("rating_organization_3"));
							list.add(reSet.getString("rating_organization_4"));
							list.add(reSet.getString("rating_independent_1"));
							list.add(reSet.getString("rating_independent_2"));
							list.add(reSet.getString("rating_writing_1"));
							list.add(reSet.getString("rating_writing_2"));

							list.add(reSet.getString("quest_techniques"));
							list.add(reSet.getString("quest_publications"));
							list.add(reSet.getString("quest_conferences"));
							list.add(reSet.getString("quest_assessment"));
							list.add(reSet.getString("submit_date"));
							return list;
						}
					});
			return list;
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	/****************************************************
	 * Manage Student Evaluation *
	 ****************************************************/
	public List<EvaluationBean> getEvaluationsStu(int applicationID) {

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT eval_year,eval_semester,deadline,notified_date,submit_date, 'Mid-Term' as eval_point \n");
		sql.append("FROM ").append(TABLE_EVAL_MIDTERM_STU).append(" WHERE application_id=? \n");
		return jdbcTemplate.query(sql.toString(), new Object[] { applicationID }, new RowMapper<EvaluationBean>() {
			@Override
			public EvaluationBean mapRow(ResultSet reSet, int rowNum) throws SQLException {
				EvaluationBean bean = new EvaluationBean();
				bean.setApplicationID(applicationID);
				bean.setNotifiedDate(reSet.getDate("notified_date"));
				bean.setEvalPoint(reSet.getString("eval_point"));
				bean.setEvalYear(reSet.getInt("eval_year"));
				bean.setEvalTerm(reSet.getString("eval_semester"));
				bean.setSubmitDate(reSet.getDate("submit_date"));
				bean.setEvalDeadline(reSet.getDate("deadline"));
				return bean;
			}
		});
	}

	public int addStudentEvaluation(String applicationID, EvaluationBean evaluationBeanStu) {

		String table = evaluationBeanStu.getEvalPoint().equalsIgnoreCase(
				ProgramCode.EVALUATION_POINT.MIDTERM.toString()) ? TABLE_EVAL_MIDTERM_STU : TABLE_EVAL_END_STU;

		try {
			jdbcTemplate.queryForObject(
					"SELECT application_id FROM " + table
							+ " WHERE application_id = ? AND eval_year = ? AND eval_semester = ?",
					new Object[] { applicationID, evaluationBeanStu.getEvalYear(), evaluationBeanStu.getEvalTerm() },
					Integer.class);
		} catch (EmptyResultDataAccessException e) {
			// insert
			String endDate = Parse.FORMAT_DATETIME.format(evaluationBeanStu.getEvalDeadline());
			endDate = endDate.split("\\s+")[0] + " 23:59:59";
			String sql = "INSERT INTO " + table
					+ " (application_id, eval_year, eval_semester, mentee_id, mentor_id, deadline)"
					+ " VALUES (?, ?, ?, ?, ?, ?)";
			Object[] params = new Object[] { applicationID, evaluationBeanStu.getEvalYear(),
					evaluationBeanStu.getEvalTerm(), evaluationBeanStu.getMenteeID(), evaluationBeanStu.getMentorID(),
					endDate };
			return jdbcTemplate.update(sql, params);
		}

		return 0;
	}

	public int setEvaluationDeadlineStu(String applicationID, String evalYear, String evalSemester, String evalPoint,
			Date deadline) {
		String table = ProgramCode.EVALUATION_POINT.MIDTERM.toString().equalsIgnoreCase(evalPoint)
				? TABLE_EVAL_MIDTERM_STU
				: TABLE_EVAL_END_STU;
		StringBuilder sql = new StringBuilder();
		sql.append("UPDATE ").append(table).append(" SET deadline=? ")
				.append("where application_id = ? AND eval_year = ? AND eval_semester = ? \n");
		return jdbcTemplate.update(sql.toString(), new Object[] { deadline, applicationID, evalYear, evalSemester });
	}

	public EvaluationBean getEvalutionBasicStu(String applicationID, String evalYear, String evalSemester,
			String evalPoint) {
		String table = ProgramCode.EVALUATION_POINT.MIDTERM.toString().equalsIgnoreCase(evalPoint)
				? TABLE_EVAL_MIDTERM_STU
				: TABLE_EVAL_END_STU;
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT a.*,b.first_name,b.last_name,b.email,c.mentor_first_name,c.mentor_last_name \n");
		sql.append("FROM \n");
		sql.append("(SELECT * FROM ").append(table).append("\n");
		sql.append("where application_id = ? AND eval_year = ? AND eval_semester = ?) AS a \n");
		sql.append("LEFT JOIN ").append(TABLE_PROFILE_STUDENT).append(" b\n");
		sql.append("ON a.mentee_id = b.user_id\n");
		sql.append("LEFT JOIN ").append(TABLE_PROFILE_MENTOR).append(" c\n");
		sql.append("ON a.mentor_id = c.mentor_id\n");

		try {
			EvaluationBean evaluationBean = jdbcTemplate.queryForObject(sql.toString(),
					new Object[] { applicationID, evalYear, evalSemester }, new RowMapper<EvaluationBean>() {
						@Override
						public EvaluationBean mapRow(ResultSet reSet, int rowNum) throws SQLException {
							EvaluationBean bean = new EvaluationBean();
							bean.setEvalPoint(evalPoint);
							bean.setEvalYear(reSet.getInt("eval_year"));
							bean.setEvalTerm(reSet.getString("eval_semester"));
							bean.setMenteeName(reSet.getString("first_name") + " " + reSet.getString("last_name"));
							bean.setMentorName(
									reSet.getString("mentor_first_name") + " " + reSet.getString("mentor_last_name"));
							bean.setMenteeEmail(reSet.getString("email"));
							bean.setEvalDeadline(reSet.getDate("deadline"));
							return bean;
						}
					});
			return evaluationBean;
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	public int setEvaluationNotifiedDateStu(String applicationID, String evalYear, String evalSemester,
			String evalPoint) {
		String table = ProgramCode.EVALUATION_POINT.MIDTERM.toString().equalsIgnoreCase(evalPoint)
				? TABLE_EVAL_MIDTERM_STU
				: TABLE_EVAL_END_STU;
		StringBuilder sql = new StringBuilder();
		sql.append("UPDATE ").append(table).append(" SET notified_date=? ")
				.append("where application_id = ? AND eval_year = ? AND eval_semester = ? \n");
		return jdbcTemplate.update(sql.toString(), new Object[] { new Date(), applicationID, evalYear, evalSemester });
	}

	public List<String> getEvaluationMidTermStu(String applicationID, String evalYear, String evalSemester,
			String evalPoint) {
		String table = ProgramCode.TABLE_APPLICATION_DETAIL.get(ProgramCode.URS);

		StringBuilder sql = new StringBuilder();
		sql.append(
				"SELECT a.*, b.project_title,c.first_name,c.last_name,d.mentor_first_name,d.mentor_last_name,d.mentor_email,d.mentor_department \n");
		sql.append("FROM ").append(TABLE_EVAL_MIDTERM_STU).append(" a \n");
		sql.append("LEFT JOIN ").append(table).append(" b\n");
		sql.append("ON a.application_id = b.application_id\n");
		sql.append("LEFT JOIN ").append(TABLE_PROFILE_STUDENT).append(" c\n");
		sql.append("ON a.mentee_id = c.user_id\n");
		sql.append("LEFT JOIN ").append(TABLE_PROFILE_MENTOR).append(" d\n");
		sql.append("ON a.mentor_id = d.mentor_id\n");
		sql.append("WHERE a.application_id = ? AND a.eval_year = ? AND a.eval_semester = ? \n");

		try {
			List<String> list = jdbcTemplate.queryForObject(sql.toString(),
					new Object[] { applicationID, evalYear, evalSemester }, new RowMapper<List<String>>() {
						@Override
						public List<String> mapRow(ResultSet reSet, int rowNum) throws SQLException {
							if (reSet.getString("submit_date") == null) {
								return null;
							}
							List<String> list = new ArrayList<String>();
							list.add(reSet.getString("eval_semester") + " " + reSet.getInt("eval_year"));
							list.add(reSet.getString("application_id"));
							list.add(reSet.getString("project_title"));
							list.add(reSet.getString("mentor_first_name"));
							list.add(reSet.getString("mentor_last_name"));
							list.add(reSet.getString("mentor_email"));
							list.add(reSet.getString("mentor_department"));
							list.add(reSet.getString("first_name"));
							list.add(reSet.getString("last_name"));
							list.add(reSet.getString("question_1"));
							list.add(reSet.getString("question_2"));
							list.add(reSet.getString("question_3"));
							list.add(reSet.getString("submit_date"));
							return list;
						}
					});
			return list;
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	public List<List<String>> getEvaluationsMidTermStu(List<String> ids) {
		String table = ProgramCode.TABLE_APPLICATION_DETAIL.get(ProgramCode.URS);
		StringBuilder sql = new StringBuilder();
		sql.append(
				"SELECT a.*, b.project_title,c.first_name,c.last_name,d.mentor_first_name,d.mentor_last_name,d.mentor_email,d.mentor_department \n");
		sql.append("FROM ").append(TABLE_EVAL_MIDTERM_STU).append(" a \n");
		sql.append("LEFT JOIN ").append(table).append(" b\n");
		sql.append("ON a.application_id = b.application_id\n");
		sql.append("LEFT JOIN ").append(TABLE_PROFILE_STUDENT).append(" c\n");
		sql.append("ON a.mentee_id = c.user_id\n");
		sql.append("LEFT JOIN ").append(TABLE_PROFILE_MENTOR).append(" d\n");
		sql.append("ON a.mentor_id = d.mentor_id\n");
		sql.append("WHERE a.application_id IN (");
		sql.append(ids.stream().collect(Collectors.joining(",")));
		sql.append(")\n");

		return jdbcTemplate.query(sql.toString(), new Object[] {}, new RowMapper<List<String>>() {
			@Override
			public List<String> mapRow(ResultSet reSet, int rowNum) throws SQLException {
				List<String> list = new ArrayList<String>();
				list.add(reSet.getString("eval_semester") + " " + reSet.getInt("eval_year"));
				list.add(reSet.getString("application_id"));
				list.add(reSet.getString("project_title"));
				list.add(reSet.getString("mentor_first_name"));
				list.add(reSet.getString("mentor_last_name"));
				list.add(reSet.getString("mentor_email"));
				list.add(reSet.getString("mentor_department"));
				list.add(reSet.getString("first_name"));
				list.add(reSet.getString("last_name"));
				list.add(reSet.getString("question_1"));
				list.add(reSet.getString("question_2"));
				list.add(reSet.getString("question_3"));
				list.add(reSet.getString("submit_date"));
				return list;
			}
		});

	}

	/*
	 * Functions added by qixu
	 *
	 **/

	/*
	 * get Application by given application id
	 */
	public ApplicationBean getApplicationByApplicationID(String applicationID, String program) {
		StringBuilder sql = new StringBuilder();
		String tableAppDetail = ProgramCode.TABLE_APPLICATION_DETAIL.get(program);
		sql.append("SELECT * \n");
		sql.append("FROM ").append(TABLE_APPLICATION_LIST).append(" a \n");
		sql.append("LEFT JOIN ").append(tableAppDetail).append(" b\n");
		sql.append("ON a.application_id = b.application_id\n");
		sql.append("WHERE a.application_id = ");
		sql.append(applicationID);
		sql.append("\n");

		// System.out.println(sql);
		switch (program) {
		// programsTwoYr
		case ProgramCode.CCCONF:
			return getSingleApplicationURS(sql);

		case ProgramCode.MESA:
			return getSingleApplicationURS(sql);

		case ProgramCode.TRANS:
			return getSingleApplicationURS(sql);

		// programsFourYr
		case ProgramCode.URS:
			return getSingleApplicationURS(sql);

		case ProgramCode.SCCORE:
			return getSingleApplicationURS(sql);

		case ProgramCode.IREP:
			return getSingleApplicationURS(sql);
		}
		return null;

	}

	public ApplicationBean getSingleApplicationURS(StringBuilder sql) {
		return jdbcTemplate.queryForObject(sql.toString(), new RowMapper<ApplicationBean>() {
			@Override
			public ApplicationBean mapRow(ResultSet reSet, int rowNum) throws SQLException {

				ApplicationBean bean = new ApplicationBean();
				bean.setUserID(reSet.getInt("user_id"));
				bean.setApplicationID(reSet.getInt("application_id"));
				bean.setSchoolYear(reSet.getInt("school_year"));
				bean.setSchoolSemester(reSet.getString("school_semester"));
				bean.setSchoolTarget(reSet.getString("academic_school"));
				bean.setProgramNameAbbr(reSet.getString("program"));

				// get academicInfo
				AcademicBean academicBean = null;
				String academicSchool = reSet.getString("academic_school");
				Date academic_grad_date = reSet.getDate("academic_grad_date");
				if (academic_grad_date != null) {
					academicBean = new AcademicBean();
					academicBean.setAcademicSchool(academicSchool);
					academicBean.setAcademicYear(reSet.getString("academic_year"));
					academicBean.setAcademicGradDate(reSet.getDate("academic_grad_date"));
					academicBean.setAcademicBannerID(reSet.getString("academic_banner_id"));
					academicBean.setAcademicMajor(reSet.getString("academic_major"));
					academicBean.setAcademicMinor(reSet.getString("academic_minor"));
					academicBean.setAcademicGPA(reSet.getFloat("academic_gpa"));
					academicBean.setAcademicSchoolFullName();
					bean.setAcademicBean(academicBean);
				}

				// get essayBean
				EssayBean essayBean = null;
				String essayEducationalGoal = reSet.getString("essay_educational_goal");
				if (essayEducationalGoal != null) { // *
					essayBean = new EssayBean();
					essayBean.setEssayEducationalGoal(essayEducationalGoal);
					bean.setEssayBean(essayBean);
				}

				// get invInfo
				InvolvementBean involvementBean = null;
				String programEverIn = reSet.getString("program_ever_in");
				if (programEverIn != null) { // *
					involvementBean = new InvolvementBean();
					involvementBean.setProgramEverIn(Integer.parseInt(programEverIn));
					involvementBean.setProgramEverInSemesters(reSet.getString("program_ever_in_semesters"));

					String ampScholarship = reSet.getString("amp_scholarship");
					involvementBean.setAmpScholarship(Integer.parseInt(ampScholarship));
					if (ampScholarship.equals("1")) {
						involvementBean.setAmpScholarshipSchool(reSet.getString("amp_scholarship_school"));
						involvementBean.setAmpScholarshipType(reSet.getString("amp_scholarship_type"));
						involvementBean.setAmpScholarshipSemester(reSet.getString("amp_scholarship_semester"));
						involvementBean.setAmpScholarshipYear(reSet.getString("amp_scholarship_year"));
						involvementBean
								.setAmpScholarshipAmount(Float.parseFloat(reSet.getString("amp_scholarship_amount")));
					}

					involvementBean.setOtherScholarship(Integer.parseInt(reSet.getString("other_scholarship")));
					involvementBean.setListOtherScholarship(reSet.getString("list_other_scholarship"));
					bean.setInvolvementBean(involvementBean);
				}

				// get mentor info : mentorID => get mentor profile
				// MentorBean mentorBean = null;
				String mentorID = reSet.getString("mentor_id");
				if (mentorID != null) {
					bean.setMentorID(Integer.parseInt(mentorID));
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

				/* Transcript */
				Blob blob = reSet.getBlob("transcript_content");
				if (blob != null && blob.length() > 0) {
					byte[] content = blob.getBytes(1, (int) blob.length());
					blob.free();
					FileBucket fileBucket = new FileBucket();
					fileBucket.setFileContent(content);
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
						bean.setApplicantSubmitDate(Parse.FORMAT_DATE_MDY.parse(applicantSubmitDate));

						String completeDate = reSet.getString("complete_date");
						if (completeDate != null) {
							date = Parse.FORMAT_DATETIME.parse(completeDate);
							completeDate = Parse.FORMAT_DATE_MDY.format(date);
							bean.setCompleteDate(Parse.FORMAT_DATE_MDY.parse(completeDate));
						}
					} catch (ParseException e) {
						e.printStackTrace();
					}
				}
				return bean;
			}
		});
	}
}
