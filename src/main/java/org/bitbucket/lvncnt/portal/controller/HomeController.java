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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.bitbucket.lvncnt.portal.dao.StudentDAOImpl;
import org.bitbucket.lvncnt.portal.dao.UserDAO;
import org.bitbucket.lvncnt.portal.dao.UserDAOImpl;
import org.bitbucket.lvncnt.portal.model.*;
import org.bitbucket.lvncnt.portal.service.MailService;
import org.bitbucket.lvncnt.portal.service.PasswordValidator;
import org.bitbucket.lvncnt.portal.service.ProgramCode;
import org.bitbucket.lvncnt.portal.service.RegisterValidator;
import org.bitbucket.lvncnt.utilities.Parse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@SessionAttributes({"flagadmin"})
public class HomeController {
	
	private static final String VIEW_LOGIN = "index/login";
	private static final String VIEW_REGISTER = "index/register";
	private static final String VIEW_RESETPASSWORD = "index/resetpassword";
	private static final String VIEW_RESETPASSWORD_CONFIRM = "index/resetpassword-confirm"; 
	private static final String VIEW_CONTACT = "index/contact"; 
	private static final String VIEW_PAGE_NOT_FOUND = "error/404"; 
	
	@Value("${URL.mynmamp.base}") private String mynmampBaseURL; 
	
	@Autowired private UserDAO userDAO;
	@Autowired private StudentDAOImpl studentDAO;
	@Autowired private RegisterValidator registerValidator;
	@Autowired private PasswordValidator passwordValidator;
	@Autowired private MailService mailService;
	@Autowired private ObjectMapper objectMapper; 
 
	@GetMapping(value = {"/","/login" })
	public String login() {
		return VIEW_LOGIN;
	}
	
	@GetMapping("/home")
	public ModelAndView student(@AuthenticationPrincipal UserImpl user) {
		
		ModelAndView model = new ModelAndView(); 
		List<ApplicationBean> applicationList = studentDAO.getApplicationList(user.getUserID());
		model.addObject("applicationList", applicationList.stream()
				.filter(bean -> 
				bean.getSchoolYear() == LocalDate.now().getYear() || bean.getSchoolYear() == LocalDate.now().getYear() + 1 )
				.collect(Collectors.toList())); 
		model.addObject("name", user.getFirstName()); 
		
		String pstatement = ((UserDAOImpl)userDAO).getProfilePersonalStatement(user.getUserID());
		if(Parse.isBlankOrNull(pstatement)) {
			model.addObject("pstatement", "");
		}else {
			model.addObject("pstatement", pstatement);  
		}
		model.setViewName("student/student-home");	
 		return model;
 	}
	
