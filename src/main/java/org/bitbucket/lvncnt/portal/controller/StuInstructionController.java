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

import org.bitbucket.lvncnt.portal.dao.AdminDAOImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class StuInstructionController {
	
	private static final String VIEW_INSTRUCTION = "student/instruction"; 
	
	@Autowired
	private AdminDAOImpl adminDAO;
	
	@GetMapping("/instruction")
	public String getProgramRequirement(ModelMap model){
		if(!model.containsAttribute("windows")){
			model.addAttribute("windows", adminDAO.getProgramDeadlines());
		}
		return VIEW_INSTRUCTION; 
	}
}
