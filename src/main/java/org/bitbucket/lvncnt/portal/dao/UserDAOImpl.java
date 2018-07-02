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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.bitbucket.lvncnt.portal.model.*;
import org.bitbucket.lvncnt.portal.service.ProgramCode;
import org.bitbucket.lvncnt.utilities.Parse;
import org.bitbucket.lvncnt.utilities.Security;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.stream.Collectors;

@Repository("userDAO")
public class UserDAOImpl implements UserDAO, Schemacode{
 
	@Autowired
	private TransactionTemplate transactionTemplate; 
	@Autowired
	private JdbcTemplate jdbcTemplate; 
	
	@Autowired
	private ObjectMapper objectMapper; 
 
	static{
		TimeZone.setDefault(TimeZone.getTimeZone("America/Denver"));
	}
	 
	@Autowired
	BCryptPasswordEncoder passwordEncoder; 

	@Override
	public User get(String email) {
		//System.out.println("email "+email);
		StringBuilder sql = new StringBuilder(); 
		sql.append("SELECT user_id, email, first_name, last_name, birth_date, password, role FROM "); 
		sql.append(TABLE_USER).append(" WHERE email = ? LIMIT 1");
		try{
			User user = jdbcTemplate.queryForObject(sql.toString(), 
					new Object[]{email}, 
					new RowMapper<User>() {
						@Override
						public User mapRow(ResultSet reSet, int rowNum)
								throws SQLException {
							User user = new User(); 
							user.setUserID(reSet.getInt("user_id"));
							user.setEmail(reSet.getString("email"));
							user.setFirstName(reSet.getString("first_name"));
							user.setLastName(reSet.getString("last_name"));
							user.setBirthDate(reSet.getDate("birth_date"));
							user.setPassword(reSet.getString("password"));
  
							user.setRole(Role.valueOf(reSet.getString("role")));
							return user; 
						}
					}); 
			
			switch (user.getRole()){
			case USER:
				break; 
			case STAFF: 
			case ADMIN: 
				String sql2 = "SELECT affiliation FROM "+ TABLE_PROFILE_STAFF + " WHERE user_id = ? LIMIT 1"; 
				String affiliation = jdbcTemplate.queryForObject(sql2, new Object[]{user.getUserID()}, 
						new RowMapper<String>() {
							@Override
							public String mapRow(ResultSet reSet, int rowNum)
									throws SQLException {
								return reSet.getString("affiliation"); 
							}
						}); 
				user.setAffiliation(affiliation);
				break;
			default:
				break; 
			}
			return user; 
		}catch(EmptyResultDataAccessException e){
			return null; 
		}
	}
	 
	public User getUserByUserID(int userID) {
		StringBuilder sql = new StringBuilder(); 
		sql.append("SELECT user_id, email, first_name, last_name, birth_date FROM "); 
		sql.append(TABLE_USER).append(" WHERE user_id = ? LIMIT 1");
		 
		try{
			User user = jdbcTemplate.queryForObject(sql.toString(), 
					new Object[]{userID}, 
					new RowMapper<User>() {
						@Override
						public User mapRow(ResultSet reSet, int rowNum)
								throws SQLException {
						  
							User user = new User(); 
							user.setUserID(reSet.getInt("user_id"));
							user.setEmail(reSet.getString("email"));
							user.setFirstName(reSet.getString("first_name"));
							user.setLastName(reSet.getString("last_name"));
							user.setBirthDate(reSet.getDate("birth_date"));
							return user; 
						}
					}); 
			return user; 
		}catch(EmptyResultDataAccessException e){
			return null; 
		}
	}
	
	/**
	 * Register 
	 */
	public int isAccountExist(String email) {
		try{
			return jdbcTemplate.queryForObject(
					"SELECT user_id FROM " + TABLE_USER + " WHERE email = ?", 
					new Object[]{email.trim()}, Integer.class); 
		}catch(EmptyResultDataAccessException e){
			return 0; 
		}
	}
	
	public int register(User student){
		
		String sql = "INSERT INTO " + TABLE_USER
				+ " (user_id, role, first_name, last_name, birth_date, email, password) VALUES (?,?,?,?,?,?,?)";

		int userID = generateUserID(); 
		int result = jdbcTemplate.update(sql, new Object[]{
				userID, Role.USER.toString(), student.getFirstName().trim(), student.getLastName().trim(), 
				student.getBirthDate(), student.getEmail().trim(), passwordEncoder.encode(student.getPassword())	
		});
		return result == 0 ? 0 : userID;
	}

