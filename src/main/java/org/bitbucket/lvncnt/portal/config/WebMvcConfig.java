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

import javax.naming.NamingException;
import javax.sql.DataSource;

import org.lightcouch.CouchDbClient;
import org.lightcouch.CouchDbException;
import org.lightcouch.CouchDbProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jndi.JndiTemplate;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.bitbucket.lvncnt.portal.controller.ExceptionHandler;
 
@Configuration
@EnableWebMvc
@PropertySource("classpath:mail.properties")
@ComponentScan(basePackages = {"org.bitbucket.lvncnt.portal", "org.bitbucket.lvncnt.portal.config"})
public class WebMvcConfig extends WebMvcConfigurerAdapter{

	@Autowired private Environment env; 

	@Override
	public void configureDefaultServletHandling(
			DefaultServletHandlerConfigurer configurer) {
		configurer.enable();
	}
	
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/css/**").addResourceLocations("/css/");
		registry.addResourceHandler("/js/**").addResourceLocations("/js/");
	        registry.addResourceHandler("/webjars/**").addResourceLocations(
	                "classpath:/META-INF/resources/webjars/");
	}

	@Override
	public void addViewControllers(ViewControllerRegistry registry) {
		super.addViewControllers(registry);
	}
	
	@Bean
	@Scope("prototype")
	Logger logger(InjectionPoint injectionPoint) {
		return LoggerFactory.getLogger(injectionPoint.getMethodParameter().getContainingClass()); 
	}

	@Bean(name="dataSource")
	public DataSource getDataSource() throws NamingException{
		JndiTemplate jndiTemplate = new JndiTemplate(); 
		DataSource dataSource = (DataSource) jndiTemplate.lookup("java:comp/env/jdbc/"
			+ env.getProperty("mysql.dbname")); 
		return dataSource; 
	}
	
	@Bean
	public JdbcTemplate jdbcTemplate() throws NamingException {
		return new JdbcTemplate(getDataSource()); 
	}
	
	@Bean(name="transactionManager")
	public DataSourceTransactionManager getTransactionManager() throws NamingException{
		return new DataSourceTransactionManager(getDataSource()); 
	}
	 
	@Bean(name="transactionTemplate")
	public TransactionTemplate getTransactionTemplate() throws NamingException{
		TransactionTemplate transactionTemplate = new TransactionTemplate(getTransactionManager()); 
		return transactionTemplate; 
	} 
 
	@Bean(name="objectMapper")
	public ObjectMapper objectMapper() {
		return new ObjectMapper(); 
	}

	@Bean
	public MessageSource messageSource(){
		ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource(); 
		messageSource.setBasename("messages");
		messageSource.setDefaultEncoding("UTF-8");
		return messageSource; 
	}

	@Bean(name = "multipartResolver")
	public StandardServletMultipartResolver resolver(){
		return new StandardServletMultipartResolver(); 
	}
 
	@Bean
	public ExceptionHandler exceptionHandler(){
		return new ExceptionHandler(); 
	}
	 
	@Bean(name="couchDbClient")
	public CouchDbClient couchDbClient(){
		CouchDbClient client = null; 
		try{
			CouchDbProperties properties = new CouchDbProperties()
			  .setDbName(env.getProperty("couchdb.dbname"))
			  .setCreateDbIfNotExist(Boolean.getBoolean(env.getProperty("couchdb.createdb.if-not-exist")))
			  .setProtocol(env.getProperty("couchdb.protocol"))
			  .setHost(env.getProperty("couchdb.host"))
			  .setPort(Integer.valueOf(env.getProperty("couchdb.port")))
			  .setUsername(env.getProperty("couchdb.username"))
			  .setPassword(env.getProperty("couchdb.password"))
			  .setMaxConnections(100)
			  .setConnectionTimeout(0);
			client =  new CouchDbClient(properties); 
		}catch(CouchDbException e){
		}
		return client; 
		 
	}  
 
}
