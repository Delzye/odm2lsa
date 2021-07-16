package parser.odm;

import java.util.ArrayList;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class Survey
{
	protected String name;
	protected String description;
	protected String id;
	
	protected ArrayList<QuestionGroup> groups;
	protected ArrayList<Question> questions;
	protected ArrayList<AnswerOption> answer_list;

	public Survey()
	{
		groups = new ArrayList<QuestionGroup>();
		questions = new ArrayList<Question>();
		answer_list = new ArrayList<>();
	}

	public void addGroup(QuestionGroup qg)
	{
		groups.add(qg);
	}
	public void addQuestion(Question q)
	{
		questions.add(q);
	}
	public void addAnswer(AnswerOption ao)
	{
		answer_list.add(ao);
	}
}

