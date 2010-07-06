/*                     __                                               *\
**     ________ ___   / /  ___     Scala Android                        **
**    / __/ __// _ | / /  / _ |    (c) 2009-2010, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */


package scala.android.provider;

/*
 * This helper class is a workaround for accessing Java static constants from
 * Scala code when whose constants are defined in Java static inner interfaces.
 *
 * @author Stephane Micheloud
 * @version 1.0
 */
//public final class android.provider.ContactsContract
public final class ContactsContract {

    /** @since API level 5 */
    public static final String AUTHORITY =
        android.provider.ContactsContract.AUTHORITY;
    /** @since API level 5 */
    public static final android.net.Uri AUTHORITY_URI =
        android.provider.ContactsContract.AUTHORITY_URI;
    /** @since API level 5 */
    public static final String CALLER_IS_SYNCADAPTER =
        android.provider.ContactsContract.CALLER_IS_SYNCADAPTER;

    public static final class CommonDataKinds {

        // public static final class android.provider.ContactsContract.CommonDataKinds.Email
        public static final class Email {

            // constants defined in the static class 
            // android.provider.ContactsContract.CommonDataKinds.Email

            /** @since API level 5 */
            public static final android.net.Uri CONTENT_FILTER_URI =
                android.provider.ContactsContract.CommonDataKinds.Email.CONTENT_FILTER_URI;
            /** @since API level 5 */
            public static final String CONTENT_ITEM_TYPE =
                android.provider.ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE;
            /** @since API level 5 */
            public static final android.net.Uri CONTENT_LOOKUP_URI =
                android.provider.ContactsContract.CommonDataKinds.Email.CONTENT_LOOKUP_URI;
            /** @since API level 5 */
            public static final String CONTENT_TYPE =
                android.provider.ContactsContract.CommonDataKinds.Email.CONTENT_TYPE;
            /** @since API level 5 */
            public static final android.net.Uri CONTENT_URI =
                android.provider.ContactsContract.CommonDataKinds.Email.CONTENT_URI;
            /** @since API level 5 */
            public static final String DISPLAY_NAME =
                android.provider.ContactsContract.CommonDataKinds.Email.DISPLAY_NAME;
            /** @since API level 5 */
            public static final int TYPE_HOME =
                android.provider.ContactsContract.CommonDataKinds.Email.TYPE_HOME;
            /** @since API level 5 */
            public static final int TYPE_MOBILE =
                android.provider.ContactsContract.CommonDataKinds.Email.TYPE_MOBILE;
            /** @since API level 5 */
            public static final int TYPE_OTHER =
                android.provider.ContactsContract.CommonDataKinds.Email.TYPE_OTHER;
            /** @since API level 5 */
            public static final int TYPE_WORK =
                android.provider.ContactsContract.CommonDataKinds.Email.TYPE_WORK;

            // forwarders to methods defined in the static class
            // android.provider.ContactsContract.CommonDataKinds.Email

            public static final CharSequence getTypeLabel(android.content.res.Resources res,
                                                          int type, CharSequence label) {
                return android.provider.ContactsContract.CommonDataKinds.Email.getTypeLabel(res, type, label);
            }
            public static final int getTypeLabelResource(int type) {
                return android.provider.ContactsContract.CommonDataKinds.Email.getTypeLabelResource(type);
            }

            // constants inherited from the public interface
            // android.provider.BaseColumns

            /** @since API level 1 */
            public static final String _COUNT =
                android.provider.BaseColumns._COUNT;
            /** @since API level 1 */
            public static final String _ID =
                android.provider.BaseColumns._ID;

            // constants inherited from the static interface
            // android.provider.ContactsContract.CommonDataKinds.CommonColumns

            /* @since API level 5 */
            public static final String DATA =
                android.provider.ContactsContract.CommonDataKinds.Email.DATA;        
            /* @since API level 5 */
            public static final String LABEL =
                android.provider.ContactsContract.CommonDataKinds.Email.LABEL;        
            /* @since API level 5 */
            public static final String TYPE =
                android.provider.ContactsContract.CommonDataKinds.Email.TYPE;   
     
        } // Email

