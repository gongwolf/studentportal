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

import org.bitbucket.lvncnt.portal.model.ApplicationBean;
import org.bitbucket.lvncnt.portal.model.EvaluationBean;
import org.bitbucket.lvncnt.portal.model.ProjectBean;
import org.bitbucket.lvncnt.portal.model.SignatureBean;
import org.bitbucket.lvncnt.portal.service.ProgramCode;
import org.bitbucket.lvncnt.utilities.Parse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.*;

@Repository("mentorDAO")
public class MentorDAOImpl implements Schemacode{
 
	@Autowired
	private JdbcTemplate jdbcTemplate; 
	
	static{
		TimeZone.setDefault(TimeZone.getTimeZone("America/Denver"));
	}

	public List<ApplicationBean> getMenteeList(int mentorID){
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT a.user_id, a.application_id,a.school_year, a.school_semester, "
			+ "a.applicant_submit_date, a.mentor_submit_date,b.first_name, b.last_name, b.email\n");
		sql.append("FROM ").append(TABLE_APPLICATION_LIST).append(" a \n"); 
		sql.append("LEFT JOIN ").append(TABLE_USER).append(" b\n"); 
		sql.append("ON a.user_id = b.user_id\n"); 
		sql.append("WHERE a.mentor_id=? ORDER BY mentor_submit_date IS NULL DESC, mentor_submit_date DESC");
		
