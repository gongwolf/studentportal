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

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.format.annotation.DateTimeFormat;
 
public class User implements Serializable{
 
	private static final long serialVersionUID = 1L;
	 
	private Integer userID;
	private Integer userRole; 
 
	private String firstName;
	
	private String lastName;
	
	@DateTimeFormat(pattern="MM/dd/yyyy")
	private Date birthDate;
	
	@NotBlank(message = "{NotBlank.user.email}")
	@Email(message="{Email.user.email}")
	private String email;
 
	private transient String password;
	private transient String confirmPassword; 
	private Role role = Role.USER; 
	
	private String affiliation;
 	
	public User() {}
	public User(Integer userID, Integer userRole, String firstName,
			String lastName, Date birthDate, String email) {
		 
		this.userID = userID;
		this.userRole = userRole;
		this.firstName = firstName;
		this.lastName = lastName;
		this.birthDate = birthDate;
		this.email = email;
	}
	public Integer getUserID() {
		return userID;
	}
	public void setUserID(Integer userID) {
		this.userID = userID;
	}
	public Integer getUserRole() {
		return userRole;
	}
	public void setUserRole(Integer userRole) {
		this.userRole = userRole;
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
		if(birthDate == null) return; 
		this.birthDate = (Date)birthDate.clone();
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	 
	public Role getRole() {
		return role;
	}
	public void setRole(Role role) {
		this.role = role;
	}
	public String getConfirmPassword() {
		return confirmPassword;
	}
	public void setConfirmPassword(String confirmPassword) {
		this.confirmPassword = confirmPassword;
	}
	public String getAffiliation() {
		return affiliation;
	}
	public void setAffiliation(String affiliation) {
		this.affiliation = affiliation;
	}
 
}