        // public static final class android.provider.ContactsContract.CommonDataKinds.Organization
        public static final class Organization {

            public static final String COMPANY =
                android.provider.ContactsContract.CommonDataKinds.Organization.COMPANY;
            public static final String CONTENT_ITEM_TYPE =
                android.provider.ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE;
            public static final String DEPARTMENT =
                android.provider.ContactsContract.CommonDataKinds.Organization.DEPARTMENT;
            public static final String JOB_DESCRIPTION =
                android.provider.ContactsContract.CommonDataKinds.Organization.JOB_DESCRIPTION;
            public static final String OFFICE_LOCATION =
                android.provider.ContactsContract.CommonDataKinds.Organization.OFFICE_LOCATION;
            public static final String PHONETIC_NAME =
                android.provider.ContactsContract.CommonDataKinds.Organization.PHONETIC_NAME;
            public static final String SYMBOL =
                android.provider.ContactsContract.CommonDataKinds.Organization.SYMBOL; 
            public static final String TITLE =
                android.provider.ContactsContract.CommonDataKinds.Organization.TITLE;
            public static final int TYPE_OTHER =
                android.provider.ContactsContract.CommonDataKinds.Organization.TYPE_OTHER;
            public static final int TYPE_WORK =
                android.provider.ContactsContract.CommonDataKinds.Organization.TYPE_WORK;

            // forwarders to methods defined in Organization itself
            public static final CharSequence getTypeLabel(android.content.res.Resources res,
                                                          int type, CharSequence label) {
                return android.provider.ContactsContract.CommonDataKinds.Organization.getTypeLabel(res, type, label);
            }
            public static final int getTypeLabelResource(int type) {
                return android.provider.ContactsContract.CommonDataKinds.Organization.getTypeLabelResource(type);
            }

            // constants inherited from the public interface
            // android.provider.BaseColumns

            /** @since API level 1 */
            public static final String _COUNT =
                android.provider.BaseColumns._COUNT;
            /** @since API level 1 */
            public static final String _ID =
                android.provider.BaseColumns._ID;

            // inherited from interface
            // android.provider.ContactsContract.CommonDataKinds.BaseTypes

            public static final int TYPE_CUSTOM =
                android.provider.ContactsContract.CommonDataKinds.Organization.TYPE_CUSTOM;

            // inherited from interface
            // android.provider.ContactsContract.CommonDataKinds.CommonColumns

            public static final String DATA =
                android.provider.ContactsContract.CommonDataKinds.Organization.DATA;
            public static final String LABEL =
                android.provider.ContactsContract.CommonDataKinds.Organization.LABEL;
            public static final String TYPE =
                android.provider.ContactsContract.CommonDataKinds.Organization.TYPE;

            // constants inherited from the protected static interface
            // android.provider.ContactsContract.ContactOptionsColumns

            /** @since API level 5 */
            public static final String CUSTOM_RINGTONE =
                android.provider.ContactsContract.CommonDataKinds.Organization.CUSTOM_RINGTONE;
            /** @since API level 5 */
            public static final String LAST_TIME_CONTACTED =
                android.provider.ContactsContract.CommonDataKinds.Organization.LAST_TIME_CONTACTED;
            /** @since API level 5 */
            public static final String SEND_TO_VOICEMAIL =
                android.provider.ContactsContract.CommonDataKinds.Organization.SEND_TO_VOICEMAIL;
            /** @since API level 5 */
            public static final String STARRED =
                android.provider.ContactsContract.CommonDataKinds.Organization.STARRED;
            /** @since API level 5 */
            public static final String TIMES_CONTACTED =
                android.provider.ContactsContract.CommonDataKinds.Organization.TIMES_CONTACTED;

            // constants inherited from the protected static interface
            // android.provider.ContactsContract.ContactStatusColumns

