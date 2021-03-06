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

import com.itextpdf.text.pdf.PdfContentByte;

public class SolidLine implements LineDash {
	private static final float LINE_WIDTH = 0.25f;
	
    public void applyLineDash(PdfContentByte canvas) {
    	canvas.setLineWidth(LINE_WIDTH);
    }
}
