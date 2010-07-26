/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.photostream

import android.os.{Handler, Message, Process}

import java.util.concurrent.{BlockingQueue, Callable, CancellationException,
         ExecutionException, FutureTask, LinkedBlockingQueue, ThreadFactory, 
         ThreadPoolExecutor, TimeoutException, TimeUnit}
import java.util.concurrent.atomic.AtomicInteger

/**
 * <p>UserTask enables proper and easy use of the UI thread. This class allows
 * to perform background operations and publish results on the UI thread without
 * having to manipulate threads and/or handlers.</p>
 *
 * <p>A user task is defined by a computation that runs on a background thread
 * and whose result is published on the UI thread. A user task is defined by
 * 3 generic types, called <code>Params</code>, <code>Progress</code> and
 * <code>Result</code>, and 4 steps, called <code>begin</code>,
 * <code>doInBackground</code>, <code>processProgress<code> and
 * <code>end</code>.</p>
 *
 * <h2>Usage</h2>
 * <p>UserTask must be subclassed to be used. The subclass will override at
 * least one method ({@link #doInBackground(Object[])}), and most often will
 * override a second one ({@link #onPostExecute(Object)}.)</p>
 *
 * <p>Here is an example of subclassing:</p>
 * <pre>
 * private class DownloadFilesTask extends UserTask&lt;URL, Int, Long&gt; {
 *   def File doInBackground(urls: URL*): Long = {
 *     val count = urls.length;
 *     var totalSize = 0;
 *     for (i <- 0 until count) {
 *       totalSize += Downloader.downloadFile(urls(i))
 *       publishProgress(((i / count.toFloat) * 100).toInt)
 *     }
       totalSize
 *   }
 *
 *   def onProgressUpdate(progress: Int*) {
 *     setProgressPercent(progress(0))
 *   }
 *
 *   def onPostExecute(result: Long) {
 *     showDialog("Downloaded " + result + " bytes")
 *   }
 * }
 * </pre>
 *
 * <p>Once created, a task is executed very simply:</p>
 * <pre>
 * new DownloadFilesTask().execute(new URL[] { ... });
 * </pre>
 *
 * <h2>User task's generic types</h2>
 * <p>The three types used by a user task are the following:</p>
 * <ol>
 *     <li><code>Params</code>, the type of the parameters sent to the task upon
 *     execution.</li>
 *     <li><code>Progress</code>, the type of the progress units published during
 *     the background computation.</li>
 *     <li><code>Result</code>, the type of the result of the background
 *     computation.</li>
 * </ol>
 * <p>Not all types are always used by a user task. To mark a type as unused,
 * simply use the type {@link Void}:</p>
 * <pre>
 * private class MyTask extends UserTask<Void, Void, Void) { ... }
 * </pre>
 *
 * <h2>The 4 steps</h2>
 * <p>When a user task is executed, the task goes through 4 steps:</p>
 * <ol>
 *     <li>{@link #onPreExecute()}, invoked on the UI thread immediately after the task
 *     is executed. This step is normally used to setup the task, for instance by
 *     showing a progress bar in the user interface.</li>
 *     <li>{@link #doInBackground(Object[])}, invoked on the background thread
 *     immediately after {@link # onPreExecute ()} finishes executing. This step is used
 *     to perform background computation that can take a long time. The parameters
 *     of the user task are passed to this step. The result of the computation must
 *     be returned by this step and will be passed back to the last step. This step
 *     can also use {@link #publishProgress(Object[])} to publish one or more units
 *     of progress. These values are published on the UI thread, in the
 *     {@link #onProgressUpdate(Object[])} step.</li>
 *     <li>{@link # onProgressUpdate (Object[])}, invoked on the UI thread after a
 *     call to {@link #publishProgress(Object[])}. The timing of the execution is
 *     undefined. This method is used to display any form of progress in the user
 *     interface while the background computation is still executing. For instance,
 *     it can be used to animate a progress bar or show logs in a text field.</li>
 *     <li>{@link # onPostExecute (Object)}, invoked on the UI thread after the background
 *     computation finishes. The result of the background computation is passed to
 *     this step as a parameter.</li>
 * </ol>
 *
 * <h2>Threading rules</h2>
 * <p>There are a few threading rules that must be followed for this class to
 * work properly:</p>
 * <ul>
 *     <li>The task instance must be created on the UI thread.</li>
 *     <li>{@link #execute(Object[])} must be invoked on the UI thread.</li>
 *     <li>Do not call {@link # onPreExecute ()}, {@link # onPostExecute (Object)},
 *     {@link #doInBackground(Object[])}, {@link # onProgressUpdate (Object[])}
 *     manually.</li>
 *     <li>The task can be executed only once (an exception will be thrown if
 *     a second execution is attempted.)</li>
 * </ul>
 */
// Note: Context bound "Params: ClassManifest" (see section 7.4 in SLS)
abstract class UserTask[Params: ClassManifest, Progress, Result] {
  import UserTask._  // companion object

  private val NoResult = null.asInstanceOf[Result]