            /** @since API level 5 */
            public static final String CONTACT_PRESENCE =
                android.provider.ContactsContract.CommonDataKinds.Organization.CONTACT_PRESENCE;
            /** @since API level 5 */
            public static final String CONTACT_STATUS =
                android.provider.ContactsContract.CommonDataKinds.Organization.CONTACT_STATUS;
            /** @since API level 5 */
            public static final String CONTACT_STATUS_ICON =
                android.provider.ContactsContract.CommonDataKinds.Organization.CONTACT_STATUS_ICON;
            /** @since API level 5 */
            public static final String CONTACT_STATUS_LABEL =
                android.provider.ContactsContract.CommonDataKinds.Organization.CONTACT_STATUS_LABEL;
            /** @since API level 5 */
            public static final String CONTACT_STATUS_RES_PACKAGE =
                android.provider.ContactsContract.CommonDataKinds.Organization.CONTACT_STATUS_RES_PACKAGE;
            /** @since API level 5 */
            public static final String CONTACT_STATUS_TIMESTAMP =
                android.provider.ContactsContract.CommonDataKinds.Organization.CONTACT_STATUS_TIMESTAMP;

            // constants inherited from the protected static interface
            // android.provider.ContactsContract.ContactsColumns

            /** @since API level 5 */
            public static final String DISPLAY_NAME =
                android.provider.ContactsContract.CommonDataKinds.Organization.DISPLAY_NAME;
            /** @since API level 5 */
            public static final String HAS_PHONE_NUMBER =
                android.provider.ContactsContract.CommonDataKinds.Organization.HAS_PHONE_NUMBER;
            /** @since API level 5 */
            public static final String IN_VISIBLE_GROUP =
                android.provider.ContactsContract.CommonDataKinds.Organization.IN_VISIBLE_GROUP;
            /** @since API level 5 */
            public static final String LOOKUP_KEY =
                android.provider.ContactsContract.CommonDataKinds.Organization.LOOKUP_KEY;
            /** @since API level 5 */
            public static final String PHOTO_ID =
                android.provider.ContactsContract.CommonDataKinds.Organization.PHOTO_ID;

            // constants inherited from interface
            // android.provider.ContactsContract.RawContactsColumns

            /** @since API level 5 */
            public static final String AGGREGATION_MODE =
                android.provider.ContactsContract.CommonDataKinds.Organization.AGGREGATION_MODE;
            /** @since API level 5 */
            public static final String CONTACT_ID =
                android.provider.ContactsContract.CommonDataKinds.Organization.CONTACT_ID;
            /** @since API level 5 */
            public static final String DELETED =
                android.provider.ContactsContract.CommonDataKinds.Organization.DELETED;

            // constants inherited from the protected static interface
            // android.provider.ContactsContract.StatusColumns

            /* @since API level 8 */
            public static final int AVAILABLE =
                android.provider.ContactsContract.CommonDataKinds.Organization.AVAILABLE;
            /* @since API level 8 */
            public static final int AWAY =
                android.provider.ContactsContract.CommonDataKinds.Organization.AWAY;
            /* @since API level 8 */
            public static final int DO_NOT_DISTURB =
                android.provider.ContactsContract.CommonDataKinds.Organization.DO_NOT_DISTURB;
            /* @since API level 8 */
            public static final int IDLE =
                android.provider.ContactsContract.CommonDataKinds.Organization.IDLE;
            /* @since API level 8 */
            public static final int INVISIBLE =
                android.provider.ContactsContract.CommonDataKinds.Organization.INVISIBLE;
            /* @since API level 8 */
            public static final int OFFLINE =
                android.provider.ContactsContract.CommonDataKinds.Organization.OFFLINE;
            /* @since API level 5 */
            public static final String PRESENCE =
                android.provider.ContactsContract.CommonDataKinds.Organization.PRESENCE;
            /* @since API level 8 */
            public static final String PRESENCE_CUSTOM_STATUS =
                android.provider.ContactsContract.CommonDataKinds.Organization.PRESENCE_CUSTOM_STATUS;
            /* @since API level 8 */
            public static final String PRESENCE_STATUS =
                android.provider.ContactsContract.CommonDataKinds.Organization.PRESENCE_STATUS;
            /* @since API level 5 */
            public static final String STATUS =
                android.provider.ContactsContract.CommonDataKinds.Organization.STATUS;
            /* @since API level 5 */
            public static final String STATUS_ICON =
                android.provider.ContactsContract.CommonDataKinds.Organization.STATUS_ICON;
            /* @since API level 5 */
            public static final String STATUS_LABEL =
                android.provider.ContactsContract.CommonDataKinds.Organization.STATUS_LABEL;
            /* @since API level 5 */
            public static final String STATUS_RES_PACKAGE =
                android.provider.ContactsContract.CommonDataKinds.Organization.STATUS_RES_PACKAGE;
            /* @since API level 5 */
            public static final String STATUS_TIMESTAMP =
                android.provider.ContactsContract.CommonDataKinds.Organization.STATUS_TIMESTAMP;

        } // Organization

