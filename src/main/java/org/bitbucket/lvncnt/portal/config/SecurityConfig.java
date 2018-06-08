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
package org.bitbucket.lvncnt.portal.config;

import org.bitbucket.lvncnt.portal.service.CustomSuccessHandler;
import org.bitbucket.lvncnt.portal.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.filter.CharacterEncodingFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	
	@Autowired
	private UserDetailsServiceImpl userDetailsServiceImpl;
	
	@Autowired
	private CustomSuccessHandler customSuccessHandler;
	
	@Autowired
	private LogoutSuccessHandler logoutSuccessHandler; 
	 
	@Override
	protected void configure(AuthenticationManagerBuilder auth)
			throws Exception {
		auth.userDetailsService(userDetailsServiceImpl).passwordEncoder(passwordEncoder());
		
	}
 
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		 CharacterEncodingFilter encodingFilter = new CharacterEncodingFilter();
		    encodingFilter.setEncoding("UTF-8");
		    encodingFilter.setForceEncoding(true);
		    http.addFilterBefore(encodingFilter, CsrfFilter.class); 
		    
	        http.authorizeRequests()
	        .antMatchers("/home*").access("hasRole('ROLE_USER')")
	        .antMatchers("/profile*").access("hasRole('ROLE_USER')")
	        .antMatchers("/setting*").access("hasRole('ROLE_USER')")
	        .antMatchers("/application/**").access("hasRole('ROLE_USER')")
	        .antMatchers("/selfreport/**").access("hasRole('ROLE_USER')")
	        .antMatchers("/evaluation/**").access("hasRole('ROLE_USER')")
	        .antMatchers("/mentor/**").access("hasRole('ROLE_MENTOR')")
	        .antMatchers("/admin/manage-program/**").access("hasRole('ROLE_ADMIN')")
	        .antMatchers("/admin/manage-selfreport/**").access("hasRole('ROLE_ADMIN')")
	        .antMatchers("/admin/manage-templates/**").access("hasRole('ROLE_ADMIN')")
	        .antMatchers("/admin/manage-ampconf/**").access("hasRole('ROLE_ADMIN')")
	        .antMatchers("/admin/manage-staff/**").access("hasRole('ROLE_ADMIN')")
	        .antMatchers("/admin/manage-account/**").access("hasRole('ROLE_ADMIN')")
	        .antMatchers("/admin/search-applicant/**").access("hasRole('ROLE_ADMIN')")
	        .antMatchers("/admin/**").access("hasAnyRole('ROLE_ADMIN','ROLE_STAFF')")
	        .antMatchers("/evaluation/**").permitAll()
	        .antMatchers("/projects/review/*").permitAll()
	        .antMatchers("/recommendations/upload/*").permitAll()
			.antMatchers("/login").permitAll()
			.antMatchers("/contact").permitAll()
			.antMatchers("/register*").permitAll()
			.antMatchers("/LICENSE/**").permitAll()
			.antMatchers("/resetpassword/**").permitAll()
			.antMatchers("/instruction*").permitAll()
			.antMatchers("/css/**", "/js/**", "/webjars/**").permitAll()
			.anyRequest().authenticated()
			.and()
		.formLogin()
			.loginPage("/login").successHandler(customSuccessHandler)  
			.loginProcessingUrl("/login")
			.failureUrl("/login?error")
			.usernameParameter("email")
			.passwordParameter("password")
			.and()
		.logout()
			.logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
			.logoutSuccessHandler(logoutSuccessHandler)
			.permitAll();
	        
	        http.sessionManagement().invalidSessionUrl("/login"); 
		
	}
	
	@Bean(name="passwordEncoder")
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(11);
    }
  
}  
