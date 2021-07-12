package app;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
		Pattern lsa_pattern = Pattern.compile("^(\\.?/?(?:.*/)*)(.*?)\\.xml$", Pattern.CASE_INSENSITIVE);
		Matcher lsa_matcher = lsa_pattern.matcher(path);
		log.info("Checking Filepaths");

		if(!lsa_matcher.find()) {
			log.error("Path is not a valid xml file (is the file extension 'xml'?)!");
			System.exit(1);
		}
		OdmParser odm_parser = new OdmParser(new File(path));
		Survey s = odm_parser.parseFile(form);
		LssWriter lss_writer = new LssWriter(s);
		lss_writer.createDocument();

		output_path += output_path.charAt(output_path.length()-1) == '/' ? "" : "/";
		lss_writer.writeFile(output_path);
	}
}
