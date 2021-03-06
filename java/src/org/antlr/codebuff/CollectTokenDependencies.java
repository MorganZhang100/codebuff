package org.antlr.codebuff;

import org.antlr.codebuff.misc.BuffUtils;
import org.antlr.codebuff.misc.HashBag;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.Vocabulary;
import org.antlr.v4.runtime.misc.Pair;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Identify token dependencies like { }, [], etc... for use when
 *  computing features.  For example, if there is no newline after '{'
 *  in a method, probably shouldn't have a newline before the '}'. We need
 *  to find matching symbols in order to synchronize the newline generation.
 *
 * RAW NOTES:

Sample counts of rule nodes and unique token lists:

 80 block: ['{', '}']
 197 blockStatement:
 3 classBody: ['{', '}']
 52 classBodyDeclaration:
 5 typeArguments: ['<', ',', '>']
 9 typeArguments: ['<', '>']
 14 qualifiedName: [Identifier, '.', Identifier, '.', Identifier]
 6 qualifiedName: [Identifier]
 1 typeDeclaration:
 8 typeSpec: ['[', ']']
 173 typeSpec:
 40 parExpression: ['(', ')']
 1 classDeclaration: ['class', Identifier, 'extends']
 2 classDeclaration: ['class', Identifier]
 1 enumDeclaration: ['enum', Identifier, '{', '}']
 1 expression: ['!']
 9 expression: ['!=']
 2 expression: ['&&']
 109 expression: ['(', ')']
 1 forControl: [';', ';']
 3 forControl:
 ...

 Repeated tokens should not be counted as most likely they are separators or terminators
 but not always. E.g., forControl: [';', ';']

 Build r->(a,b) tuples for every a,b in tokens list for r and unique a,b.
 E.g., typeSpec: ['[', ']']

 gives

 typeSpec -> ('[',']')

 For any a=b, assume it's not a dependency so build set r->{a} for each rule r.
 E.g., qualifiedName: [Identifier, '.', Identifier, '.', Identifier]

 gives

 qualifiedName -> (Id,.), (Id,Id), (.,Id)
 qualifiedName -> {Id,.}

 meaning we disregard all tuples for qualifiedName.

 With too few samples, we might get confused. E.g.,

 5 typeArguments: ['<', ',', '>']
 9 typeArguments: ['<', '>']

 gives

 typeArguments -> (<,','), (<,>), (',',>)

 Hmm..the count is 9+5 for (<,>), but just 5 for (<,',') and (',',>).
 If we pick just one tuple for each rule, we get (<,>) as winner. cool.
 By that argument, these would yield (class,Id) which is probably ok.

 1 classDeclaration: ['class', Identifier, 'extends']
 2 classDeclaration: ['class', Identifier]

 If there are multiple unique tokens every time like:

 enumDeclaration: ['enum', Identifier, '{', '}']

 we can't decide which token to pair with which. For now we can choose
 arbitrarily but later maybe choose first to last token.

 Oh! Actually, we should only consider tokens that are literals. Tokens
 like Id won't be that useful. That would give

 enumDeclaration: ['enum', '{', '}']

 which is easier.

 Can't really choose most frequent all the time. E.g., statement yields:

 statement: 11:'if','else' 11:'throw',';' 4:'for','(' 4:'for',')' 35:'return',';' 4:'(',')'

 and then would pick ('return',';') as the dependency. Ah. We need pairs
 not by rule but rule and which alternative. Otherwise rules with lots of
 alts will not be able to pick stuff out. Well, that info isn't available
 except for interpreted parsing. dang. I would have to subclass the
 ATN simulator to create different context objects. Doable but ignore for now.

 For matching symbols like {}, [], let's use that info to get a unique match
 when this algorithm doesn't give one.
 */
public class CollectTokenDependencies implements ParseTreeListener {
	/** a bit of "overfitting" or tailoring to the most common pairs in all
	 *  computer languages to improve accuracy when choosing pairs.
	 *  I.e., it never makes sense to choose ('@',')') when ('(',')') is
	 *  available.  Don't assume these pairs exist, just give them
	 *  preference IF they exist in the dependency pairs.
	 */
	public static final char[] CommonPairs = new char[255];
	static {
		CommonPairs['}'] = '{';
		CommonPairs[')'] = '(';
		CommonPairs[']'] = '[';
		CommonPairs['>'] = '<';
	}

	/** Map a rule name to a bag of (a,b) tuples that counts occurrences */
	protected Map<String,HashBag<Pair<Integer,Integer>>> ruleToPairsBag = new HashMap<>();

	/** Track repeated token refs per rule */
	protected Map<String,Set<Integer>> ruleToRepeatedTokensSet = new HashMap<>();

	/** We need parser vocabulary so we can filter for literals like '{' vs ID */
	protected Vocabulary vocab;

	protected String[] ruleNames;

	public CollectTokenDependencies(Vocabulary vocab, String[] ruleNames) {
		this.vocab = vocab;
		this.ruleNames = ruleNames;
	}

