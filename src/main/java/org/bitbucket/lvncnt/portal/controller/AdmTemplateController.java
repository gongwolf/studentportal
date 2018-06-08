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
package org.bitbucket.lvncnt.portal.controller;

import org.bitbucket.lvncnt.portal.service.ProgramCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.regex.Pattern;

@Controller
@RequestMapping("/admin/manage-templates")
public class AdmTemplateController {
	
	private static final String VIEW_MANAGE_TEMPLATES = "admin/manage-templates"; 
	private static final String VIEW_EDIT_TEMPLATE = "admin/edit-template";
	private static final String HEADER = "<html><body><br />${date}<br /><br />${bean.firstName} ${bean.lastName}<br />${bean.addressLine1}<br />${bean.addressCity}, ${bean.addressState} ${bean.addressZip}<br /><br />Dear ${bean.firstName},<br /><br />"; 
	private static final String FOOTER = "</body></html>"; 
	
	@Autowired private MessageSource messageSource; 
	
	private static final Logger logger = LoggerFactory.getLogger(AdmTemplateController.class);
	
	@GetMapping("")
	public String getMangageTemplates(){
		return VIEW_MANAGE_TEMPLATES; 
	}
	
	@GetMapping("/edit/{program}/{decision}")
	public String getEditTemplate(@PathVariable String program, 
			@PathVariable String decision, ModelMap model, HttpServletRequest request){
		model.addAttribute("programFull", ProgramCode.PROGRAMS.get(program.toUpperCase()));
		model.addAttribute("program", program); 
		model.addAttribute("decision", decision);
		
		String filename = messageSource.getMessage(ProgramCode.getDecisionMessage(program, decision)
				, null, Locale.ENGLISH); 
	 
		String dir = request.getServletContext().getRealPath("fmtemplates/");
		Path path = Paths.get(dir, filename);
		try {
			StringBuilder letter = new StringBuilder(new String(Files.readAllBytes(path))); 
			letter.delete(0, HEADER.length()); 
			String letter2 = letter.toString().replaceFirst(Pattern.quote(HEADER), ""); 
			letter2 = letter2.replaceFirst(Pattern.quote(FOOTER), ""); 
			model.addAttribute("letter", letter2);
		} catch (IOException e) {
			logger.error(e.getMessage());
		} 
		return VIEW_EDIT_TEMPLATE; 
	}
	
	@PostMapping("/save/{program}/{decision}")
	public View setTemplate(@PathVariable String program, 
			@PathVariable String decision, ModelMap model, 
			@RequestParam String letter, HttpServletRequest request){
	 
		letter = HEADER + letter + FOOTER; 
		String filename = messageSource.getMessage(ProgramCode.getDecisionMessage(program, decision)
				, null, Locale.ENGLISH); 
	 
		String dir = request.getServletContext().getRealPath("fmtemplates/");
		Path path = Paths.get(dir, filename); 
		try {
			Files.copy(new ByteArrayInputStream(letter.getBytes()), path, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		return new RedirectView(String.format("%s/edit/%s/%s", 
				"/admin/manage-templates", program, decision), true); 
	}

}
