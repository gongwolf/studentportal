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

import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.ui.freemarker.FreeMarkerConfigurationFactoryBean;

@Configuration
@PropertySources({
	@PropertySource("classpath:mail.properties")
})
public class MailConfig {

	@Value("${javamail.host}")
	private String host; 
	
	@Value("${javamail.port}")
	private String port; 
	
	@Value("${javamail.username}")
	private String username; 
	
	@Value("${javamail.password}")
	private String password; 
	
	@Value("${mail.transport.protocol}")
	private String protocol; 

	@Bean
	public JavaMailSender getMailSender(){
		JavaMailSenderImpl mailSender = new JavaMailSenderImpl(); 
	 
		mailSender.setHost(host);
        mailSender.setPort(Integer.valueOf(port));
        mailSender.setUsername(username);
        mailSender.setPassword(password);
             
        Properties javaMailProperties = new Properties(); 
        javaMailProperties.put("mail.smtp.starttls.enable", "true");
        javaMailProperties.put("mail.smtp.auth", "true");
        javaMailProperties.put("mail.transport.protocol", protocol);
         
        mailSender.setJavaMailProperties(javaMailProperties);
		
		return mailSender; 
	}

	@Bean
	public FreeMarkerConfigurationFactoryBean getFreeMarkerConfiguration(){
		FreeMarkerConfigurationFactoryBean bean = new FreeMarkerConfigurationFactoryBean(); 
		bean.setTemplateLoaderPath("/fmtemplates/");
		bean.setPreferFileSystemAccess(false);
		return bean; 
	}
	
}
