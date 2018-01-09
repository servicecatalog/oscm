/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

// Generated from C:\home\workspace\oscm-app-iaas\resources\firewall_policy_grammar\FWPolicyLexer.g4 by ANTLR 4.1
package org.oscm.app.iaas.fwpolicy;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.ATNSimulator;
import org.antlr.v4.runtime.atn.LexerATNSimulator;
import org.antlr.v4.runtime.atn.PredictionContextCache;
import org.antlr.v4.runtime.dfa.DFA;

@SuppressWarnings({ "all", "warnings", "unchecked", "unused", "cast" })
public class FWPolicyLexer extends Lexer {
    protected static final DFA[] _decisionToDFA;
    protected static final PredictionContextCache _sharedContextCache = new PredictionContextCache();
    public static final int
            ARROW=1, COMMA=2, COLON=3, QUOTE=4, SEMICOLON=5, HASHMARK= 6, MINUS = 7, SLASH = 8, LPAR = 9,
            RPAR = 10, ZONE = 11, PROTOCOL = 12, PORT = 13, SERVICE = 14,
            IP = 15, OCTET = 16, NUMBER = 17, WS = 18;
    public static String[] modeNames = { "DEFAULT_MODE" };

    public static final String[] tokenNames = { "<INVALID>", "'>'", "','",
            "':'", "'\"'", "';'", "'#'", "'-'", "'/'", "'('", "')'", "ZONE",
            "PROTOCOL", "PORT", "SERVICE", "IP", "OCTET", "NUMBER", "WS" };
    public static final String[] ruleNames = { "ARROW", "COMMA", "COLON",
            "QUOTE", "SEMICOLON", "HASHMARK", "MINUS", "SLASH", "LPAR", "RPAR",
            "ZONE", "PROTOCOL", "PORT", "SERVICE", "IP", "OCTET", "NUMBER",
            "ALPHANUM", "Digit", "WS" };

    public FWPolicyLexer(CharStream input) {
        super(input);
        _interp = new LexerATNSimulator(this, _ATN, _decisionToDFA,
                _sharedContextCache);
    }

    @Override
    public String getGrammarFileName() {
        return "FWPolicyLexer.g4";
    }

    @Override
    public String[] getTokenNames() {
        return tokenNames;
    }

    @Override
    public String[] getRuleNames() {
        return ruleNames;
    }

    @Override
    public String[] getModeNames() {
        return modeNames;
    }

    @Override
    public ATN getATN() {
        return _ATN;
    }

    @Override
    public void action(RuleContext _localctx, int ruleIndex, int actionIndex) {
        switch (ruleIndex) {
        case 6:
            MINUS_action((RuleContext) _localctx, actionIndex);
            break;

        case 7:
            SLASH_action((RuleContext) _localctx, actionIndex);
            break;

        case 8:
            LPAR_action((RuleContext) _localctx, actionIndex);
            break;

        case 9:
            RPAR_action((RuleContext) _localctx, actionIndex);
            break;

        case 19:
            WS_action((RuleContext) _localctx, actionIndex);
            break;
        }
    }

    private void RPAR_action(RuleContext _localctx, int actionIndex) {
        switch (actionIndex) {
        case 3:
            _channel = HIDDEN;
            break;
        }
    }

    private void WS_action(RuleContext _localctx, int actionIndex) {
        switch (actionIndex) {
        case 4:
            _channel = HIDDEN;
            break;
        }
    }

    private void SLASH_action(RuleContext _localctx, int actionIndex) {
        switch (actionIndex) {
        case 1:
            _channel = HIDDEN;
            break;
        }
    }

    private void LPAR_action(RuleContext _localctx, int actionIndex) {
        switch (actionIndex) {
        case 2:
            _channel = HIDDEN;
            break;
        }
    }

    private void MINUS_action(RuleContext _localctx, int actionIndex) {
        switch (actionIndex) {
        case 0:
            _channel = HIDDEN;
            break;
        }
    }

