/* Copyright 2019 The TensorFlow Authors. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/

package org.tensorflow.lite.nnapi;

import org.tensorflow.lite.Delegate;
import org.tensorflow.lite.TensorFlowLite;

/** {@link Delegate} for NNAPI inference. */
public class NnApiDelegate implements Delegate, AutoCloseable {

  private static final long INVALID_DELEGATE_HANDLE = 0;

  private long delegateHandle;

  /** Delegate options. */
  public static final class Options {
    public Options() {}

    /**
     * undefined, specifies default behavior. so far, the default setting of NNAPI is
     * EXECUTION_PREFERENCE_FAST_SINGLE_ANSWER
     */
    public static final int EXECUTION_PREFERENCE_UNDEFINED = -1;

    /**
     * Prefer executing in a way that minimizes battery drain. This is desirable for compilations
     * that will be executed often.
     */
    public static final int EXECUTION_PREFERENCE_LOW_POWER = 0;

    /**
     * Prefer returning a single answer as fast as possible, even if this causes more power
     * consumption.
     */
    public static final int EXECUTION_PREFERENCE_FAST_SINGLE_ANSWER = 1;

    /**
     * Prefer maximizing the throughput of successive frames, for example when processing successive
     * frames coming from the camera.
     */
    public static final int EXECUTION_PREFERENCE_SUSTAINED_SPEED = 2;

    /**
     * Sets the inference preference for precision/compilation/runtime tradeoffs.
     *
     * @param preference One of EXECUTION_PREFERENCE_LOW_POWER,
     *     EXECUTION_PREFERENCE_FAST_SINGLE_ANSWER, and EXECUTION_PREFERENCE_SUSTAINED_SPEED.
     */
    public Options setExecutionPreference(int preference) {
      this.executionPreference = preference;
      return this;
    }

    public Options setAcceleratorName(String name) {
      this.acceleratorName = name;
      return this;
    }

    public Options setCacheDir(String cacheDir) {
      this.cacheDir = cacheDir;
      return this;
    }

    public Options setModelToken(String modelToken) {
      this.modelToken = modelToken;
      return this;
    }

    /**
     * Sets the maximum number of graph partitions that the delegate will try to delegate. If more
     * partitions could be delegated than the limit, the ones with the larger number of nodes will
     * be chosen. If unset it will use the NNAPI default limit.
     */
    public Options setMaxNumberOfDelegatedPartitions(int limit) {
      this.maxDelegatedPartitions = limit;
      return this;
    }

    private int executionPreference = EXECUTION_PREFERENCE_UNDEFINED;
    private String acceleratorName = null;
    private String cacheDir = null;
    private String modelToken = null;
    private Integer maxDelegatedPartitions = null;
  }

  public NnApiDelegate(Options options) {
    // Ensure the native TensorFlow Lite libraries are available.
    TensorFlowLite.init();
    delegateHandle =
        createDelegate(
            options.executionPreference,
            options.acceleratorName,
            options.cacheDir,
            options.modelToken,
            options.maxDelegatedPartitions != null ? options.maxDelegatedPartitions : -1);
  }

  public NnApiDelegate() {
    this(new Options());
  }

  @Override
  public long getNativeHandle() {
    return delegateHandle;
  }

  /**
   * Frees TFLite resources in C runtime.
   *
   * <p>User is expected to call this method explicitly.
   */
  @Override
  public void close() {
    if (delegateHandle != INVALID_DELEGATE_HANDLE) {
      deleteDelegate(delegateHandle);
      delegateHandle = INVALID_DELEGATE_HANDLE;
    }
  }

  //
  private static native long createDelegate(
      int preference,
      String deviceName,
      String cacheDir,
      String modelToken,
      int maxDelegatedPartitions);

  private static native void deleteDelegate(long delegateHandle);
}
