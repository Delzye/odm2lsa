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
