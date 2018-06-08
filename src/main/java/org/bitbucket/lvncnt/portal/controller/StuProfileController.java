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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.bitbucket.lvncnt.portal.dao.UserDAO;
import org.bitbucket.lvncnt.portal.dao.UserDAOImpl;
import org.bitbucket.lvncnt.portal.model.*;
import org.bitbucket.lvncnt.portal.service.FileValidator;
import org.bitbucket.lvncnt.portal.service.ProfileContactValidator;
import org.bitbucket.lvncnt.portal.service.ProgramCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.FileCopyUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

@Controller
@RequestMapping("/profile")
public class StuProfileController {
	
	private static final String VIEW_PROFILE = "student/profile"; 
	private static final String SECTION_BIOGRAPHY = "biography.do";
	private static final String SECTION_CONTACT = "contact.do";
	private static final String SECTION_ETHNICITY = "ethnicity.do";
	private static final String SECTION_DISCLOSURE = "disclosure.do";
	 
	@Autowired private UserDAO userDAO;
	@Autowired private MessageSource messageSource; 
	@Autowired private ProfileContactValidator profileContactValidator;
	@Autowired private FileValidator fileValidator;
	@Autowired private ObjectMapper objectMapper; 
	
	@InitBinder("fileBucket")
	protected void initBinderFileBucket(WebDataBinder binder){
		binder.setValidator(fileValidator);
	}
	
	@GetMapping("")
	public String getProfile(ModelMap model, @AuthenticationPrincipal UserImpl user)
			throws UnsupportedEncodingException{
		ProfileBean profileBean = ((UserDAOImpl)userDAO).getProfileStudent(user.getUserID());
		if(!model.containsAttribute("biographyBean")){
			if(profileBean == null || profileBean.getBiographyBean() == null){
				model.addAttribute("biographyBean", new BiographyBean());
			}else{
				model.addAttribute("biographyBean", profileBean.getBiographyBean());
			}
		}
		if(!model.containsAttribute("contactBean")){
			if(profileBean == null || profileBean.getContactBean()== null){
				model.addAttribute("contactBean", new ContactBean());
			}else{
				model.addAttribute("contactBean", profileBean.getContactBean());
			}
		}
		if(!model.containsAttribute("ethnicityBean")){
			if(profileBean == null || profileBean.getEthnicityBean()== null){
				model.addAttribute("ethnicityBean", new EthnicityBean());
			}else{
				model.addAttribute("ethnicityBean", profileBean.getEthnicityBean());
			}
		}
		if(!model.containsAttribute("fileBucket")){
			if(profileBean == null || profileBean.getFileBucket() == null){
				model.addAttribute("fileBucket", new FileBucket());
			}else{
				model.addAttribute("fileBucket", profileBean.getFileBucket());
			}
		} 
	   
		model.addAttribute("citizenship", ProgramCode.CITIZENSHIP);
		model.addAttribute("yesno", ProgramCode.YES_NO);
		model.addAttribute("phoneTypes", ProgramCode.PHONE_TYPES);
		model.addAttribute("gender", ProgramCode.GENDER);
		model.addAttribute("states", ProgramCode.STATES);
		
		model.addAttribute("race", ProgramCode.CHECKBOX_RACE);
		model.addAttribute("disability", ProgramCode.DISABILITY_STATUS);
		
		model.addAttribute("status", model.get("status")); 
		model.addAttribute("section", model.get("section")); 
		return VIEW_PROFILE; 
	}
	 
