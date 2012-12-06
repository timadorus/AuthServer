/* -*- java -*- */
/*
 * Filename:          org.timadorus.auth.sgs.test/Base64Test.java
 *                                                                       *
 * Project:           TimadorusAuthServer
 *
 * This file is distributed under the GNU Public License 2.0
 * See the file Copying for more information
 *
 * copyright (c) 2012 Lutz Behnke <lutz.behnke@gmx.de>
 *
 * THE AUTHOR MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE
 * SUITABILITY OF THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR
 * A PARTICULAR PURPOSE, OR NON-INFRINGEMENT. THE AUTHOR SHALL NOT BE
 * LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING,
 * MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 */

package org.timadorus.auth.sgs.test;

import static org.junit.Assert.assertEquals;

import java.nio.ByteBuffer;
import java.util.HashMap;

import org.junit.Test;
import org.timadorus.auth.sgs.Base64;

/** test the base64 implementation.
 * 
 * This does not really test, but run some simple data through.
 * 
 * @author sage
 *
 */
public class Base64Test {

  /**
   * 
   * @throws Exception for any exception not caught by the test.
   */
  @Test
  public void testEncodeDecode() throws Exception {
    byte[] buff = {65, 66, 67};
    ByteBuffer inBuff = ByteBuffer.wrap(buff);
    ByteBuffer encOutBuff = ByteBuffer.allocate((inBuff.capacity()/3)*4);
    
    Base64.encode(inBuff, encOutBuff);
    encOutBuff.rewind();
    ByteBuffer.wrap(Base64.decode(encOutBuff.array()));
    
    
    HashMap<Integer,Long> map = new HashMap<Integer, Long>();
    
    String encodedObject = Base64.encodeObject(map);
    Object decodedObject = Base64.decodeToObject(encodedObject);

    assertEquals(map, decodedObject);

    String encodedObjectZip = Base64.encodeObject(map, Base64.GZIP);
    decodedObject = Base64.decodeToObject(encodedObjectZip);
    
    assertEquals(map, decodedObject);
  
    Base64.decode(Base64.encodeBytesToBytes(buff));
    
  }

}
