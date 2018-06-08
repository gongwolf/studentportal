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
package org.bitbucket.lvncnt.portal.service;

import org.bitbucket.lvncnt.portal.model.User;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Service("staffValidator")
public class StaffValidator implements Validator {
 
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
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "affiliation", 
				"required.user.affiliation", "Required.");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "password", 
				"required.password.password", "Password is required.");
		User user = (User)target; 
		if(!org.bitbucket.lvncnt.utilities.Validator.validateEmailAddress(user.getEmail())){
			errors.rejectValue("email", "Email.user.email", "Please provide a valid email address."); 
		}
	}
	
}
