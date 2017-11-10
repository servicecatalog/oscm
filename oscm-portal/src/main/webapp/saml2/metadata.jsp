<?xml version="1.0" encoding="UTF-8" ?>
<%@ page import="org.oscm.saml2.api.SpMetadataGenerator"%>
<%@ page import="org.oscm.converter.XMLConverter"%>
<%@ page import="org.w3c.dom.Document"%>
<%@ page import="org.oscm.internal.intf.ConfigurationService"%>
<%@ page import="org.oscm.internal.types.enumtypes.ConfigurationKey"%>
<%@ page import="org.oscm.ui.common.ServiceAccess"%>
<%@ page language="java" contentType="text/xml; charset=UTF-8" pageEncoding="UTF-8"%>
<%
  ConfigurationService configService =  ServiceAccess.getServiceAcccessFor(session).getService(ConfigurationService.class);
  String entityId = configService.getVOConfigurationSetting(ConfigurationKey.SSO_ISSUER_ID, "global").getValue();  
  String base_url_https = configService.getVOConfigurationSetting(ConfigurationKey.BASE_URL_HTTPS, "global").getValue();
  String base_url_http = configService.getVOConfigurationSetting(ConfigurationKey.BASE_URL, "global").getValue();
  
  SpMetadataGenerator generator = new SpMetadataGenerator(entityId, base_url_https, base_url_http);
  Document metadata = generator.generate();
  String xmlDocument = XMLConverter.convertToString(metadata, false);
%>
<%=xmlDocument%>