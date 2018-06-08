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

import org.bitbucket.lvncnt.portal.model.Password;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Service("passwordValidator")
public class PasswordValidator implements Validator{
 
	@Override
	public boolean supports(Class<?> clazz) {
		return Password.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "password", 
				"required.password.password", "Password is required.");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "confirmPassword", 
				"required.password.confirmPassword", "Password is required.");
		Password user = (Password)target;
		if(!user.getPassword().equals(user.getConfirmPassword())){
			errors.rejectValue("password", "NotMatch.password.password", "Password does not match."); 
		}
		
		if(!org.bitbucket.lvncnt.utilities.Validator.validatePassword(user.getPassword())){
			errors.rejectValue("password", "Pattern.password.password", "Enter a correct password."); 
		}
	}
}
