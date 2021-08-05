/* MIT License

Copyright (c) 2021 Anton Mende (Delzye)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE. */
package app;

import lombok.extern.log4j.Log4j;

import java.io.File;
@Log4j
public class App {
	protected static File lss_file;
	protected static File lsr_file;

    public static void main(String[] args)
    {
		long beforeUsedMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

		// There have to be 2 or 3 parameters, Path to ODM-File, FormOID and optionally an output path
		if (args.length < 2 || args.length > 3) {
			invalid_params();
		}
		String output_path = args.length == 2 ? "" : args[2];
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