  private final val mWorker = new WorkerRunnable[Params, Result]() {
    @throws(classOf[Exception])
    def call(): Result = {
      Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND)
      doInBackground(mParams: _*)
    }
  }

  private final val mFuture = new FutureTask[Result](mWorker) {
    override protected def done() {
      var message: Message = null
      var result: Result = NoResult

      try {
        result = get()
        message = sHandler.obtainMessage(
          MESSAGE_POST_RESULT,
          new UserTaskResult[Result](UserTask.this, result))
        message.sendToTarget()
      } catch {
        case e: InterruptedException =>
          android.util.Log.w(LOG_TAG, e)
        case e: ExecutionException =>
          throw new RuntimeException(
            "An error occured while executing doInBackground()",
            e.getCause)
        case e: CancellationException =>
          message = sHandler.obtainMessage(
            MESSAGE_POST_CANCEL,
            new UserTaskResult[Result](UserTask.this, NoResult))
          message.sendToTarget()
        case t: Throwable =>
          throw new RuntimeException(
            "An error occured while executing doInBackground()", t)
      }
    }
  }

  @volatile
  private var mStatus = Status.PENDING

  /**
   * Returns the current status of this task.
   *
   * @return The current status.
   */
  final def getStatus: Status = mStatus

  /**
   * Override this method to perform a computation on a background thread. The
   * specified parameters are the parameters passed to {@link #execute(Object[])}
   * by the caller of this task.
   *
   * This method can call {@link #publishProgress(Object[])} to publish updates
   * on the UI thread.
   *
   * @param params The parameters of the task.
   *
   * @return A result, defined by the subclass of this task.
   *
   * @see #onPreExecute()
   * @see #onPostExecute(Object)
   * @see #publishProgress(Object[])
   */
  def doInBackground(params: Params*): Result

  /**
   * Runs on the UI thread before {@link #doInBackground(Object[])}.
   *
   * @see #onPostExecute(Object)
   * @see #doInBackground(Object[])
   */
  def onPreExecute() {
  }

  /**
   * Runs on the UI thread after {@link #doInBackground(Object[])}. The
   * specified result is the value returned by {@link #doInBackground(Object[])}
   * or null if the task was cancelled or an exception occured.
   *
   * @param result The result of the operation computed by {@link #doInBackground(Object[])}.
   *
   * @see #onPreExecute()
   * @see #doInBackground(Object[])
   */
  @SuppressWarnings(Array("UnusedDeclaration"))
  def onPostExecute(result: Result) {
  }

  /**
   * Runs on the UI thread after {@link #publishProgress(Object[])} is invoked.
   * The specified values are the values passed to {@link #publishProgress(Object[])}.
   *
   * @param values The values indicating progress.
   *
   * @see #publishProgress(Object[])
   * @see #doInBackground(Object[])
   */
  @SuppressWarnings(Array("UnusedDeclaration"))
  def onProgressUpdate(values: Progress*) {
  }

  /**
   * Runs on the UI thread after {@link #cancel(boolean)} is invoked.
   *
   * @see #cancel(boolean)
   * @see #isCancelled()
   */
  def onCancelled() {
  }

  /**
   * Returns <tt>true</tt> if this task was cancelled before it completed
   * normally.
   *
   * @return <tt>true</tt> if task was cancelled before it completed
   *
   * @see #cancel(boolean)
   */
  final def isCancelled: Boolean = mFuture.isCancelled

  /**
   * Attempts to cancel execution of this task.  This attempt will
   * fail if the task has already completed, already been cancelled,
   * or could not be cancelled for some other reason. If successful,
   * and this task has not started when <tt>cancel</tt> is called,
   * this task should never run.  If the task has already started,
   * then the <tt>mayInterruptIfRunning</tt> parameter determines
   * whether the thread executing this task should be interrupted in
   * an attempt to stop the task.
   *
   * @param mayInterruptIfRunning <tt>true</tt> if the thread executing this
   *        task should be interrupted; otherwise, in-progress tasks are allowed
   *        to complete.
   *
   * @return <tt>false</tt> if the task could not be cancelled,
   *         typically because it has already completed normally;
   *         <tt>true</tt> otherwise
   *
   * @see #isCancelled()
   * @see #onCancelled()
   */
  final def cancel(mayInterruptIfRunning: Boolean): Boolean =
    mFuture cancel mayInterruptIfRunning

  /**
   * Waits if necessary for the computation to complete, and then
   * retrieves its result.
   *
   * @return The computed result.
   *
   * @throws CancellationException If the computation was cancelled.
   * @throws ExecutionException If the computation threw an exception.
   * @throws InterruptedException If the current thread was interrupted
   *         while waiting.
   */
  @throws(classOf[InterruptedException])
  @throws(classOf[ExecutionException])
  final def get: Result = mFuture.get

  /**
   * Waits if necessary for at most the given time for the computation
   * to complete, and then retrieves its result.
   *
   * @param timeout Time to wait before cancelling the operation.
   * @param unit The time unit for the timeout.
   *
   * @return The computed result.
   *
   * @throws CancellationException If the computation was cancelled.
   * @throws ExecutionException If the computation threw an exception.
   * @throws InterruptedException If the current thread was interrupted
   *         while waiting.
   * @throws TimeoutException If the wait timed out.
   */
  @throws(classOf[InterruptedException])
  @throws(classOf[ExecutionException])
  @throws(classOf[TimeoutException])
  final def get(timeout: Long, unit: TimeUnit): Result =
    mFuture.get(timeout, unit)

  /**
   * Executes the task with the specified parameters. The task returns
   * itself (this) so that the caller can keep a reference to it.
   *
   * This method must be invoked on the UI thread.
   *
   * @param params The parameters of the task.
   *
   * @return This instance of UserTask.
   *
   * @throws IllegalStateException If {@link #getStatus()} returns either
   *         {@link UserTask.Status#RUNNING} or {@link UserTask.Status#FINISHED}.
   */
  final def execute(params: Params*): this.type = {
    if (mStatus != Status.PENDING) {
      mStatus match {
        case Status.RUNNING =>
          throw new IllegalStateException("Cannot execute task:"
                            + " the task is already running.")
        case Status.FINISHED =>
          throw new IllegalStateException("Cannot execute task:"
                            + " the task has already been executed "
                            + "(a task can be executed only once)")
      }
    }

    mStatus = Status.RUNNING

    onPreExecute()

    mWorker.mParams = params.toArray
    sExecutor execute mFuture

    this
  }

  /**
   * This method can be invoked from {@link #doInBackground(Object[])} to
   * publish updates on the UI thread while the background computation is
   * still running. Each call to this method will trigger the execution of
   * {@link #onProgressUpdate(Object[])} on the UI thread.
   *
   * @param values The progress values to update the UI with.
   *
   * @see # onProgressUpdate (Object[])
   * @see #doInBackground(Object[])
   */
  protected final def publishProgress(values: Progress*) {
    sHandler.obtainMessage(MESSAGE_POST_PROGRESS,
                new UserTaskResult1[Progress](this, values: _*)).sendToTarget()
  }

  private def finish(result: Result) {
    onPostExecute(result)
    mStatus = Status.FINISHED
  }

}

