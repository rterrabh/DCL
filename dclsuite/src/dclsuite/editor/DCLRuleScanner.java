package dclsuite.editor;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

public class DCLRuleScanner extends RuleBasedScanner{
	
	private static Color REGULAR_COLOR = new Color(Display.getCurrent(), new RGB(0,0,0));
	private static Color DEPENDENCY_TYPE_COLOR = new Color(Display.getCurrent(), new RGB(51,66,119));
	private static Color COMMENT_COLOR = new Color(Display.getCurrent(), new RGB(64,127,96));
	
	public DCLRuleScanner() {
		IToken boldToken = new Token(new TextAttribute(REGULAR_COLOR,null,SWT.BOLD));
		IToken italicToken = new Token(new TextAttribute(REGULAR_COLOR,null,SWT.ITALIC));
		IToken dependencyTypeToken = new Token(new TextAttribute(DEPENDENCY_TYPE_COLOR,null,SWT.BOLD));
		IToken commentToken = new Token(new TextAttribute(COMMENT_COLOR,null,SWT.ITALIC));
		
		IRule[] rules = {
				new SingleLineRule("only", " ", boldToken),
				new SingleLineRule("can-only", "-", boldToken),
				new SingleLineRule("cannot", "-", boldToken),
				new SingleLineRule("can", "-", boldToken),
				new SingleLineRule("must", "-", boldToken),
				new SingleLineRule("module", " ", boldToken),
				new SingleLineRule("access", " ", dependencyTypeToken),
				new SingleLineRule("declare", " ", dependencyTypeToken),
				new SingleLineRule("handle", " ", dependencyTypeToken),
				new SingleLineRule("extend", " ", dependencyTypeToken),
				new SingleLineRule("implement", " ", dependencyTypeToken),
				new SingleLineRule("derive", " ", dependencyTypeToken),
				new SingleLineRule("create", " ", dependencyTypeToken),
				new SingleLineRule("annotate", " ", dependencyTypeToken),
				new SingleLineRule("depend", " ", dependencyTypeToken),
				new SingleLineRule("throw", " ", dependencyTypeToken),
				new SingleLineRule("\"", "\"", italicToken),
				new SingleLineRule("$", "java", italicToken),
				new SingleLineRule("$", "system", italicToken),
				new EndOfLineRule("%", commentToken)
		};
		
		setRules(rules);
	}

}
