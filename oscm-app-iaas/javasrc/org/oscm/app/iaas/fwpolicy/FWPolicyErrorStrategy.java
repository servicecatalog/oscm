/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.app.iaas.fwpolicy;

import java.util.StringTokenizer;

import org.antlr.v4.runtime.DefaultErrorStrategy;
import org.antlr.v4.runtime.InputMismatchException;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Token;

import org.oscm.app.iaas.i18n.Messages;

public class FWPolicyErrorStrategy extends DefaultErrorStrategy {
    /**
     * Instead of recovering from exception e, rethrow it wrapped in a generic
     * RuntimeException so it is not caught by the rule function catches.
     * Exception e is the "cause" of the RuntimeException.
     */

    @Override
    public void recover(Parser recognizer, RecognitionException e) {
        String message = Messages.get(Messages.DEFAULT_LOCALE,
                "error_invalid_firewallconfig", new Object[] {
                        e.getOffendingToken().getText(),
                        e.getInputStream().toString() });

        throw new RuntimeException(message);
    }

    /**
     * Make sure we don't attempt to recover inline; if the parser successfully
     * recovers, it won't throw an exception.
     */
    @Override
    public Token recoverInline(Parser recognizer) throws RecognitionException {
        InputMismatchException e = new InputMismatchException(recognizer);

        String policies = recognizer.getInputStream().getText();
        StringTokenizer tk = new StringTokenizer(policies, ";");
        String policy = "";
        int idx = 0;
        while (tk.hasMoreElements()) {
            policy = (String) tk.nextElement();
            idx += policy.length();
            if (idx >= e.getOffendingToken().getStartIndex()) {
                break;
            }
        }

        String message = Messages.get(Messages.DEFAULT_LOCALE,
                "error_invalid_firewallconfig", new Object[] {
                        e.getOffendingToken().getText(), policy });
        throw new RuntimeException(message);
    }

    /** Make sure we don't attempt to recover from problems in subrules. */
    @Override
    public void sync(Parser recognizer) {
    }
}