	public int registerMentor( String firstName, String lastName, String email, String password){
		
		String sql = "INSERT INTO " + TABLE_USER
				+ " (user_id, role, first_name, last_name, birth_date, email, password) VALUES (?,?,?,?,?,?,?)";

		int userID = generateUserID(); 
		String cryptedPassword = passwordEncoder.encode(password);
		jdbcTemplate.update(sql, new Object[]{
				userID, Role.MENTOR.toString(), firstName, lastName, 
				new Date(), email.trim(), cryptedPassword	
		});
		return userID;
	}
	 
	private int generateUserID() {
		int id = 0;
		String sql = "SELECT user_id FROM " + TABLE_USER + " WHERE user_id = ?";
		do {
			id = (int) Math.floor(1000000 + Math.random() * 8999999);
		} while (isExist(sql, new Object[]{id}));
		return id;
	}
	
	public boolean isExist(String sql, Object... args) {
	    boolean result = jdbcTemplate.query(sql, args, new ResultSetExtractor<Boolean>() {
	        @Override
	        public Boolean extractData(ResultSet rs) throws SQLException,DataAccessException {
	            boolean result = rs.next();
	            return result;
	        }
	    });
	    return result;
	}
	
	/**
	 * Update Profile: password
	 */
	public int updatePassword(int userID, String newPassword) {
		int result = 0;
		String sql = "UPDATE " + TABLE_USER + " SET password = ? WHERE user_id = ?";
		String cryptedPassword = passwordEncoder.encode(newPassword); 
		result = jdbcTemplate.update(sql, new Object[]{cryptedPassword, userID});
		return result;
	}
    public String updateResetKey(String email){
    	String randomStr = UUID.randomUUID().toString();
		Timestamp outDate = new Timestamp(System.currentTimeMillis() + 24 * 60 * 60 * 1000);
		long resetTimeout = outDate.getTime(); 
		String str = email + "$" + resetTimeout + "$" + randomStr; 
		String resetKey = Security.digest(str, Security.SHA256);
		
		String sql = "UPDATE " + TABLE_USER
				+ " SET reset_key = ?, reset_timeout = ? WHERE email = ?";
		int result = jdbcTemplate.update(sql, new Object[]{
			resetKey, resetTimeout, email
		}); 
		  
		return result == 0 ? null : resetKey; 
    }
	
    public boolean isResetPasswordKeyValid(String resetKey){
    	if(resetKey == null || resetKey.length() == 0){
    		return false; 
    	}
    	
    	long currTime = new Timestamp(System.currentTimeMillis()).getTime();
    	String sql =  "SELECT reset_timeout FROM " + TABLE_USER
				+ " WHERE reset_key = ?";
    	
    	try{
    		return jdbcTemplate.query(sql, 
    				new Object[]{resetKey}, 
    				new ResultSetExtractor<Boolean>(){

						@Override
						public Boolean extractData(ResultSet reSet)
								throws SQLException, DataAccessException {
							if (reSet.next()) {
								long resetTimeout = reSet.getLong("reset_timeout");
								if(currTime <= resetTimeout){
									return true; 
								}
							}
							return false; 
						}
    			
    		}); 
    		}catch(EmptyResultDataAccessException e){
    			return false ;
    		}
    }

    public int updatePassword(String resetKey, String newPassword){
    	String sql = "UPDATE " + TABLE_USER + " SET password = ? WHERE reset_key = ?";
    	String cryptedPassword = passwordEncoder.encode(newPassword); 
		return jdbcTemplate.update(sql, new Object[]{cryptedPassword, resetKey});
    }
    
    public int updatePassword(String firstName, String lastName,String email, String newPassword){
    	String sql = "UPDATE " + TABLE_USER + 
    			" SET password = ? WHERE first_name=? AND last_name=? AND email=? AND role <> ?";
		return jdbcTemplate.update(sql, new Object[]{passwordEncoder.encode(newPassword), 
				firstName, lastName, email, Role.ADMIN.toString()});
    }
    
    public boolean checkAdminIdentity(String rawpassword){
    	try{
			String sql = "SELECT password FROM " + TABLE_USER + " WHERE role = ?";
			return jdbcTemplate.query(sql, new Object[]{Role.ADMIN.toString()}, 
				new ResultSetExtractor<Boolean>(){
				@Override
				public Boolean extractData(ResultSet reSet) throws SQLException,
						DataAccessException {
						if(reSet.next()){
							return passwordEncoder.matches(rawpassword, reSet.getString("password"));
						}
					return false;
				}
			}); 
		}catch(EmptyResultDataAccessException e){
			return false; 
		}
    }
    