        // public static final class android.provider.ContactsContract.CommonDataKinds.Phone
        public static final class Phone {

            public static final android.net.Uri CONTENT_FILTER_URI =
                android.provider.ContactsContract.CommonDataKinds.Phone.CONTENT_FILTER_URI;
            public static final String CONTENT_ITEM_TYPE =
                android.provider.ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE;
            public static final String CONTENT_TYPE =
                android.provider.ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE;
            public static final android.net.Uri CONTENT_URI =
                android.provider.ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
            public static final String NUMBER =
                android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER;
            public static final int TYPE_ASSISTANT =
                android.provider.ContactsContract.CommonDataKinds.Phone.TYPE_ASSISTANT;
            public static final int TYPE_CALLBACK =
                android.provider.ContactsContract.CommonDataKinds.Phone.TYPE_CALLBACK;
            public static final int TYPE_CAR =
                android.provider.ContactsContract.CommonDataKinds.Phone.TYPE_CAR;
            public static final int TYPE_COMPANY_MAIN =
                android.provider.ContactsContract.CommonDataKinds.Phone.TYPE_COMPANY_MAIN;
            public static final int TYPE_FAX_HOME =
                android.provider.ContactsContract.CommonDataKinds.Phone.TYPE_FAX_HOME;
            public static final int TYPE_FAX_WORK =
                android.provider.ContactsContract.CommonDataKinds.Phone.TYPE_FAX_WORK;
            public static final int TYPE_HOME =
                android.provider.ContactsContract.CommonDataKinds.Phone.TYPE_HOME;
            public static final int TYPE_ISDN =
                android.provider.ContactsContract.CommonDataKinds.Phone.TYPE_ISDN;
            public static final int TYPE_MAIN =
                android.provider.ContactsContract.CommonDataKinds.Phone.TYPE_MAIN;
            public static final int TYPE_MMS =
                android.provider.ContactsContract.CommonDataKinds.Phone.TYPE_MMS;
            public static final int TYPE_MOBILE =
                android.provider.ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE;
            public static final int TYPE_OTHER =
                android.provider.ContactsContract.CommonDataKinds.Phone.TYPE_OTHER;
            public static final int TYPE_OTHER_FAX =
                android.provider.ContactsContract.CommonDataKinds.Phone.TYPE_OTHER_FAX;
            public static final int TYPE_PAGER =
                android.provider.ContactsContract.CommonDataKinds.Phone.TYPE_PAGER;
            public static final int TYPE_RADIO =
                android.provider.ContactsContract.CommonDataKinds.Phone.TYPE_RADIO;
            public static final int TYPE_TELEX =
                android.provider.ContactsContract.CommonDataKinds.Phone.TYPE_TELEX;
            public static final int TYPE_TTY_TDD =
                android.provider.ContactsContract.CommonDataKinds.Phone.TYPE_TTY_TDD;
            public static final int TYPE_WORK =
                android.provider.ContactsContract.CommonDataKinds.Phone.TYPE_WORK;
            public static final int TYPE_WORK_MOBILE =
                android.provider.ContactsContract.CommonDataKinds.Phone.TYPE_WORK_MOBILE;
            public static final int TYPE_WORK_PAGER =
                android.provider.ContactsContract.CommonDataKinds.Phone.TYPE_WORK_PAGER;