	@Override
	public void enterEveryRule(ParserRuleContext ctx) {
		String ruleName = ruleNames[ctx.getRuleIndex()];
		List<TerminalNode> tnodes = getDirectTerminalChildren(ctx);
		// Find all ordered unique pairs of literals;
		// no (a,a) pairs and only literals like '{', 'begin', '}', ...
		// Add a for (a,a) into ruleToRepeatedTokensSet for later filtering
		for (int i=0; i<tnodes.size(); i++) {
			for (int j = i+1; j<tnodes.size(); j++) {
				TerminalNode a = tnodes.get(i);
				TerminalNode b = tnodes.get(j);
				int atype = a.getSymbol().getType();
				int btype = b.getSymbol().getType();
				// only include literals like '{' and ':' not IDENTIFIER etc...
				if ( vocab.getLiteralName(atype)==null || vocab.getLiteralName(btype)==null ) {
					continue;
				}

				if ( atype==btype ) {
					Set<Integer> repeatedTokensSet = ruleToRepeatedTokensSet.get(ruleName);
					if ( repeatedTokensSet==null ) {
						repeatedTokensSet = new HashSet<>();
						ruleToRepeatedTokensSet.put(ruleName, repeatedTokensSet);
					}
					repeatedTokensSet.add(atype);
				}
				else {
					Pair<Integer, Integer> pair = new Pair<>(atype, btype);
					HashBag<Pair<Integer, Integer>> pairsBag = ruleToPairsBag.get(ruleName);
					if ( pairsBag==null ) {
						pairsBag = new HashBag<>();
						ruleToPairsBag.put(ruleName, pairsBag);
					}
					pairsBag.add(pair);
				}
			}
		}
	}

	/** Return the list of token dependences for each rule in a Map.
	 */
	public Map<String, List<Pair<Integer, Integer>>> getDependencies() {
		return stripPairsWithRepeatedTokens();
	}

	public static int getMatchingLeftTokenType(Token curToken,
	                                           List<Integer> viableMatchingLeftTokenTypes,
	                                           Vocabulary vocab)
	{
		int matchingLeftTokenType = viableMatchingLeftTokenTypes.get(0); // by default just pick first
		for (int ttype : viableMatchingLeftTokenTypes) {
			String aliteral = vocab.getLiteralName(ttype);
			String bliteral = vocab.getLiteralName(curToken.getType());
			if ( aliteral!=null && aliteral.length()==3 &&
				 bliteral!=null && bliteral.length()==3 ) {
				char leftChar = aliteral.charAt(1);
				char rightChar = bliteral.charAt(1);
				if (rightChar < 255 && CommonPairs[rightChar] == leftChar) {
					matchingLeftTokenType = ttype;
					break;
				}
			}
		}
		return matchingLeftTokenType;
	}

	/** Return a new map from rulename to List of (a,b) pairs stripped of
	 *  tuples (a,b) where a or b is in rule repeated token set.
	 *  E.g., before removing repeated token ',', we see:
	 *
	 *  elementValueArrayInitializer: 4:'{',',' 1:'{','}' 4:',','}'
	 *
	 *  After removing tuples containing repeated tokens, we get:
	 *
	 *  elementValueArrayInitializer: 1:'{','}'
	*/
	protected Map<String,List<Pair<Integer,Integer>>> stripPairsWithRepeatedTokens() {
		Map<String,List<Pair<Integer,Integer>>> ruleToPairsWoRepeats = new HashMap<>();
		// For each rule
		for (String ruleName : ruleToPairsBag.keySet()) {
			Set<Integer> ruleRepeatedTokens = ruleToRepeatedTokensSet.get(ruleName);
			HashBag<Pair<Integer, Integer>> pairsBag = ruleToPairsBag.get(ruleName);
			// If there are repeated tokens for this rule
			if ( ruleRepeatedTokens!=null ) {
				// Remove all (a,b) for b in repeated token set
				List<Pair<Integer, Integer>> pairsWoRepeats =
					BuffUtils.filter(pairsBag.keySet(),
                                     p -> !ruleRepeatedTokens.contains(p.a) && !ruleRepeatedTokens.contains(p.b));
				ruleToPairsWoRepeats.put(ruleName, pairsWoRepeats);
			}
			else {
				ruleToPairsWoRepeats.put(ruleName, new ArrayList<>(pairsBag.keySet()));
			}
		}
		return ruleToPairsWoRepeats;
	}

	/** Return a list of the set of terminal nodes that are direct children of ctx. */
	public static List<TerminalNode> getDirectTerminalChildren(ParserRuleContext ctx) {
		if ( ctx.children==null ) {
			return Collections.emptyList();
		}

		List<TerminalNode> tokenNodes = new ArrayList<>();
		for (ParseTree o : ctx.children) {
			if ( o instanceof TerminalNode ) {
				TerminalNode tnode = (TerminalNode)o;
				tokenNodes.add(tnode);
			}
		}

		return tokenNodes;
	}

	// satisfy interface only

	@Override
	public void exitEveryRule(ParserRuleContext ctx) { }

	@Override
	public void visitTerminal(TerminalNode node) { }

	@Override
	public void visitErrorNode(ErrorNode node) { }
}
