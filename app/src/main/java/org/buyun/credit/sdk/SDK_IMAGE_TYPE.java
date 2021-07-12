
package org.buyun.credit.sdk;

public enum SDK_IMAGE_TYPE {
  SDK_IMAGE_TYPE_RGB24,
  SDK_IMAGE_TYPE_RGBA32,
  SDK_IMAGE_TYPE_BGRA32,
  SDK_IMAGE_TYPE_NV12,
  SDK_IMAGE_TYPE_NV21,
  SDK_IMAGE_TYPE_YUV420P,
  SDK_IMAGE_TYPE_YVU420P,
  SDK_IMAGE_TYPE_YUV422P,
  SDK_IMAGE_TYPE_YUV444P,
  SDK_IMAGE_TYPE_Y,
  SDK_IMAGE_TYPE_BGR24;

  public final int creditValue() {
    return creditValue;
  }

  public static SDK_IMAGE_TYPE creditToEnum(int creditValue) {
    SDK_IMAGE_TYPE[] creditValues = SDK_IMAGE_TYPE.class.getEnumConstants();
    if (creditValue < creditValues.length && creditValue >= 0 && creditValues[creditValue].creditValue == creditValue)
      return creditValues[creditValue];
    for (SDK_IMAGE_TYPE creditEnum : creditValues)
      if (creditEnum.creditValue == creditValue)
        return creditEnum;
    throw new IllegalArgumentException("No enum " + SDK_IMAGE_TYPE.class + " with value " + creditValue);
  }

  @SuppressWarnings("unused")
  private SDK_IMAGE_TYPE() {
    this.creditValue = creditNext.next++;
  }

  @SuppressWarnings("unused")
  private SDK_IMAGE_TYPE(int creditValue) {
    this.creditValue = creditValue;
    creditNext.next = creditValue+1;
  }

  @SuppressWarnings("unused")
  private SDK_IMAGE_TYPE(SDK_IMAGE_TYPE creditEnum) {
    this.creditValue = creditEnum.creditValue;
    creditNext.next = this.creditValue+1;
  }

  private final int creditValue;

  private static class creditNext {
    private static int next = 0;
  }
}

