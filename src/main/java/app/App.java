package app;

import org.apache.log4j.BasicConfigurator;

import lombok.extern.log4j.Log4j;

import java.io.File;
@Log4j
public class App {
	protected static File lss_file;
	protected static File lsr_file;

    public static void main(String[] args)
    {
		BasicConfigurator.configure();
		long beforeUsedMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

		// There have to be 1 or 2 parameters, LSA-File and output path
		// if (args.length == 0 || args.length > 2) {
		// 	invalid_params();
		// }
		String path = "./src/main/xml/ODMTestfile.xml";
		String se = "StudyEventOID.1";
		String form = "FormOID.1";
		OdmConverter odm_converter = new OdmConverter();
		odm_converter.convert(path, se, form);

		// Performance Output:
		long afterUsedMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		log.info("Utilized memory: " + ((afterUsedMem-beforeUsedMem)/(1024*1024)) + "mb");
	}

	public static void invalid_params()
	{
		log.info("Usage: java -jar <lsa2odm-jar-name> <.lsa-File> <Output-Path (optional)>");
	}
}
