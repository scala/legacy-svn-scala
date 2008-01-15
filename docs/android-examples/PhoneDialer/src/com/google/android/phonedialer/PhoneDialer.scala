package com.google.android.phonedialer

import _root_.android.app.ListActivity
import _root_.android.content.Intent
import _root_.android.database.Cursor
import _root_.android.os.Bundle
import _root_.android.provider.Contacts.{People, PeopleColumns, Phones}
import _root_.android.widget.{ListAdapter, ListView, SimpleCursorAdapter}
import _root_.android.view.View

/**
 *  Based on YouTube demo by Dan Morrill
 *  (http://www.youtube.com/watch?v=I6ObTqIiYfE)
 */
class PhoneDialer extends ListActivity {
  private var mAdapter1: ListAdapter = _

  /** Called when the activity is first created. */
  override def onCreate(icicle: Bundle) {
    super.onCreate(icicle)
    val c = getContentResolver.query(People.CONTENT_URI, null, null, null, null)
    startManagingCursor(c)
    
    val columns = Array(PeopleColumns.NAME)
    val names = Array(R.id.row_entry)
    
    mAdapter1 = new SimpleCursorAdapter(this, R.layout.main, c, columns, names)
    setListAdapter(mAdapter1)
  }
  
  override def onListItemClick(l: ListView, v: View, position: Int, id: Long) {
    super.onListItemClick(l, v, position, id)
    val i = new Intent(Intent.CALL_ACTION)
    val c = mAdapter1.getItem(position).asInstanceOf[Cursor]
    val phoneId = c.getLong(c.getColumnIndex(People.PREFERRED_PHONE_ID))
    i.setData(Phones.CONTENT_URI.addId(phoneId))
    
    startActivity(i)
  }
}
