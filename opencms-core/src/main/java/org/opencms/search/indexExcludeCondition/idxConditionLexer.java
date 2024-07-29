package org.opencms.search.indexExcludeCondition;

// Generated from idxCondition.g4 by ANTLR 4.7.1
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class idxConditionLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.7.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		AND=1, OR=2, NOT=3, TRUE=4, FALSE=5, GT=6, GE=7, LT=8, LE=9, EQ=10, NEQ=11, 
		LPAREN=12, RPAREN=13, DECIMAL=14, STRING=15, IDENTIFIER=16, WS=17;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] ruleNames = {
		"AND", "OR", "NOT", "TRUE", "FALSE", "GT", "GE", "LT", "LE", "EQ", "NEQ", 
		"LPAREN", "RPAREN", "DECIMAL", "STRING", "IDENTIFIER", "WS"
	};

	private static final String[] _LITERAL_NAMES = {
		null, "'AND'", "'OR'", "'NOT'", "'true'", "'false'", "'>'", "'>='", "'<'", 
		"'<='", "'='", "'!='", "'('", "')'"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, "AND", "OR", "NOT", "TRUE", "FALSE", "GT", "GE", "LT", "LE", "EQ", 
		"NEQ", "LPAREN", "RPAREN", "DECIMAL", "STRING", "IDENTIFIER", "WS"
	};
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}


	public idxConditionLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "idxCondition.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getChannelNames() { return channelNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2\23u\b\1\4\2\t\2\4"+
		"\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t"+
		"\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\3\2\3\2\3\2\3\2\3\3\3\3\3\3\3\4\3\4\3\4\3\4\3\5\3\5\3\5\3\5\3\5\3\6\3"+
		"\6\3\6\3\6\3\6\3\6\3\7\3\7\3\b\3\b\3\b\3\t\3\t\3\n\3\n\3\n\3\13\3\13\3"+
		"\f\3\f\3\f\3\r\3\r\3\16\3\16\3\17\5\17P\n\17\3\17\6\17S\n\17\r\17\16\17"+
		"T\3\17\3\17\6\17Y\n\17\r\17\16\17Z\5\17]\n\17\3\20\3\20\7\20a\n\20\f\20"+
		"\16\20d\13\20\3\20\3\20\3\21\3\21\7\21j\n\21\f\21\16\21m\13\21\3\22\6"+
		"\22p\n\22\r\22\16\22q\3\22\3\22\2\2\23\3\3\5\4\7\5\t\6\13\7\r\b\17\t\21"+
		"\n\23\13\25\f\27\r\31\16\33\17\35\20\37\21!\22#\23\3\2\6\3\2\62;\b\2)"+
		")\60\60\62;C\\aac|\5\2C\\aac|\5\2\13\f\16\17\"\"\2{\2\3\3\2\2\2\2\5\3"+
		"\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2"+
		"\21\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2\27\3\2\2\2\2\31\3\2\2\2\2\33\3"+
		"\2\2\2\2\35\3\2\2\2\2\37\3\2\2\2\2!\3\2\2\2\2#\3\2\2\2\3%\3\2\2\2\5)\3"+
		"\2\2\2\7,\3\2\2\2\t\60\3\2\2\2\13\65\3\2\2\2\r;\3\2\2\2\17=\3\2\2\2\21"+
		"@\3\2\2\2\23B\3\2\2\2\25E\3\2\2\2\27G\3\2\2\2\31J\3\2\2\2\33L\3\2\2\2"+
		"\35O\3\2\2\2\37^\3\2\2\2!g\3\2\2\2#o\3\2\2\2%&\7C\2\2&\'\7P\2\2\'(\7F"+
		"\2\2(\4\3\2\2\2)*\7Q\2\2*+\7T\2\2+\6\3\2\2\2,-\7P\2\2-.\7Q\2\2./\7V\2"+
		"\2/\b\3\2\2\2\60\61\7v\2\2\61\62\7t\2\2\62\63\7w\2\2\63\64\7g\2\2\64\n"+
		"\3\2\2\2\65\66\7h\2\2\66\67\7c\2\2\678\7n\2\289\7u\2\29:\7g\2\2:\f\3\2"+
		"\2\2;<\7@\2\2<\16\3\2\2\2=>\7@\2\2>?\7?\2\2?\20\3\2\2\2@A\7>\2\2A\22\3"+
		"\2\2\2BC\7>\2\2CD\7?\2\2D\24\3\2\2\2EF\7?\2\2F\26\3\2\2\2GH\7#\2\2HI\7"+
		"?\2\2I\30\3\2\2\2JK\7*\2\2K\32\3\2\2\2LM\7+\2\2M\34\3\2\2\2NP\7/\2\2O"+
		"N\3\2\2\2OP\3\2\2\2PR\3\2\2\2QS\t\2\2\2RQ\3\2\2\2ST\3\2\2\2TR\3\2\2\2"+
		"TU\3\2\2\2U\\\3\2\2\2VX\7\60\2\2WY\t\2\2\2XW\3\2\2\2YZ\3\2\2\2ZX\3\2\2"+
		"\2Z[\3\2\2\2[]\3\2\2\2\\V\3\2\2\2\\]\3\2\2\2]\36\3\2\2\2^b\7$\2\2_a\t"+
		"\3\2\2`_\3\2\2\2ad\3\2\2\2b`\3\2\2\2bc\3\2\2\2ce\3\2\2\2db\3\2\2\2ef\7"+
		"$\2\2f \3\2\2\2gk\t\4\2\2hj\t\3\2\2ih\3\2\2\2jm\3\2\2\2ki\3\2\2\2kl\3"+
		"\2\2\2l\"\3\2\2\2mk\3\2\2\2np\t\5\2\2on\3\2\2\2pq\3\2\2\2qo\3\2\2\2qr"+
		"\3\2\2\2rs\3\2\2\2st\b\22\2\2t$\3\2\2\2\n\2OTZ\\bkq\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}