/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

// Generated from C:\home\workspace\oscm-app-iaas\resources\firewall_policy_grammar\FWPolicyParser.g4 by ANTLR 4.1
package org.oscm.app.iaas.fwpolicy;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.antlr.v4.runtime.NoViableAltException;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.ATNSimulator;
import org.antlr.v4.runtime.atn.ParserATNSimulator;
import org.antlr.v4.runtime.atn.PredictionContextCache;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.tree.TerminalNode;

import org.oscm.app.iaas.data.FWPolicy;
import org.oscm.app.iaas.data.FWPolicy.Action;
import org.oscm.app.iaas.data.FWPolicy.Protocol;

@SuppressWarnings({ "all", "warnings", "unchecked", "unused", "cast" })
public class FWPolicyParser extends Parser {
    protected static final DFA[] _decisionToDFA;
    protected static final PredictionContextCache _sharedContextCache = new PredictionContextCache();
    public static final int COLON = 3, HASHMARK = 6, QUOTE = 4, WS = 18,
            SLASH = 8, LPAR = 9, IP = 15, SERVICE = 14, COMMA = 2, ZONE = 11,
            NUMBER = 17, PORT = 13, ARROW = 1, RPAR = 10, SEMICOLON = 5,
            MINUS = 7, PROTOCOL = 12, OCTET = 16;
    public static final String[] tokenNames = { "<INVALID>", "'>'", "','",
            "':'", "'\"'", "';'", "'#'", "'-'", "'/'", "'('", "')'", "ZONE",
            "PROTOCOL", "PORT", "SERVICE", "IP", "OCTET", "NUMBER", "WS" };
    public static final int RULE_policies = 0, RULE_policy = 1,
            RULE_source_zone = 2, RULE_source_ip = 3, RULE_source_port = 4,
            RULE_dest_zone = 5, RULE_dest_service = 6, RULE_dest_ip = 7,
            RULE_dest_port = 8, RULE_protocol = 9;
    public static final String[] ruleNames = { "policies", "policy",
            "source_zone", "source_ip", "source_port", "dest_zone",
            "dest_service", "dest_ip", "dest_port", "protocol" };

    @Override
    public String getGrammarFileName() {
        return "FWPolicyParser.g4";
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
    public ATN getATN() {
        return _ATN;
    }

    public FWPolicyParser(TokenStream input) {
        super(input);
        _interp = new ParserATNSimulator(this, _ATN, _decisionToDFA,
                _sharedContextCache);
    }

    public static class PoliciesContext extends ParserRuleContext {
        public List<FWPolicy> pList;

        public TerminalNode EOF() {
            return getToken(FWPolicyParser.EOF, 0);
        }

        public List<TerminalNode> SEMICOLON() {
            return getTokens(FWPolicyParser.SEMICOLON);
        }

        public TerminalNode SEMICOLON(int i) {
            return getToken(FWPolicyParser.SEMICOLON, i);
        }

        public List<PolicyContext> policy() {
            return getRuleContexts(PolicyContext.class);
        }

        public PolicyContext policy(int i) {
            return getRuleContext(PolicyContext.class, i);
        }

        public PoliciesContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_policies;
        }
    }

    public final PoliciesContext policies() throws RecognitionException {
        PoliciesContext _localctx = new PoliciesContext(_ctx, getState());
        enterRule(_localctx, 0, RULE_policies);

        ((PoliciesContext) _localctx).pList = new ArrayList<FWPolicy>();

        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(20);
                policy(_localctx.pList);
                setState(25);
                _errHandler.sync(this);
                _la = _input.LA(1);
                while (_la == SEMICOLON) {
                    {
                        {
                            setState(21);
                            match(SEMICOLON);
                            setState(22);
                            policy(_localctx.pList);
                        }
                    }
                    setState(27);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                }
                setState(28);
                match(EOF);
            }

