package parser.odm;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

	public OdmParser(String path)
	{
		survey = new Survey();
		odm = new File(path);
		if (!odm.exists()) {
			log.error("ODM File does not exist!");
			System.exit(1);
		}
	}

	public Survey parseFile(String form_oid)
	{
		try{
			SAXReader sax_reader = new SAXReader();
			doc = sax_reader.read(odm);
			// "//FormDef[@OID='" + form_oid + "']" does not work because of the namespace set in ODM
			Element form = (Element) doc.selectSingleNode("//*[name()='FormDef' and @OID='" + form_oid + "']");

			survey.setName(form.attributeValue("Name"));

			HashMap<String, List<String>> q_oids = parseItemGroups(form);
			HashMap<String, List<String>> cl_oids = parseItems(form, q_oids);
			parseCodeLists(form, cl_oids);

			//%%%%%%%%%%%%%%%%%%%%%%%%TMP$%$%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
			
			for (QuestionGroup qg : survey.getGroups()) {
				log.info("GroupID " + qg.getGid() + "| GroupName " + qg.getName());
			}
			for (Question q : survey.getQuestions()) {
				log.info("GID " + q.getGid() + " | QID " + q.getQid());
			}
			for (AnswerOption ao : survey.getAnswer_list()) {
				log.info("AnswerID " + ao.getAid() + "| Question " + ao.getQid());
				log.debug("Language " + ao.getLanguage() + "|| Code " + ao.getCode() + ": Option " + ao.getAnswer());
			}

		} catch (Exception e) {
			log.error(e.getMessage());
			System.exit(1);
		}
		return survey;
	}

	private HashMap<String, List<String>> parseItemGroups(Node form)
	{
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
			String desc = ig.elementText("Description");
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

			Element q_elem = (Element) item.selectSingleNode("*[name()='Question']/*[name()='TranslatedText']");
			if (q_elem == null) {
				log.info("No question Text, continuing");
				continue;
			}

			String desc = item.elementText("Description");
			String oid = item.attributeValue("OID");

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
				log.info(oid);
				type = "A";
			}

			String q_str = q_elem.getText();
			String l = q_elem.attributeValue("lang");

			for (String gr : q_oids.get(oid)) {
				String x_str = Integer.toString(x);

				Question q = new Question(x_str, gr, type, q_str, item.attributeValue("Name"), "Y", l);
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
		// List of all relevant Items
		ArrayList<Element> code_lists = new ArrayList<>();
		@SuppressWarnings("unchecked")
		List<Element> all_code_lists = doc.selectNodes("//*[name()='CodeList']");
		for (Element code_list : all_code_lists) {
			if (cl_oids.containsKey(code_list.attributeValue("OID"))) {
				code_lists.add(code_list);
			}
		}

		int x = 0;
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
			boolean is_enum_item = elems.get(1).getName().equals("EnumeratedItem");
			
			int sortorder = 0;
			// Iterate through all Items
			for (Element el : elems) {
				val = el.attributeValue("CodedValue");
				if (is_enum_item) {
					ans = val;
					// TODO: language not accurate
					lang = "en";
				} else {
					Element text = (Element) el.selectSingleNode("//*[name()='TranslatedText']");
					ans = text.getText();
					lang = text.attributeValue("xml:lang");
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
