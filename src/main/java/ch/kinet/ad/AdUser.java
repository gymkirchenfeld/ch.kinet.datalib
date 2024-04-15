/*
 * Copyright (C) 2012 - 2024 by Stefan Rothe
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY); without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ch.kinet.ad;

import ch.kinet.Util;
import ch.kinet.ldap.LdapConnection;
import ch.kinet.ldap.LdapException;
import ch.kinet.ldap.LdapObject;
import ch.kinet.ldap.Name;
import ch.kinet.ldap.SearchResult;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class AdUser extends LdapObject {

    private static final int ACCOUNT_DISABLED = 0x00000002;
    private static final String ACCOUNT_EXPIRATION_DATE = "accountExpirationDate";
    private static final String DEPARTMENT = "department";
    private static final String DESCRIPTION = "description";
    private static final String DISPLAY_NAME = "displayName";
    private static final String EMPLOYEE_TYPE = "employeeType";
    private static final String EMPLOYEE_NUMBER = "employeeNumber";
    private static final String GIVEN_NAME = "givenName";
    private static final String IS_ACCOUNT_LOCKED = "isAccountLocked";
    private static final String MAIL = "mail";
    private static final String MEMBER_OF = "memberOf";
    private static final String MS_EXCH_HIDE_FROM_ADDRESS_LISTS = "msExchHideFromAddressLists";
    private static final String O = "o";
    private static final String PERSONAL_TITLE = "personalTitle";
    private static final String PROFILE_PATH = "profilePath";
    private static final String PROXY_ADDRESSES = "proxyAddresses";
    private static final String PWD_LAST_SET = "pwdLastSet";
    private static final String SAM_ACCOUNT_NAME = "samAccountName";
    private static final String SN = "sn";
    private static final String TELEPHONE_NUMBER = "telephoneNumber";
    private static final String UNICODE_PWD = "unicodePwd";
    private static final String USER_ACCOUNT_CONTROL = "userAccountControl";
    private static final String USER_PRINCIPAL_NAME = "userPrincipalName";
    public static final String[] ATTRIBUTES = {
        ACCOUNT_EXPIRATION_DATE, DEPARTMENT, DESCRIPTION, DISPLAY_NAME, EMPLOYEE_TYPE, EMPLOYEE_NUMBER, GIVEN_NAME,
        IS_ACCOUNT_LOCKED, MAIL, MEMBER_OF, MS_EXCH_HIDE_FROM_ADDRESS_LISTS, O, PERSONAL_TITLE, PROFILE_PATH,
        PROXY_ADDRESSES, SAM_ACCOUNT_NAME, SN, TELEPHONE_NUMBER, USER_ACCOUNT_CONTROL, USER_PRINCIPAL_NAME, PWD_LAST_SET
    };
    private int userAccountControl;

    AdUser(LdapConnection connection, Name dn) {
        super(connection, dn);
        setAsString("objectClass", "user");
    }

    AdUser(LdapConnection connection, SearchResult searchResult) {
        super(connection, searchResult, UNICODE_PWD);
        userAccountControl = getAsInt(USER_ACCOUNT_CONTROL, 0);
    }

    public String getDepartment() {
        return getAsString(DEPARTMENT);
    }

    public String getDisplayName() {
        return getAsString(DISPLAY_NAME);
    }

    public String getEmailAddress() {
        return getAsString(MAIL);
    }

    public String getEmployeeType() {
        return getAsString(EMPLOYEE_TYPE);
    }

    public String getFirstName() {
        return getAsString(GIVEN_NAME);
    }

    public int getId() {
        try {
            return getAsInt(EMPLOYEE_NUMBER, -1);
        }
        catch (LdapException ex) {
            throw new RuntimeException(ex);
        }
    }

    public String getLastName() {
        return getAsString(SN);
    }

    public Set<String> getMemberOf() {
        return getAsStrings(MEMBER_OF).collect(Collectors.toSet());
    }

    public String getOrganization() {
        return getAsString(O);
    }

    /**
     * Return the content of 'personalTitle' AD field. It usually contains the personal title of a person (e.g. Mr.,
     * Mrs. or Herr, Frau)
     */
    public String getPersonalTitle() {
        return getAsString(PERSONAL_TITLE);
    }

    public Set<String> getProxyAddresses() {
        return getAsStrings(PROXY_ADDRESSES).collect(Collectors.toSet());
    }

    public String getProfilePath() {
        return getAsString(PROFILE_PATH);
    }

    public String getSamAccountName() {
        return getAsString(SAM_ACCOUNT_NAME);
    }

    public String getTelephoneNumber() {
        return getAsString(TELEPHONE_NUMBER);
    }

    public String getUserPrincipalName() {
        return getAsString(USER_PRINCIPAL_NAME);
    }

    public boolean isAccountDisabled() {
        return getUacFlag(ACCOUNT_DISABLED);
    }

    public boolean isHiddenInAddressLists() {
        return getAsBoolean(MS_EXCH_HIDE_FROM_ADDRESS_LISTS, false);
    }

    public void setAccountDisabled(boolean value) {
        setUacFlag(ACCOUNT_DISABLED, value);
    }

    public void setDepartment(String value) {
        setAsString(DEPARTMENT, value);
    }

    public void setDescription(String value) {
        setAsString(DESCRIPTION, value);
    }

    public void setDisplayName(String value) {
        setAsString(DISPLAY_NAME, value);
    }

    public void setEmailAddress(String value) {
        setAsString(MAIL, value);
    }

    public void setEmployeeType(String value) {
        if (value == null) {
            setAsString(EMPLOYEE_TYPE, "");
        }
        else {
            setAsString(EMPLOYEE_TYPE, value);
        }
    }

    public void setFirstName(String value) {
        if (value == null) {
            setAsString(GIVEN_NAME, "");
        }
        else {
            setAsString(GIVEN_NAME, value);
        }
    }

    public void setHiddenInAddressLists(boolean value) {
        setAsBoolean(MS_EXCH_HIDE_FROM_ADDRESS_LISTS, value);
    }

    public void setId(int value) {
        setAsInt(EMPLOYEE_NUMBER, value);
    }

    public void setLastName(String value) {
        if (value == null) {
            setAsString(SN, "");
        }
        else {
            setAsString(SN, value);
        }
    }

    public void setInitialPassword(String value) {
        if (!Util.isEmpty(value)) {
            setPassword(value);
            // User must change password at next login
            setAsInt(PWD_LAST_SET, 0);
        }
    }

    public void setOrganization(String value) {
        setAsString(O, value);
    }

    public void setPassword(String value) {
        String newQuotedPassword = "\"" + value + "\"";
        setAsEncodedBytes(UNICODE_PWD, newQuotedPassword, "UTF-16LE");
    }

    public void setPersonalTitle(String value) {
        setAsString(PERSONAL_TITLE, value);
    }

    public void setProfilePath(String value) {
        setAsString(PROFILE_PATH, value);
    }

    public void setProxyAddresses(Collection<String> value) {
        setAsStrings(PROXY_ADDRESSES, value);
    }

    public void setSamAccountName(String value) {
        setAsString(SAM_ACCOUNT_NAME, value);
    }

    public void setTelephoneNumber(String value) {
        setAsString(TELEPHONE_NUMBER, value);
    }

    public void setUserPrincipalName(String value) {
        setAsString(USER_PRINCIPAL_NAME, value);
    }

    public void setUacFlag(int flag, boolean set) {
        if (set) {
            userAccountControl = userAccountControl | flag;
        }
        else {
            userAccountControl = userAccountControl & (~flag);
        }

        setAsInt(USER_ACCOUNT_CONTROL, userAccountControl);
    }

    private boolean getUacFlag(int flag) {
        return (userAccountControl & flag) == flag;
    }
}
