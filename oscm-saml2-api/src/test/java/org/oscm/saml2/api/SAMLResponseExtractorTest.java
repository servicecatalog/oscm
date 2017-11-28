/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Jun 13, 2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.saml2.api;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

import java.io.FileInputStream;

import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.oscm.converter.XMLConverter;
import org.oscm.internal.types.exception.UserIdNotFoundException;
import org.oscm.stream.Streams;
import org.w3c.dom.Document;

/**
 * Tests the retrieving of the userid from a saml response.
 * 
 * @author farmaki
 * 
 */
public class SAMLResponseExtractorTest {

    private final String FILE_UNSIGNED_RESPONSE = "javares/unsignedResponse.xml";
    private final String FILE_UNSIGNED_LOGOUT_RESPONSE = "javares/openamUnsignedLogoutResponse.xml";
    private final String FILE_UNSIGNED_RESPONSE_MISSING_USERID = "javares/unsignedResponse_missingUserid.xml";
    private final String FILE_UNSIGNED_ASSERTION = "javares/unsignedAssertion.xml";
    private final String FILE_UNSIGNED_ASSERTION_MISSING_USERID = "javares/unsignedAssertion_noConfirmationData_noUserid.xml";
    private SAMLResponseExtractor samlResponse;
    private String encodedSamlLoginResponseFromADFS = "PHNhbWxwOlJlc3BvbnNlIElEPSJfMTA4NTgzYjYtMTJiYS00MmViLTg1NmUtMGYyYjRlZWU3MzQxIiBWZXJzaW9uPSIyLjAiIElzc3VlSW5zdGFudD0iMjAxNi0wNi0yOFQwOTo1ODoyNy4zNTBaIiBEZXN0aW5hdGlvbj0iaHR0cHM6Ly9lc3RzdGF2cmV2YW5iLmludGVybi5lc3QuZnVqaXRzdS5jb206ODE4MS9vc2NtLXBvcnRhbC8iIENvbnNlbnQ9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDpjb25zZW50OnVuc3BlY2lmaWVkIiBJblJlc3BvbnNlVG89IklEXzQ3MDYzMjIzYzFlZDA3YWRkYjE2MzhiMDEzMDMyNDE5YzBmMjc4NGYiIHhtbG5zOnNhbWxwPSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6cHJvdG9jb2wiPjxJc3N1ZXIgeG1sbnM9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDphc3NlcnRpb24iPmh0dHA6Ly9FU1RFU1NBQ1RESVIuYWRmcy5jb20vYWRmcy9zZXJ2aWNlcy90cnVzdDwvSXNzdWVyPjxzYW1scDpTdGF0dXM+PHNhbWxwOlN0YXR1c0NvZGUgVmFsdWU9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDpzdGF0dXM6U3VjY2VzcyIgLz48L3NhbWxwOlN0YXR1cz48QXNzZXJ0aW9uIElEPSJfZWYzNzNjMzQtZmRmMi00OGMxLThiYTMtODQzNjgwNDYyODg5IiBJc3N1ZUluc3RhbnQ9IjIwMTYtMDYtMjhUMDk6NTg6MjcuMzUwWiIgVmVyc2lvbj0iMi4wIiB4bWxucz0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOmFzc2VydGlvbiI+PElzc3Vlcj5odHRwOi8vRVNURVNTQUNURElSLmFkZnMuY29tL2FkZnMvc2VydmljZXMvdHJ1c3Q8L0lzc3Vlcj48ZHM6U2lnbmF0dXJlIHhtbG5zOmRzPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwLzA5L3htbGRzaWcjIj48ZHM6U2lnbmVkSW5mbz48ZHM6Q2Fub25pY2FsaXphdGlvbk1ldGhvZCBBbGdvcml0aG09Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvMTAveG1sLWV4Yy1jMTRuIyIgLz48ZHM6U2lnbmF0dXJlTWV0aG9kIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvMjAwMC8wOS94bWxkc2lnI3JzYS1zaGExIiAvPjxkczpSZWZlcmVuY2UgVVJJPSIjX2VmMzczYzM0LWZkZjItNDhjMS04YmEzLTg0MzY4MDQ2Mjg4OSI+PGRzOlRyYW5zZm9ybXM+PGRzOlRyYW5zZm9ybSBBbGdvcml0aG09Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvMDkveG1sZHNpZyNlbnZlbG9wZWQtc2lnbmF0dXJlIiAvPjxkczpUcmFuc2Zvcm0gQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxLzEwL3htbC1leGMtYzE0biMiIC8+PC9kczpUcmFuc2Zvcm1zPjxkczpEaWdlc3RNZXRob2QgQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwLzA5L3htbGRzaWcjc2hhMSIgLz48ZHM6RGlnZXN0VmFsdWU+Y3hFWnpxTysxNVNXQUIwSDBXMUNVSGttZXc0PTwvZHM6RGlnZXN0VmFsdWU+PC9kczpSZWZlcmVuY2U+PC9kczpTaWduZWRJbmZvPjxkczpTaWduYXR1cmVWYWx1ZT5tU2IvVHhYeUFIQnN2a0dHak5iVmlOQkRtbkVBWVFWaitFSUdkSGJoMVhqVWM0N21tQ3VOSlBXaXUrazBTdUQybHY3SjkrN0ZsdnhhYlJiUFk5RHNTbVlYZi9KQVZxWmowMTBIU2g1d2VZdVR4bUZxUzBVUi9aNnErOE85Q2NId1R1bXpST25zbWtPbVJLaTdIdkZoM04raGZjOFRRbW13QVVFSjdrc2RWYWxiWlFaMmlTOVFTM3VwcDFSWHlIT0VnUWhHMVNOYkgvRGtJUCsyWUdFYTFaZWdzN2I3V285U1A1UFVpL2ZFeFExaWdtTVNLcDU0WllQdVB3WUVBN0MrenJnVTFrd0VJNWJhRzZvVkpWNW1aek94QzdkS3ZFRDBNWkdzUldsVW1uWTNtcXk4b3RwNnZZbE00Q2JVVGFEcFZoOWJZbXpsTE40eUpZSEt5bWdmVEE9PTwvZHM6U2lnbmF0dXJlVmFsdWU+PEtleUluZm8geG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvMDkveG1sZHNpZyMiPjxkczpYNTA5RGF0YT48ZHM6WDUwOUNlcnRpZmljYXRlPk1JSUM1akNDQWM2Z0F3SUJBZ0lRZlp1eXUrUDlDcWRNc2NlNW1YbFNuakFOQmdrcWhraUc5dzBCQVFzRkFEQXZNUzB3S3dZRFZRUURFeVJCUkVaVElGTnBaMjVwYm1jZ0xTQkZVMVJGVTFOQlExUkVTVkl1WVdSbWN5NWpiMjB3SGhjTk1UWXdNekF5TVRNMU16QXlXaGNOTVRjd016QXlNVE0xTXpBeVdqQXZNUzB3S3dZRFZRUURFeVJCUkVaVElGTnBaMjVwYm1jZ0xTQkZVMVJGVTFOQlExUkVTVkl1WVdSbWN5NWpiMjB3Z2dFaU1BMEdDU3FHU0liM0RRRUJBUVVBQTRJQkR3QXdnZ0VLQW9JQkFRREhZOHRicWUzT2xFb1ZTMy8vTVdvL0tNZkEwdS9Ta1NERUpySDNXWWxPRUxVOFV3NURhbGJ6SVFKUC9UbFFmQVNaMmw5Qkx1Umx3STkvenY0UDh1eTRJTzQzY25mcWpsdHZrcmJIaHdyOUlJWUxYSWZ6MWZzN0RZS0RxOVFvc2Z4akxTcjFuSW9hajQyV241WlVzZmRueUh5dlZEZ1dST3N4eWVOMTkxb3IyVTVQR2ZqRjI4RkcwMUg5TUcyQ1JxMDNzOGdNREUrbysvSklOUW9jbWhDdXQrZUFLREViNzVINml1b2QwNXk0TGl3cURpdy9COHNVa09xb0xCZnZYbE1UQ01TQkM2T0tGc0JwMU9IdkJ1bGMwaHVrMDhvV0pYNnVHVzVBb0FoSDdheHdhWHphclZqOThPQW5tZk1EL01DVE8zMktMY0I1aVVSazJEaWJPdWtwQWdNQkFBRXdEUVlKS29aSWh2Y05BUUVMQlFBRGdnRUJBRC8yUmtZbUF3dll6b05VeU5LOFV4RDVQZ3VmdVFkZUIrNDc0Y25SRGk4V2h3Q1N4ZXE2TUEvdERueTBlK3hwWlFuR2UvUjJtanhiMkdwVHpiRVk0NG1oVWJNT0w2YWs3MkpaL0s5aW0vV1lTK3p6d0QwUVlLU2MxUXJmbzI0NzdSQWZtNm5pK3lWR1UzSU04VkNlM2lDMjhWSE9pSkpIQVFNckNpN0xnL0ROQ0E3NExQemRSdzYwSU1lck9UbnowS0gybjNBTGlsMWNnckM5dXUzZ01IYkg0cThSbk5UWVRlUDVOVUpOQUdKbWp3ZGRZaGROajF6aUNWRnZ1Y0d3OWUzb3BKdzVpZExyNUdTNFF3U0hoampyZUpkSVlsOUQ2a2pMZldIMThhYklHU2dsSGZPSEh5bTdBUUhUWG10OU1FZ0R0eGhEdlUzK3dBNHBVZlc0a0k4PTwvZHM6WDUwOUNlcnRpZmljYXRlPjwvZHM6WDUwOURhdGE+PC9LZXlJbmZvPjwvZHM6U2lnbmF0dXJlPjxTdWJqZWN0PjxOYW1lSUQgRm9ybWF0PSJodHRwOi8vc2NoZW1hcy54bWxzb2FwLm9yZy9jbGFpbXMvVVBOIj5BZG1pbkBhZGZzLmNvbTwvTmFtZUlEPjxTdWJqZWN0Q29uZmlybWF0aW9uIE1ldGhvZD0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOmNtOmJlYXJlciI+PFN1YmplY3RDb25maXJtYXRpb25EYXRhIEluUmVzcG9uc2VUbz0iSURfNDcwNjMyMjNjMWVkMDdhZGRiMTYzOGIwMTMwMzI0MTljMGYyNzg0ZiIgTm90T25PckFmdGVyPSIyMDE2LTA2LTI4VDEwOjAzOjI3LjM1MFoiIFJlY2lwaWVudD0iaHR0cHM6Ly9lc3RzdGF2cmV2YW5iLmludGVybi5lc3QuZnVqaXRzdS5jb206ODE4MS9vc2NtLXBvcnRhbC8iIC8+PC9TdWJqZWN0Q29uZmlybWF0aW9uPjwvU3ViamVjdD48Q29uZGl0aW9ucyBOb3RCZWZvcmU9IjIwMTYtMDYtMjhUMDk6NTg6MjcuMzQ4WiIgTm90T25PckFmdGVyPSIyMDE2LTA2LTI4VDEwOjU4OjI3LjM0OFoiPjxBdWRpZW5jZVJlc3RyaWN0aW9uPjxBdWRpZW5jZT5lc3RzdGF2cmV2YW5iLW9zY208L0F1ZGllbmNlPjwvQXVkaWVuY2VSZXN0cmljdGlvbj48L0NvbmRpdGlvbnM+PEF0dHJpYnV0ZVN0YXRlbWVudD48QXR0cmlidXRlIE5hbWU9InVzZXJpZCI+PEF0dHJpYnV0ZVZhbHVlPkFkbWluQGFkZnMuY29tPC9BdHRyaWJ1dGVWYWx1ZT48L0F0dHJpYnV0ZT48L0F0dHJpYnV0ZVN0YXRlbWVudD48QXV0aG5TdGF0ZW1lbnQgQXV0aG5JbnN0YW50PSIyMDE2LTA2LTI4VDA5OjU4OjI3LjI0NVoiIFNlc3Npb25JbmRleD0iX2VmMzczYzM0LWZkZjItNDhjMS04YmEzLTg0MzY4MDQ2Mjg4OSI+PEF1dGhuQ29udGV4dD48QXV0aG5Db250ZXh0Q2xhc3NSZWY+dXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOmFjOmNsYXNzZXM6UGFzc3dvcmRQcm90ZWN0ZWRUcmFuc3BvcnQ8L0F1dGhuQ29udGV4dENsYXNzUmVmPjwvQXV0aG5Db250ZXh0PjwvQXV0aG5TdGF0ZW1lbnQ+PC9Bc3NlcnRpb24+PC9zYW1scDpSZXNwb25zZT4=";
    private String encodedSamlLogoutResponseFromADFS = "fVJNj4MgFPwrhjui9QuJNWnaHky6l9r0sJcNIu66UTA8aPbnL7bZQ5NNbzCZeTNvoAI+Tws76U/t7FnCohXIoDls0QfNIspFUeC4Fx1Ou5LjkmYCpzSlWVzmQkYUBVdpYNRqizZhhIIGwMlGgeXKeiiKcxzleEMvUc6yjGVJmCflOwoOEuyouL0rv6xdgBHiMS+8GXnjqgtHZaVRoQfDwX2PFlwo9MxoTGOiQcx40cbyiaBgv2Ze/ZxRTHMYgSk+S2BWsHb3dmI+GhMPEnMKFinGYZS9j6v+Vr7odeO8SGPeD5gmZYHThJe4y+mAiyFPk0yUPI4kCn7mSQG79/bacjHaaqEnVFf3XsxD+lrEAaRZe0H12ouv5dhejm27218OzTn06WDtgawH4qm3UUgg1jiwFXnY1NXjUVvLrYPn2173MrjyycnXMeDOZq0TfjyggNQVeZ5K/vs59S8=+PHNhbWxwOlN0YXR1c0NvZGUgVmFsdWU9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDpzdGF0dXM6U3VjY2VzcyIgLz48L3NhbWxwOlN0YXR1cz48QXNzZXJ0aW9uIElEPSJfZWYzNzNjMzQtZmRmMi00OGMxLThiYTMtODQzNjgwNDYyODg5IiBJc3N1ZUluc3RhbnQ9IjIwMTYtMDYtMjhUMDk6NTg6MjcuMzUwWiIgVmVyc2lvbj0iMi4wIiB4bWxucz0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOmFzc2VydGlvbiI+PElzc3Vlcj5odHRwOi8vRVNURVNTQUNURElSLmFkZnMuY29tL2FkZnMvc2VydmljZXMvdHJ1c3Q8L0lzc3Vlcj48ZHM6U2lnbmF0dXJlIHhtbG5zOmRzPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwLzA5L3htbGRzaWcjIj48ZHM6U2lnbmVkSW5mbz48ZHM6Q2Fub25pY2FsaXphdGlvbk1ldGhvZCBBbGdvcml0aG09Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvMTAveG1sLWV4Yy1jMTRuIyIgLz48ZHM6U2lnbmF0dXJlTWV0aG9kIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvMjAwMC8wOS94bWxkc2lnI3JzYS1zaGExIiAvPjxkczpSZWZlcmVuY2UgVVJJPSIjX2VmMzczYzM0LWZkZjItNDhjMS04YmEzLTg0MzY4MDQ2Mjg4OSI+PGRzOlRyYW5zZm9ybXM+PGRzOlRyYW5zZm9ybSBBbGdvcml0aG09Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvMDkveG1sZHNpZyNlbnZlbG9wZWQtc2lnbmF0dXJlIiAvPjxkczpUcmFuc2Zvcm0gQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxLzEwL3htbC1leGMtYzE0biMiIC8+PC9kczpUcmFuc2Zvcm1zPjxkczpEaWdlc3RNZXRob2QgQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwLzA5L3htbGRzaWcjc2hhMSIgLz48ZHM6RGlnZXN0VmFsdWU+Y3hFWnpxTysxNVNXQUIwSDBXMUNVSGttZXc0PTwvZHM6RGlnZXN0VmFsdWU+PC9kczpSZWZlcmVuY2U+PC9kczpTaWduZWRJbmZvPjxkczpTaWduYXR1cmVWYWx1ZT5tU2IvVHhYeUFIQnN2a0dHak5iVmlOQkRtbkVBWVFWaitFSUdkSGJoMVhqVWM0N21tQ3VOSlBXaXUrazBTdUQybHY3SjkrN0ZsdnhhYlJiUFk5RHNTbVlYZi9KQVZxWmowMTBIU2g1d2VZdVR4bUZxUzBVUi9aNnErOE85Q2NId1R1bXpST25zbWtPbVJLaTdIdkZoM04raGZjOFRRbW13QVVFSjdrc2RWYWxiWlFaMmlTOVFTM3VwcDFSWHlIT0VnUWhHMVNOYkgvRGtJUCsyWUdFYTFaZWdzN2I3V285U1A1UFVpL2ZFeFExaWdtTVNLcDU0WllQdVB3WUVBN0MrenJnVTFrd0VJNWJhRzZvVkpWNW1aek94QzdkS3ZFRDBNWkdzUldsVW1uWTNtcXk4b3RwNnZZbE00Q2JVVGFEcFZoOWJZbXpsTE40eUpZSEt5bWdmVEE9PTwvZHM6U2lnbmF0dXJlVmFsdWU+PEtleUluZm8geG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvMDkveG1sZHNpZyMiPjxkczpYNTA5RGF0YT48ZHM6WDUwOUNlcnRpZmljYXRlPk1JSUM1akNDQWM2Z0F3SUJBZ0lRZlp1eXUrUDlDcWRNc2NlNW1YbFNuakFOQmdrcWhraUc5dzBCQVFzRkFEQXZNUzB3S3dZRFZRUURFeVJCUkVaVElGTnBaMjVwYm1jZ0xTQkZVMVJGVTFOQlExUkVTVkl1WVdSbWN5NWpiMjB3SGhjTk1UWXdNekF5TVRNMU16QXlXaGNOTVRjd016QXlNVE0xTXpBeVdqQXZNUzB3S3dZRFZRUURFeVJCUkVaVElGTnBaMjVwYm1jZ0xTQkZVMVJGVTFOQlExUkVTVkl1WVdSbWN5NWpiMjB3Z2dFaU1BMEdDU3FHU0liM0RRRUJBUVVBQTRJQkR3QXdnZ0VLQW9JQkFRREhZOHRicWUzT2xFb1ZTMy8vTVdvL0tNZkEwdS9Ta1NERUpySDNXWWxPRUxVOFV3NURhbGJ6SVFKUC9UbFFmQVNaMmw5Qkx1Umx3STkvenY0UDh1eTRJTzQzY25mcWpsdHZrcmJIaHdyOUlJWUxYSWZ6MWZzN0RZS0RxOVFvc2Z4akxTcjFuSW9hajQyV241WlVzZmRueUh5dlZEZ1dST3N4eWVOMTkxb3IyVTVQR2ZqRjI4RkcwMUg5TUcyQ1JxMDNzOGdNREUrbysvSklOUW9jbWhDdXQrZUFLREViNzVINml1b2QwNXk0TGl3cURpdy9COHNVa09xb0xCZnZYbE1UQ01TQkM2T0tGc0JwMU9IdkJ1bGMwaHVrMDhvV0pYNnVHVzVBb0FoSDdheHdhWHphclZqOThPQW5tZk1EL01DVE8zMktMY0I1aVVSazJEaWJPdWtwQWdNQkFBRXdEUVlKS29aSWh2Y05BUUVMQlFBRGdnRUJBRC8yUmtZbUF3dll6b05VeU5LOFV4RDVQZ3VmdVFkZUIrNDc0Y25SRGk4V2h3Q1N4ZXE2TUEvdERueTBlK3hwWlFuR2UvUjJtanhiMkdwVHpiRVk0NG1oVWJNT0w2YWs3MkpaL0s5aW0vV1lTK3p6d0QwUVlLU2MxUXJmbzI0NzdSQWZtNm5pK3lWR1UzSU04VkNlM2lDMjhWSE9pSkpIQVFNckNpN0xnL0ROQ0E3NExQemRSdzYwSU1lck9UbnowS0gybjNBTGlsMWNnckM5dXUzZ01IYkg0cThSbk5UWVRlUDVOVUpOQUdKbWp3ZGRZaGROajF6aUNWRnZ1Y0d3OWUzb3BKdzVpZExyNUdTNFF3U0hoampyZUpkSVlsOUQ2a2pMZldIMThhYklHU2dsSGZPSEh5bTdBUUhUWG10OU1FZ0R0eGhEdlUzK3dBNHBVZlc0a0k4PTwvZHM6WDUwOUNlcnRpZmljYXRlPjwvZHM6WDUwOURhdGE+PC9LZXlJbmZvPjwvZHM6U2lnbmF0dXJlPjxTdWJqZWN0PjxOYW1lSUQgRm9ybWF0PSJodHRwOi8vc2NoZW1hcy54bWxzb2FwLm9yZy9jbGFpbXMvVVBOIj5BZG1pbkBhZGZzLmNvbTwvTmFtZUlEPjxTdWJqZWN0Q29uZmlybWF0aW9uIE1ldGhvZD0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOmNtOmJlYXJlciI+PFN1YmplY3RDb25maXJtYXRpb25EYXRhIEluUmVzcG9uc2VUbz0iSURfNDcwNjMyMjNjMWVkMDdhZGRiMTYzOGIwMTMwMzI0MTljMGYyNzg0ZiIgTm90T25PckFmdGVyPSIyMDE2LTA2LTI4VDEwOjAzOjI3LjM1MFoiIFJlY2lwaWVudD0iaHR0cHM6Ly9lc3RzdGF2cmV2YW5iLmludGVybi5lc3QuZnVqaXRzdS5jb206ODE4MS9vc2NtLXBvcnRhbC8iIC8+PC9TdWJqZWN0Q29uZmlybWF0aW9uPjwvU3ViamVjdD48Q29uZGl0aW9ucyBOb3RCZWZvcmU9IjIwMTYtMDYtMjhUMDk6NTg6MjcuMzQ4WiIgTm90T25PckFmdGVyPSIyMDE2LTA2LTI4VDEwOjU4OjI3LjM0OFoiPjxBdWRpZW5jZVJlc3RyaWN0aW9uPjxBdWRpZW5jZT5lc3RzdGF2cmV2YW5iLW9zY208L0F1ZGllbmNlPjwvQXVkaWVuY2VSZXN0cmljdGlvbj48L0NvbmRpdGlvbnM+PEF0dHJpYnV0ZVN0YXRlbWVudD48QXR0cmlidXRlIE5hbWU9InVzZXJpZCI+PEF0dHJpYnV0ZVZhbHVlPkFkbWluQGFkZnMuY29tPC9BdHRyaWJ1dGVWYWx1ZT48L0F0dHJpYnV0ZT48L0F0dHJpYnV0ZVN0YXRlbWVudD48QXV0aG5TdGF0ZW1lbnQgQXV0aG5JbnN0YW50PSIyMDE2LTA2LTI4VDA5OjU4OjI3LjI0NVoiIFNlc3Npb25JbmRleD0iX2VmMzczYzM0LWZkZjItNDhjMS04YmEzLTg0MzY4MDQ2Mjg4OSI+PEF1dGhuQ29udGV4dD48QXV0aG5Db250ZXh0Q2xhc3NSZWY+dXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOmFjOmNsYXNzZXM6UGFzc3dvcmRQcm90ZWN0ZWRUcmFuc3BvcnQ8L0F1dGhuQ29udGV4dENsYXNzUmVmPjwvQXV0aG5Db250ZXh0PjwvQXV0aG5TdGF0ZW1lbnQ+PC9Bc3NlcnRpb24+PC9zYW1scDpSZXNwb25zZT4=";

