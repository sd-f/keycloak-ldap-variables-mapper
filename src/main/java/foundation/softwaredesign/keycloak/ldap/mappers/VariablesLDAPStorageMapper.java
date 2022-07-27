/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package foundation.softwaredesign.keycloak.ldap.mappers;

import org.keycloak.component.ComponentModel;
import org.keycloak.models.LDAPConstants;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.UserModelDelegate;
import org.keycloak.storage.ldap.LDAPStorageProvider;
import org.keycloak.storage.ldap.idm.model.LDAPObject;
import org.keycloak.storage.ldap.idm.query.internal.LDAPQuery;
import org.keycloak.storage.ldap.mappers.AbstractLDAPStorageMapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Mapper useful for the LDAP deployments when some attribute (usually CN) is mapped to full name of user
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class VariablesLDAPStorageMapper extends AbstractLDAPStorageMapper {

  public static final String LDAP_VARIABLE_ATTRIBUTE = "variable.attribute";
  public static final String USER_MODEL_ATTRIBUTE = "user.model.attribute";

  private class Attribute {

    private static final String PATTERN = "^(.*)\\$\\{(.+)\\}(.*)$";
    private String prefix = "";
    private String suffix = "";
    private String attributeName = LDAPConstants.CN;

    public Attribute(final ComponentModel model) {
      String attribute = model.getConfig().getFirst(LDAP_VARIABLE_ATTRIBUTE);
      Pattern r = Pattern.compile(PATTERN);
      Matcher m = r.matcher(attribute);
      if (m.find()) {
        String group = m.group(1);
        if (group != null && !group.isEmpty()) {
          this.prefix = group;
        }
        group = m.group(2);
        if (group != null && !group.isEmpty()) {
          this.attributeName = group;
        }
        group = m.group(3);
        if (group != null && !group.isEmpty()) {
          this.suffix = group;
        }
      }
    }

    public String getAttributeName() {
      return attributeName;
    }

    public String getValue(final String ldapAttributeValue) {
      return prefix + ldapAttributeValue + suffix;
    }
  }

  public VariablesLDAPStorageMapper(ComponentModel mapperModel, LDAPStorageProvider ldapProvider) {
    super(mapperModel, ldapProvider);
  }

  @Override
  public void onImportUserFromLDAP(LDAPObject ldapUser, UserModel user, RealmModel realm, boolean isCreate) {
    String userModelAttribute = getUserModelAttribute();
    Attribute attribute = new Attribute(mapperModel);
    String ldapAttributeValue = ldapUser.getAttributeAsString(attribute.getAttributeName());
    if (ldapAttributeValue == null) {
      user.removeAttribute(userModelAttribute);
      return;
    }
    user.setSingleAttribute(userModelAttribute, attribute.getValue(ldapAttributeValue));
  }

  @Override
  public void onRegisterUserToLDAP(LDAPObject ldapUser, UserModel localUser, RealmModel realm) {

  }

  @Override
  public UserModel proxy(LDAPObject ldapUser, UserModel delegate, RealmModel realm) {

    final String userModelAttrName = getUserModelAttribute();
    final Attribute attribute = new Attribute(mapperModel);
    final String ldapAttrName = attribute.getAttributeName();

    delegate = new UserModelDelegate(delegate) {

      @Override
      public String getFirstAttribute(String name) {
        if (name.equalsIgnoreCase(userModelAttrName)) {
          return attribute.getValue(ldapUser.getAttributeAsString(ldapAttrName));
        } else {
          return super.getFirstAttribute(name);
        }
      }

    };

    return delegate;

  }

  @Override
  public void beforeLDAPQuery(LDAPQuery query) {
    final String userModelAttrName = getUserModelAttribute();
    final Attribute attribute = new Attribute(mapperModel);
    final String ldapAttrName = attribute.getAttributeName();
    query.addReturningReadOnlyLdapAttribute(ldapAttrName);
  }

  private boolean isBlank(String attr) {
    return attr == null || attr.trim().isEmpty();
  }

  private String getUserModelAttribute() {
    return mapperModel.getConfig().getFirst(USER_MODEL_ATTRIBUTE);
  }
}
