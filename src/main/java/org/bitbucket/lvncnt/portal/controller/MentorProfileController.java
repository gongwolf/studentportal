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

import org.bitbucket.lvncnt.portal.dao.UserDAO;
import org.bitbucket.lvncnt.portal.dao.UserDAOImpl;
import org.bitbucket.lvncnt.portal.model.EthnicityBean;
import org.bitbucket.lvncnt.portal.model.MentorBean;
import org.bitbucket.lvncnt.portal.model.ProfileBean;
import org.bitbucket.lvncnt.portal.model.UserImpl;
import org.bitbucket.lvncnt.portal.service.ProgramCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/mentor/profile")
public class MentorProfileController {
	
	private static final String VIEW_PROFILE = "mentor/profile"; 
	private static final String SECTION_MENTOR = "mentor.do";
	private static final String SECTION_ETHNICITY = "ethnicity.do";
	 
	@Autowired private UserDAO userDAO;
	
	@GetMapping("")
	public String getProfile(ModelMap model, @AuthenticationPrincipal UserImpl user){
		
		ProfileBean profileBean = ((UserDAOImpl)userDAO).getProfileMentor(user.getUserID());
		if(!model.containsAttribute("mentorBean")){
			if(profileBean == null || profileBean.getMentorBean() == null){
				model.addAttribute("mentorBean", new MentorBean());
			}else{
				model.addAttribute("mentorBean", profileBean.getMentorBean());
			}
		} 
		
		if(!model.containsAttribute("ethnicityBean")){
			if(profileBean == null || profileBean.getEthnicityBean()== null){
				model.addAttribute("ethnicityBean", new EthnicityBean());
			}else{
				model.addAttribute("ethnicityBean", profileBean.getEthnicityBean());
			}
		}
		  
		model.addAttribute("yesno", ProgramCode.YES_NO);
		model.addAttribute("academicSchool", ProgramCode.ACADEMIC_SCHOOL);
		model.addAttribute("mentorPrefix", ProgramCode.MENTOR_PREFIX);
		model.addAttribute("race", ProgramCode.CHECKBOX_RACE);
		model.addAttribute("disability", ProgramCode.DISABILITY_STATUS);
		
		model.addAttribute("status", model.get("status")); 
		model.addAttribute("section", model.get("section")); 
		return VIEW_PROFILE; 
	}
 
	@PostMapping("/mentor.do")
	public String setProfileBiography(ModelMap model,
			@ModelAttribute("mentorBean") MentorBean mentorBean, 
			BindingResult bindingResult, @AuthenticationPrincipal UserImpl user, 
			@ModelAttribute("status") String status, @ModelAttribute("section") String section,
			RedirectAttributes redirectAttributes){
	 
		new Validator() {
			@Override
			public boolean supports(Class<?> clazz) {
				return MentorBean.class.isAssignableFrom(clazz);
			}
			
			@Override
			public void validate(Object target, Errors errors) {
				ValidationUtils.rejectIfEmptyOrWhitespace(errors, "mentorFirstName", 
						"NotBlank.mentorBean.mentorFirstName", "Required.");
				ValidationUtils.rejectIfEmptyOrWhitespace(errors, "mentorLastName", 
						"NotBlank.mentorBean.mentorLastName", "Required.");
				ValidationUtils.rejectIfEmptyOrWhitespace(errors, "mentorPrefix", 
						"NotBlank.mentorBean.mentorPrefix", "Required.");
				ValidationUtils.rejectIfEmptyOrWhitespace(errors, "mentorEmail", 
						"NotBlank.mentorBean.mentorEmail", "Required.");
				ValidationUtils.rejectIfEmptyOrWhitespace(errors, "mentorTitle", 
						"NotBlank.mentorBean.mentorTitle", "Required.");
				ValidationUtils.rejectIfEmptyOrWhitespace(errors, "mentorInstitution", 
						"NotBlank.mentorBean.mentorInstitution", "Required.");
				ValidationUtils.rejectIfEmptyOrWhitespace(errors, "mentorDept", 
						"NotBlank.mentorBean.mentorDept", "Required.");
				ValidationUtils.rejectIfEmptyOrWhitespace(errors, "mentorPhone", 
						"NotBlank.mentorBean.mentorPhone", "Required.");
				ValidationUtils.rejectIfEmptyOrWhitespace(errors, "mentorBuilding", 
						"NotBlank.mentorBean.mentorBuilding", "Required.");
				ValidationUtils.rejectIfEmptyOrWhitespace(errors, "mentorFax", 
						"NotBlank.mentorBean.mentorFax", "Required.");
			}
		}.validate(mentorBean, bindingResult);
	 
		if(bindingResult.hasErrors()){
 			redirectAttributes.addFlashAttribute("section", SECTION_MENTOR);
			String FORM_NAME = "mentorBean";
			String key = BindingResult.MODEL_KEY_PREFIX + FORM_NAME; 
			redirectAttributes.addFlashAttribute(key, bindingResult);
			redirectAttributes.addFlashAttribute("mentorBean", mentorBean);
			return "redirect:/mentor/profile";
		}
		 
		int result = ((UserDAOImpl)userDAO).updateProfileMentorBiography(user.getUserID(), mentorBean); 
		if(result != 0){
			status = "Profile updated successfully!"; 
		}else{
			status = "Error occurred during submission!"; 
		}
		redirectAttributes.addFlashAttribute("status", status); 
		redirectAttributes.addFlashAttribute("section", SECTION_MENTOR); 
		model.clear();
		return "redirect:/mentor/profile";
	}
 
	 
	@PostMapping("/ethnicity.do")
	public String setProfileEthnicity(ModelMap model, @AuthenticationPrincipal UserImpl mentor,
			@ModelAttribute("ethnicityBean") EthnicityBean ethnicityBean, 
			BindingResult bindingResult, 
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
			return "redirect:/mentor/profile";
		}
		 
		int result = ((UserDAOImpl)userDAO).updateProfileMentorEthnicity(mentor.getUserID(), ethnicityBean); 
		if(result != 0){
			status = "Profile updated successfully!"; 
		}else{
			status = "Error occurred during submission!"; 
		}
		redirectAttributes.addFlashAttribute("status", status); 
		redirectAttributes.addFlashAttribute("section", SECTION_ETHNICITY); 
		model.clear();
		return "redirect:/mentor/profile";
	} 
}
