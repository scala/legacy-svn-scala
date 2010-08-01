/*                     __                                               *\
**     ________ ___   / /  ___     Scala Android                        **
**    / __/ __// _ | / /  / _ |    (c) 2009-2010, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */


package scala.android.app

import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.CompoundButton
import android.widget.CompoundButton.OnCheckedChangeListener

/**
 * @author Stephane Micheloud
 * @version 1.0
 */
class Activity extends android.app.Activity { // android.view.ContextThemeWrapper

  @inline
  def findView[V <: android.view.View](id: Int): V =
    super.findViewById(id).asInstanceOf[V]

  implicit def onClick$Action(action: (View) => Any) =
    new View.OnClickListener() {
      def onClick(v: View) { action(v) }
    }

  implicit def onClick$Action(action: => Any) =
    new View.OnClickListener() {
      def onClick(v: View) { action }
    }

  implicit def onCheckedChanged$Action(action: (CompoundButton, Boolean) => Any) =
    new OnCheckedChangeListener() {
      def onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        action(buttonView, isChecked)
      }
    }

  implicit def onCheckedChanged$Action(action: (Boolean) => Any) =
    new OnCheckedChangeListener() {
      def onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        action(isChecked)
      }
    }

  implicit def onCheckedChanged$Action(action: => Any) =
    new OnCheckedChangeListener() {
      def onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        action
      }
    }

  protected trait RichOnItemSelectedListener extends OnItemSelectedListener {
    protected var nothingAction: AdapterView[_] => Any = (_) => 0
    def orNothing(action: AdapterView[_] => Any) {
      nothingAction = action
    }
    def orNothing(action: => Any) {
      nothingAction = (_) => action
    }
  }

  implicit def onItemSelected$Action(selectedAction: (AdapterView[_], View, Int, Long) => Any) =
    new RichOnItemSelectedListener() {
      def onItemSelected(parent: AdapterView[_], view: View, position: Int, i: Long) {
        selectedAction(parent, view, position, i)
      }
      def onNothingSelected(parent: AdapterView[_]) {
        nothingAction(parent)
      }
    }

  implicit def onItemSelected$Action(selectedAction: AdapterView[_] => Any) =
    new RichOnItemSelectedListener() {
      def onItemSelected(parent: AdapterView[_], view: View, position: Int, i: Long) {
        selectedAction(parent)
      }
      def onNothingSelected(parent: AdapterView[_]) {
        nothingAction(parent)
      }
    }

  implicit def onItemSelected$Action(selectedAction: => Any) =
    new RichOnItemSelectedListener() {
      def onItemSelected(parent: AdapterView[_], view: View, position: Int, i: Long) {
        selectedAction
      }
      def onNothingSelected(parent: AdapterView[_]) {
        nothingAction(parent)
      }
    }

}

object Activity {

  /** @since API level 4 */
  final val ACCESSIBILITY_SERVICE = Activity$.ACCESSIBILITY_SERVICE

  /** @since API level 5 */
  final val ACCOUNT_SERVICE = Activity$.ACCOUNT_SERVICE

  /** @since API level 1 */
  final val ACTIVITY_SERVICE = Activity$.ACTIVITY_SERVICE

  /** @since API level 1 */
  final val ALARM_SERVICE = Activity$.ALARM_SERVICE

  /** @since API level 1 */
  final val AUDIO_SERVICE = Activity$.AUDIO_SERVICE

  /** @since API level 1 */
  final val BIND_AUTO_CREATE = Activity$.BIND_AUTO_CREATE

  /** @since API level 1 */
  final val BIND_DEBUG_UNBIND = Activity$.BIND_DEBUG_UNBIND

  /** @since API level 8 */
  final val BIND_NOT_FOREGROUND = Activity$.BIND_NOT_FOREGROUND

  /** @since API level 1 */
  final val CLIPBOARD_SERVICE = Activity$.CLIPBOARD_SERVICE

  /** @since API level 1 */
  final val CONNECTIVITY_SERVICE = Activity$.CONNECTIVITY_SERVICE

  /** @since API level 1 */
  final val CONTEXT_IGNORE_SECURITY = Activity$.CONTEXT_IGNORE_SECURITY

  /** @since API level 1 */
  final val CONTEXT_INCLUDE_CODE = Activity$.CONTEXT_INCLUDE_CODE

  /** @since API level 1 */
  final val DEFAULT_KEYS_SHORTCUT = Activity$.DEFAULT_KEYS_SHORTCUT

  /** @since API level 1 */
  protected final val FOCUSED_STATE_SET = Activity$.FOCUSED_STATE_SET

  /** @since API level 1 */
  final val MODE_APPEND = Activity$.MODE_APPEND

  /** @since API level 1 */
  final val MODE_PRIVATE = Activity$.MODE_PRIVATE

  /** @since API level 1 */
  final val RESULT_CANCELED = Activity$.RESULT_CANCELED

  /** @since API level 1 */
  final val RESULT_FIRST_USER = Activity$.RESULT_FIRST_USER

  /** @since API level 1 */
  final val RESULT_OK = Activity$.RESULT_OK

}