	@GetMapping("/admin/home")
	public ModelAndView admin(@AuthenticationPrincipal UserImpl user){
		ModelAndView model = new ModelAndView(); 
		model.addObject("name", user.getFirstName()); 
		model.addObject("flagadmin", user.getRole().equals(Role.ADMIN));
		model.addObject("university", ProgramCode.ACADEMIC_SCHOOL.get(user.getAffiliation()));
		
		List<String> programs = new ArrayList<>(ProgramCode.PROGRAMS.keySet());
		List<Integer> years = IntStream.rangeClosed(-3, 1).map(i -> LocalDate.now().getYear() + i).boxed().collect(Collectors.toList());
		
		Map<String, Map<String, Integer>> mapAccept = new HashMap<>();
        Map<String, Map<String, Integer>> mapSemester = new HashMap<>();
        for(String p: programs) {
        		Map<String, Integer> m1 = new HashMap<>();
            Map<String, Integer> m2 = new HashMap<>();
            for (int y : years) {
                m1.put(y + "-1", 0);
                m1.put(y + "-0", 0);

                m2.put(y + "-Spring", 0);
                m2.put(y + "-Summer", 0);
                m2.put(y + "-Fall", 0);
            }
            mapAccept.put(p, m1);
            mapSemester.put(p, m2);
        } 
       
        ((UserDAOImpl)userDAO).getStatisticsBySemesterAndAccept(years, mapAccept, mapSemester);
		 
        Map<String, Integer> mapRegistration = new HashMap<>();
        ((UserDAOImpl)userDAO).getRegistrationNumber(mapRegistration);
        int totalApplication = 0; 
        model.addObject("mapRegistration", mapRegistration);
        
        try {
			model.addObject("categories", objectMapper.writeValueAsString(years));
			
			ArrayNode array = objectMapper.createArrayNode();
			for (String program : programs) {
				ObjectNode node = objectMapper.createObjectNode(); 
				node.set("name", objectMapper.convertValue(program, JsonNode.class));
				
	            int[] r = new int[years.size()];
	            for(Entry<String, Integer> e: mapSemester.get(program).entrySet()) {
	            	 	String k = e.getKey().split("-")[0];
		            int i = years.indexOf(Integer.parseInt(k));
		            r[i] += e.getValue();
		            totalApplication += e.getValue(); 
	            } 
	            node.set("data", objectMapper.convertValue(r, JsonNode.class));
				array.add(node);
	        }
			model.addObject("totalApplication", totalApplication);
			model.addObject("mapAccept", mapAccept);
			model.addObject("mapSemester", mapSemester);
			model.addObject("series", array);
		
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		model.setViewName("admin/admin-home");
		return model; 
	}
	
	@GetMapping("/mentor/home")
	public ModelAndView mentor(@AuthenticationPrincipal UserImpl user){
		ModelAndView model = new ModelAndView(); 
		model.addObject("name", user.getFirstName()); 
		model.setViewName("mentor/mentor-home");
		return model; 
	}
	
	@GetMapping(value="/register")
	public String getRegisterPage(ModelMap model){
		if(!model.containsAttribute("user")){
			model.addAttribute("user", new User());
		}
		model.addAttribute("yesno", ProgramCode.YES_NO);
		model.addAttribute("status", model.get("status"));
		return VIEW_REGISTER; 
	}
	
	@RequestMapping(value="/register", method=RequestMethod.POST)
	public String registerUser(ModelMap model, @ModelAttribute("user") User user, 
			@RequestParam(value="isMentor", defaultValue="") String isMentor,
			BindingResult bindingResult, @ModelAttribute("status") String status, 
			RedirectAttributes redirectAttributes){
		  
		registerValidator.validate(user, bindingResult);
		if(bindingResult.hasErrors() || isMentor.isEmpty() || isMentor.equals("1")){
			String FORM_NAME = "user"; 
			String key = BindingResult.MODEL_KEY_PREFIX + FORM_NAME; 
			redirectAttributes.addFlashAttribute(key, bindingResult);
			redirectAttributes.addFlashAttribute("user", user);
		}else if (((UserDAOImpl)userDAO).isAccountExist(user.getEmail()) != 0) {
				status = "Account " + user.getEmail() + " Already Registered! ";
		}else{ 
			int userID = ((UserDAOImpl)userDAO).register(user);  
			if(userID == 0){
				status = "Registration Failed, Try Again! ";
			}else{
				// create profile in profile_student table 
				((UserDAOImpl)userDAO).createProfileStudent(userID); 
				status = "Registered Successfully! ";
			} 
		}
		redirectAttributes.addFlashAttribute("status", status);
		return "redirect:/register" ; 
	}
	
	@GetMapping(value="/resetpassword")
	public String getResetpasswordView(ModelMap model){
		if(!model.containsAttribute("user")){
			model.addAttribute("user", new User()); 
		}
		model.addAttribute("status", model.get("status"));
		return VIEW_RESETPASSWORD; 
	}
	
	@RequestMapping(value="/resetpassword", method=RequestMethod.POST)
	public String resetpassword(ModelMap model, @ModelAttribute("user") User user, 
			BindingResult bindingResult, @ModelAttribute("status") String status, 
			RedirectAttributes redirectAttributes){
		new Validator() {
			@Override
			public boolean supports(Class<?> clazz) {
				return User.class.isAssignableFrom(clazz); 
			}
			@Override
			public void validate(Object target, Errors errors) {
				ValidationUtils.rejectIfEmptyOrWhitespace(errors, "email", 
						"required.user.email", "Required.");
			}
		}.validate(user, bindingResult);; 
		if(bindingResult.hasErrors()){
			String FORM_NAME = "user"; 
			String key = BindingResult.MODEL_KEY_PREFIX + FORM_NAME; 
			redirectAttributes.addFlashAttribute(key, bindingResult);
			redirectAttributes.addFlashAttribute("user", user);
		}else{
			if (((UserDAOImpl)userDAO).isAccountExist(user.getEmail()) == 0) {
				status = "Account " + user.getEmail() + " Not Registered! ";
			}else{
				// set reset_key and reset_timeout in the user table 
				String resetKey = ((UserDAOImpl)userDAO).updateResetKey(user.getEmail()); 
				// then send email to user 
				if(resetKey != null){
					String url = String.format("%s/resetpassword/%s", mynmampBaseURL, resetKey);
					boolean success = mailService.sendMailResetpassword(user.getEmail(), url); 
					if(!success){
						status = "Submission Failed, Try Again!"; 
					}else{
						status = "Password reset email was sent to the email address associated with your account!";
					}
				}
			}
			
			redirectAttributes.addFlashAttribute("status", status);
		}
		 
		return "redirect:/resetpassword"; 
	}
	
	@RequestMapping(value="/resetpassword/{key}", method=RequestMethod.GET)
	public String resetpasswordConfirm(ModelMap model, @PathVariable("key") String key){
		 
		// check if link valid and expired -> go to error page 
		if (!((UserDAOImpl)userDAO).isResetPasswordKeyValid(key)) {
			return VIEW_PAGE_NOT_FOUND;
		}
	 
		if(!model.containsAttribute("userPass") ){
			model.addAttribute("userPass",  new Password());
		}
		   
		model.addAttribute("status", model.get("status")); 
		return VIEW_RESETPASSWORD_CONFIRM; 
	}

	@RequestMapping(value="/resetpassword/{key}", method=RequestMethod.POST)
	public String uploadRecommendations(ModelMap model,
			@ModelAttribute("userPass") Password userPass, BindingResult bindingResult,   
			@ModelAttribute("status") String status, 
			@PathVariable("key") String key, RedirectAttributes redirectAttributes){
		 
		passwordValidator.validate(userPass, bindingResult);
		if(bindingResult.hasErrors()){
			String FORM_NAME = "userPass"; 
			redirectAttributes.addFlashAttribute(BindingResult.MODEL_KEY_PREFIX + FORM_NAME, bindingResult);
			redirectAttributes.addFlashAttribute("userPass", userPass);
		}else{
			int result = ((UserDAOImpl)userDAO).updatePassword(key, userPass.getPassword()); 
			if(result != 0){
				status = "Password reset successfully!"; 
			}else{
				status = "Error occurred during submission!"; 
			}
			redirectAttributes.addFlashAttribute("status", status); 
		}
		return "redirect:/resetpassword/"+key;
	}
	
	@GetMapping(value="/contact")
	public String contact(){
		return VIEW_CONTACT; 
	}
	
}