    @Before
    public void setup() throws Exception {
        samlResponse = new SAMLResponseExtractor();
    }

    private Document loadDocument(String file) throws Exception {
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
            Document document = XMLConverter.convertToDocument(inputStream);
            inputStream.close();
            return document;
        } finally {
            Streams.close(inputStream);
        }
    }

    private String encode(String s) {
        return Base64.encodeBase64String(StringUtils.getBytesUtf8(s));
    }

    private String getEncodedIdpResponse(String unsignedResponse)
            throws Exception {
        Document document = loadDocument(unsignedResponse);
        String idpResponse = XMLConverter.convertToString(document, true);
        return encode(idpResponse);
    }

    @Test
    public void getUserId_Response() throws Exception {
        // given the content of an encoded idp response
        String encodedIdpResponse = getEncodedIdpResponse(FILE_UNSIGNED_RESPONSE);

        // when
        String userId = samlResponse.getUserId(encodedIdpResponse);

        // then
        assertEquals("administrator", userId);
    }

    @Test
    public void isFromLogout() throws Exception {
        // when
        boolean fromLogout = samlResponse.isFromLogout(encodedSamlLoginResponseFromADFS);
        assertFalse(fromLogout);

        fromLogout = samlResponse.isFromLogout(encodedSamlLogoutResponseFromADFS);
        assertTrue(fromLogout);
    }

    @Test
    public void isFromLogin() throws Exception {
        boolean fromLogin = samlResponse.isFromLogin(encodedSamlLoginResponseFromADFS);
        assertTrue(fromLogin);

        fromLogin = samlResponse.isFromLogin(encodedSamlLogoutResponseFromADFS);
        assertFalse(fromLogin);
    }

    @Test(expected = UserIdNotFoundException.class)
    public void getUserIdNotFound_Response() throws Exception {
        // given the content of an encoded idp response where the userid
        // attribute is missing in the original content
        String encodedIdpResponse = getEncodedIdpResponse(FILE_UNSIGNED_RESPONSE_MISSING_USERID);

        // when
        samlResponse.getUserId(encodedIdpResponse);

        // then a UserIdNotFoundException is expected
    }

    @Test
    public void getUserId_Assertion() throws Exception {
        // given
        String encodedAssertion = getEncodedIdpResponse(FILE_UNSIGNED_ASSERTION);

        // when
        String userId = samlResponse.getUserId(encodedAssertion);

        // then
        assertEquals("administrator", userId);
    }

    @Test
    public void getIssuerId_Assertion() throws Exception {
        // given
        String encodedAssertion = getEncodedIdpResponse(FILE_UNSIGNED_ASSERTION);

        // when
        String issuerID = samlResponse.getIssuer(encodedAssertion);

        // then
        assertEquals("https://idp.example.org/SAML2", issuerID);
    }

    @Test
    public void getTenantId_Assertion() throws Exception {
        // given
        String encodedAssertion = getEncodedIdpResponse(FILE_UNSIGNED_ASSERTION);

        // when
        String tenantID = samlResponse.getTenantID(encodedAssertion);

        // then
        assertEquals("8f96dede", tenantID);
    }

    @Test(expected = UserIdNotFoundException.class)
    public void getUserIdNotFound_Assertion() throws Exception {
        // given
        String encodedIdpResponse = getEncodedIdpResponse(FILE_UNSIGNED_ASSERTION_MISSING_USERID);

        // when
        samlResponse.getUserId(encodedIdpResponse);

        // then a UserIdNotFoundException is expected
    }

    @Test(expected = UserIdNotFoundException.class)
    public void getUserId_error() throws Exception {
        // given an XPathExpressionException when loading the attributes
        SAMLResponseExtractor samlResponse = spy(new SAMLResponseExtractor());
        doThrow(new XPathExpressionException("")).when(samlResponse)
                .extractUserId(any(Document.class));

        String encodedIdpResponse = getEncodedIdpResponse(FILE_UNSIGNED_RESPONSE);

        // when
        samlResponse.getUserId(encodedIdpResponse);

        // then a UserIdNotFoundException is expected
    }

    @Test
    public void getStatusCodeFromLogoutResponse() throws Exception {
        // when
        String statusCode = samlResponse.getSAMLLogoutResponseStatusCode(encodedSamlLogoutResponseFromADFS);

        // then
        assertEquals("Success", statusCode);
    }

}
