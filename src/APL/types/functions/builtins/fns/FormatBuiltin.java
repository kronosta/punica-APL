package APL.types.functions.builtins.fns;

import APL.*;
import APL.errors.NYIError;
import APL.types.*;
import APL.types.functions.Builtin;

public class FormatBuiltin extends Builtin {
  public FormatBuiltin() {
    super("⍕", 0x011);
  }
  
  public Obj call(Value w) {
    if (w instanceof Num) return Main.toAPL(w.toString(), w.token);
    throw new NYIError("can't format non-numbers", this, w);
  }
  
//  public Obj call(Value a, Value w) {
//
//  }
}