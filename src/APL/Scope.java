package APL;

import APL.errors.DomainError;
import APL.types.*;
import APL.types.functions.Builtin;

import java.util.HashMap;

public class Scope {
  private HashMap<String, Obj> vars = new HashMap<>();
  private Scope parent = null;
  public boolean alphaDefined;
  public Scope() {
    vars.put("⎕IO", new Num("1"));
    vars.put("⎕COND", Main.toAPL("01", new Token(TType.str, "01",0, "'01'")));
  }
  public Scope(Scope p) {
    parent = p;
  }
  private Scope owner(String name) {
    if (vars.containsKey(name)) return this;
    else if (parent == null) return null;
    else return parent.owner(name);
  }

  public Obj update (String name, Obj val) { // sets wherever var already exists
    Scope sc = owner(name);
    if (sc == null) sc = this;
    sc.set(name, val);
    return val;
  }
  public Obj set (String name, Obj val) { // sets in current scope
    if (name.equals("⎕COND")) {
      if (! (val instanceof Arr)) throw new DomainError("setting ⎕COND to " + Main.human(val.type));
      String s = ((Arr)val).string(false);
      if (s == null) throw new DomainError("⎕COND must be set to a character vector");
      String m = s.endsWith(" ")? s.substring(0, s.length()-1) : s;
      
      if (!m.equals("01") && !m.equals(">0") && !m.equals("≠0")) {
        throw new DomainError("⎕COND must be one of '01', '>0', '≠0' optionally followed by ' ' if space should be falsy");
      }
    }
    vars.put(name, val);
    return val;
  }
  public Obj get (String name) {
    if (name.startsWith("⎕")) {
      switch (name) {
        case "⎕MILLIS": return new Num(System.currentTimeMillis() - Main.startingMillis);
        case "⎕TIME": return new Timer(this, true);
        case "⎕HTIME": return new Timer(this, false);
        case "⎕A": return Main.alphabet;
        case "⎕L": return Main.lowercaseAlphabet;
        case "⎕LA": return Main.lowercaseAlphabet;
        case "⎕ERASE": return new Eraser();
        case "⎕GC": System.gc(); return Num.ONE;
        case "⎕DEATHLOGGER": return new DeathLogger();
      }
    }
    Obj f = vars.get(name);
    if (f == null) {
      if (parent == null) return null;
      else return parent.get(name);
    } else {
      f.shy = false;
      return f;
    }
  }
  Obj getVar(String name) {
    Obj v = get(name);
    if (v == null) return new PlainVar(name, this);
    return v.varData(name, this); // experimental
  }
  public String toString() {
    return toString("");
  }
  public String toString(String prep) {
    StringBuilder res = new StringBuilder("{\n");
    String cp = prep+"  ";
    for (String n : vars.keySet()) res.append(cp).append(n).append(" ← ").append(get(n)).append("\n");
    if (parent != null) res.append(cp).append("parent: ").append(parent.toString(cp));
    res.append(prep).append("}\n");
    return res.toString();
  }
  static class DeathLogger extends Builtin {
    DeathLogger() {
      super("⎕DEATHLOGGER");
    }
  
    @Override
    public Obj call(Value w) {
      return new DyingObj(w.toString());
    }
    static class DyingObj extends Obj {
      String msg;
      DyingObj(String s) {
        super(Type.set);
        this.msg = s;
      }
  
      @SuppressWarnings("deprecation") // as this thing is only used for debugging, this should be fine
      @Override
      protected void finalize() {
        System.out.println(msg+" died");
      }
      public String toString() {
        return "⎕DEATHLOGGER["+msg+"]";
      }
    }
  }
  static class Timer extends Builtin {
    boolean simple;
    Timer(Scope sc, boolean simple) {
      super("⎕TIME");
      valid = 0x001;
      this.sc = sc;
      this.simple = simple;
    }
    public Obj call(Value w) {
      return call(Num.ONE, w);
    }
    public Obj call(Value a, Value w) {
      int n = ((Num) a).intValue();
      long start = System.nanoTime();
      for (int i = 0; i < n; i++) Main.exec(w.fromAPL(), sc);
      long end = System.nanoTime();
      if (simple) return new Num((end-start)/n);
      else {
        double t = end-start;
        t/= n;
        if (t < 1000) return Main.toAPL(t+" nanos", new Token(TType.expr, "nanos", 0, "the thing that made ⎕htime"));
        t/= 1e6;
        if (t > 500) return Main.toAPL((t/1000d)+" seconds", new Token(TType.expr, "seconds", 0, "the thing that made ⎕htime"));
        return Main.toAPL(t+" millis", new Token(TType.expr, "millis", 0, "the thing that made ⎕htime"));
      }
    }
  }
  static class Eraser extends Builtin {
    Eraser() {
      super("⎕ERASE");
      valid = 0x001;
    }
    
    public Obj call(Value w) {
      w.set(null);
      return w;
    }
  }
  public double rand(double d) { // TODO seeds
    return Math.random() * d;
  }
  public double rand(int n) {
    return Math.floor(Math.random() * n);
  }
}