		return jdbcTemplate.query(sql.toString(), new Object[]{mentorID}, 
				new ResultSetExtractor<List<ApplicationBean>>(){

				@Override
				public List<ApplicationBean> extractData(ResultSet reSet)
						throws SQLException, DataAccessException {
					List<ApplicationBean> menteeList = new ArrayList<ApplicationBean>();
					while(reSet.next()){
						ApplicationBean bean = new ApplicationBean(); 
						Integer menteeID = reSet.getInt("user_id");
						String menteeFirstName = reSet.getString("first_name");
						String menteeLastName = reSet.getString("last_name");
						String menteeEmail = reSet.getString("email");
						
						bean.setUserID(menteeID);
						bean.setApplicationID(reSet.getInt("application_id"));
						bean.setSchoolYear(reSet.getInt("school_year"));
						bean.setSchoolSemester(reSet.getString("school_semester"));
						bean.setFirstName(menteeFirstName);
						bean.setLastName(menteeLastName);
						bean.setEmail(menteeEmail);
						Date date = null;
						String start = reSet.getString("mentor_submit_date");
						if (start != null) {
							try {
								date = Parse.FORMAT_DATETIME.parse(start);
								start = Parse.FORMAT_DATE_MDY.format(date);
								bean.setMentorSubmitDate(Parse.FORMAT_DATE_MDY.parse(start));
							} catch (ParseException e) {
								e.printStackTrace();
							}
						}
						 
						menteeList.add(bean); 
					}
					return menteeList;
				}
			}); 
		
	}
	 
	public ApplicationBean getMenteeBean(int mentorID, int applicationID){
		StringBuilder sql = new StringBuilder();
		sql.append(
				"SELECT a.user_id, a.application_id, "
				+ "a.school_year, a.school_semester, "
				+ "b.first_name, b.last_name, b.email\n");
		
		sql.append("FROM ").append(TABLE_APPLICATION_LIST).append(" a \n"); 
		sql.append("LEFT JOIN ").append(TABLE_USER).append(" b\n"); 
		sql.append("ON a.user_id = b.user_id\n"); 
		sql.append("WHERE a.application_id=? AND a.mentor_id=?");
		
		try{
			return jdbcTemplate.queryForObject(sql.toString(), 
				new Object[]{applicationID, mentorID}, 
				new RowMapper<ApplicationBean>() {
					@Override
					public ApplicationBean mapRow(ResultSet reSet, int rowNum)
							throws SQLException {
						ApplicationBean bean = new ApplicationBean(); 
						Integer menteeID = reSet.getInt("user_id");
						String menteeFirstName = reSet.getString("first_name");
						String menteeLastName = reSet.getString("last_name");
						String menteeEmail = reSet.getString("email");
						
						bean.setUserID(menteeID);
						bean.setApplicationID(reSet.getInt("application_id"));
						bean.setSchoolYear(reSet.getInt("school_year"));
						bean.setSchoolSemester(reSet.getString("school_semester"));
						bean.setFirstName(menteeFirstName);
						bean.setLastName(menteeLastName);
						bean.setEmail(menteeEmail);
						 
						return bean; 
					}
				}); 
		}catch(EmptyResultDataAccessException e){
			return null; 
		}
	}
	
	public ProjectBean getProjectBean(int menteeID, int applicationID){
		String table = ProgramCode.TABLE_APPLICATION_DETAIL.get(ProgramCode.URS);
		String sql = "SELECT * FROM " + table + " WHERE user_id = ? AND application_id = ?";
	 
		try{
			return jdbcTemplate.queryForObject(sql, new Object[]{menteeID, applicationID}, 
				new RowMapper<ProjectBean>(){

					@Override
					public ProjectBean mapRow(ResultSet reSet, int rowNum)
							throws SQLException {
						ProjectBean projectBean = null; 
						String projectTitle = reSet.getString("project_title");
						if(projectTitle != null){
							projectBean = new ProjectBean(); 
							projectBean.setProjectTitle(projectTitle);
							Integer isExternal = reSet.getInt("project_is_external"); 
							projectBean.setExternalProject(isExternal);;
							if(isExternal != null && isExternal == 1){
								projectBean.setExternalAgency(reSet.getString("project_external_agency"));
								projectBean.setExternalTitle(reSet.getString("project_external_title"));
								projectBean.setExternalDuration(reSet.getString("project_external_duration"));
							}
							projectBean.setProjectGoal(reSet.getString("project_goal"));
							projectBean.setProjectMethod(reSet.getString("project_method"));
							projectBean.setProjectResult(reSet.getString("project_result"));
							projectBean.setProjectTask(reSet.getString("project_task"));
							projectBean.setMentorSignature(reSet.getString("project_mentor_signature")); // *
							 
							try {
								Date date = null;
								String start = reSet.getString("project_mentor_signature_date"); // *
								if (start != null) {
									date = Parse.FORMAT_DATETIME.parse(start);
									start = Parse.FORMAT_DATE_MDY.format(date);
									projectBean.setMentorSignatureDate(Parse.tryParseDateMDY(start));
								}
							} catch (NullPointerException | ParseException e) {
								e.printStackTrace();
							}
							 
						}
 						return projectBean;
					}
				
			}); 
		}catch(EmptyResultDataAccessException e){
			return null; 
		}
	}
	
	public boolean isProjectComplete(int applicationID){
		String table = ProgramCode.TABLE_APPLICATION_DETAIL.get(ProgramCode.URS);
		String sql = "SELECT project_title,project_goal,project_method,project_task FROM " + table
				+ " WHERE application_id = ?";
		try{
			return jdbcTemplate.query(sql, new Object[]{applicationID}, 
				new ResultSetExtractor<Boolean>(){
				@Override
				public Boolean extractData(ResultSet reSet) throws SQLException,
						DataAccessException {
					if (reSet.next()) {
						if(reSet.getString("project_title") != null
								&& reSet.getString("project_goal") != null
								&& reSet.getString("project_method") != null
								&& reSet.getString("project_task") != null){
							return true; 
						} 
					}
					return false;
				}
			}); 
			
		}catch(EmptyResultDataAccessException e){
			return false; 
		}
	}
	
	public int submitProject(int applicationID, SignatureBean bean){
		String table = ProgramCode.TABLE_APPLICATION_DETAIL.get(ProgramCode.URS);
		String sql1 = "UPDATE " + table
				+ " SET project_mentor_signature = ?, project_mentor_signature_date = ? "
				+ "WHERE application_id = ?";
		String sql2 = "UPDATE " + TABLE_APPLICATION_LIST
				+ " SET mentor_submit_date = ? "
				+ "WHERE application_id = ?";
		int result = jdbcTemplate.update(sql1, new Object[]{
			bean.getSignature(), bean.getSignatureDate(), applicationID 	
		}); 
		
		if(result == 0) return 0; 
		return jdbcTemplate.update(sql2, new Object[]{
				new Date(), applicationID
		}); 
	 
	}
	
	/************************
	 * Mentee Evaluations 
	 ************************/
	public List<EvaluationBean> getEvaluations(int mentorID) {
		 
		StringBuilder sql = new StringBuilder();
		sql.append("(SELECT application_id,eval_year,eval_semester,deadline,submit_date,notified_date,'Mid-Term' as eval_point,b.first_name,b.last_name \n");
		sql.append("FROM ").append(TABLE_EVAL_MIDTERM).append(" AS a\n")
		   .append("LEFT JOIN ").append(TABLE_PROFILE_STUDENT).append(" AS b\n")
		   .append("ON a.mentee_id = b.user_id").append(" WHERE a.mentor_id=? )\n");
		
		sql.append("UNION ALL \n");
		sql.append("(SELECT application_id,eval_year,eval_semester,deadline,submit_date,notified_date,'End-Of-Semester' as eval_point,b.first_name,b.last_name \n");
		sql.append("FROM ").append(TABLE_EVAL_END).append(" AS a\n")
		   .append("LEFT JOIN ").append(TABLE_PROFILE_STUDENT).append(" AS b\n")
		   .append("ON a.mentee_id = b.user_id").append(" WHERE a.mentor_id=? )\n")
		   .append("ORDER BY submit_date DESC, notified_date DESC");
	 
		return jdbcTemplate.query(sql.toString(), new Object[]{mentorID, mentorID}, 
				new RowMapper<EvaluationBean>() {
					@Override
					public EvaluationBean mapRow(ResultSet reSet, int rowNum)
							throws SQLException {
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
	
	public int setEvaluationMidTerm(String applicationID, String evalYear, 
			String evalSemester, HashMap<String, Object> map) {
		StringBuilder sql = new StringBuilder();
		sql.append("UPDATE ").append(TABLE_EVAL_MIDTERM)
				.append(" SET rating_report=?,rating_has=?,rating_complete=?,rating_demonstrate=?,rating_gain=?,radio_meet=?, ")
				.append("quest_assessment=?,quest_conference=?,quest_concern=?,quest_assist=?,submit_date=? ")
				.append("WHERE application_id = ? AND eval_year = ? AND eval_semester = ? ");
  
		return jdbcTemplate.update(sql.toString(), new Object[]{
			map.get("rating-report"), map.get("rating-has"), map.get("rating-complete"), 
	        map.get("rating-demonstrate"), map.get("rating-gain"), map.get("radio-meet"), 
	        map.get("quest-assessment"), map.get("quest-conference"),
	        map.get("quest-concern"), map.get("quest-assist"), new Date(), applicationID, evalYear, evalSemester
		}); 
	}
	
	public int setEvaluationEnd(String applicationID, String evalYear, 
			String evalSemester, HashMap<String, Object> map) {
		StringBuilder sql = new StringBuilder();
		sql.append("UPDATE ").append(TABLE_EVAL_END)
				.append(" SET rating_lab_1=?,rating_lab_2=?,rating_lab_3=?,")
				.append("rating_organization_1=?,rating_organization_2=?,rating_organization_3=?,rating_organization_4=?,")
				.append("rating_independent_1=?,rating_independent_2=?,")
				.append("rating_writing_1=?,rating_writing_2=?,")
				.append("quest_techniques=?,quest_publications=?,quest_conferences=?,quest_assessment=?,submit_date=? ")
				.append("WHERE application_id = ? AND eval_year = ? AND eval_semester = ? ");
  
		return jdbcTemplate.update(sql.toString(), new Object[]{
			map.get("rating-lab-1"),map.get("rating-lab-2"),map.get("rating-lab-3"),
			map.get("rating-organization-1"),map.get("rating-organization-2"),
			map.get("rating-organization-3"),map.get("rating-organization-4"),
			map.get("rating-independent-1"),map.get("rating-independent-2"),
			map.get("rating-writing-1"),map.get("rating-writing-2"),
			map.get("quest-techniques"),map.get("quest-publications"),
			map.get("quest-conferences"),map.get("quest-assessment"), 
			new Date(), applicationID, evalYear, evalSemester
		}); 
	}
	
}
