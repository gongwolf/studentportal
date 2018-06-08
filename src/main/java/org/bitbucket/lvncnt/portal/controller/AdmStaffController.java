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
import org.bitbucket.lvncnt.portal.model.User;
import org.bitbucket.lvncnt.portal.service.ProgramCode;
import org.bitbucket.lvncnt.portal.service.StaffValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

@Controller
@RequestMapping("/admin/manage-staff")
public class AdmStaffController {
	
	private static final String VIEW_MANAGE_STAFF = "admin/manage-staff"; 
	
	@Autowired private UserDAO userDAO;
	@Autowired private StaffValidator staffValidator;
	@Autowired private ObjectMapper objectMapper; 
	
	@GetMapping("")
	public ModelAndView getManageProgram(ModelMap model) {
		if(!model.containsAttribute("user")){
			model.addAttribute("user", new User());
		}
		if(!model.containsAttribute("staffList")){
			model.addAttribute("staffList", ((UserDAOImpl)userDAO).getStaffList());
		}
		model.addAttribute("schools", ProgramCode.ACADEMIC_SCHOOL);
		model.addAttribute("status", model.get("status")); 
		return new ModelAndView(VIEW_MANAGE_STAFF); 
	}
	
	@PostMapping("/create.do")
	public View createStaffAccount(ModelMap model, @ModelAttribute("user") User user, 
			@RequestParam(value = "phone", required = false) String phone,
			BindingResult bindingResult, RedirectAttributes redirectAttributes){
		staffValidator.validate(user, bindingResult);
		RedirectView view = new RedirectView("/admin/manage-staff", true); 
		if(bindingResult.hasErrors()){
			String FORM_NAME = "user"; 
			String key = BindingResult.MODEL_KEY_PREFIX + FORM_NAME; 
			redirectAttributes.addFlashAttribute(key, bindingResult);
			redirectAttributes.addFlashAttribute("user", user);
			return view;
		}
		 
		boolean result = ((UserDAOImpl)userDAO).createStaffAccount(user, phone); 
		String status; 
		if(result){
			status = String.format("Account Added. Email: %s, Password: %s", user.getEmail(), user.getPassword()); 
		}else{
			status = "Error occurred during submission!"; 
		}
		redirectAttributes.addFlashAttribute("status", status); 
		return view;
	}
	
	@PostMapping("/delete.do")
	public @ResponseBody String deleteStaffAccount(@RequestParam("userID") int userID){
		boolean status = ((UserDAOImpl)userDAO).deleteStaffAccount(userID); 
		ObjectNode json = objectMapper.createObjectNode(); 
		if(status){
			json.set("status", objectMapper.convertValue("ok", JsonNode.class));
		}else{
			json.set("status", objectMapper.convertValue("error", JsonNode.class));
		} 
		return json.toString();
	}

}
