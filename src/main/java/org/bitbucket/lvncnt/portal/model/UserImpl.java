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

import java.util.Collection;
import java.util.Date;

import org.springframework.security.core.GrantedAuthority;

 
public class UserImpl extends org.springframework.security.core.userdetails.User{
 
	private static final long serialVersionUID = 1L;
	
	private User user; 
	
	public UserImpl(String username, String password, boolean enabled,
			boolean accountNonExpired, boolean credentialsNonExpired,
			boolean accountNonLocked,
			Collection<? extends GrantedAuthority> authorities) {
		super(username, password, enabled, accountNonExpired, credentialsNonExpired,
				accountNonLocked, authorities);
	}

	public UserImpl(String username, String password,
			Collection<? extends GrantedAuthority> authorities) {
		super(username, password, authorities);
	}
	
	public UserImpl(User user, Collection<? extends GrantedAuthority> authorities){
		super(user.getEmail(), user.getPassword(), authorities);
		this.user = user; 
	}

	public Integer getUserID() {
		return user.getUserID();
	}
	 
	public String getFirstName() {
		return user.getFirstName();
	}
	 
	public String getLastName() {
		return user.getLastName();
	}
	 
	public Date getBirthDate() {
		if(user.getBirthDate() == null) return null;
		return (Date)user.getBirthDate().clone();
	}
	 
	public String getEmail() {
		return user.getEmail();
	}
	 
	public void setEmail(String email) {
		user.setEmail(email);
	}
	
	public Role getRole() {
		return user.getRole();
	}
	
	public String getConfirmPassword() {
		return user.getConfirmPassword();
	}
	public String getPassword() {
		return user.getPassword();
	}
	 
	public String getAffiliation() {
		return user.getAffiliation();
	}
 
}
