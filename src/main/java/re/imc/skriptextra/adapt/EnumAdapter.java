package re.imc.skriptextra.adapt;

import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.ParseContext;

public class EnumAdapter {

    public static ExpressionType EVENT;
    public static ParseContext PARSE;

    static {
        try {
            EVENT = ExpressionType.EVENT;
        } catch (Throwable t) {
            EVENT = ExpressionType.SIMPLE;
        }

        try {
            PARSE = ParseContext.PARSE;
        } catch (Throwable t) {
            PARSE = ParseContext.COMMAND;
        }
    }
}
