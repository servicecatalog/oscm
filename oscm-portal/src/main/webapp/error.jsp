<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page isErrorPage="true" 
  import="java.io.*" 
  import="org.oscm.ui.common.ExceptionHandler"
%>
<% 
if (ExceptionHandler.isInvalidUserSession(exception)) { 
    response.sendRedirect(request.getContextPath() +"/default.jsf");
}
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" 
                      "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<h:head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
  <title></title>
  <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/public/css.jsf"/>
</h:head>

<h:body>

<div id="w_004_h_e_g_header">

  <!-- Product Series Logos -->
  <div id="w_004_h_e_logo">

    <div id="w_004_h_e_g_header_r">
      <div id="w_004_h_e_g_header_ie6">
        <!-- Fujitsu logo -->
        <div id="w_004_h_e_logo_r"></div>

      </div>
    </div>
    
  </div>

</div>

<p>
&nbsp;
</p>
<p>
&nbsp;
</p>

<center>
<table cellspacing="0" cellpadding="0" border="0"><tr><td>
<div style="text-align:left;">
  <div class="rich-panel-header"><%=ExceptionHandler.getErrorTitle(request)%></div>
  
  <div style="border: 1px solid grey; width: 300px; height: 80px; padding: 5px 5px 5px 5px;">
    <%=ExceptionHandler.getErrorText(request)%>
    <%
    /*
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    Throwable t = ExceptionHandler.unwrapException(ExceptionHandler.getEJBException(exception));
    if (t != null) {
        t.printStackTrace(pw);
    } else {
        exception.printStackTrace(pw);
    }
    out.print(sw);
    sw.close();
    pw.close();
    */
    %>
  </div>
</div>
</td></tr></table>
</center>

</h:body>

</html>