            // forwarders to methods defined in the static class
            // android.provider.ContactsContract.CommonDataKinds.Phone

            public static final CharSequence getTypeLabel(android.content.res.Resources res,
                                                          int type, CharSequence label) {
                return android.provider.ContactsContract.CommonDataKinds.Phone.getTypeLabel(res, type, label);
            }
            public static final int getTypeLabelResource(int type) {
                return android.provider.ContactsContract.CommonDataKinds.Phone.getTypeLabelResource(type);
            }

            // inherited

            public static final String TYPE =
                android.provider.ContactsContract.CommonDataKinds.Phone.TYPE;

        }  // Phone

        // android.provider.ContactsContract.CommonDataKinds.StructuredName
        public static final class StructuredName {

            public static final String CONTENT_ITEM_TYPE =
                android.provider.ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE;
            public static final String DISPLAY_NAME =
                android.provider.ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME;
            public static final String FAMILY_NAME =
                android.provider.ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME;
            public static final String GIVEN_NAME =
                android.provider.ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME;
            public static final String MIDDLE_NAME =
                android.provider.ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME;
            public static final String PHONETIC_FAMILY_NAME =
                android.provider.ContactsContract.CommonDataKinds.StructuredName.PHONETIC_FAMILY_NAME;
            public static final String PHONETIC_GIVEN_NAME =
                android.provider.ContactsContract.CommonDataKinds.StructuredName.PHONETIC_GIVEN_NAME;
            public static final String PHONETIC_MIDDLE_NAME =
                android.provider.ContactsContract.CommonDataKinds.StructuredName.PHONETIC_MIDDLE_NAME;
            public static final String PREFIX =
                android.provider.ContactsContract.CommonDataKinds.StructuredName.PREFIX;
            public static final String SUFFIX =
                android.provider.ContactsContract.CommonDataKinds.StructuredName.SUFFIX;

            // constants inherited from the public interface
            // android.provider.BaseColumns

            /** @since API level 1 */
            public static final String _COUNT =
                android.provider.BaseColumns._COUNT;
            /** @since API level 1 */
            public static final String _ID =
                android.provider.BaseColumns._ID;

            // constants inherited from the protected static interface
            // android.provider.ContactsContract.ContactOptionsColumns

            /** @since API level 5 */
            public static final String CUSTOM_RINGTONE =
                android.provider.ContactsContract.Contacts.CUSTOM_RINGTONE;
            /** @since API level 5 */
            public static final String LAST_TIME_CONTACTED =
                android.provider.ContactsContract.Contacts.LAST_TIME_CONTACTED;
            /** @since API level 5 */
            public static final String SEND_TO_VOICEMAIL =
                android.provider.ContactsContract.Contacts.SEND_TO_VOICEMAIL;
            /** @since API level 5 */
            public static final String STARRED =
                android.provider.ContactsContract.Contacts.STARRED;
            /** @since API level 5 */
            public static final String TIMES_CONTACTED =
                android.provider.ContactsContract.Contacts.TIMES_CONTACTED;

            // constants inherited from the protected static interface
            // android.provider.ContactsContract.ContactStatusColumns

            /** @since API level 5 */
            public static final String CONTACT_PRESENCE =
                android.provider.ContactsContract.Contacts.CONTACT_PRESENCE;
            /** @since API level 5 */
            public static final String CONTACT_STATUS =
                android.provider.ContactsContract.Contacts.CONTACT_STATUS;
            /** @since API level 5 */
            public static final String CONTACT_STATUS_ICON =
                android.provider.ContactsContract.Contacts.CONTACT_STATUS_ICON;
            /** @since API level 5 */
            public static final String CONTACT_STATUS_LABEL =
                android.provider.ContactsContract.Contacts.CONTACT_STATUS_LABEL;
            /** @since API level 5 */
            public static final String CONTACT_STATUS_RES_PACKAGE =
                android.provider.ContactsContract.Contacts.CONTACT_STATUS_RES_PACKAGE;
            /** @since API level 5 */
            public static final String CONTACT_STATUS_TIMESTAMP =
                android.provider.ContactsContract.Contacts.CONTACT_STATUS_TIMESTAMP;

        } // StructuredName

    } // CommonDataKinds

