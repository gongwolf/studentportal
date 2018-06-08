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

import org.bitbucket.lvncnt.portal.model.AcademicBean;
import org.bitbucket.lvncnt.utilities.Parse;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Service("applicationAcademicValidator")
public class ApplicationAcademicValidator implements Validator {

	@Override
	public boolean supports(Class<?> clazz) {
		return AcademicBean.class.isAssignableFrom(clazz);
	}
	
	@Override
	public void validate(Object target, Errors errors) {
		
		AcademicBean bean = (AcademicBean) target; 
		String program = bean.getProgramNameAbbr();
		
		switch (program) {
		case ProgramCode.CCCONF:
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "academicSchool", 
					"NotBlank.academicBean.academicSchool", "Required");
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "academicMajor", 
					"NotBlank.academicBean.academicMajor", "Required");
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "academicStatus", 
					"NotBlank.academicBean.academicStatus", "Required");
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "academicCredit", 
					"NotBlank.academicBean.academicCredit", "Required");
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "academicTransferSchool", 
					"NotBlank.academicBean.academicTransferSchool", "Required");
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "academicTransferDate", 
					"NotBlank.academicBean.academicTransferDate", "Required");
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "academicIntendedMajor", 
					"NotBlank.academicBean.academicIntendedMajor", "Required");
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "academicGPA", 
					"NotBlank.academicBean.academicGPA", "Required");
			if(bean.getAcademicTransferDate() != null){
				if(!org.bitbucket.lvncnt.utilities.Validator.validateDateMMDDYYYY(Parse.FORMAT_DATE_MDY.format(bean.getAcademicTransferDate()))){
					errors.rejectValue("academicTransferDate", "typeMismatch", "Incorrect format."); 
				}
			}
			break;

		case ProgramCode.MESA:
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "academicSchool", 
					"NotBlank.academicBean.academicSchool", "Required");
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "academicYear", 
					"NotBlank.academicBean.academicYear", "Required");
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "academicGPA", 
					"NotBlank.academicBean.academicGPA", "Required");
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "academicGradDate", 
					"NotBlank.academicBean.academicGradDate", "Required");
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "academicTransferSchool", 
					"NotBlank.academicBean.academicTransferSchool", "Required");
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "academicTransferDate", 
					"NotBlank.academicBean.academicTransferDate", "Required");
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "academicIntendedMajor", 
					"NotBlank.academicBean.academicIntendedMajor", "Required");
			if(bean.getAcademicGradDate() != null){
				if(!org.bitbucket.lvncnt.utilities.Validator.validateDateMMDDYYYY(Parse.FORMAT_DATE_MDY.format(bean.getAcademicGradDate()))){
					errors.rejectValue("academicGradDate", "typeMismatch", "Incorrect format."); 
				}
			}
			if(bean.getAcademicTransferDate() != null){
				if(!org.bitbucket.lvncnt.utilities.Validator.validateDateMMDDYYYY(Parse.FORMAT_DATE_MDY.format(bean.getAcademicTransferDate()))){
					errors.rejectValue("academicTransferDate", "typeMismatch", "Incorrect format."); 
				}
			}
			break; 
			
		case ProgramCode.SCCORE:
		case ProgramCode.URS:
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "academicSchool", 
					"NotBlank.academicBean.academicSchool", "Required");
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "academicYear", 
					"NotBlank.academicBean.academicYear", "Required");
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "academicGradDate", 
					"NotBlank.academicBean.academicGradDate", "Required");
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "academicMajor", 
					"NotBlank.academicBean.academicMajor", "Required");
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "academicGPA", 
					"NotBlank.academicBean.academicGPA", "Required");
			if(bean.getAcademicGradDate() != null){
				if(!org.bitbucket.lvncnt.utilities.Validator.validateDateMMDDYYYY(Parse.FORMAT_DATE_MDY.format(bean.getAcademicGradDate()))){
					errors.rejectValue("academicGradDate", "typeMismatch", "Incorrect format."); 
				}
			}
			break; 
			
		case ProgramCode.TRANS:
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "academicSchool", 
					"NotBlank.academicBean.academicSchool", "Required");
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "academicYear", 
					"NotBlank.academicBean.academicYear", "Required");
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "academicGradDate", 
					"NotBlank.academicBean.academicGradDate", "Required");
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "academicMajor", 
					"NotBlank.academicBean.academicMajor", "Required");
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "academicGPA", 
					"NotBlank.academicBean.academicGPA", "Required");
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "academicTransferSchool", 
					"NotBlank.academicBean.academicTransferSchool", "Required");
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "academicTransferDate", 
					"NotBlank.academicBean.academicTransferDate", "Required");
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "academicIntendedMajor", 
					"NotBlank.academicBean.academicIntendedMajor", "Required");
			if(bean.getAcademicGradDate() != null){
				if(!org.bitbucket.lvncnt.utilities.Validator.validateDateMMDDYYYY(Parse.FORMAT_DATE_MDY.format(bean.getAcademicGradDate()))){
					errors.rejectValue("academicGradDate", "typeMismatch", "Incorrect format."); 
				}
			}
			if(bean.getAcademicTransferDate() != null){
				if(!org.bitbucket.lvncnt.utilities.Validator.validateDateMMDDYYYY(Parse.FORMAT_DATE_MDY.format(bean.getAcademicTransferDate()))){
					errors.rejectValue("academicTransferDate", "typeMismatch", "Incorrect format."); 
				}
			}
			break; 
			
		default:
			break;
		}
 
	}
}