	@PostMapping("/biography.do")
	public String setProfileBiography(ModelMap model,
			@ModelAttribute("biographyBean") BiographyBean biographyBean, 
			BindingResult bindingResult, @AuthenticationPrincipal UserImpl user, 
			@ModelAttribute("status") String status, @ModelAttribute("section") String section,
			RedirectAttributes redirectAttributes) throws UnsupportedEncodingException{
		new Validator() {
			@Override
			public boolean supports(Class<?> clazz) {
				return BiographyBean.class.isAssignableFrom(clazz);
			}
			
			@Override
			public void validate(Object target, Errors errors) {
				String ssnLastFour = biographyBean.getSsnLastFour(); 
				
				if(ssnLastFour.length() != 4 || 
						!ssnLastFour.matches("^[0-9]{4}$")){
					errors.rejectValue("ssnLastFour", 
							"Pattern.biographyBean.ssnLastFour", "Please provide a valid number.");
				}
				ValidationUtils.rejectIfEmptyOrWhitespace(errors, "middleName", 
						"Pattern.biographyBean.middleName", "Please provide middle initial.");
				ValidationUtils.rejectIfEmptyOrWhitespace(errors, "parentHasDegree", 
						"Pattern.biographyBean.parentHasDegree", "Required.");
				ValidationUtils.rejectIfEmptyOrWhitespace(errors, "gender", 
						"NotBlank.biographyBean.gender", "Please select gender.");
				ValidationUtils.rejectIfEmptyOrWhitespace(errors, "isNMResident", 
						"NotBlank.biographyBean.isNMResident", "Please select resident status.");
				ValidationUtils.rejectIfEmptyOrWhitespace(errors, "citizenship", 
						"NotBlank.biographyBean.citizenship", "Please select citizenship.");
			}
		}.validate(biographyBean, bindingResult);
	 
		if(bindingResult.hasErrors()){
 			redirectAttributes.addFlashAttribute("section", SECTION_BIOGRAPHY);
				 
			// Preserving Validation Error Messages On Spring Mvc Form Post-redirect-get
			String FORM_NAME = "biographyBean"; 
			String key = BindingResult.MODEL_KEY_PREFIX + FORM_NAME; 
			redirectAttributes.addFlashAttribute(key, bindingResult);
			redirectAttributes.addFlashAttribute("biographyBean", biographyBean);
			return "redirect:/profile"; 
		}
		 
		int result = ((UserDAOImpl)userDAO).updateProfileStudentBiography(user.getUserID(), biographyBean); 
		if(result != 0){
			status = "Profile updated successfully!"; 
		}else{
			status = "Error occurred during submission!"; 
		}
		redirectAttributes.addFlashAttribute("status", status); 
		redirectAttributes.addFlashAttribute("section", SECTION_BIOGRAPHY); 
		model.clear();
		return "redirect:/profile"; 
	}
	
	@PostMapping("/contact.do")
	public String setProfileContact(ModelMap model,
			@ModelAttribute("contactBean") ContactBean contactBean, 
			BindingResult bindingResult, @AuthenticationPrincipal UserImpl user, 
			@ModelAttribute("status") String status, @ModelAttribute("section") String section,
			RedirectAttributes redirectAttributes){
		
	 	profileContactValidator.validate(contactBean, bindingResult);
		if(bindingResult.hasErrors()){
 			redirectAttributes.addFlashAttribute("section", SECTION_CONTACT);
			String FORM_NAME = "contactBean";
			String key = BindingResult.MODEL_KEY_PREFIX + FORM_NAME; 
			redirectAttributes.addFlashAttribute(key, bindingResult);
			redirectAttributes.addFlashAttribute("contactBean", contactBean);
			return "redirect:/profile"; 
		}
		 
		int result = ((UserDAOImpl)userDAO).updateProfileStudentContact(user.getUserID(), contactBean); 
		if(result != 0){
			status = "Profile updated successfully!"; 
		}else{
			status = "Error occurred during submission!"; 
		}
		redirectAttributes.addFlashAttribute("status", status); 
		redirectAttributes.addFlashAttribute("section", SECTION_CONTACT); 
		model.clear();
		return "redirect:/profile"; 
	}
	
	@PostMapping("/ethnicity.do")
	public String setProfileEthnicity(ModelMap model,
			@ModelAttribute("ethnicityBean") EthnicityBean ethnicityBean, 
			BindingResult bindingResult, @AuthenticationPrincipal UserImpl user, 
			@ModelAttribute("status") String status, @ModelAttribute("section") String section,
			RedirectAttributes redirectAttributes){
	 
		new Validator() {
			@Override
			public boolean supports(Class<?> clazz) {
				return EthnicityBean.class.isAssignableFrom(clazz);
			}
			
			@Override
			public void validate(Object target, Errors errors) {
				
				if(ethnicityBean.getRace() == null || 
						ethnicityBean.getRace().size() == 0){
					errors.rejectValue("race", 
							"Pattern.ethnicityBean.race", "Please select one.");
				} 
				ValidationUtils.rejectIfEmptyOrWhitespace(errors, "isHispanic", 
						"NotBlank.ethnicityBean.isHispanic", "Please select one.");
				ValidationUtils.rejectIfEmptyOrWhitespace(errors, "disability", 
						"NotBlank.ethnicityBean.disability", "Please select one.");
			}
		}.validate(ethnicityBean, bindingResult);
	  
		if(bindingResult.hasErrors()){
 			redirectAttributes.addFlashAttribute("section", SECTION_ETHNICITY);
			String FORM_NAME = "ethnicityBean";
			String key = BindingResult.MODEL_KEY_PREFIX + FORM_NAME; 
			redirectAttributes.addFlashAttribute(key, bindingResult);
			redirectAttributes.addFlashAttribute("ethnicityBean", ethnicityBean);
			return "redirect:/profile"; 
		}
		 
		int result = ((UserDAOImpl)userDAO).updateProfileStudentEthnicity(user.getUserID(), ethnicityBean); 
		if(result != 0){
			status = "Profile updated successfully!"; 
		}else{
			status = "Error occurred during submission!"; 
		}
		redirectAttributes.addFlashAttribute("status", status); 
		redirectAttributes.addFlashAttribute("section", SECTION_ETHNICITY); 
		model.clear();
		return "redirect:/profile"; 
	}
 
