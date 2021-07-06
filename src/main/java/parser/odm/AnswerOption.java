package parser.odm;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class AnswerOption
{
	protected String aid;
	protected String qid;
	protected String language;
	protected String code;
	protected String answer;
	protected String sortorder;

	public AnswerOption(AnswerOption ao)
	{
		this.aid = ao.aid;
		this.qid = ao.qid;
		this.language = ao.language;
		this.code = ao.code;
		this.answer = ao.answer;
		this.sortorder = ao.sortorder;
	}
}