	/**
	 * Create profile in profile_student/mentor table 
	 */
	public int createProfileStudent(int userID) {
		String sql = "INSERT INTO " + TABLE_PROFILE_STUDENT
				+ " (user_id,first_name,last_name,birth_date,email) "
				+ "SELECT user_id,first_name,last_name,birth_date,email FROM " + TABLE_USER 
				+ " WHERE user_id = ?";
		return jdbcTemplate.update(sql, new Object[]{userID}); 
	}
	public int createProfileMentor(int userID) {
		String sql = "INSERT INTO " + TABLE_PROFILE_MENTOR
				+ " (mentor_id,mentor_first_name,mentor_last_name,mentor_email) "
				+ "SELECT user_id,first_name,last_name,email FROM " + TABLE_USER 
				+ " WHERE user_id = ?";
		return jdbcTemplate.update(sql, new Object[]{userID}); 
	}
	
	/**
	 * Update Profile: Admin email
	 */
	public int updateProfileAdmin(int userID, Role userRole, String newEmail) {
		
		int result = 0;
		 
		String sql = "UPDATE " + TABLE_USER
				+ " SET email = ? WHERE user_id = ? AND role = ?";
		result = jdbcTemplate.update(sql, new Object[]{newEmail, userID, userRole.toString()}); 
		return result;
	}
	
	/** List all user */ 
	@Override
	public List<User> list() {
		String SQL_SELECT = "SELECT user_id, email, first_name, last_name FROM " + "user";
		List<User> listUser = jdbcTemplate.query(SQL_SELECT, new RowMapper<User>(){
			//use of RowMapper to map a row in the result set to a POJO object.
			@Override
			public User mapRow(ResultSet reSet, int rowNum) throws SQLException {
				User user = new User(); 
				user.setUserID(reSet.getInt("user_id"));
				user.setEmail(reSet.getString("email"));
				user.setFirstName(reSet.getString("first_name"));
				user.setLastName(reSet.getString("last_name"));
				return user;
			}
		}); 
		return listUser;
	}
	
	/** Insert or update */ 
	public void saveOrUpdate(User user){
		if(user.getUserID() > 0){
			String sql = "UPDATE user SET first_name=?, last_name=?, "
	                + "email=? WHERE user_id=?";
			jdbcTemplate.update(sql, 
					user.getFirstName(), user.getLastName(), user.getEmail(), user.getUserID()); 
		}else{
		    String sql = "INSERT INTO user (first_name, last_name, email)"
		                + " VALUES (?, ?, ?)";
		    jdbcTemplate.update(sql, 
					user.getFirstName(), user.getLastName(), user.getEmail()); 
		}

	}
	
	/** Delete */ 
	public void delete(int userID) {
	    String sql = "DELETE FROM user WHERE user_id=?";
	    jdbcTemplate.update(sql, userID);
	}
	
	
	public ProfileBean getProfileStudent(int userID){
		String sql = "SELECT * FROM " + TABLE_PROFILE_STUDENT
				+ " WHERE user_id = ?";
		try{
			ProfileBean profileBean = jdbcTemplate.queryForObject(sql, 
				new Object[]{userID}, 
				new RowMapper<ProfileBean>() {
					@Override
					public ProfileBean mapRow(ResultSet reSet, int rowNum)
							throws SQLException {
					  
						ProfileBean bean = new ProfileBean(); 
						// biography info
						bean.setUserID(reSet.getInt("user_id"));
						BiographyBean biographyBean = new BiographyBean();
						biographyBean.setFirstName(reSet.getString("first_name"));
						biographyBean.setLastName(reSet.getString("last_name"));
						biographyBean.setMiddleName(reSet.getString("middle_name"));
						biographyBean.setBirthDate(reSet.getDate("birth_date"));
						
						if(!Parse.isBlankOrNull(reSet.getString("ssn_last_four"))) {
							biographyBean.setSsnLastFour(reSet.getString("ssn_last_four"));
							if(!Parse.isBlankOrNull(reSet.getString("gender"))) {
								biographyBean.setGender(reSet.getString("gender"));
							}
							if(!Parse.isBlankOrNull(reSet.getString("is_nm_resident"))) {
								biographyBean.setIsNMResident(reSet.getInt("is_nm_resident"));
							}
							if(!Parse.isBlankOrNull(reSet.getString("citizenship"))) {
								biographyBean.setCitizenship(reSet.getString("citizenship"));
							}
							if(!Parse.isBlankOrNull(reSet.getString("parent_has_degree"))) {
								biographyBean.setParentHasDegree(reSet.getInt("parent_has_degree"));
							}
						} 
						bean.setBiographyBean(biographyBean);
						
						// contact info
						ContactBean contactBean = null;
						if(reSet.getString("email") != null){
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
							if(race != null){
								ethnicityBean.setRace(Arrays.asList(race.trim().split("\\s*,\\s*")));
							}
							bean.setEthnicityBean(ethnicityBean);
						}
						
						// get Disclosure form 
						FileBucket fileBucket = null;
						Blob transcriptBlob = reSet.getBlob("disclosure_form_content"); 
						if (transcriptBlob != null && transcriptBlob.length() != 0) {
							fileBucket = new FileBucket(); 
							fileBucket.setFileExist(1);
							bean.setFileBucket(fileBucket);
						}
						return bean; 
					}
				}); 
			return profileBean; 
		}catch(EmptyResultDataAccessException e){
			return null; 
		}
	}
	
