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
package parser.odm;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import lombok.extern.log4j.Log4j;

@Log4j
public class OdmParser
{
	Survey survey;
	File odm;
	Document doc;

	public OdmParser(File odm)
	{
		survey = new Survey();
		this.odm = odm;
	}

	public Survey parseFile(String form_oid)
	{
		try{
			log.info("Reding ODM-File");
			SAXReader sax_reader = new SAXReader();
			doc = sax_reader.read(odm);
			// "//FormDef[@OID='" + form_oid + "']" does not work because of the namespace set in ODM
			Element form = (Element) doc.selectSingleNode("//*[name()='FormDef' and @OID='" + form_oid + "']");

			survey.setName(form.attributeValue("Name"));

			HashMap<String, List<String>> q_oids = parseItemGroups(form);
			HashMap<String, List<String>> cl_oids = parseItems(form, q_oids);
			parseCodeLists(form, cl_oids);

		} catch (Exception e) {
			log.error(e.getMessage());
			System.exit(1);
		}
		return survey;
	}

	private HashMap<String, List<String>> parseItemGroups(Node form)
	{
		log.info("Parsing ItemGroups");
		// List of all ItemGroupOIDs
		ArrayList<String> ig_oids = new ArrayList<>();
		@SuppressWarnings("unchecked")
		List<Element> ig_refs = form.selectNodes("*[name()='ItemGroupRef']");
		for (Element ref : ig_refs) {
			ig_oids.add(ref.attributeValue("ItemGroupOID"));
		}

		// List of all ItemGroups of the form
		ArrayList<Element> igs = new ArrayList<>();
		@SuppressWarnings("unchecked")
		List<Element> all_igs = doc.selectNodes("//*[name()='ItemGroupDef']");
		for (Element ig : all_igs) {
			if (ig_oids.contains(ig.attributeValue("OID"))) {
				igs.add(ig);
			}
		}

		// Add question groups and generate list of Items
		HashMap<String, String> ig_map = new HashMap<>();
		// Key: ItemOID Value: List of ItemGroupOIDs
		HashMap<String, List<String>> q_oids = new HashMap<>();

		int x = 1;
		@SuppressWarnings("unchecked")
		List<Element> all_items = doc.selectNodes("//*[name()='ItemDef']");
		for (Element ig : igs) {

			// Find out, whether there is any question in the group. Empty groups won't be parsed since they are not allowed in LimeSurvey
			// An Item does not need a question, so all items have to be checked

			@SuppressWarnings("unchecked")
			List<Element> q_list = ig.selectNodes("*[name()='ItemRef']");
			List<String> all_q_oids = q_list.stream().map(e -> e.attributeValue("ItemOID")).collect(Collectors.toList());
			boolean contains_question = all_items.stream().filter(e -> all_q_oids.contains(e.attributeValue("OID"))).anyMatch(e -> (e.selectSingleNode("*[name()='Question']/*[name()='TranslatedText']") != null));

			if (!contains_question) {
				log.info("Skipping Question Group " + ig.attributeValue("OID") + ", it contains no questions");
				continue;
			}

			// If the group has at least one question, parse it
			String x_str = Integer.toString(x);
			String desc = "";
			if (ig.element("Description") != null) {
				desc = ig.element("Description").elementText("TranslatedText");
			}
			QuestionGroup qg = new QuestionGroup(ig.attributeValue("Name"), x_str, desc);
			survey.addGroup(qg);
			ig_map.put(ig.attributeValue("OID"), x_str);
			
			// Add all Question OIDs of the group to the Map
			for (String q_ref : all_q_oids) {
				log.debug("Putting Question " + q_ref + " into group " + x_str + "(" + ig.attributeValue("OID") + ")");
				List<String> l;
				if ((l = q_oids.get(q_ref)) == null) {
					l = new ArrayList<>();
					l.add(x_str);
					q_oids.put(q_ref, l);
				} else {
					l.add(x_str);
				}

			}
			x++;
		}
		return q_oids;
	}

