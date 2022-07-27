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
import org.keycloak.component.ComponentValidationException;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;
import org.keycloak.storage.ldap.LDAPStorageProvider;
import org.keycloak.storage.ldap.mappers.AbstractLDAPStorageMapper;
import org.keycloak.storage.ldap.mappers.AbstractLDAPStorageMapperFactory;
import org.keycloak.storage.ldap.mappers.UserAttributeLDAPStorageMapper;

import java.util.List;

public class VariablesLDAPStorageMapperFactory extends AbstractLDAPStorageMapperFactory {

  public static final String PROVIDER_ID = "variables-ldap-mapper";

  protected static final List<ProviderConfigProperty> configProperties;

  static {
    configProperties = getConfigProps(null);
  }

  private static List<ProviderConfigProperty> getConfigProps(ComponentModel parent) {
    return ProviderConfigurationBuilder.create()
        .property().name(VariablesLDAPStorageMapper.LDAP_VARIABLE_ATTRIBUTE)
        .label("LDAP Variable Attribute")
        .helpText("Template for LDAP attribute like 'someprefix-${cn}' (only one variable supported at the moment)")
        .type(ProviderConfigProperty.STRING_TYPE)
        .defaultValue("prefix-${cn}")
        .add()
        .property().name(UserAttributeLDAPStorageMapper.USER_MODEL_ATTRIBUTE)
        .label("User Model Attribute")
        .helpText(
            "Name of the UserModel property or attribute you want to map the LDAP attribute into. For example 'firstName', 'lastName, 'email', 'street' etc.")
        .type(ProviderConfigProperty.STRING_TYPE)
        .add()
        .build();
  }

  @Override
  public String getHelpText() {
    return "Used to map a customized (prefixed, suffixed) LDAP attribute to UserModel in Keycloak DB";
  }

  @Override
  public List<ProviderConfigProperty> getConfigProperties() {
    return configProperties;
  }

  @Override
  public List<ProviderConfigProperty> getConfigProperties(RealmModel realm, ComponentModel parent) {
    return getConfigProps(parent);
  }

  @Override
  public String getId() {
    return PROVIDER_ID;
  }

  @Override
  public void validateConfiguration(KeycloakSession session, RealmModel realm, ComponentModel config)
      throws ComponentValidationException {
    checkMandatoryConfigAttribute(VariablesLDAPStorageMapper.LDAP_VARIABLE_ATTRIBUTE, "LDAP Variable Attribute", config);

    ComponentModel parent = realm.getComponent(config.getParentId());
    if (parent == null) {
      throw new ComponentValidationException("can't find parent component model");

    }
  }

  @Override
  protected AbstractLDAPStorageMapper createMapper(ComponentModel mapperModel, LDAPStorageProvider federationProvider) {
    return new VariablesLDAPStorageMapper(mapperModel, federationProvider);
  }
}
