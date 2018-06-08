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

import java.io.IOException;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.itextpdf.text.pdf.PdfReader;

import org.bitbucket.lvncnt.portal.model.FileBucket;
import net.sf.jmimemagic.Magic;
import net.sf.jmimemagic.MagicMatch;

@Component
public class FileValidator implements Validator {

	private static final long MAX_FILE_SIZE = 5242880;
	
	@Override
	public boolean supports(Class<?> clazz) {
		return FileBucket.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		FileBucket file = (FileBucket) target; 
		 
		if(file != null){
			if(file.getFile().getSize() == 0){
				errors.rejectValue("file","NotBlank.fileBucket.file", "Please select a file.");
			}else if(file.getFile().getSize() > MAX_FILE_SIZE){
				errors.rejectValue("file","Size.fileBucket.file", "File size should be less than 5MB.");
			}else{
				try {
					if(file.getFile().getBytes() != null){
						MagicMatch match = Magic.getMagicMatch(file.getFile().getBytes());
						String mineType = match.getMimeType(); 
						if(!mineType.equalsIgnoreCase("application/pdf")){
							errors.rejectValue("file","typeMismatch.fileBucket.file", "Only pdf files are allowed.");
						}else if(isEncrypted(file.getFile().getBytes())) {
							errors.rejectValue("file","Error.fileBucket.file", "An error has occurred while uploading the file.");
						}
					}
				} catch (Exception e) {
					errors.rejectValue("file","typeMismatch.fileBucket.file", "Only pdf files are allowed.");
				} 
			}
		}
	}
	
	private boolean isEncrypted(byte[] in) {
		PdfReader reader;
		PdfReader.unethicalreading = true; 
		try {
			reader = new PdfReader(in);
			if(reader.isEncrypted()) return true; 
		} catch (IOException e) {
			return true; 
		} 
		return false; 
	}

}
