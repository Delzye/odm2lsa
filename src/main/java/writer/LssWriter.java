package writer;

import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import lombok.extern.log4j.Log4j;
import parser.odm.AnswerOption;
import parser.odm.Question;
import parser.odm.QuestionGroup;
import parser.odm.Survey;

@Log4j
public class LssWriter
{
	protected Survey survey;
	protected Document doc;


	public LssWriter(Survey s)
	{
		this.survey = s;
		this.doc = DocumentHelper.createDocument();
	}

	public void createDocument()
	{
		/* 0: answers
		 * 1: answer_l10ns
		 * 2: groups
		 * 3: group_l10ns
		 * 4: questions
		 * 5: subquestions
		 * 6: question_l10ns
		 * 7: question_attributes
		 * 8: surveys
		 * 9: surveys_languagesettings
		 */
		Element[] elems = createDocumentRoot();

		addGroups(elems[2], elems[3]);
		addQuestions(elems[4], elems[6], elems[7]);
		addAnswers(elems[0], elems[1]);
		addSurveyData(elems[8], elems[9]);
	}

	public void writeFile(String path)
	{
		try{
			SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy:HH-mm-ss");  
			Date date = new Date();  
			FileWriter fileWriter = new FileWriter(path + "survey_" + formatter.format(date) + ".lss");
			OutputFormat format = OutputFormat.createPrettyPrint();
			XMLWriter writer = new XMLWriter(fileWriter, format);
			writer.write(doc);
			writer.close();
		} catch (Exception e) {
			log.error(e);
		}
	}

	private Element[] createDocumentRoot()
	{
		Element[] lss_elems = new Element[10];
		Element root = doc.addElement("document");
		root.addElement("LimeSurveyDocType")
			.addText("Survey");
		root.addElement("DBVersion")
			.addText("443");
		root.addElement("languages")
			.addElement("language")
			.addText("en"); // TODO
		lss_elems[0] = root.addElement("answers")
			.addElement("rows");
		lss_elems[1] = root.addElement("answer_l10ns")
			.addElement("rows");
		lss_elems[2] = root.addElement("groups")
			.addElement("rows");
		lss_elems[3] = root.addElement("group_l10ns")
			.addElement("rows");
		lss_elems[4] = root.addElement("questions")
			.addElement("rows");
		lss_elems[5] = root.addElement("subquestions")
			.addElement("rows");
		lss_elems[6] = root.addElement("question_l10ns")
			.addElement("rows");
		lss_elems[7] = root.addElement("question_attributes")
			.addElement("rows");
		lss_elems[8] = root.addElement("surveys")
			.addElement("rows");
		lss_elems[9] = root.addElement("surveys_languagesettings")
			.addElement("rows");
		return lss_elems;
	}

	private void addAnswers(Element answers, Element answer)
	{
		for (AnswerOption ao : survey.getAnswer_list()) {
			Element row = answers.addElement("row");
			row.addElement("aid")
				.addCDATA(ao.getAid());
			row.addElement("qid")
				.addCDATA(ao.getQid());
			row.addElement("code")
				.addCDATA(ao.getCode());
			row.addElement("sortorder")
				.addCDATA(ao.getSortorder());

			row = answer.addElement("row");
			row.addElement("id")
				.addCDATA(ao.getAid());
			row.addElement("aid")
				.addCDATA(ao.getAid());
			row.addElement("answer")
				.addCDATA(ao.getAnswer());
			row.addElement("language")
				.addCDATA(ao.getLanguage());
		}

	}

	private void addGroups(Element groups, Element group)
	{
		for (QuestionGroup qg : survey.getGroups()) {
			Element row = groups.addElement("row");
			row.addElement("gid")
				.addCDATA(qg.getGid());
			row.addElement("group_order")
				.addCDATA(qg.getGid());

			row = group.addElement("row");
			row.addElement("id")
			   .addCDATA(qg.getGid());
			row.addElement("gid")
				.addCDATA(qg.getGid());
			row.addElement("group_name")
				.addCDATA(qg.getName());
			if (qg.getDescription() != null) {
				row.addElement("description").addCDATA(qg.getDescription());
			}
			row.addElement("language")
				.addCDATA("en"); // TODO
			row.addElement("group_order")
				.addCDATA(qg.getGid());
		}
	}

	private void addQuestions(Element questions, Element question, Element question_attributes)
	{
		for (Question q : survey.getQuestions()) {
			Element row = questions.addElement("row");
			row.addElement("qid")
				.addCDATA(q.getQid());
			row.addElement("parent_qid")
				.addCDATA("0");
			row.addElement("gid")
				.addCDATA(q.getGid());
			row.addElement("type")
				.addCDATA(q.getType());
			row.addElement("title")
				.addCDATA("G" + q.getGid() + "Q" + q.getQid());
			row.addElement("mandatory")
				.addCDATA(q.getMandatory());
			row.addElement("question_order")
				.addCDATA("1");
			row.addElement("relevance")
				.addCDATA("1");

			row = question.addElement("row");
			row.addElement("id")
				.addCDATA(q.getQid());
			row.addElement("qid")
				.addCDATA(q.getQid());
			row.addElement("question")
				.addCDATA(q.getQuestion());
			if (q.getHelp() != null) {
				row.addElement("help")
					.addCDATA(q.getHelp());
			}
			row.addElement("language")
				.addCDATA(q.getLanguage());
		}

	}

	private void addSurveyData(Element surveys, Element surveyls)
	{
		Element row = surveys.addElement("row");
		row.addElement("gsid")
			.addCDATA("1");
		row.addElement("language")
			.addCDATA("en"); //TODO

		row = surveyls.addElement("row");
		row.addElement("surveyls_language")
			.addCDATA("en"); //TODO
		row.addElement("surveyls_title")
			.addCDATA(survey.getName());
	}
}
