package eu.tecfox.profileconfig.docxBuilder;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


/**
 * Test class for {@link DocxToPdf}.
 * 
 * @since 1.0
 * @author Florin Schikarski
 */
public class DocxToPdfTest {

    private static final String DOCX_PATH = "./src/test/java/eu/tecfox/profileconfig/docxBuilder/testResources/Template.docx";

    private static final String PDF_PATH = "./src/test/java/eu/tecfox/profileconfig/docxBuilder/testResources/Template.pdf";


    @BeforeEach
    void setup() {

        // remove pdf file
        new File(PDF_PATH).delete();
    }

    @Test
    void convert_shouldProducePdfFile() {
        
        // convert
        DocxToPdf.convert(DOCX_PATH, PDF_PATH);        
                           
        // check that pdf file exists
        assertTrue(new File(PDF_PATH).exists());
    }
}