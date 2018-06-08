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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bitbucket.lvncnt.portal.dao.AdminDAOImpl;
import org.bitbucket.lvncnt.portal.model.ProgramBean;
import org.bitbucket.lvncnt.portal.service.ProgramCode;
import org.bitbucket.lvncnt.utilities.Parse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

@Controller
@RequestMapping("/admin")
public class AdmProgramController {
	
	private static final String VIEW_MANAGE_PROGRAM = "admin/manage-program"; 
	private static final String VIEW_EDIT_PROGRAM = "admin/edit-program"; 
	
	private static final Logger logger = LoggerFactory.getLogger(AdmProgramController.class); 
	
	@Autowired private AdminDAOImpl adminDAO;
	@Autowired private ObjectMapper objectMapper; 
	
	/** Manage Program  **/ 
	@GetMapping("/manage-program")
	public String getManageProgram(ModelMap model) {
		if(!model.containsAttribute("windowList")){
			try {
				model.addAttribute("windowList", 
						objectMapper.writeValueAsString(adminDAO.getApplicationWindows()));
			} catch (JsonProcessingException e) {
				logger.error(e.getMessage()); 
			} 
		}
		if(!model.containsAttribute("programBean")){
			model.addAttribute("programBean", new ProgramBean());
		}
		model.addAttribute("programs", ProgramCode.PROGRAMS);
		model.addAttribute("academicSemester", ProgramCode.ACADEMIC_SEMESTER);
		model.addAttribute("status", model.get("status")); 
		return VIEW_MANAGE_PROGRAM; 
	}
	
	@PostMapping("/manage-program")
	public View addSelfreportWindow(ModelMap model, 
			@ModelAttribute("programBean") ProgramBean programBean,
			BindingResult bindingResult, RedirectAttributes redirectAttributes){
		 
		new Validator() {
			@Override
			public boolean supports(Class<?> clazz) {
				return ProgramBean.class.isAssignableFrom(clazz);
			}
			
			@Override
			public void validate(Object target, Errors errors) {
				ValidationUtils.rejectIfEmptyOrWhitespace(errors, "programNameAbbr", 
						"NotBlank.programBean.programNameAbbr", "Please select a program.");
				ValidationUtils.rejectIfEmptyOrWhitespace(errors, "year", 
						"NotBlank.programBean.year", "Please select a year.");
				ValidationUtils.rejectIfEmptyOrWhitespace(errors, "semester", 
						"NotBlank.programBean.semester", "Please select a semester.");
				ValidationUtils.rejectIfEmptyOrWhitespace(errors, "startDate", 
						"NotNull.programBean.startDate", "Start date is required.");
				ValidationUtils.rejectIfEmptyOrWhitespace(errors, "endDate", 
						"NotNull.programBean.endDate", "End date is required."); 
			}
		}.validate(programBean, bindingResult);
		
		RedirectView redirectView = new RedirectView(); 
		redirectView.setContextRelative(true);
		if(bindingResult.hasErrors()){
			String FORM_NAME = "programBean"; 
			String key = BindingResult.MODEL_KEY_PREFIX + FORM_NAME; 
			redirectAttributes.addFlashAttribute(key, bindingResult);
			redirectAttributes.addFlashAttribute("programBean", programBean);
		}else {
			int result = adminDAO.addApplicationWindow(programBean); 
			String status; 
			if(result != 0){
				status = "Added successfully!"; 
			}else{
				status = "Error occurred during submission!"; 
			}
			redirectAttributes.addFlashAttribute("status", status); 
		}
		redirectView.setUrl("/admin/manage-program");
		return redirectView; 
	}
	
	@GetMapping("/manage-program/edit/{program}/{windowID}")
	public String getEditProgram(ModelMap model, @PathVariable("program") String program,
			@PathVariable("windowID") int windowID){
		if(!model.containsAttribute("programBean")){
			model.addAttribute("programBean", adminDAO.getApplicationWindow(windowID, program)); 
		}
		
		model.addAttribute("academicSemester", ProgramCode.ACADEMIC_SEMESTER);
		model.addAttribute("program", program); 
		model.addAttribute("windowID", windowID);
		return VIEW_EDIT_PROGRAM ; 
	}
	
	@PostMapping("/manage-program/edit/{program}/{windowID}")
	public View setApplicationWindow(ModelMap model, 
			@PathVariable("program") String program,
			@PathVariable("windowID") int windowID, 
			@ModelAttribute("programBean") ProgramBean programBean,
			BindingResult bindingResult, RedirectAttributes redirectAttributes){
 
		new Validator() {
			@Override
			public boolean supports(Class<?> clazz) {
				return ProgramBean.class.isAssignableFrom(clazz);
			}
			
			@Override
			public void validate(Object target, Errors errors) {
				ValidationUtils.rejectIfEmptyOrWhitespace(errors, "startDate", 
						"NotNull.programBean.startDate", "Start date is required.");
				ValidationUtils.rejectIfEmptyOrWhitespace(errors, "endDate", 
						"NotNull.programBean.endDate", "End date is required.");
				if(programBean.getEndDate() != null  &&
						!org.bitbucket.lvncnt.utilities.Validator.validateDateMMDDYYYY(Parse.FORMAT_DATE_MDY.format(programBean.getEndDate()))) {
					errors.rejectValue("signatureDate", "typeMismatch", "Incorrect format.");
				} 
			}
		}.validate(programBean, bindingResult);
		
		RedirectView view = new RedirectView();
		view.setContextRelative(true);
		if(bindingResult.hasErrors()){
			model.addAttribute("academicSemester", ProgramCode.ACADEMIC_SEMESTER);
			model.addAttribute("program", program); 
			model.addAttribute("windowID", windowID);
			String FORM_NAME = "programBean"; 
			String key = BindingResult.MODEL_KEY_PREFIX + FORM_NAME; 
			redirectAttributes.addFlashAttribute(key, bindingResult);
			redirectAttributes.addFlashAttribute("programBean", programBean);
			view.setUrl("/admin/manage-program/edit/{program}/{windowID}");
		} else {
			int result = adminDAO.setApplicationWindow(windowID, program, programBean); 
			String status; 
			if(result != 0){
				status = "Program updated successfully!"; 
			}else{
				status = "Error occurred during submission!"; 
			}
			redirectAttributes.addFlashAttribute("status", status); 
			view.setUrl("/admin/manage-program");
		} 
		return view; 
	}
	
	@GetMapping("/manage-program/cancel/{program}/{windowID}")
	public View cancelApplicationWindow(ModelMap model, 
			@PathVariable("program") String program, 
			@PathVariable("windowID") int windowID, 
			RedirectAttributes redirectAttributes){
		model.clear();
		int result = adminDAO.deleteApplicationWindow(windowID, program); 
		String status; 
		if(result != 0){
			status = "Cancelled successfully!"; 
		}else{
			status = "Error occurred during submission!"; 
		} 
		redirectAttributes.addFlashAttribute("status", status); 
		RedirectView view = new RedirectView("/admin/manage-program", true);
		return view; 
	}

}
