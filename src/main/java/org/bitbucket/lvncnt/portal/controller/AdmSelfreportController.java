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

import org.bitbucket.lvncnt.portal.dao.AdminDAOImpl;
import org.bitbucket.lvncnt.portal.model.selfreport.SelfReportBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

@Controller
@RequestMapping("/admin/manage-selfreport") 
public class AdmSelfreportController {
	
	private static final String VIEW_MANAGE_SELFREPORT = "admin/manage-selfreport"; 
	private static final String VIEW_EDIT_SELFREPORT = "admin/edit-selfreport"; 

	@Autowired private AdminDAOImpl adminDAO;
 
	/** Manage Selfreport **/
	
	@GetMapping("")
	public ModelAndView getManagSelfreport(ModelMap model){
		if(!model.containsAttribute("selfreportWindows")){
			model.addAttribute("selfreportWindows",  adminDAO.getSelfreportWindows(false));
		}
		if(!model.containsAttribute("selfReportBean")){
			model.addAttribute("selfReportBean", new SelfReportBean());
		}
		
		model.addAttribute("status", model.get("status")); 
		return new ModelAndView(VIEW_MANAGE_SELFREPORT); 
	}
	
	@PostMapping("")
	public View addSelfreportWindow(ModelMap model, 
			@ModelAttribute("selfReportBean") @Valid SelfReportBean selfReportBean,
			BindingResult bindingResult, RedirectAttributes redirectAttributes){
		RedirectView view = new RedirectView();
		view.setContextRelative(true);
		if(bindingResult.hasErrors()){
			String FORM_NAME = "selfReportBean"; 
			String key = BindingResult.MODEL_KEY_PREFIX + FORM_NAME; 
			redirectAttributes.addFlashAttribute(key, bindingResult);
			redirectAttributes.addFlashAttribute("selfReportBean", selfReportBean);
		}else {
			int result = adminDAO.addSelfreportWindow(selfReportBean); 
			String status; 
			if(result != 0){
				status = "Updated successfully!"; 
			}else{
				status = "Error occurred during submission!"; 
			}
			redirectAttributes.addFlashAttribute("status", status); 
		}
		return new RedirectView("/admin/manage-selfreport", true); 
	}
 
	@GetMapping(value="/edit", params={"windowid"})
	public ModelAndView getEditSelfreport(ModelMap model, 
			@RequestParam("windowid") int windowID){
		if(!model.containsAttribute("selfReportBean")){
			model.addAttribute("selfReportBean", adminDAO.getSelfreportWindow(windowID)); 
		}
		model.addAttribute("windowid", windowID); 
		return new ModelAndView(VIEW_EDIT_SELFREPORT); 
	}
	 
	@PostMapping(value="/edit", params={"windowid"})
	public ModelAndView setEditProgram(ModelMap model, @RequestParam("windowid") int windowID,
			@ModelAttribute("selfReportBean") @Valid SelfReportBean selfReportBean,
			BindingResult bindingResult, RedirectAttributes redirectAttributes){
		if(bindingResult.hasErrors()){
			model.addAttribute("windowid", windowID); 
			return new ModelAndView(VIEW_EDIT_SELFREPORT);
		}
		int result = adminDAO.setSelfreportWindow(windowID, selfReportBean); 
		String status; 
		if(result != 0){
			status = "Program updated successfully!"; 
		}else{
			status = "Error occurred during submission!"; 
		} 
		redirectAttributes.addFlashAttribute("status", status); 
		
		return new ModelAndView(new RedirectView("/admin/manage-selfreport", true)); 
	}

}
