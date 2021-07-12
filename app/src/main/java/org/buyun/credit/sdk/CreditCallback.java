package org.buyun.credit.sdk;

public class CreditCallback {
  private long creditCPtr;
  protected boolean creditCMemOwn;

  protected CreditCallback(long cPtr, boolean cMemoryOwn) {
    creditCMemOwn = cMemoryOwn;
    creditCPtr = cPtr;
  }

  protected static long getCPtr(CreditCallback obj) {
    return (obj == null) ? 0 : obj.creditCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (creditCPtr != 0) {
      if (creditCMemOwn) {
        creditCMemOwn = false;
        creditjni.callback_delete(creditCPtr);
      }
      creditCPtr = 0;
    }
  }

  protected void creditDirectorDisconnect() {
    creditCMemOwn = false;
    delete();
  }

  public void creditReleaseOwnership() {
    creditCMemOwn = false;
    creditjni.callback_ownership(this, creditCPtr, false);
  }

  public void creditTakeOwnership() {
    creditCMemOwn = true;
    creditjni.callback_ownership(this, creditCPtr, true);
  }

  protected CreditCallback() {
    this(creditjni.callback_create(), true);
    creditjni.callback_connect(this, creditCPtr, creditCMemOwn, true);
  }

  public void onNewResult(CreditResult result) {
    creditjni.callback_result(creditCPtr, this, CreditResult.getCPtr(result), result);
  }

}
