package org.timadorus.auth.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.timadorus.auth.server.AuthServerTest;
import org.timadorus.auth.sgs.test.Base64Test;
import org.timadorus.auth.sgs.test.TokenAuthenticatorTest;

/**
 * @author sage
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
            // SGS
            TokenAuthenticatorTest.class,
            Base64Test.class,
            
            // Server
            AuthServerTest.class
})
public class AuthServiceSmokeTest {

  /**
   * testDummy
   *
   * This Test serves the sole purpose of having a test in existence, so that the automated build 
   * environment can be set up properly
   * 
   * @throws Exception
   */
  @Test
  public void testDummy() throws Exception {
    // pass 
  }

}
