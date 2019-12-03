/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/
package org.oscm.ws.concurrency;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

import org.junit.Before;
import org.junit.Test;
import org.oscm.intf.IdentityService;
import org.oscm.types.enumtypes.OrganizationRoleType;
import org.oscm.types.enumtypes.UserRoleType;
import org.oscm.vo.VOOrganization;
import org.oscm.vo.VOUser;
import org.oscm.vo.VOUserDetails;
import org.oscm.ws.base.MailReader;
import org.oscm.ws.base.ServiceFactory;
import org.oscm.ws.base.WebserviceTestBase;

public class ConcurrencyTest {

  private static final int NUMBER_OF_THREADS = 5;
  private static final int NUMBER_OF_THREAD_EXECUTIONS = 20;
  private static volatile int counter = 0;

  private volatile VOOrganization organization;
  private volatile String organizationAdminKey;

  private final List<Thread> creatorThreads = new CopyOnWriteArrayList<Thread>();
  public final List<String> userKeys = new CopyOnWriteArrayList<String>();
  public final List<String> userIds = new CopyOnWriteArrayList<String>();

  private class CreatorThreadGroup extends ThreadGroup {
    private final Map<Thread, Throwable> uncaughtExceptions =
        Collections.synchronizedMap(new HashMap<Thread, Throwable>());

    public CreatorThreadGroup(String name) {
      super(name);
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
      uncaughtExceptions.put(t, e);
    }

    public Map<Thread, Throwable> getUncaughtExceptions() {
      return uncaughtExceptions;
    }
  }

  private class CreateUserThread extends Thread {
    private final int executionTimes;
    private final String organizationAdminKey;
    private final MailReader mailReader;
    private final String organizationId;
    private final String emailUser;

    public CreateUserThread(
        ThreadGroup threadGroup,
        int executionTimes,
        String organizationId,
        String organizationAdminId,
        String emailUser)
        throws Exception {
      super(threadGroup, "CONCURRENCYTEST_THREAD_" + (counter++));
      this.executionTimes = executionTimes;
      this.organizationId = organizationId;
      this.organizationAdminKey = organizationAdminId;
      this.emailUser = emailUser;
      this.mailReader = new MailReader();
      mailReader.setMailUser(emailUser);
    }

    @Override
    public void run() {
      try {
        IdentityService idSrv =
            ServiceFactory.getDefault()
                .getIdentityService(organizationAdminKey, WebserviceTestBase.DEFAULT_PASSWORD);

        List<String> localUserKeys = new ArrayList<String>();
        for (int i = 0; i < executionTimes; i++) {
          VOUserDetails user = createVoUser();
          VOUserDetails createdUser =
              idSrv.createUser(
                  user,
                  Arrays.asList(UserRoleType.ORGANIZATION_ADMIN, UserRoleType.TECHNOLOGY_MANAGER),
                  null);

          initialPasswordChange(localUserKeys, createdUser);
          System.out.println(getName() + ": created user " + localUserKeys.get(i));
          userIds.add(user.getUserId());
        }

        userKeys.addAll(localUserKeys);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    private VOUserDetails createVoUser() {
      VOUserDetails user = new VOUserDetails();
      String userId = "user_" + getName() + "_" + System.currentTimeMillis();
      user.setUserId(userId);
      user.setOrganizationId(organizationId);
      user.setEMail(emailUser);
      user.setLocale("en");
      return user;
    }

    private void initialPasswordChange(List<String> userKeys, VOUserDetails createdUser)
        throws Exception {
      String[] userKeyAndPass = mailReader.readPassAndKeyFromEmail(createdUser.getUserId());
      String userKey = userKeyAndPass[0];
      String userPwd = userKeyAndPass[1];
      userKeys.add(userKey);
      IdentityService userIdSrv = ServiceFactory.getDefault().getIdentityService(userKey, userPwd);
      userIdSrv.changePassword(userPwd, WebserviceTestBase.DEFAULT_PASSWORD);

      // sometimes WS tests fail at id.changePassword() with
      // "Unauthorized"
      // probably because the new mail arrives later in inbox, so a
      // previous
      // mail is read - ensure this is not happening
      mailReader.deleteMails();
    }
  }

  private class DisturberThread extends Thread {

    public DisturberThread(ThreadGroup threadGroup) {
      super(threadGroup, "CONCURRENCYTEST_DISTURBER_THREAD");
    }

    @Override
    public void run() {
      while (!interrupted()) {
        if (userIds.size() > 0) {
          String userId = pickRandomUser(userIds);
          try {
            IdentityService idSrv =
                ServiceFactory.getDefault()
                    .getIdentityService(organizationAdminKey, WebserviceTestBase.DEFAULT_PASSWORD);

            VOUser user = new VOUser();
            user.setUserId(userId);
            VOUserDetails userDetails = idSrv.getUserDetails(user);
            System.out.println(
                "DISTURBER ("
                    + userId
                    + "): read user information... ("
                    + userDetails.getKey()
                    + ")");
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        }
      }
    }

    private String pickRandomUser(List<String> idOrKeyList) {
      Random r = new Random();
      int index = r.nextInt(idOrKeyList.size());
      return idOrKeyList.get(index);
    }
  }

  @Before
  public void setup() throws Exception {
    WebserviceTestBase.getMailReader().deleteMails();
    String administratorId = "admin_" + System.currentTimeMillis();
    organization =
        WebserviceTestBase.createOrganization(
            administratorId,
            OrganizationRoleType.TECHNOLOGY_PROVIDER,
            OrganizationRoleType.SUPPLIER);
    organizationAdminKey = WebserviceTestBase.readLastMailAndSetCommonPassword(administratorId);
  }

  @Test
  public void concurrencyTest_createUsers() throws Exception {
    final CreatorThreadGroup threadGroup =
        new CreatorThreadGroup("CONCURRENCYTEST_CreationThreads");

    // create creator threads
    for (int i = 1; i <= NUMBER_OF_THREADS; i++) {
      final CreateUserThread thread =
          new CreateUserThread(
              threadGroup,
              NUMBER_OF_THREAD_EXECUTIONS,
              organization.getOrganizationId(),
              organizationAdminKey,
              "bes.testuser" + i + "@dev.est.fujitsu.com");
      creatorThreads.add(thread);
    }

    // create disturber thread
    DisturberThread disturber = new DisturberThread(threadGroup);

    // execute all threads
    disturber.start();
    for (int i = 0; i < NUMBER_OF_THREADS; i++) {
      creatorThreads.get(i).start();
    }

    // wait for threads to die
    try {
      for (int i = 0; i < NUMBER_OF_THREADS; i++) {
        creatorThreads.get(i).join();
      }
      disturber.interrupt();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    // check thread group for uncaught exceptions
    if (!threadGroup.getUncaughtExceptions().isEmpty()) {
      for (Thread t : threadGroup.getUncaughtExceptions().keySet()) {
        Throwable e = threadGroup.getUncaughtExceptions().get(t);
        System.out.println("EXCEPTION FOR THREAD :" + t.getName());
        System.out.println(WebserviceTestBase.convertStacktrace(e));
      }
      fail();
    }
  }
}
