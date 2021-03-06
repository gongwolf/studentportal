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

package org.bitbucket.lvncnt.portal.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class PortfolioDocument implements Serializable {
	private static final long serialVersionUID = 1L;
	private String fileId; 
	private String name; 
	private String description;
	private LocalDateTime createDate; 
	public String getFileId() {
		return fileId;
	}
	public void setFileId(String fileId) {
		this.fileId = fileId;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	} 
	public LocalDateTime getCreateDate() {
		if(createDate == null) return null; 
		return createDate;
	}
	public void setCreateDate(LocalDateTime createDate) {
		if(createDate == null) return; 
		this.createDate = createDate;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
}
