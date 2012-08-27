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
				new SingleLineRule("ac", "cess", dependencyTypeToken),
				new SingleLineRule("can", "-", boldToken),
				new SingleLineRule("cann", "ot-", boldToken),
				new SingleLineRule("cr", "eate", dependencyTypeToken),
				new SingleLineRule("dec", "lare", dependencyTypeToken),
				new SingleLineRule("dep", "end", dependencyTypeToken),
				new SingleLineRule("der", "ive", dependencyTypeToken),
				new SingleLineRule("ex", "tend", dependencyTypeToken),
				new SingleLineRule("ha", "ndle", dependencyTypeToken),
				new SingleLineRule("im", "plement", dependencyTypeToken),				
				new SingleLineRule("mo", "dule", boldToken),
				new SingleLineRule("mu", "st-", boldToken),
				new SingleLineRule("on", "ly", boldToken),
				new SingleLineRule("us", "eannotation", dependencyTypeToken),
				new SingleLineRule("th", "row", dependencyTypeToken),
				
				new SingleLineRule("\"", "\"", italicToken),
				new SingleLineRule("$ja", "va", italicToken),
				new SingleLineRule("$sys", "tem", italicToken),
				new EndOfLineRule("%", commentToken)
		};
		
		setRules(rules);
	}

}