    // public static class android.provider.ContactsContract.Contacts
    public static class Contacts {

        // constants defined in the public class
        // android.provider.ContactsContract.Contacts

        /* @since API level 5 */
        public static final android.net.Uri CONTENT_FILTER_URI =
            android.provider.ContactsContract.Contacts.CONTENT_FILTER_URI;
        /* @since API level 5 */
        public static final android.net.Uri CONTENT_GROUP_URI =
            android.provider.ContactsContract.Contacts.CONTENT_GROUP_URI;
        /* @since API level 5 */
        public static final android.net.Uri CONTENT_STREQUENT_URI =
            android.provider.ContactsContract.Contacts.CONTENT_STREQUENT_URI;
        /* @since API level 5 */
        public static final String CONTENT_TYPE =
            android.provider.ContactsContract.Contacts.CONTENT_TYPE;
        /* @since API level 5 */
        public static final android.net.Uri CONTENT_URI =
            android.provider.ContactsContract.Contacts.CONTENT_URI;
        /* @since API level 5 */
        public static final String CONTENT_VCARD_TYPE =
            android.provider.ContactsContract.Contacts.CONTENT_VCARD_TYPE;
        /* @since API level 5 */
        public static final android.net.Uri CONTENT_VCARD_URI =
            android.provider.ContactsContract.Contacts.CONTENT_VCARD_URI;

        // forwarders to methods defined in the public class
        // android.provider.ContactsContract.Contacts

        public static android.net.Uri getLookupUri(long contactId, String lookupKey) {
            return android.provider.ContactsContract.Contacts.getLookupUri(contactId, lookupKey);
        }

        // constants inherited from the public interface
        // android.provider.BaseColumns

        /** @since API level 1 */
        public static final String _COUNT =
            android.provider.BaseColumns._COUNT;
        /** @since API level 1 */
        public static final String _ID =
            android.provider.BaseColumns._ID;

        // constants inherited from the protected static interface
        // android.provider.ContactsContract.ContactOptionsColumns

        /** @since API level 5 */
        public static final String CUSTOM_RINGTONE =
            android.provider.ContactsContract.Contacts.CUSTOM_RINGTONE;
        /** @since API level 5 */
        public static final String LAST_TIME_CONTACTED =
            android.provider.ContactsContract.Contacts.LAST_TIME_CONTACTED;
        /** @since API level 5 */
        public static final String SEND_TO_VOICEMAIL =
            android.provider.ContactsContract.Contacts.SEND_TO_VOICEMAIL;
        /** @since API level 5 */
        public static final String STARRED =
            android.provider.ContactsContract.Contacts.STARRED;
        /** @since API level 5 */
        public static final String TIMES_CONTACTED =
            android.provider.ContactsContract.Contacts.TIMES_CONTACTED;

        // constants inherited from the protected static interface
        // android.provider.ContactsContract.ContactStatusColumns

        /** @since API level 5 */
        public static final String CONTACT_PRESENCE =
            android.provider.ContactsContract.Contacts.CONTACT_PRESENCE;
        /** @since API level 5 */
        public static final String CONTACT_STATUS =
            android.provider.ContactsContract.Contacts.CONTACT_STATUS;
        /** @since API level 5 */
        public static final String CONTACT_STATUS_ICON =
            android.provider.ContactsContract.Contacts.CONTACT_STATUS_ICON;
        /** @since API level 5 */
        public static final String CONTACT_STATUS_LABEL =
            android.provider.ContactsContract.Contacts.CONTACT_STATUS_LABEL;
        /** @since API level 5 */
        public static final String CONTACT_STATUS_RES_PACKAGE =
            android.provider.ContactsContract.Contacts.CONTACT_STATUS_RES_PACKAGE;
        /** @since API level 5 */
        public static final String CONTACT_STATUS_TIMESTAMP =
            android.provider.ContactsContract.Contacts.CONTACT_STATUS_TIMESTAMP;