	private HashMap<String, List<String>> parseItems(Node form, HashMap<String, List<String>> q_oids)
	{
		log.info("Parsing items");
		// List of all relevant Items
		ArrayList<Element> items = new ArrayList<>();
		@SuppressWarnings("unchecked")
		List<Element> all_items = doc.selectNodes("//*[name()='ItemDef']");
		for (Element item : all_items) {
			if (q_oids.containsKey(item.attributeValue("OID"))) {
				items.add(item);
			}
		}
		
		// Add questions and generate a list of code lists
		HashMap<String, String> item_map = new HashMap<>();
		// Key: ItemOID Value: ItemGroupOID
		HashMap<String, List<String>> cl_oids = new HashMap<>();
		int x = 1;
		for (Element item : items) {

			String desc = "";
			if (item.element("Description") != null) {
				desc = item.element("Description").elementText("TranslatedText");
			}
			String oid = item.attributeValue("OID");

			Element q_elem = (Element) item.selectSingleNode("*[name()='Question']/*[name()='TranslatedText']");
			if (q_elem == null) {
				log.info("No question text in question " + oid + ", continuing");
				continue;
			}

			String type;
			switch (item.attributeValue("DataType")) {
				case "string":
				case "text":
					type = "T";
					break;
				case "float":
				case "integer":
					type = "N";
					break;
				default:
					type = "";
					log.warn("Question DataType not supported");
			}
			if (item.selectSingleNode("*[name()='CodeListRef']") != null) {
				type = "L";
			}

			String q_str = q_elem.getText();
			String l = q_elem.attributeValue("lang");

			for (String gr : q_oids.get(oid)) {
				String x_str = Integer.toString(x);

				Question q = new Question(x_str, gr, type, q_str, item.attributeValue("Name"), "N", l); //TODO: change mandatory to dynamic
				q.setHelp(desc);
				survey.addQuestion(q);
				item_map.put(item.attributeValue("OID"), x_str);

				Element cl = (Element) item.selectSingleNode("*[name()='CodeListRef']");
				if (cl != null) {
					String cl_oid = cl.attributeValue("CodeListOID");
					List<String> cl_oid_list = cl_oids.get(cl_oid);
					if (cl_oid_list == null) {
						ArrayList<String> new_list = new ArrayList<>();
						new_list.add(x_str);
						cl_oids.put(cl_oid, new_list);
					} else {
						cl_oid_list.add(x_str);
					}
				}
				x++;
			}
		}
		return cl_oids;
	}

	private void parseCodeLists(Node form, HashMap<String, List<String>> cl_oids)
	{
		for(Map.Entry<String, List<String>> e : cl_oids.entrySet()) {
			log.debug("CodeList: " + e.getKey());
			for(String x : e.getValue()) {
				log.debug("Qid:" + x);
			}
		}

		log.info("Parsing CodeLists");
		// List of all relevant Items
		ArrayList<Element> code_lists = new ArrayList<>();
		@SuppressWarnings("unchecked")
		List<Element> all_code_lists = doc.selectNodes("//*[name()='CodeList']");
		for (Element code_list : all_code_lists) {
			if (cl_oids.containsKey(code_list.attributeValue("OID"))) {
				code_lists.add(code_list);
			}
		}

		int x = 1;
		String x_str, val, ans, lang;
		// Iterate through all relevant lists and add them to the survey
		for (Element code_list : code_lists) {
			
			String oid = code_list.attributeValue("OID");

			@SuppressWarnings("unchecked")
			List<Element> elems = code_list.elements();

			//exclude description, alias and externalcodelist elements
			elems = elems.stream().filter(el -> el.getName().equals("EnumeratedItem") || el.getName().equals("CodeListItem")).collect(Collectors.toList());

			// find out whether this is a list of EnumeratedItems or CodeListItems
			// Note: There can be a description and a random amount of aliases, the position of the first Item is unknown, so we have to search the entire list of child elements
			boolean is_enum_item = elems.get(0).getName().equals("EnumeratedItem");
			
			int sortorder = 0;
			// Iterate through all Items
			for (Element el : elems) {
				val = el.attributeValue("CodedValue");
				lang = "en";
				// TODO: language not accurate
				if (is_enum_item) {
					ans = val;
				} else {
					Element text = (Element) el.selectSingleNode("*[name()='Decode']/*[name()='TranslatedText']");
					ans = text.getText();
					// lang = text.attributeValue("xml:lang");
				}

				AnswerOption ao = new AnswerOption("", "", lang, val, ans, Integer.toString(sortorder));

				for (String qid : cl_oids.get(oid)) {
					x_str = Integer.toString(x);
					AnswerOption ao_cpy = new AnswerOption(ao);
					ao_cpy.setAid(x_str);
					ao_cpy.setQid(qid);
					survey.addAnswer(ao_cpy);
					x++;
				}
				sortorder++;
			}
		}
	}
}
