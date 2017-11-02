<%@ page import="org.oscm.ui.common.Constants"%>
<%@ page import="org.oscm.ui.common.SessionListener"%>
<%
//logout page for the tests 
session.removeAttribute(Constants.SESS_ATTR_USER);
SessionListener.cleanup(session);

response.sendRedirect(request.getContextPath()+"/default.jsf");
%>
