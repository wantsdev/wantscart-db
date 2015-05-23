package com.tucaohui.dbwolf.sqljep.function;

import com.tucaohui.dbwolf.sqljep.ASTFunNode;
import com.tucaohui.dbwolf.sqljep.JepRuntime;
import com.tucaohui.dbwolf.sqljep.ParseException;

/**
 * 
 * @author struct
 * 
 */
public class Hash extends PostfixCommand {

    final public int getNumberOfParameters() {
        return 1;
    }

    public Comparable<?>[] evaluate(ASTFunNode node, JepRuntime runtime) throws ParseException {
        node.childrenAccept(runtime.ev, null);
        Comparable<?> param = runtime.stack.pop();
        return new Comparable<?>[] { param };
    }

    public static Comparable<?> hash(Comparable<?> param) throws ParseException {
        if (param == null) {
            return null;
        }

        return param.hashCode();
    }

    public Comparable<?> getResult(Comparable<?>... comparables) throws ParseException {
        return hash(comparables[0]);
    }
}