	public User getProfileStudentBasic(int userID){
		String sql = "SELECT user_id,email,first_name,last_name,birth_date FROM " + TABLE_USER
				+ " WHERE user_id = ?";
		try{
			User user = jdbcTemplate.queryForObject(sql, 
				new Object[]{userID}, 
				new RowMapper<User>() {
					@Override
					public User mapRow(ResultSet reSet, int rowNum)
							throws SQLException {
						User user = new User(); 
						user.setUserID(reSet.getInt("user_id"));
						user.setEmail(reSet.getString("email"));
						user.setFirstName(reSet.getString("first_name"));
						user.setLastName(reSet.getString("last_name"));
						user.setBirthDate(reSet.getDate("birth_date"));
						return user; 
					}
				}); 
			return user; 
		}catch(EmptyResultDataAccessException e){
			return null; 
		}
	}
	
	/**
	 * Update Profile: Student Biography
	 */
	public int updateProfileStudentBiography(int userID, BiographyBean bean) {
  
		String sql = "INSERT INTO " + TABLE_PROFILE_STUDENT
				+ " (user_id,middle_name,ssn_last_four,gender,is_nm_resident,citizenship,parent_has_degree) VALUES (?,?,?,?,?,?,?)"
				+ " ON DUPLICATE KEY UPDATE "
				+ "middle_name = VALUES(middle_name), "
				+ "ssn_last_four = VALUES(ssn_last_four), "
				+ "gender = VALUES(gender), "
				+ "is_nm_resident = VALUES(is_nm_resident), "
				+ "citizenship = VALUES(citizenship), "
				+ "parent_has_degree = VALUES(parent_has_degree)" ; 
		return jdbcTemplate.update(sql, new Object[]{
				userID, bean.getMiddleName(),
				bean.getSsnLastFour(), bean.getGender(), 
				bean.getIsNMResident(), bean.getCitizenship(), bean.getParentHasDegree()
		}); 
	}
	
	/**
	 * Update Profile: Student Ethnicity
	 */
	public int updateProfileStudentEthnicity(int userID, EthnicityBean bean) {
		String sql = "INSERT INTO " + TABLE_PROFILE_STUDENT
				+ " (user_id,is_hispanic,race,disability"
				+ ") VALUES (?,?,?,?)"
				+ " ON DUPLICATE KEY UPDATE "
				+ "is_hispanic = VALUES(is_hispanic), "
				+ "race = VALUES(race), "
				+ "disability = VALUES(disability)";
		return jdbcTemplate.update(sql, new Object[]{
				userID, bean.getIsHispanic(), 
				String.join(",", bean.getRace()), bean.getDisability()
		}); 
	}
	
