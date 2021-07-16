package app;

import java.io.File;

import lombok.extern.log4j.Log4j;
import parser.odm.OdmParser;
import parser.odm.Survey;
import writer.LssWriter;

@Log4j
public class OdmConverter
{
	public static void convert(String path, String form, String output_path)
	{		
		// Check if the first parameter is actually a odm path
		log.info("Checking Filepaths");
		File f = new File(path);
		String ext = f.getName().substring(f.getName().lastIndexOf(".") + 1);

		if(!f.isFile() || !ext.equals("xml")) {
			log.error("Path is not a valid xml file (is the file extension 'xml'?)!");
			System.exit(1);
		}
		OdmParser odm_parser = new OdmParser(new File(path));
		Survey s = odm_parser.parseFile(form);
		LssWriter lss_writer = new LssWriter(s);
		lss_writer.createDocument();

		lss_writer.writeFile(output_path);
	}
}
