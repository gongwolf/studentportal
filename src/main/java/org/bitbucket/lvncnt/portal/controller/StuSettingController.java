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

import javax.validation.Valid;

import org.bitbucket.lvncnt.portal.dao.UserDAO;
import org.bitbucket.lvncnt.portal.dao.UserDAOImpl;
import org.bitbucket.lvncnt.portal.model.Password;
import org.bitbucket.lvncnt.portal.model.UserImpl;
import org.bitbucket.lvncnt.portal.service.PasswordValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/setting")
public class StuSettingController {
	
	private static final String VIEW_SETTING = "student/setting"; 
	private static final String SECTION_PASSWORD = "password.do";
	
	@Autowired private UserDAO userDAO;
	@Autowired private PasswordValidator passwordValidator;

	@GetMapping("")
	public String getSetting(ModelMap model) {
		if(!model.containsAttribute("userPass") ){
			model.addAttribute("userPass",  new Password());
		} 
		model.addAttribute("status", model.get("status")); 
		model.addAttribute("section", SECTION_PASSWORD); 
		return VIEW_SETTING; 
	}
	
	@PostMapping("password.do")
	public String setProfilePassword(ModelMap model,
			@ModelAttribute("userPass") @Valid Password userPass, 
			BindingResult bindingResult, 
			@AuthenticationPrincipal UserImpl user,
			@ModelAttribute("status") String status, 
			RedirectAttributes redirectAttributes){
	 
		passwordValidator.validate(userPass, bindingResult);
		if(bindingResult.hasErrors()){
			String FORM_NAME = "userPass"; 
			String key = BindingResult.MODEL_KEY_PREFIX + FORM_NAME; 
			redirectAttributes.addFlashAttribute(key, bindingResult);
			redirectAttributes.addFlashAttribute("userPass", userPass);
			return "redirect:/setting"; 
		}

		int result = ((UserDAOImpl)userDAO).updatePassword(user.getUserID(), userPass.getPassword());
		if(result != 0){
			status = "Password updated successfully!"; 
		}else{
			status = "Error occurred during submission!"; 
		}
		redirectAttributes.addFlashAttribute("status", status); 

		model.clear();
		return "redirect:/setting"; 
	}
}
