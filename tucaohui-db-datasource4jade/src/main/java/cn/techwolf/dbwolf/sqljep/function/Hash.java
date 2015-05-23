package cn.techwolf.dbwolf.sqljep.function;

import cn.techwolf.dbwolf.sqljep.ASTFunNode;
import cn.techwolf.dbwolf.sqljep.JepRuntime;
import cn.techwolf.dbwolf.sqljep.ParseException;

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
