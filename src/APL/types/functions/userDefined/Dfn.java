package APL.types.functions.userDefined;

import APL.*;
import APL.types.*;

public class Dfn extends Fun {
  private final Token token;
  private final Scope psc;
  Dfn(Token t, Scope sc) {
    token = t;
    psc = sc;
    valid = 0x011;
  }
  public Obj call(Value w) {
    Main.printdbg("dfn call", w);
    sc = new Scope(psc);
    sc.set("⍺", new Variable(sc, "⍺"));
    sc.set("⍵", w);
    return Main.execLines(token, sc);
  }
  public Obj call(Value a, Value w) {
    Main.printdbg("dfn call", a, w);
    sc = new Scope(psc);
    sc.set("⍺", a);
    sc.set("⍵", w);
    sc.alphaDefined = true;
    return Main.execLines(token, sc);
  }
  public String toString() {
    return token.toRepr();
  }
  
  @Override
  public Type type() {
    return Type.fn;
  }
  
  public String name() { return "dfn"; }
}