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
import org.bitbucket.lvncnt.portal.model.User;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

@Controller
@RequestMapping("/admin/manage-account")
public class AdmAccountController {
	
	private static final String VIEW_MANAGE_ACCOUNT = "admin/manage-account"; 
	
	@Autowired private UserDAO userDAO;
	
	@GetMapping("")
	public String getManageAccountPage(ModelMap model) {
		if(!model.containsAttribute("user")){
			model.addAttribute("user", new User());
		}
		model.addAttribute("status", model.get("status")); 
		return VIEW_MANAGE_ACCOUNT; 
	}
	
	@PostMapping("/resetpassword.do")
	public View resetAccountPassword(ModelMap model, @ModelAttribute("user") User user,
			@RequestParam(value = "adminpassword", required=false) String adminpassword,
			BindingResult bindingResult, RedirectAttributes redirectAttributes) {
		new Validator() {
			@Override
			public boolean supports(Class<?> clazz) {
				return User.class.isAssignableFrom(clazz);
			}
			
			@Override
			public void validate(Object target, Errors errors) {
				ValidationUtils.rejectIfEmptyOrWhitespace(errors, "firstName", 
						"required.user.firstName", "Required.");
				ValidationUtils.rejectIfEmptyOrWhitespace(errors, "lastName", 
						"required.user.lastName", "Required.");
				ValidationUtils.rejectIfEmptyOrWhitespace(errors, "email", 
						"required.user.email", "Required.");
				ValidationUtils.rejectIfEmptyOrWhitespace(errors, "password", 
						"required.password.password", "Password is required.");
			}
		}.validate(user, bindingResult);
		if(bindingResult.hasErrors()){
			String FORM_NAME = "user"; 
			String key = BindingResult.MODEL_KEY_PREFIX + FORM_NAME; 
			redirectAttributes.addFlashAttribute(key, bindingResult);
			redirectAttributes.addFlashAttribute("user", user);
		}else if(adminpassword == null || (!((UserDAOImpl)userDAO).checkAdminIdentity(adminpassword))){
			redirectAttributes.addFlashAttribute("user", user);
			redirectAttributes.addFlashAttribute("status", "Password for Administrator is incorrect!"); 
		}else{
			int result = ((UserDAOImpl)userDAO).updatePassword(user.getFirstName().trim(), user.getLastName().trim(), user.getEmail().trim(), user.getPassword());
			String status; 
			if(result != 0){
				status = String.format("Password reseted. Email: %s, New password: %s", user.getEmail(), user.getPassword()); 
			}else{
				status = String.format("Account does not exist for %s %s (%s)", 
						user.getFirstName(), user.getLastName(), user.getEmail());
			}
			redirectAttributes.addFlashAttribute("status", status); 
		}
		return new RedirectView("/admin/manage-account", true); 
	}

}