            for (Iterator<FWPolicy> it = _localctx.pList.iterator(); it
                    .hasNext();) {
                FWPolicy p = it.next();
                if (p.getDstPort() != null && p.getDstPort().indexOf(",") > 0) {
                    String portList = p.getDstPort();
                    StringTokenizer tk = new StringTokenizer(portList, ",");
                    p.setDstPort((String) tk.nextElement());
                    while (tk.hasMoreElements()) {
                        String port = (String) tk.nextElement();
                        FWPolicy pol = p.clone();
                        pol.setDstPort(port);
                        _localctx.pList.add(pol);
                        it = _localctx.pList.iterator(); // due to concurrent
                                                         // modification
                                                         // exception
                    }
                }

                if (p.getSrcPort().indexOf(",") > 0) {
                    String portList = p.getSrcPort();
                    StringTokenizer tk = new StringTokenizer(portList, ",");
                    p.setSrcPort((String) tk.nextElement());
                    while (tk.hasMoreElements()) {
                        String port = (String) tk.nextElement();
                        FWPolicy pol = p.clone();
                        pol.setSrcPort(port);
                        _localctx.pList.add(pol);
                        it = _localctx.pList.iterator(); // due to concurrent
                                                         // modification
                                                         // exception
                    }
                }
            }

        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public static class PolicyContext extends ParserRuleContext {
        public List<FWPolicy> pList;
        public FWPolicy p;

        public Dest_portContext dest_port() {
            return getRuleContext(Dest_portContext.class, 0);
        }

        public Source_portContext source_port() {
            return getRuleContext(Source_portContext.class, 0);
        }

        public ProtocolContext protocol() {
            return getRuleContext(ProtocolContext.class, 0);
        }

        public TerminalNode ARROW() {
            return getToken(FWPolicyParser.ARROW, 0);
        }

        public Source_zoneContext source_zone() {
            return getRuleContext(Source_zoneContext.class, 0);
        }

        public Dest_ipContext dest_ip() {
            return getRuleContext(Dest_ipContext.class, 0);
        }

        public Source_ipContext source_ip() {
            return getRuleContext(Source_ipContext.class, 0);
        }

        public Dest_zoneContext dest_zone() {
            return getRuleContext(Dest_zoneContext.class, 0);
        }

        public Dest_serviceContext dest_service() {
            return getRuleContext(Dest_serviceContext.class, 0);
        }

        public PolicyContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public PolicyContext(ParserRuleContext parent, int invokingState,
                List<FWPolicy> pList) {
            super(parent, invokingState);
            this.pList = pList;
        }

        @Override
        public int getRuleIndex() {
            return RULE_policy;
        }
    }

    public final PolicyContext policy(List<FWPolicy> pList)
            throws RecognitionException {
        PolicyContext _localctx = new PolicyContext(_ctx, getState(), pList);
        enterRule(_localctx, 2, RULE_policy);

        ((PolicyContext) _localctx).p = new FWPolicy();
        _localctx.p.setAction(Action.Accept);
        _localctx.p.setSrc(null);
        _localctx.p.setSrcPort("any");
        _localctx.p.setDst(null);
        _localctx.p.setDstPort("any");
        _localctx.p.setProtocol(Protocol.TCP);
        _localctx.p.setDstService("NONE");
        _localctx.p.setDstType("IP");

        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(30);
                source_zone(_localctx.p);
                setState(32);
                _la = _input.LA(1);
                if (_la == IP) {
                    {
                        setState(31);
                        source_ip(_localctx.p);
                    }
                }

                setState(35);
                _la = _input.LA(1);
                if (_la == PORT) {
                    {
                        setState(34);
                        source_port(_localctx.p);
                    }
                }

                setState(37);
                match(ARROW);
                setState(38);
                dest_zone(_localctx.p);
                setState(49);
                switch (_input.LA(1)) {
                case SERVICE: {
                    setState(39);
                    dest_service(_localctx.p);
                }
                    break;
                case EOF:
                case SEMICOLON:
                case PROTOCOL:
                case PORT:
                case IP: {
                    setState(41);
                    _la = _input.LA(1);
                    if (_la == IP) {
                        {
                            setState(40);
                            dest_ip(_localctx.p);
                        }
                    }

                    setState(44);
                    _la = _input.LA(1);
                    if (_la == PORT) {
                        {
                            setState(43);
                            dest_port(_localctx.p);
                        }
                    }

                    setState(47);
                    _la = _input.LA(1);
                    if (_la == PROTOCOL) {
                        {
                            setState(46);
                            protocol(_localctx.p);
                        }
                    }

                }
                    break;
                default:
                    throw new NoViableAltException(this);
                }
            }

