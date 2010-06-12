/*                     __                                               *\
**     ________ ___   / /  ___     Scala Android                        **
**    / __/ __// _ | / /  / _ |    (c) 2009-2010, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */


package com.example.android.apis.app;

import android.net.Uri;
import android.provider.ContactsContract;

/*
 * This helper class is a workaround for accessing Java static constants from
 * Scala code when whose constants are defined in Java static inner interfaces.
 *
 * @author Stephane Micheloud
 * @version 1.0
 */
public final class CONTACTS {

    public static final String _ID =
        ContactsContract.Contacts._ID;

    public static final String DISPLAY_NAME =
        ContactsContract.Contacts.DISPLAY_NAME;

    public static final String STARRED =
        ContactsContract.Contacts.STARRED;

    public static final String TIMES_CONTACTED =
        ContactsContract.Contacts.TIMES_CONTACTED;

    public static final String CONTACT_PRESENCE =
        ContactsContract.Contacts.CONTACT_PRESENCE;

    public static final String PHOTO_ID =
        ContactsContract.Contacts.PHOTO_ID;

    public static final String LOOKUP_KEY =
        ContactsContract.Contacts.LOOKUP_KEY;

    public static final String HAS_PHONE_NUMBER =
        ContactsContract.Contacts.HAS_PHONE_NUMBER;

    public static Uri getLookupUri(long contactId, String lookupKey) {
        return ContactsContract.Contacts.getLookupUri(contactId, lookupKey);
    }

}

