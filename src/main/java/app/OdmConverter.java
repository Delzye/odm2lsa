package app;

import lombok.extern.log4j.Log4j;
import parser.odm.OdmParser;
import parser.odm.Survey;
import writer.LssWriter;

@Log4j
public class OdmConverter
{
	public void convert(String path, String se, String form)
	{		
		OdmParser odm_parser = new OdmParser(path);
		Survey s = odm_parser.parseFile(form);
		LssWriter lss_writer = new LssWriter(s);
		lss_writer.createDocument();
		lss_writer.writeFile("./");
	}

	public static void invalid_params()
	{
		log.info("Usage: java -jar <lsa2odm-jar-name> <.lsa-File> <Output-Path (optional)>");
	}
}