        // constants inherited from the protected static interface
        // android.provider.ContactsContract.ContactsColumns

        /** @since API level 5 */
        public static final String DISPLAY_NAME =
            android.provider.ContactsContract.Contacts.DISPLAY_NAME;
        /** @since API level 5 */
        public static final String HAS_PHONE_NUMBER =
            android.provider.ContactsContract.Contacts.HAS_PHONE_NUMBER;
        /** @since API level 5 */
        public static final String IN_VISIBLE_GROUP =
            android.provider.ContactsContract.Contacts.IN_VISIBLE_GROUP;
        /** @since API level 5 */
        public static final String LOOKUP_KEY =
            android.provider.ContactsContract.Contacts.LOOKUP_KEY;
        /** @since API level 5 */
        public static final String PHOTO_ID =
            android.provider.ContactsContract.Contacts.PHOTO_ID;
    }

    // public static final class android.provider.ContactsContract.Data
    public static final class Data {

        public static final String CONTENT_TYPE =
            android.provider.ContactsContract.Data.CONTENT_TYPE;
        public static final android.net.Uri CONTENT_URI =
            android.provider.ContactsContract.Data.CONTENT_URI;

        // constants inherited from the public interface
        // android.provider.BaseColumns

        /** @since API level 1 */
        public static final String _COUNT =
            android.provider.BaseColumns._COUNT;
        /** @since API level 1 */
        public static final String _ID =
            android.provider.BaseColumns._ID;

        // constants inherited from the static interface
        // android.provider.ContactsContract.ContactsColumns

        public static final String DISPLAY_NAME =
            android.provider.ContactsContract.Data.DISPLAY_NAME;
        public static final String HAS_PHONE_NUMBER =
            android.provider.ContactsContract.Data.HAS_PHONE_NUMBER;
        public static final String IN_VISIBLE_GROUP =
            android.provider.ContactsContract.Data.IN_VISIBLE_GROUP;
        public static final String LOOKUP_KEY =
            android.provider.ContactsContract.Data.LOOKUP_KEY;
        public static final String PHOTO_ID =
            android.provider.ContactsContract.Data.PHOTO_ID;

        // constants inherited from the static interface 
        // android.provider.ContactsContract.DataColumns
        public static final String MIMETYPE =
            android.provider.ContactsContract.Data.MIMETYPE;
        public static final String RAW_CONTACT_ID =
            android.provider.ContactsContract.Data.RAW_CONTACT_ID;
        public static final String SYNC1 =
            android.provider.ContactsContract.Data.SYNC1;
        public static final String SYNC2 =
            android.provider.ContactsContract.Data.SYNC2;
        public static final String SYNC3 =
            android.provider.ContactsContract.Data.SYNC3;
        public static final String SYNC4 =
            android.provider.ContactsContract.Data.SYNC4;
    }

    public static final class RawContacts {

        // constants defined in RawContacts itself
        public static final int AGGREGATION_MODE_DEFAULT =
            android.provider.ContactsContract.RawContacts.AGGREGATION_MODE_DEFAULT;
        public static final int AGGREGATION_MODE_DISABLED =
            android.provider.ContactsContract.RawContacts.AGGREGATION_MODE_DISABLED;
        public static final int AGGREGATION_MODE_IMMEDIATE =
            android.provider.ContactsContract.RawContacts.AGGREGATION_MODE_IMMEDIATE;
        public static final int AGGREGATION_MODE_SUSPENDED =
            android.provider.ContactsContract.RawContacts.AGGREGATION_MODE_SUSPENDED;
        public static final String CONTENT_ITEM_TYPE =
            android.provider.ContactsContract.RawContacts.CONTENT_ITEM_TYPE;
        public static final String CONTENT_TYPE =
            android.provider.ContactsContract.RawContacts.CONTENT_TYPE;
        public static final android.net.Uri CONTENT_URI =
            android.provider.ContactsContract.RawContacts.CONTENT_URI;
        /* present in javadoc but not in jar !?
        public static final String DISPLAY_NAME_ALTERNATIVE =
            android.provider.ContactsContract.RawContacts.DISPLAY_NAME_ALTERNATIVE;
        public static final String DISPLAY_NAME_PRIMARY =
            android.provider.ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY;
        public static final String DISPLAY_NAME_SOURCE =
            android.provider.ContactsContract.RawContacts.DISPLAY_NAME_SOURCE;
        public static final String PHONETIC_NAME =
            android.provider.ContactsContract.RawContacts.PHONETIC_NAME;
        public static final String PHONETIC_NAME_STYLE =
            android.provider.ContactsContract.RawContacts.PHONETIC_NAME_STYLE;
        public static final String SORT_KEY_ALTERNATIVE =
            android.provider.ContactsContract.RawContacts.SORT_KEY_ALTERNATIVE;
        public static final String SORT_KEY_PRIMARY =
            android.provider.ContactsContract.RawContacts.SORT_KEY_PRIMARY;
        */
        // forwarders to methods defined in RawContacts itself

