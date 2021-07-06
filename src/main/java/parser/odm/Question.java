package parser.odm;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class Question
{
	protected String qid;
	protected String gid;
	protected String type;
	protected String question;
	protected String help;
	protected String title;
	protected String mandatory;
	protected String language;
	protected String cond; // Do not show the question if the referenced condition evaluates to true
	// Only for Questions with numerical input
	protected String float_range_min;
	protected String float_range_max;

	public Question(String qid, String gid, String type, String q, String title, String m, String l)
	{
		this.qid = qid;
		this.gid = gid;
		this.type = type;
		this.question = q;
		this.title = title;
		this.mandatory = m;
		this.language = l;
	}

	public Question(Question q)
	{
		this.qid = q.qid;
		this.gid = q.gid;
		this.type = q.type;
		this.question = q.question;
		this.title = q.title;
		this.mandatory = q.mandatory;
		this.language = q.language;
		this.cond = q.cond;
	}
}
