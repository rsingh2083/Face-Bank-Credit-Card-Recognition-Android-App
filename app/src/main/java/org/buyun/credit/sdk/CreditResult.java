package org.buyun.credit.sdk;

public class CreditResult {
  private long creditCPtr;
  protected boolean creditCMemOwn;

  protected CreditResult(long cPtr, boolean cMemoryOwn) {
    creditCMemOwn = cMemoryOwn;
    creditCPtr = cPtr;
  }

  protected static long getCPtr(CreditResult obj) {
    return (obj == null) ? 0 : obj.creditCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (creditCPtr != 0) {
      if (creditCMemOwn) {
        creditCMemOwn = false;
        creditjni.result_delete(creditCPtr);
      }
      creditCPtr = 0;
    }
  }

  public CreditResult() {
    this(creditjni.result_create1(), true);
  }

  public CreditResult(int code, String phrase, String json, long numZones) {
    this(creditjni.result_create2(code, phrase, json, numZones), true);
  }

  public CreditResult(int code, String phrase, String json) {
    this(creditjni.result_create3(code, phrase, json), true);
  }

  public CreditResult(CreditResult other) {
    this(creditjni.result_create4(CreditResult.getCPtr(other), other), true);
  }

  public int code() {
    return creditjni.result_code(creditCPtr, this);
  }

  public String phrase() {
    return creditjni.result_phrase(creditCPtr, this);
  }

  public String json() {
    return creditjni.result_json(creditCPtr, this);
  }

  public long numCards() {
    return creditjni.result_numcards(creditCPtr, this);
  }

  public boolean isOK() {
    return creditjni.result_isok(creditCPtr, this);
  }

}