	@GetMapping("/download/template/student-agreement-and-disclosure-form")
	public String downloadDisclosureFormTemplate(ModelMap model,
			@ModelAttribute("status") String status, @ModelAttribute("section") String section,
			RedirectAttributes redirectAttributes, HttpServletRequest request, HttpServletResponse response){
	  
		String filename = messageSource.getMessage("Document.disclosure-form", null, Locale.ENGLISH); 
		String dir = request.getServletContext().getRealPath("resources/downloads/");
		Path path = Paths.get(dir, filename); 
		if(Files.exists(path)){
			response.setContentType("application/pdf");            
			response.setHeader("Content-disposition", "attachment; filename="+ filename);
			try {
				Files.copy(path, response.getOutputStream());
				response.getOutputStream().flush();
			} catch (IOException e) {
				e.printStackTrace();
			} 
		}else{
			redirectAttributes.addFlashAttribute("status", 
					messageSource.getMessage("NotExist.file", null, Locale.ENGLISH)); 
			redirectAttributes.addFlashAttribute("section", SECTION_DISCLOSURE); 

			model.clear();
			return "redirect:/profile"; 
		}
		return null; 
	}
	
	@PostMapping("/upload/student-agreement-and-disclosure-form")
	public String uploadDisclosureForm(ModelMap model,
			@Valid FileBucket fileBucket, BindingResult bindingResult, 
			@AuthenticationPrincipal UserImpl user, 
			@ModelAttribute("status") String status, 
			@ModelAttribute("section") String section,
			RedirectAttributes redirectAttributes){
		 
		if(bindingResult.hasErrors()){
			String FORM_NAME = "fileBucket"; 
			String key = BindingResult.MODEL_KEY_PREFIX + FORM_NAME; 
			redirectAttributes.addFlashAttribute(key, bindingResult);
			redirectAttributes.addFlashAttribute("fileBucket", fileBucket);
		}else{
			int result = ((UserDAOImpl)userDAO).uploadDisclosureForm(user.getUserID(), fileBucket);
			if(result != 0){
				status = "File uploaded successfully!"; 
			}else{
				status = "Error occurred during submission!"; 
			}
		}
		
		redirectAttributes.addFlashAttribute("status", status); 
		redirectAttributes.addFlashAttribute("section", SECTION_DISCLOSURE); 
		return "redirect:/profile";
	}
	
	@GetMapping(value="/download/student-agreement-and-disclosure-form")
	public void downloadStudentDisclosureForm(@AuthenticationPrincipal UserImpl user, 
			HttpServletResponse response) throws IOException{
		String fileName = String.format("%s_%s_disclosure_form.pdf", user.getFirstName(), user.getLastName()); 
		response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName.toLowerCase() + "\"");
        response.setContentType("application/pdf");
        byte[] content = ((UserDAOImpl)userDAO).downloadDisclosureForm(user.getUserID());  
		FileCopyUtils.copy(content, response.getOutputStream());
	}
	
	/** Personal Statement **/
	@PostMapping("/personal-statement")
	public @ResponseBody String updatePersonalStatement(
			@AuthenticationPrincipal UserImpl user, @RequestParam("pstatement") String pstatement) {
		 int result = ((UserDAOImpl)userDAO).updateProfilePersonalStatement(user.getUserID(), pstatement); 
		 ObjectNode response = objectMapper.createObjectNode(); 
		 if(result != 0){
			response.set("status", objectMapper.convertValue("ok", JsonNode.class));
		}else{
			response.set("status", objectMapper.convertValue("error", JsonNode.class));
		}
		return response.toString(); 
	}
	
	
}
