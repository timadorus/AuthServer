package org.timadorus.auth.test;

import junit.framework.Test;

import org.eclipse.hyades.test.common.junit.DefaultTestArbiter;
import org.eclipse.hyades.test.common.junit.HyadesTestCase;
import org.eclipse.hyades.test.common.junit.HyadesTestSuite;

/**
 * Generated code for the test suite <b>AuthServiceSmokeTest</b> located at
 * <i>/TimadorusAuthServer/test-src/org/timadorus/auth/test/AuthServiceSmokeTest.testsuite</i>.
 */
public class AuthServiceSmokeTest extends HyadesTestCase {
  /**
   * Constructor for AuthServiceSmokeTest.
   * @param name
   */
  public AuthServiceSmokeTest(String name) {
    super(name);
  }

  /**
   * Returns the JUnit test suite that implements the <b>AuthServiceSmokeTest</b>
   * definition.
   */
  public static Test suite() {
    HyadesTestSuite authServiceSmokeTest = new HyadesTestSuite("AuthServiceSmokeTest");
    authServiceSmokeTest.setArbiter(DefaultTestArbiter.INSTANCE).setId("A1DFB696417090D0CECAB46563353531");

    return authServiceSmokeTest;
  }

  /**
   * @see junit.framework.TestCase#setUp()
   */
  protected void setUp() throws Exception {
  }

  /**
   * @see junit.framework.TestCase#tearDown()
   */
  protected void tearDown() throws Exception {
  }

  /**
   * testDummy
   *
   * This Test serves the sole purpose of having a test in existence, so that the automated build environment can be set up properly
   * 
   * @throws Exception
   */
  public void testDummy() throws Exception {
    // pass 
  }

}
