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

import org.bitbucket.lvncnt.portal.model.SignatureBean;
import org.bitbucket.lvncnt.utilities.Parse;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Component
public class SignatureValidator implements Validator {
 
	@Override
	public boolean supports(Class<?> clazz) {
		return SignatureBean.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		SignatureBean bean = (SignatureBean)target; 
		 
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "signature", 
				"NotBlank.signatureBean.signature", "Required");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "signatureDate", 
				"NotBlank.signatureBean.signatureDate", "Required");
		if(bean.getSignatureDate() != null){
			if(!org.bitbucket.lvncnt.utilities.Validator.validateDateMMDDYYYY(Parse.FORMAT_DATE_MDY.format(bean.getSignatureDate()))){
				errors.rejectValue("signatureDate", "typeMismatch", "Incorrect format.");
			}
		} 
	}

}
