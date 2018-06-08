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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

public class ExceptionHandler implements HandlerExceptionResolver{
	
	private Logger logger = LoggerFactory.getLogger(ExceptionHandler.class); 
	private static final String VIEW_PAGE_NOT_FOUND = "error/404"; 

	@Override
	public ModelAndView resolveException(HttpServletRequest request,
			HttpServletResponse response, Object handle, Exception e) {
		logger.error("Request: " + request.getRequestURL() + " raised " + e);
		return new ModelAndView(VIEW_PAGE_NOT_FOUND);
	}

}
