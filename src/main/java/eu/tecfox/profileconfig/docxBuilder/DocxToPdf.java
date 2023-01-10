/*
 * Copyright 2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.tecfox.profileconfig.docxBuilder;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.springframework.http.HttpStatus;

import com.documents4j.api.DocumentType;
import com.documents4j.api.IConverter;
import com.documents4j.job.LocalConverter;

import eu.tecfox.profileconfig.exception.ApiRequestException;


/**
 * Class responsible for converting docx files to pdf files.
 * 
 * @since 1.0
 * @author Florin Schikarski
 */
public class DocxToPdf {
    
	/**
	 * Converts any docx file into a pdf file.
	 * 
	 * @param docxPath path to the docx file.
	 * @param pdfPath target path for the pdf fiile.
	 * @throws ApiRequestException if any path is not found.
	 */
	public static void convert(String docxPath, String pdfPath) {

        try (InputStream docxInputStream = new FileInputStream(docxPath);
            OutputStream outputStream = new FileOutputStream(pdfPath)) {
            IConverter converter = LocalConverter.builder().build();

            converter.convert(docxInputStream).as(DocumentType.DOCX)
											  .to(outputStream)
											  .as(DocumentType.PDF)
											  .execute();

			converter.shutDown();

        } catch (IOException e) {
			throw new ApiRequestException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}