object UserTask {

  private final val LOG_TAG = "UserTask"

  private final val CORE_POOL_SIZE = 1
  private final val MAXIMUM_POOL_SIZE = 10
  private final val KEEP_ALIVE = 10

  /**
   * Indicates the current status of the task. Each status will be set only once
   * during the lifetime of a task.
   */
  object Status extends Enumeration {
    /**
     * PENDING indicates that the task has not been executed yet.
     * RUNNING indicates that the task is running.
     * FINISHED indicates that {@link UserTask#onPostExecute(Object)} has finished.
     */
    val PENDING, RUNNING, FINISHED = Value
  }
  type Status = Status.Value

  private final val sWorkQueue: BlockingQueue[Runnable] =
    new LinkedBlockingQueue[Runnable](MAXIMUM_POOL_SIZE)

  private final val sThreadFactory = new ThreadFactory() {
    private final val mCount = new AtomicInteger(1)

    def newThread(r: Runnable): Thread =
      new Thread(r, "UserTask #" + mCount.getAndIncrement)
  }

  private final val sExecutor =
    new ThreadPoolExecutor(CORE_POOL_SIZE,
                           MAXIMUM_POOL_SIZE, KEEP_ALIVE, TimeUnit.SECONDS,
                           sWorkQueue, sThreadFactory)

  private final val MESSAGE_POST_RESULT = 0x1
  private final val MESSAGE_POST_PROGRESS = 0x2
  private final val MESSAGE_POST_CANCEL = 0x3

  private final val sHandler = new InternalHandler()

  private class InternalHandler extends Handler {
    @SuppressWarnings(Array("unchecked", "RawUseOfParameterizedType"))
    override def handleMessage(msg: Message) {
      msg.what match {
        case MESSAGE_POST_RESULT =>
          val result = msg.obj.asInstanceOf[UserTaskResult[Any]]
          // There is only one result
          result.mTask finish result.mData(0)
        case MESSAGE_POST_PROGRESS =>
          val result = msg.obj.asInstanceOf[UserTaskResult1[Any]]
          result.mTask onProgressUpdate result.mData
        case MESSAGE_POST_CANCEL =>
          val result = msg.obj.asInstanceOf[UserTaskResult[Any]]
          result.mTask.onCancelled()
      }
    }
  }

  private abstract class WorkerRunnable[Params, Result] extends Callable[Result] {
    var mParams: Array[Params] = _
  }

  @SuppressWarnings(Array("RawUseOfParameterizedType"))
  private case class UserTaskResult[Data](mTask: UserTask[_, _, Data], mData: Data*)

  @SuppressWarnings(Array("RawUseOfParameterizedType"))
  private case class UserTaskResult1[Data](mTask: UserTask[_, Data, _], mData: Data*)

}
