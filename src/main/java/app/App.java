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

		// There have to be 2 or 3 parameters, Path to ODM-File, FormOID and optionally an output path
		if (args.length < 2 || args.length > 3) {
			invalid_params();
		}
		String output_path = args.length == 2 ? "./" : args[2];
		OdmConverter.convert(args[0], args[1], output_path);

		// Performance Output:
		long afterUsedMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		log.info("Utilized memory: " + ((afterUsedMem-beforeUsedMem)/(1024*1024)) + "mb");
	}

	public static void invalid_params()
	{
		log.info("Usage: java -jar <odm2lss-jar-name> <ODM-File> <FormOID> <Output-Path (optional)>");
	}
}