	/**
	 * Update Profile: Student Contact
	 */
	public int updateProfileStudentContact(int userID, ContactBean bean) {
		String sql = "INSERT INTO " + TABLE_PROFILE_STUDENT
				+ " (user_id, "
				+ "email_alternate, phone_num1, phone_type1, phone_num2, phone_type2,receive_sms,"
				+ "permanent_address_line1,permanent_address_line2,permanent_address_city,permanent_address_state,permanent_address_zip,permanent_address_county,"
				+ "current_address_line1,current_address_line2,current_address_city,current_address_state,current_address_zip,current_address_county,"
				+ "ec_first_name,ec_last_name,ec_relationship,ec_phone_num1,ec_phone_type1,ec_phone_num2,"
				+ "ec_phone_type2,ec_address_line1,ec_address_line2,ec_address_city,ec_address_state,ec_address_zip,ec_address_county"
				+ ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"
				+ " ON DUPLICATE KEY UPDATE "
				+ "email_alternate = VALUES(email_alternate), "
				+ "phone_num1 = VALUES(phone_num1), "
				+ "phone_type1 = VALUES(phone_type1), "
				+ "phone_num2 = VALUES(phone_num2), "
				+ "phone_type2 = VALUES(phone_type2), "
				+ "receive_sms = VALUES(receive_sms), "
				+ "permanent_address_line1 = VALUES(permanent_address_line1), "
				+ "permanent_address_line2 = VALUES(permanent_address_line2), "
				+ "permanent_address_city = VALUES(permanent_address_city), "
				+ "permanent_address_state = VALUES(permanent_address_state), "
				+ "permanent_address_zip = VALUES(permanent_address_zip), "
				+ "permanent_address_county = VALUES(permanent_address_county), "
				+ "current_address_line1 = VALUES(current_address_line1), "
				+ "current_address_line2 = VALUES(current_address_line2), "
				+ "current_address_city = VALUES(current_address_city), "
				+ "current_address_state = VALUES(current_address_state), "
				+ "current_address_zip = VALUES(current_address_zip), "
				+ "current_address_county = VALUES(current_address_county), "
				+ "ec_first_name = VALUES(ec_first_name), "
				+ "ec_last_name = VALUES(ec_last_name), "
				+ "ec_relationship = VALUES(ec_relationship), "
				+ "ec_phone_num1 = VALUES(ec_phone_num1), "
				+ "ec_phone_type1 = VALUES(ec_phone_type1), "
				+ "ec_phone_num2 = VALUES(ec_phone_num2), "
				+ "ec_phone_type2 = VALUES(ec_phone_type2), "
				+ "ec_address_line1 = VALUES(ec_address_line1), "
				+ "ec_address_line2 = VALUES(ec_address_line2), "
				+ "ec_address_city = VALUES(ec_address_city), "
				+ "ec_address_state = VALUES(ec_address_state), "
				+ "ec_address_zip = VALUES(ec_address_zip), "
				+ "ec_address_county = VALUES(ec_address_county)";
		
		return jdbcTemplate.update(sql, new Object[]{
			// Email and Phone
			userID, bean.getEmailAltn(), 
			bean.getPhoneNum1(), bean.getPhoneType1(), 
			bean.getPhoneNum2(), bean.getPhoneType2(), 
			(bean.getReceiveSMS() == null) ? 0 : 1,
			// Permanent address
			bean.getPermanentAddressLine1(),bean.getPermanentAddressLine2(),
			bean.getPermanentAddressCity(),bean.getPermanentAddressState(),
			bean.getPermanentAddressZip(),bean.getPermanentAddressCounty(),
			// Current Address
			bean.getCurrentAddressLine1(),bean.getCurrentAddressLine2(),
			bean.getCurrentAddressCity(),bean.getCurrentAddressState(),
			bean.getCurrentAddressZip(),bean.getCurrentAddressCounty(),

			// Emergency Contact
			bean.getEcFirstName(),bean.getEcLastName(),
			bean.getEcRelationship(),bean.getEcPhoneNum1(),
			bean.getEcPhoneType1(),bean.getEcPhoneNum2(),
			bean.getEcPhoneType2(),bean.getEcAddressLine1(),
			bean.getEcAddressLine2(),bean.getEcAddressCity(),
			bean.getEcAddressState(),bean.getEcAddressZip(),bean.getEcAddressCounty()
		}); 
	}
	
	public int uploadDisclosureForm(int userID, FileBucket fileBucket){
		String sql = "INSERT INTO " + TABLE_PROFILE_STUDENT
				+ " (user_id,disclosure_form_name,disclosure_form_type,disclosure_form_size,disclosure_form_content"
				+ ") VALUES (?,?,?,?,?)" + " ON DUPLICATE KEY UPDATE "
				+ "disclosure_form_name = VALUES(disclosure_form_name), disclosure_form_type = VALUES(disclosure_form_type), "
				+ "disclosure_form_size = VALUES(disclosure_form_size), disclosure_form_content = VALUES(disclosure_form_content)";
	
		MultipartFile file = fileBucket.getFile(); 
		return jdbcTemplate.update(sql, new PreparedStatementSetter(){
			@Override
			public void setValues(PreparedStatement ps) throws SQLException {
				ps.setInt(1, userID);
				ps.setString(2, file.getOriginalFilename());
				ps.setString(3, file.getContentType());
				ps.setLong(4, file.getSize());
				try {
					ps.setBytes(5, file.getBytes());
				} catch (IOException e) {
					return; 
				}
			}
		});  
	}
	
	public byte[] downloadDisclosureForm(int userID){
		String sql = "SELECT disclosure_form_content FROM " + TABLE_PROFILE_STUDENT
				+ " WHERE user_id = ?";
		return jdbcTemplate.queryForObject(sql, byte[].class, new Object[]{userID}); 
	}
	
	/**
	 * Update Profile: Student Personal Statement
	 */
	public int updateProfilePersonalStatement(int userID, String ps) {
		String sql = "INSERT INTO " + TABLE_PROFILE_STUDENT
				+ " (user_id,personal_statement"
				+ ") VALUES (?,?)"
				+ " ON DUPLICATE KEY UPDATE "
				+ "personal_statement = VALUES(personal_statement)";
		return jdbcTemplate.update(sql, new Object[]{ userID, ps}); 
	}
	
