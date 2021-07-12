package org.buyun.credit.sdk;

public class CreditSDK {
  private long creditCPtr;
  protected boolean creditCMemOwn;

  protected CreditSDK(long cPtr, boolean cMemoryOwn) {
    creditCMemOwn = cMemoryOwn;
    creditCPtr = cPtr;
  }

  protected static long getCPtr(CreditSDK obj) {
    return (obj == null) ? 0 : obj.creditCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (creditCPtr != 0) {
      if (creditCMemOwn) {
        creditCMemOwn = false;
        creditjni.delete(creditCPtr);
      }
      creditCPtr = 0;
    }
  }

  public static CreditResult init(Object assetManager, String jsonConfig) {
    return new CreditResult(creditjni.create1(assetManager, jsonConfig), true);
  }

  public static CreditResult init(Object assetManager) {
    return new CreditResult(creditjni.create2(assetManager), true);
  }

  public static CreditResult deInit() {
    return new CreditResult(creditjni.release(), true);
  }

  public static CreditResult process(SDK_IMAGE_TYPE imageType, java.nio.ByteBuffer imageData, long imageWidthInSamples, long imageHeightInSamples, long imageStrideInSamples, int imageExifOrientation) {
    return new CreditResult(creditjni.process1(imageType.creditValue(), imageData, imageWidthInSamples, imageHeightInSamples, imageStrideInSamples, imageExifOrientation), true);
  }

  public static CreditResult process(SDK_IMAGE_TYPE imageType, java.nio.ByteBuffer imageData, long imageWidthInSamples, long imageHeightInSamples, long imageStrideInSamples) {
    return new CreditResult(creditjni.process2(imageType.creditValue(), imageData, imageWidthInSamples, imageHeightInSamples, imageStrideInSamples), true);
  }

  public static CreditResult process(SDK_IMAGE_TYPE imageType, java.nio.ByteBuffer imageData, long imageWidthInSamples, long imageHeightInSamples) {
    return new CreditResult(creditjni.process3(imageType.creditValue(), imageData, imageWidthInSamples, imageHeightInSamples), true);
  }

  public static CreditResult process(SDK_IMAGE_TYPE imageType, java.nio.ByteBuffer yPtr, java.nio.ByteBuffer uPtr, java.nio.ByteBuffer vPtr, long widthInSamples, long heightInSamples, long yStrideInBytes, long uStrideInBytes, long vStrideInBytes, long uvPixelStrideInBytes, int exifOrientation) {
    return new CreditResult(creditjni.process4(imageType.creditValue(), yPtr, uPtr, vPtr, widthInSamples, heightInSamples, yStrideInBytes, uStrideInBytes, vStrideInBytes, uvPixelStrideInBytes, exifOrientation), true);
  }

  public static CreditResult process(SDK_IMAGE_TYPE imageType, java.nio.ByteBuffer yPtr, java.nio.ByteBuffer uPtr, java.nio.ByteBuffer vPtr, long widthInSamples, long heightInSamples, long yStrideInBytes, long uStrideInBytes, long vStrideInBytes, long uvPixelStrideInBytes) {
    return new CreditResult(creditjni.process5(imageType.creditValue(), yPtr, uPtr, vPtr, widthInSamples, heightInSamples, yStrideInBytes, uStrideInBytes, vStrideInBytes, uvPixelStrideInBytes), true);
  }

  public static CreditResult process(SDK_IMAGE_TYPE imageType, java.nio.ByteBuffer yPtr, java.nio.ByteBuffer uPtr, java.nio.ByteBuffer vPtr, long widthInSamples, long heightInSamples, long yStrideInBytes, long uStrideInBytes, long vStrideInBytes) {
    return new CreditResult(creditjni.process6(imageType.creditValue(), yPtr, uPtr, vPtr, widthInSamples, heightInSamples, yStrideInBytes, uStrideInBytes, vStrideInBytes), true);
  }

  public static CreditResult test(SDK_IMAGE_TYPE imageType) {
    return new CreditResult(creditjni.test(imageType.creditValue()), true);
  }

}