            _localctx.pList.add(_localctx.p);

        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public static class Source_zoneContext extends ParserRuleContext {
        public FWPolicy p;
        public Token ZONE;

        public TerminalNode ZONE() {
            return getToken(FWPolicyParser.ZONE, 0);
        }

        public Source_zoneContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public Source_zoneContext(ParserRuleContext parent, int invokingState,
                FWPolicy p) {
            super(parent, invokingState);
            this.p = p;
        }

        @Override
        public int getRuleIndex() {
            return RULE_source_zone;
        }
    }

    public final Source_zoneContext source_zone(FWPolicy p)
            throws RecognitionException {
        Source_zoneContext _localctx = new Source_zoneContext(_ctx, getState(),
                p);
        enterRule(_localctx, 4, RULE_source_zone);
        try {
            enterOuterAlt(_localctx, 1);
            {
                {
                    setState(51);
                    ((Source_zoneContext) _localctx).ZONE = match(ZONE);
                }

                _localctx.p.setSrcZone(((Source_zoneContext) _localctx).ZONE
                        .getText());

            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public static class Source_ipContext extends ParserRuleContext {
        public FWPolicy p;
        public Token IP;

        public TerminalNode IP() {
            return getToken(FWPolicyParser.IP, 0);
        }

        public Source_ipContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public Source_ipContext(ParserRuleContext parent, int invokingState,
                FWPolicy p) {
            super(parent, invokingState);
            this.p = p;
        }

        @Override
        public int getRuleIndex() {
            return RULE_source_ip;
        }
    }

    public final Source_ipContext source_ip(FWPolicy p)
            throws RecognitionException {
        Source_ipContext _localctx = new Source_ipContext(_ctx, getState(), p);
        enterRule(_localctx, 6, RULE_source_ip);
        try {
            enterOuterAlt(_localctx, 1);
            {
                {
                    setState(54);
                    ((Source_ipContext) _localctx).IP = match(IP);
                }

                // remove the double quotes
                String ip = ((Source_ipContext) _localctx).IP.getText();
                if (ip.indexOf("\"") == 0) {
                    ip = ip.substring(1, ip.length() - 1);
                }
                _localctx.p.setSrc(ip);

            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public static class Source_portContext extends ParserRuleContext {
        public FWPolicy p;
        public Token PORT;

        public TerminalNode PORT() {
            return getToken(FWPolicyParser.PORT, 0);
        }

        public Source_portContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public Source_portContext(ParserRuleContext parent, int invokingState,
                FWPolicy p) {
            super(parent, invokingState);
            this.p = p;
        }

        @Override
        public int getRuleIndex() {
            return RULE_source_port;
        }
    }

    public final Source_portContext source_port(FWPolicy p)
            throws RecognitionException {
        Source_portContext _localctx = new Source_portContext(_ctx, getState(),
                p);
        enterRule(_localctx, 8, RULE_source_port);
        try {
            enterOuterAlt(_localctx, 1);
            {
                {
                    setState(57);
                    ((Source_portContext) _localctx).PORT = match(PORT);
                }

                // remove the leading colon
                String port = ((Source_portContext) _localctx).PORT.getText()
                        .substring(1);
                _localctx.p.setSrcPort(port);

            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public static class Dest_zoneContext extends ParserRuleContext {
        public FWPolicy p;
        public Token ZONE;

        public TerminalNode ZONE() {
            return getToken(FWPolicyParser.ZONE, 0);
        }

        public Dest_zoneContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public Dest_zoneContext(ParserRuleContext parent, int invokingState,
                FWPolicy p) {
            super(parent, invokingState);
            this.p = p;
        }

        @Override
        public int getRuleIndex() {
            return RULE_dest_zone;
        }
    }

    public final Dest_zoneContext dest_zone(FWPolicy p)
            throws RecognitionException {
        Dest_zoneContext _localctx = new Dest_zoneContext(_ctx, getState(), p);
        enterRule(_localctx, 10, RULE_dest_zone);
        try {
            enterOuterAlt(_localctx, 1);
            {
                {
                    setState(60);
                    ((Dest_zoneContext) _localctx).ZONE = match(ZONE);
                }

                _localctx.p.setDstZone(((Dest_zoneContext) _localctx).ZONE
                        .getText());

            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public static class Dest_serviceContext extends ParserRuleContext {
        public FWPolicy p;
        public Token SERVICE;

        public TerminalNode SERVICE() {
            return getToken(FWPolicyParser.SERVICE, 0);
        }

        public Dest_serviceContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public Dest_serviceContext(ParserRuleContext parent, int invokingState,
                FWPolicy p) {
            super(parent, invokingState);
            this.p = p;
        }

        @Override
        public int getRuleIndex() {
            return RULE_dest_service;
        }
    }

    public final Dest_serviceContext dest_service(FWPolicy p)
            throws RecognitionException {
        Dest_serviceContext _localctx = new Dest_serviceContext(_ctx,
                getState(), p);
        enterRule(_localctx, 12, RULE_dest_service);
        try {
            enterOuterAlt(_localctx, 1);
            {
                {
                    setState(63);
                    ((Dest_serviceContext) _localctx).SERVICE = match(SERVICE);
                }

                // remove the left and right parenthesis
                String service = ((Dest_serviceContext) _localctx).SERVICE
                        .getText();
                service = service.substring(1, service.length() - 1);
                _localctx.p.setDstService(service);
                _localctx.p.setDstType(null);
                _localctx.p.setDstPort(null);
                _localctx.p.setProtocol(null);

            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public static class Dest_ipContext extends ParserRuleContext {
        public FWPolicy p;
        public Token IP;

        public TerminalNode IP() {
            return getToken(FWPolicyParser.IP, 0);
        }

        public Dest_ipContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public Dest_ipContext(ParserRuleContext parent, int invokingState,
                FWPolicy p) {
            super(parent, invokingState);
            this.p = p;
        }

        @Override
        public int getRuleIndex() {
            return RULE_dest_ip;
        }
    }

    public final Dest_ipContext dest_ip(FWPolicy p) throws RecognitionException {
        Dest_ipContext _localctx = new Dest_ipContext(_ctx, getState(), p);
        enterRule(_localctx, 14, RULE_dest_ip);
        try {
            enterOuterAlt(_localctx, 1);
            {
                {
                    setState(66);
                    ((Dest_ipContext) _localctx).IP = match(IP);
                }

                // remove the double quotes
                String ip = ((Dest_ipContext) _localctx).IP.getText();
                if (ip.indexOf("\"") == 0) {
                    ip = ip.substring(1, ip.length() - 1);
                }
                _localctx.p.setDst(ip);

            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public static class Dest_portContext extends ParserRuleContext {
        public FWPolicy p;
        public Token PORT;

        public TerminalNode PORT() {
            return getToken(FWPolicyParser.PORT, 0);
        }

        public Dest_portContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public Dest_portContext(ParserRuleContext parent, int invokingState,
                FWPolicy p) {
            super(parent, invokingState);
            this.p = p;
        }

        @Override
        public int getRuleIndex() {
            return RULE_dest_port;
        }
    }

    public final Dest_portContext dest_port(FWPolicy p)
            throws RecognitionException {
        Dest_portContext _localctx = new Dest_portContext(_ctx, getState(), p);
        enterRule(_localctx, 16, RULE_dest_port);
        try {
            enterOuterAlt(_localctx, 1);
            {
                {
                    setState(69);
                    ((Dest_portContext) _localctx).PORT = match(PORT);
                }

                // remove the leading colon
                String port = ((Dest_portContext) _localctx).PORT.getText()
                        .substring(1);
                _localctx.p.setDstPort(port);

            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public static class ProtocolContext extends ParserRuleContext {
        public FWPolicy p;
        public Token PROTOCOL;

        public TerminalNode PROTOCOL() {
            return getToken(FWPolicyParser.PROTOCOL, 0);
        }

        public ProtocolContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public ProtocolContext(ParserRuleContext parent, int invokingState,
                FWPolicy p) {
            super(parent, invokingState);
            this.p = p;
        }

        @Override
        public int getRuleIndex() {
            return RULE_protocol;
        }
    }

    public final ProtocolContext protocol(FWPolicy p)
            throws RecognitionException {
        ProtocolContext _localctx = new ProtocolContext(_ctx, getState(), p);
        enterRule(_localctx, 18, RULE_protocol);
        try {
            enterOuterAlt(_localctx, 1);
            {
                {
                    setState(72);
                    ((ProtocolContext) _localctx).PROTOCOL = match(PROTOCOL);
                }

                // remove the leading hashmark
                String ptcl = ((ProtocolContext) _localctx).PROTOCOL.getText()
                        .substring(1);
                if ("tcp".equals(ptcl))
                    _localctx.p.setProtocol(Protocol.TCP);
                else if ("udp".equals(ptcl))
                    _localctx.p.setProtocol(Protocol.UDP);
                else if ("tcpudp".equals(ptcl))
                    _localctx.p.setProtocol(Protocol.TCP_UDP);
                else if ("icmp".equals(ptcl))
                    _localctx.p.setProtocol(Protocol.ICMP);

            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public static final String _serializedATN = "\3\uacf5\uee8c\u4f5d\u8b0d\u4a45\u78bd\u1b2f\u3378\3\24N\4\2\t\2\4\3\t"
            + "\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t\13\3"
            + "\2\3\2\3\2\7\2\32\n\2\f\2\16\2\35\13\2\3\2\3\2\3\3\3\3\5\3#\n\3\3\3\5"
            + "\3&\n\3\3\3\3\3\3\3\3\3\5\3,\n\3\3\3\5\3/\n\3\3\3\5\3\62\n\3\5\3\64\n"
            + "\3\3\4\3\4\3\4\3\5\3\5\3\5\3\6\3\6\3\6\3\7\3\7\3\7\3\b\3\b\3\b\3\t\3\t"
            + "\3\t\3\n\3\n\3\n\3\13\3\13\3\13\3\13\2\f\2\4\6\b\n\f\16\20\22\24\2\2J"
            + "\2\26\3\2\2\2\4 \3\2\2\2\6\65\3\2\2\2\b8\3\2\2\2\n;\3\2\2\2\f>\3\2\2\2"
            + "\16A\3\2\2\2\20D\3\2\2\2\22G\3\2\2\2\24J\3\2\2\2\26\33\5\4\3\2\27\30\7"
            + "\7\2\2\30\32\5\4\3\2\31\27\3\2\2\2\32\35\3\2\2\2\33\31\3\2\2\2\33\34\3"
            + "\2\2\2\34\36\3\2\2\2\35\33\3\2\2\2\36\37\7\2\2\3\37\3\3\2\2\2 \"\5\6\4"
            + "\2!#\5\b\5\2\"!\3\2\2\2\"#\3\2\2\2#%\3\2\2\2$&\5\n\6\2%$\3\2\2\2%&\3\2"
            + "\2\2&\'\3\2\2\2\'(\7\3\2\2(\63\5\f\7\2)\64\5\16\b\2*,\5\20\t\2+*\3\2\2"
            + "\2+,\3\2\2\2,.\3\2\2\2-/\5\22\n\2.-\3\2\2\2./\3\2\2\2/\61\3\2\2\2\60\62"
            + "\5\24\13\2\61\60\3\2\2\2\61\62\3\2\2\2\62\64\3\2\2\2\63)\3\2\2\2\63+\3"
            + "\2\2\2\64\5\3\2\2\2\65\66\7\r\2\2\66\67\b\4\1\2\67\7\3\2\2\289\7\21\2"
            + "\29:\b\5\1\2:\t\3\2\2\2;<\7\17\2\2<=\b\6\1\2=\13\3\2\2\2>?\7\r\2\2?@\b"
            + "\7\1\2@\r\3\2\2\2AB\7\20\2\2BC\b\b\1\2C\17\3\2\2\2DE\7\21\2\2EF\b\t\1"
            + "\2F\21\3\2\2\2GH\7\17\2\2HI\b\n\1\2I\23\3\2\2\2JK\7\16\2\2KL\b\13\1\2"
            + "L\25\3\2\2\2\t\33\"%+.\61\63";
    public static final ATN _ATN = ATNSimulator.deserialize(_serializedATN
            .toCharArray());
    static {
        _decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
        for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
            _decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
        }
    }
}