    public static final String _serializedATN = "\3\uacf5\uee8c\u4f5d\u8b0d\u4a45\u78bd\u1b2f\u3378\2\24\u00a9\b\1\4\2"
            + "\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4"
            + "\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22"
            + "\t\22\4\23\t\23\4\24\t\24\4\25\t\25\3\2\3\2\3\3\3\3\3\4\3\4\3\5\3\5\3"
            + "\6\3\6\3\7\3\7\3\b\3\b\3\b\3\b\3\t\3\t\3\t\3\t\3\n\3\n\3\n\3\n\3\13\3"
            + "\13\3\13\3\13\3\f\6\fI\n\f\r\f\16\fJ\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3"
            + "\r\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3\r\5\r^\n\r\3\16\3\16\3\16\3\16\3\16\7"
            + "\16e\n\16\f\16\16\16h\13\16\3\16\3\16\3\16\5\16m\n\16\5\16o\n\16\3\17"
            + "\3\17\6\17s\n\17\r\17\16\17t\3\17\3\17\3\20\3\20\3\20\3\20\3\20\3\20\3"
            + "\20\3\20\3\20\3\20\5\20\u0083\n\20\3\20\3\20\7\20\u0087\n\20\f\20\16\20"
            + "\u008a\13\20\3\20\3\20\5\20\u008e\n\20\3\21\3\21\3\21\3\21\3\21\3\21\3"
            + "\21\3\21\5\21\u0098\n\21\3\22\6\22\u009b\n\22\r\22\16\22\u009c\3\23\3"
            + "\23\3\24\3\24\3\25\6\25\u00a4\n\25\r\25\16\25\u00a5\3\25\3\25\2\26\3\3"
            + "\1\5\4\1\7\5\1\t\6\1\13\7\1\r\b\1\17\t\2\21\n\3\23\13\4\25\f\5\27\r\1"
            + "\31\16\1\33\17\1\35\20\1\37\21\1!\22\1#\23\1%\2\1\'\2\1)\24\6\3\2\5\3"
            + "\2$$\7\2//\62;C\\aac|\5\2\13\f\17\17\"\"\u00b5\2\3\3\2\2\2\2\5\3\2\2\2"
            + "\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3"
            + "\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2\27\3\2\2\2\2\31\3\2\2\2\2\33\3\2\2"
            + "\2\2\35\3\2\2\2\2\37\3\2\2\2\2!\3\2\2\2\2#\3\2\2\2\2)\3\2\2\2\3+\3\2\2"
            + "\2\5-\3\2\2\2\7/\3\2\2\2\t\61\3\2\2\2\13\63\3\2\2\2\r\65\3\2\2\2\17\67"
            + "\3\2\2\2\21;\3\2\2\2\23?\3\2\2\2\25C\3\2\2\2\27H\3\2\2\2\31L\3\2\2\2\33"
            + "_\3\2\2\2\35p\3\2\2\2\37\u008d\3\2\2\2!\u0097\3\2\2\2#\u009a\3\2\2\2%"
            + "\u009e\3\2\2\2\'\u00a0\3\2\2\2)\u00a3\3\2\2\2+,\7@\2\2,\4\3\2\2\2-.\7"
            + ".\2\2.\6\3\2\2\2/\60\7<\2\2\60\b\3\2\2\2\61\62\7$\2\2\62\n\3\2\2\2\63"
            + "\64\7=\2\2\64\f\3\2\2\2\65\66\7%\2\2\66\16\3\2\2\2\678\7/\2\289\3\2\2"
            + "\29:\b\b\2\2:\20\3\2\2\2;<\7\61\2\2<=\3\2\2\2=>\b\t\3\2>\22\3\2\2\2?@"
            + "\7*\2\2@A\3\2\2\2AB\b\n\4\2B\24\3\2\2\2CD\7+\2\2DE\3\2\2\2EF\b\13\5\2"
            + "F\26\3\2\2\2GI\5%\23\2HG\3\2\2\2IJ\3\2\2\2JH\3\2\2\2JK\3\2\2\2K\30\3\2"
            + "\2\2L]\5\r\7\2MN\7v\2\2NO\7e\2\2O^\7r\2\2PQ\7w\2\2QR\7f\2\2R^\7r\2\2S"
            + "T\7v\2\2TU\7e\2\2UV\7r\2\2VW\7w\2\2WX\7f\2\2X^\7r\2\2YZ\7k\2\2Z[\7e\2"
            + "\2[\\\7o\2\2\\^\7r\2\2]M\3\2\2\2]P\3\2\2\2]S\3\2\2\2]Y\3\2\2\2^\32\3\2"
            + "\2\2_`\5\7\4\2`n\5#\22\2ab\5\5\3\2bc\5#\22\2ce\3\2\2\2da\3\2\2\2eh\3\2"
            + "\2\2fd\3\2\2\2fg\3\2\2\2go\3\2\2\2hf\3\2\2\2ij\5\17\b\2jk\5#\22\2km\3"
            + "\2\2\2li\3\2\2\2lm\3\2\2\2mo\3\2\2\2nf\3\2\2\2nl\3\2\2\2o\34\3\2\2\2p"
            + "r\5\23\n\2qs\5%\23\2rq\3\2\2\2st\3\2\2\2tr\3\2\2\2tu\3\2\2\2uv\3\2\2\2"
            + "vw\5\25\13\2w\36\3\2\2\2xy\5!\21\2yz\7\60\2\2z{\5!\21\2{|\7\60\2\2|}\5"
            + "!\21\2}~\7\60\2\2~\u0082\5!\21\2\177\u0080\5\21\t\2\u0080\u0081\5!\21"
            + "\2\u0081\u0083\3\2\2\2\u0082\177\3\2\2\2\u0082\u0083\3\2\2\2\u0083\u008e"
            + "\3\2\2\2\u0084\u0088\5\t\5\2\u0085\u0087\n\2\2\2\u0086\u0085\3\2\2\2\u0087"
            + "\u008a\3\2\2\2\u0088\u0086\3\2\2\2\u0088\u0089\3\2\2\2\u0089\u008b\3\2"
            + "\2\2\u008a\u0088\3\2\2\2\u008b\u008c\5\t\5\2\u008c\u008e\3\2\2\2\u008d"
            + "x\3\2\2\2\u008d\u0084\3\2\2\2\u008e \3\2\2\2\u008f\u0090\5\'\24\2\u0090"
            + "\u0091\5\'\24\2\u0091\u0092\5\'\24\2\u0092\u0098\3\2\2\2\u0093\u0094\5"
            + "\'\24\2\u0094\u0095\5\'\24\2\u0095\u0098\3\2\2\2\u0096\u0098\5\'\24\2"
            + "\u0097\u008f\3\2\2\2\u0097\u0093\3\2\2\2\u0097\u0096\3\2\2\2\u0098\"\3"
            + "\2\2\2\u0099\u009b\5\'\24\2\u009a\u0099\3\2\2\2\u009b\u009c\3\2\2\2\u009c"
            + "\u009a\3\2\2\2\u009c\u009d\3\2\2\2\u009d$\3\2\2\2\u009e\u009f\t\3\2\2"
            + "\u009f&\3\2\2\2\u00a0\u00a1\4\62;\2\u00a1(\3\2\2\2\u00a2\u00a4\t\4\2\2"
            + "\u00a3\u00a2\3\2\2\2\u00a4\u00a5\3\2\2\2\u00a5\u00a3\3\2\2\2\u00a5\u00a6"
            + "\3\2\2\2\u00a6\u00a7\3\2\2\2\u00a7\u00a8\b\25\6\2\u00a8*\3\2\2\2\17\2"
            + "J]flnt\u0082\u0088\u008d\u0097\u009c\u00a5";
    public static final ATN _ATN = ATNSimulator.deserialize(_serializedATN
            .toCharArray());
    static {
        _decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
        for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
            _decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
        }
    }
}