        /* since API level 5 */
        public static android.net.Uri getContactLookupUri(android.content.ContentResolver resolver,
                                                          android.net.Uri rawContactUri) {
            return android.provider.ContactsContract.RawContacts.getContactLookupUri(resolver, rawContactUri);
        }
        /* since API level 8 */
        public static android.content.EntityIterator newEntityIterator(android.database.Cursor cursor) {
            return android.provider.ContactsContract.RawContacts.newEntityIterator(cursor);
        }

        // constants inherited from the public interface
        // android.provider.BaseColumns

        /** @since API level 1 */
        public static final String _COUNT =
            android.provider.BaseColumns._COUNT;
        /** @since API level 1 */
        public static final String _ID =
            android.provider.BaseColumns._ID;

        // constants inherited from interface
        // android.provider.ContactsContract.BaseSyncColumns

        public static final String SYNC1 =
           android.provider.ContactsContract.RawContacts.SYNC1;
        public static final String SYNC2 =
           android.provider.ContactsContract.RawContacts.SYNC2;
        public static final String SYNC3 =
           android.provider.ContactsContract.RawContacts.SYNC3;
        public static final String SYNC4 =
           android.provider.ContactsContract.RawContacts.SYNC4;

        // constants inherited from interface
        // android.provider.ContactsContract.ContactOptionsColumns

        public static final String CUSTOM_RINGTONE =
           android.provider.ContactsContract.RawContacts.CUSTOM_RINGTONE;
        public static final String LAST_TIME_CONTACTED =
           android.provider.ContactsContract.RawContacts.LAST_TIME_CONTACTED;
        public static final String SEND_TO_VOICEMAIL =
           android.provider.ContactsContract.RawContacts.SEND_TO_VOICEMAIL;
        public static final String STARRED =
           android.provider.ContactsContract.RawContacts.STARRED;
        public static final String TIMES_CONTACTED =
           android.provider.ContactsContract.RawContacts.TIMES_CONTACTED;

        // constants inherited from interface
        // android.provider.ContactsContract.RawContactsColumns

        public static final String AGGREGATION_MODE =
           android.provider.ContactsContract.RawContacts.AGGREGATION_MODE;
        public static final String CONTACT_ID =
           android.provider.ContactsContract.RawContacts.CONTACT_ID;
        public static final String DELETED =
           android.provider.ContactsContract.RawContacts.DELETED;

        // constants inherited from the static interface
        // android.provider.ContactsContract.SyncColumns

        /** @since API level 5 */
        public static final String ACCOUNT_NAME =
           android.provider.ContactsContract.RawContacts.ACCOUNT_NAME;
        /** @since API level 5 */
        public static final String ACCOUNT_TYPE =
           android.provider.ContactsContract.RawContacts.ACCOUNT_TYPE;
        /** @since API level 5 */
        public static final String DIRTY =
           android.provider.ContactsContract.RawContacts.DIRTY;
        /** @since API level 5 */
        public static final String SOURCE_ID =
           android.provider.ContactsContract.RawContacts.SOURCE_ID;
        /** @since API level 5 */
        public static final String VERSION =
           android.provider.ContactsContract.RawContacts.VERSION;
    }

}

