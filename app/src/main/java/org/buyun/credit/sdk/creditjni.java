package org.buyun.credit.sdk;

public class creditjni {

  static {
    System.loadLibrary("creditsdk");
  }

  public final static native long create1(Object jarg1, String jarg2);
  public final static native long create2(Object jarg1);
  public final static native long release();

  public final static native long process1(int jarg1, java.nio.ByteBuffer jarg2, long jarg3, long jarg4, long jarg5, int jarg6);
  public final static native long process2(int jarg1, java.nio.ByteBuffer jarg2, long jarg3, long jarg4, long jarg5);
  public final static native long process3(int jarg1, java.nio.ByteBuffer jarg2, long jarg3, long jarg4);
  public final static native long process4(int jarg1, java.nio.ByteBuffer jarg2, java.nio.ByteBuffer jarg3, java.nio.ByteBuffer jarg4, long jarg5, long jarg6, long jarg7, long jarg8, long jarg9, long jarg10, int jarg11);
  public final static native long process5(int jarg1, java.nio.ByteBuffer jarg2, java.nio.ByteBuffer jarg3, java.nio.ByteBuffer jarg4, long jarg5, long jarg6, long jarg7, long jarg8, long jarg9, long jarg10);
  public final static native long process6(int jarg1, java.nio.ByteBuffer jarg2, java.nio.ByteBuffer jarg3, java.nio.ByteBuffer jarg4, long jarg5, long jarg6, long jarg7, long jarg8, long jarg9);

  public final static native long result_create1();
  public final static native long result_create2(int jarg1, String jarg2, String jarg3, long jarg4);
  public final static native long result_create3(int jarg1, String jarg2, String jarg3);
  public final static native long result_create4(long jarg1, CreditResult jarg1_);
  public final static native void result_delete(long jarg1);
  public final static native int result_code(long jarg1, CreditResult jarg1_);
  public final static native String result_phrase(long jarg1, CreditResult jarg1_);
  public final static native String result_json(long jarg1, CreditResult jarg1_);
  public final static native long result_numcards(long jarg1, CreditResult jarg1_);
  public final static native boolean result_isok(long jarg1, CreditResult jarg1_);
  public final static native long callback_create();
  public final static native void callback_delete(long jarg1);
  public final static native void callback_result(long jarg1, CreditCallback jarg1_, long jarg2, CreditResult jarg2_);
  public final static native void callback_connect(CreditCallback obj, long cptr, boolean mem_own, boolean weak_global);
  public final static native void callback_ownership(CreditCallback obj, long cptr, boolean take_or_release);

  public final static native long test(int jarg1);
  public final static native void delete(long jarg1);

  public static void callback_onresult(CreditCallback self, long result) {
    self.onNewResult((result == 0) ? null : new CreditResult(result, false));
  }

  private final static native void module_init();
  static {
    module_init();
  }
}