	public String getProfilePersonalStatement(int userID) {
		String sql = "SELECT personal_statement FROM " + TABLE_PROFILE_STUDENT
				+ " WHERE user_id = ?";
		return jdbcTemplate.queryForObject(sql, String.class, new Object[]{userID});
	}
	
	/***********************************
	 *  Check completion of  profile 
	 **********************************/
	public boolean isProfileComplete(Integer userID) {
		String sql = "SELECT middle_name,ssn_last_four,phone_num1,is_hispanic,disclosure_form_content FROM "
				+ TABLE_PROFILE_STUDENT + " WHERE user_id = ?";
		try{
			return jdbcTemplate.query(sql, new Object[]{userID}, new ResultSetExtractor<Boolean>(){
				@Override
				public Boolean extractData(ResultSet reSet) throws SQLException,
						DataAccessException {
					if (reSet.next()) {
						Blob blob = reSet.getBlob("disclosure_form_content");
						if(!Parse.isBlankOrNull(reSet.getString("middle_name")) &&
								!Parse.isBlankOrNull(reSet.getString("ssn_last_four")) &&
								!Parse.isBlankOrNull(reSet.getString("phone_num1")) &&
								!Parse.isBlankOrNull(reSet.getString("middle_name")) &&
								!Parse.isBlankOrNull(reSet.getString("middle_name")) &&
								 (blob != null && blob.length() != 0)){
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
	public boolean isMentorProfileComplete(int mentorID) {
		String sql = "SELECT mentor_title,is_hispanic FROM "
				+ TABLE_PROFILE_MENTOR + " WHERE mentor_id = ?";
		try{
			return jdbcTemplate.query(sql, new Object[]{mentorID}, new ResultSetExtractor<Boolean>(){
				@Override
				public Boolean extractData(ResultSet reSet) throws SQLException,
						DataAccessException {
					if (reSet.next()) {
						if(reSet.getString("mentor_title") != null && 
								reSet.getString("is_hispanic") != null){
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
	
	/**
	 * Profile: mentor 
	 */
	public ProfileBean getProfileMentor(int mentorID){
		String sql = "SELECT * FROM " + TABLE_PROFILE_MENTOR
				+ " WHERE mentor_id = ?";
		try{
			ProfileBean profileBean = jdbcTemplate.queryForObject(sql, 
				new Object[]{mentorID}, 
				new RowMapper<ProfileBean>() {
					@Override
					public ProfileBean mapRow(ResultSet reSet, int rowNum)
							throws SQLException {
					  
						ProfileBean bean = new ProfileBean(); 
						// biography info
						MentorBean mentorBean = new MentorBean();
						mentorBean.setMentorFirstName(reSet.getString("mentor_first_name"));
						mentorBean.setMentorLastName(reSet.getString("mentor_last_name")); 
						mentorBean.setMentorEmail(reSet.getString("mentor_email"));
						if(reSet.getString("mentor_prefix") != null){
							mentorBean.setMentorMiddleName(reSet.getString("mentor_middle_name")); 
							mentorBean.setMentorPrefix(reSet.getString("mentor_prefix"));
							mentorBean.setMentorTitle(reSet.getString("mentor_title"));
							mentorBean.setMentorInstitution(reSet.getString("mentor_institution"));
							mentorBean.setMentorDept(reSet.getString("mentor_department"));
							mentorBean.setMentorPhone(reSet.getString("mentor_phone"));
							mentorBean.setMentorBuilding(reSet.getString("mentor_building"));
							mentorBean.setMentorFax(reSet.getString("mentor_fax")); 
						}
						bean.setMentorBean(mentorBean);;
						 
						// Ethnicity 
						EthnicityBean ethnicityBean = null; 
						String isHispanic = reSet.getString("is_hispanic");
						if (isHispanic != null) {
							ethnicityBean = new EthnicityBean(); 
							ethnicityBean.setIsHispanic(Integer.parseInt(isHispanic));
							ethnicityBean.setDisability(reSet.getString("disability"));
							String race = reSet.getString("race");
							if(race != null){
								ethnicityBean.setRace(Arrays.asList(race.trim().split("\\s*,\\s*")));
							}
							bean.setEthnicityBean(ethnicityBean);
						}
						 
						return bean; 
					}
				}); 
			return profileBean; 
		}catch(EmptyResultDataAccessException e){
			return null; 
		}
	}
	public MentorBean getProfileMentorBean(int mentorID){
		String sql = "SELECT * FROM " + TABLE_PROFILE_MENTOR
				+ " WHERE mentor_id = ?";
		try{
			MentorBean profileBean = jdbcTemplate.queryForObject(sql, 
				new Object[]{mentorID}, 
				new RowMapper<MentorBean>() {
					@Override
					public MentorBean mapRow(ResultSet reSet, int rowNum)
							throws SQLException {
					 
						MentorBean mentorBean = new MentorBean();
						mentorBean.setMentorFirstName(reSet.getString("mentor_first_name"));
						mentorBean.setMentorLastName(reSet.getString("mentor_last_name")); 
						mentorBean.setMentorEmail(reSet.getString("mentor_email"));
						if(reSet.getString("mentor_prefix") != null){
							mentorBean.setMentorMiddleName(reSet.getString("mentor_middle_name")); 
							mentorBean.setMentorPrefix(reSet.getString("mentor_prefix"));
							mentorBean.setMentorTitle(reSet.getString("mentor_title"));
							mentorBean.setMentorInstitution(reSet.getString("mentor_institution"));
							mentorBean.setMentorDept(reSet.getString("mentor_department"));
							mentorBean.setMentorPhone(reSet.getString("mentor_phone"));
							mentorBean.setMentorBuilding(reSet.getString("mentor_building"));
							mentorBean.setMentorFax(reSet.getString("mentor_fax")); 
						}
						 
						return mentorBean; 
					}
				}); 
			return profileBean; 
		}catch(EmptyResultDataAccessException e){
			return null; 
		}
	}
	
	public int updateProfileMentorEthnicity(int mentorID, EthnicityBean bean) {
		 
		String sql = "INSERT INTO " + TABLE_PROFILE_MENTOR
				+ " (mentor_id,is_hispanic,race,disability"
				+ ") VALUES (?,?,?,?)"
				+ " ON DUPLICATE KEY UPDATE "
				+ "is_hispanic = VALUES(is_hispanic), "
				+ "race = VALUES(race), "
				+ "disability = VALUES(disability)";
		return jdbcTemplate.update(sql, new Object[]{
				mentorID, bean.getIsHispanic(), 
				String.join(",", bean.getRace()), bean.getDisability()
		}); 
	}
	
	public int updateProfileMentorBiography(int mentorID, MentorBean bean) {
		 
		String sql = "INSERT INTO " + TABLE_PROFILE_MENTOR
				+ " (mentor_id,"
				+ "mentor_first_name,mentor_last_name,mentor_email,"
				+ "mentor_middle_name,mentor_prefix,mentor_title,"
				+ "mentor_institution,mentor_department,mentor_phone,mentor_building,mentor_fax"
				+ ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?)"
				+ " ON DUPLICATE KEY UPDATE "
				+ "mentor_first_name = VALUES(mentor_first_name), " 
				+ "mentor_last_name = VALUES(mentor_last_name), " 
				+ "mentor_middle_name = VALUES(mentor_middle_name), " 
				+ "mentor_prefix = VALUES(mentor_prefix), "
				+ "mentor_email = VALUES(mentor_email), "
				+ "mentor_title = VALUES(mentor_title), "
				+ "mentor_institution = VALUES(mentor_institution), "
				+ "mentor_department = VALUES(mentor_department), "
				+ "mentor_phone = VALUES(mentor_phone), "
				+ "mentor_building = VALUES(mentor_building), "
				+ "mentor_fax = VALUES(mentor_fax) "
				;
		
		return jdbcTemplate.update(sql, new Object[]{
				mentorID, bean.getMentorFirstName().trim(), bean.getMentorLastName().trim(),
				bean.getMentorEmail().trim(), bean.getMentorMiddleName().trim(), bean.getMentorPrefix(),
				bean.getMentorTitle(), bean.getMentorInstitution(), bean.getMentorDept(), 
				bean.getMentorPhone(), bean.getMentorBuilding(), bean.getMentorFax()
		}); 
	}
	
	/**************************************************** 
	 * 				Manage Staff					*
	 ****************************************************/
	public ArrayNode getStaffList(){
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT * \n");
		sql.append("FROM ").append(TABLE_PROFILE_STAFF).append(" a \n");
		sql.append("LEFT JOIN ").append(TABLE_USER).append(" b\n");
		sql.append("ON a.user_id = b.user_id WHERE b.role = ?\n");
		 
		return jdbcTemplate.query(sql.toString(), new Object[]{Role.STAFF.toString()}, 
				new ResultSetExtractor<ArrayNode>(){
 
				@Override
				public ArrayNode extractData(ResultSet reSet)
						throws SQLException, DataAccessException {
					ArrayNode staffList = objectMapper.createArrayNode();
					while(reSet.next()){
						ObjectNode bean = objectMapper.createObjectNode(); 
						bean.set("userID", objectMapper.convertValue(reSet.getString("user_id"), JsonNode.class));
						bean.set("firstName", objectMapper.convertValue(reSet.getString("first_name"), JsonNode.class)); 
						bean.set("lastName", objectMapper.convertValue(reSet.getString("last_name"), JsonNode.class)); 
						bean.set("email", objectMapper.convertValue(reSet.getString("email"), JsonNode.class));
						bean.set("birthDate", objectMapper.convertValue(reSet.getString("birth_date"), JsonNode.class));
						bean.set("affiliation", objectMapper.convertValue(ProgramCode.ACADEMIC_SCHOOL.get(reSet.getString("affiliation")), JsonNode.class));
						bean.set("phone", objectMapper.convertValue(reSet.getString("phone"), JsonNode.class));
						staffList.add(bean); 
					}
					return staffList;
				}
			}); 
	}
	
	public boolean createStaffAccount(User user, String phone){
		String sql1 = "INSERT INTO " + TABLE_USER
				+ " (user_id, role, first_name, last_name, birth_date, email, password) VALUES (?,?,?,?,?,?,?)";
		String sql2 = "INSERT INTO " + TABLE_PROFILE_STAFF + " (user_id, affiliation, phone) VALUES (?,?,?)";
		return transactionTemplate.execute(new TransactionCallback<Boolean>() {
			@Override
			public Boolean doInTransaction(TransactionStatus status) {
				try{
					int userID = generateUserID();  
					String cryptedPassword = new BCryptPasswordEncoder(11).encode(user.getPassword()); 
					jdbcTemplate.update(sql1, new Object[]{
							userID, Role.STAFF.toString(), user.getFirstName(), user.getLastName(), 
							new Date(), user.getEmail(), cryptedPassword	
					});
					jdbcTemplate.update(sql2, new Object[]{
							userID, user.getAffiliation(), phone	
					});
				}catch(Exception e){
					status.setRollbackOnly();
					e.printStackTrace();
					return false; 
				}
				return true;
			}
		}); 
	}
	
	public boolean deleteStaffAccount(int userID){
		String sql1 = "DELETE FROM "+TABLE_PROFILE_STAFF+" WHERE user_id = ?"; 
		String sql2 = "DELETE FROM "+TABLE_USER+" WHERE user_id = ? AND role = ?";
		
		return transactionTemplate.execute(new TransactionCallback<Boolean>() {
			@Override
			public Boolean doInTransaction(TransactionStatus status) {
				try{
					jdbcTemplate.update(sql1, new Object[]{userID}); 
					jdbcTemplate.update(sql2, new Object[]{userID, Role.STAFF.toString()}); 
				}catch(Exception e){
					status.setRollbackOnly();
					e.printStackTrace();
					return false; 
				}
				return true;
			}
		}); 
	}
	
	/**************************************************** 
	 * 				GET Statistics				*
	 ****************************************************/
	public void getStatisticsBySemesterAndAccept(List<Integer> years, 
			Map<String, Map<String, Integer>> mapAccept, 
			Map<String, Map<String, Integer>> mapSemester) {
		StringBuilder sql = new StringBuilder();
        sql.append("SELECT school_year,school_semester,program,accept_status \n");
        sql.append("FROM ").append(TABLE_APPLICATION_LIST).append("\n");
        sql.append("WHERE school_year IN (");
        sql.append(years.stream().map(i -> i.toString()).collect(Collectors.joining(",")));
        sql.append(")\n");
        jdbcTemplate.query(sql.toString(), new ResultSetExtractor<Void>() {
			@Override
			public Void extractData(ResultSet reSet) throws SQLException, DataAccessException {
				while (reSet.next()) {
					String program = reSet.getString("program");
	                String year = reSet.getInt("school_year") + "";
	                String accept = reSet.getString("accept_status");
	                String semester = reSet.getString("school_semester");

	                if(accept == null) {
	                		accept = "0";
	                } 
	                if (mapAccept.containsKey(program)) {
	                    mapAccept.get(program).computeIfPresent(year + "-" + accept, (k, v) -> v + 1);
	                }
	                if (mapSemester.containsKey(program)) {
	                    mapSemester.get(program).computeIfPresent(year + "-" + semester, (k, v) -> v + 1);
	                } 
					
				}
				return null;
			}
		});
	}
	
	public void getRegistrationNumber(Map<String, Integer> map) {
		StringBuilder sql = new StringBuilder();
        sql.append("SELECT role, count(*) as count \n");
        sql.append("FROM ").append(TABLE_USER).append("\n");
        sql.append("GROUP BY role");
        jdbcTemplate.query(sql.toString(), new ResultSetExtractor<Void>() {
			@Override
			public Void extractData(ResultSet reSet) throws SQLException, DataAccessException {
				while (reSet.next()) {
					String role = reSet.getString("role"); 
					if(Role.USER.toString().equals(role) || Role.MENTOR.toString().equals(role)) {
						map.put(role, reSet.getInt("count"));
					}
				}
				return null;
			}
		});
	}
	 
}
