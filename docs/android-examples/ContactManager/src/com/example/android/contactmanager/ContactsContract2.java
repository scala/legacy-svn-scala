/*                     __                                               *\
**     ________ ___   / /  ___     Scala Android                        **
**    / __/ __// _ | / /  / _ |    (c) 2009-2010, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

// $Id: $


package com.example.android.contactmanager;

import android.provider.ContactsContract;

/*
 * This helper class is a workaround for accessing Java static constants from
 * Scala code when whose constants are defined in Java static inner interfaces.
 *
 * @author Stephane Micheloud
 * @version 1.0
 */
public final class ContactsContract2 {
    
    public static final class CommonDataKinds {
        public static final class Phone {
            public static final String TYPE = ContactsContract.CommonDataKinds.Phone.TYPE;        
        }
        public static final class Email {
            public static final String DATA = ContactsContract.CommonDataKinds.Email.DATA;        
            public static final String TYPE = ContactsContract.CommonDataKinds.Email.TYPE;        
        }
    }

    public static final class Contacts {
        // constants defined in static interface android.provider.ContactsContract.ContactsColumns
        public static final String _ID = ContactsContract.Contacts._ID;    
        public static final String DISPLAY_NAME = ContactsContract.Contacts.DISPLAY_NAME;               
        public static final String IN_VISIBLE_GROUP = ContactsContract.Contacts.IN_VISIBLE_GROUP;               
    }

    public static final class Data {
        // constants defined in static interface android.provider.ContactsContract.ContactsColumns
        public static final String DISPLAY_NAME = ContactsContract.Data.DISPLAY_NAME;

        // constants defined in static interface android.provider.ContactsContract.DataColumns
        public static final String MIMETYPE = ContactsContract.Data.MIMETYPE;
        public static final String RAW_CONTACT_ID = ContactsContract.Data.RAW_CONTACT_ID;
    }

    public static final class RawContacts {
        // constants defined in static interface android.provider.ContactsContract.SyncColumns
        public static final String ACCOUNT_NAME = ContactsContract.RawContacts.ACCOUNT_NAME;
        public static final String ACCOUNT_TYPE = ContactsContract.RawContacts.ACCOUNT_TYPE;
    }
}
