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
import org.bitbucket.lvncnt.portal.model.User;
import org.bitbucket.lvncnt.portal.model.UserImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

@Controller
@RequestMapping("/admin/profile") 
public class AdmProfileController {
	
	private static final String VIEW_PROFILE = "admin/profile"; 
	private static final String SECTION_PROFILE = "profile.do"; 

	@Autowired private UserDAO userDAO;

	@GetMapping(value="")
	public ModelAndView getProfile(ModelMap model, 
			@AuthenticationPrincipal UserImpl user){
		if(!model.containsAttribute("user")){
			model.addAttribute("user",  user);
		} 
	   
		model.addAttribute("status", model.get("status")); 
		model.addAttribute("section", SECTION_PROFILE);
		return new ModelAndView(VIEW_PROFILE); 
	}
	
	@PostMapping(value="/profile.do")
	public View setProfile(ModelMap model,
			@AuthenticationPrincipal UserImpl oldUser, 
			@ModelAttribute("user") @Valid User user,
			BindingResult bindingResult,  
			 @ModelAttribute("status") String status,
			 @ModelAttribute("section") String section,
			 RedirectAttributes redirectAttributes){
		
		RedirectView view = new RedirectView("/admin/profile", true); 
		if(bindingResult.hasErrors() || 
				oldUser.getEmail().equalsIgnoreCase(user.getEmail().trim())){
			String FORM_NAME = "user";
			String key = BindingResult.MODEL_KEY_PREFIX + FORM_NAME; 
			redirectAttributes.addFlashAttribute(key, bindingResult);
			redirectAttributes.addFlashAttribute("user", user);
			return view; 
		}
		
		// update email 
		int result = ((UserDAOImpl)userDAO).updateProfileAdmin(oldUser.getUserID(),
				oldUser.getRole(), user.getEmail().trim()); 
		if(result == 1){
			status = "Profile updated successfully!"; 
			oldUser.setEmail(user.getEmail());
		}else{
			status = "Error occurred during submission!"; 
		}
		 
		redirectAttributes.addFlashAttribute("status", status); 
		redirectAttributes.addFlashAttribute("section", SECTION_PROFILE); 
		model.clear();
		return view;
	} 
 
}
