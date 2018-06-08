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

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.thymeleaf.extras.java8time.dialect.Java8TimeDialect;
import org.thymeleaf.extras.springsecurity4.dialect.SpringSecurityDialect;
import org.thymeleaf.spring4.SpringTemplateEngine;
import org.thymeleaf.spring4.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring4.view.ThymeleafViewResolver;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ITemplateResolver;
 
 
@Configuration
@EnableWebMvc
public class ThymeleafConfig {
 
	@Bean(name ="templateResolver")
	public ITemplateResolver templateResolver(){
        SpringResourceTemplateResolver templateResolver = new SpringResourceTemplateResolver();
        templateResolver.setPrefix("/WEB-INF/views/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setCharacterEncoding("UTF-8");
        templateResolver.setCacheable(false);
        return templateResolver;
    }
	
	@Bean(name ="templateEngine")	    
    public SpringTemplateEngine templateEngine() {
    	SpringTemplateEngine templateEngine = new SpringTemplateEngine();
    	templateEngine.setTemplateResolver(templateResolver());
    	
        templateEngine.addDialect(new SpringSecurityDialect());
        templateEngine.addDialect(new Java8TimeDialect());
    	return templateEngine;
    }
	
	@Bean(name="viewResolver")
    public ThymeleafViewResolver getViewResolver(){
    	ThymeleafViewResolver viewResolver = new ThymeleafViewResolver(); 
    	viewResolver.setTemplateEngine(templateEngine());
    	viewResolver.setCharacterEncoding("UTF-8");
    	viewResolver.setContentType("text/html; charset=UTF-8");
    	return viewResolver;
    }

} 
