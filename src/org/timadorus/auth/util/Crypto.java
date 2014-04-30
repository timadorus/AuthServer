package org.timadorus.auth.util;

/* 
 * Password Hashing With PBKDF2 (http://crackstation.net/hashing-security.htm).
 * Copyright (c) 2013, Taylor Hornby
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, 
 * this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation 
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 */

import java.security.SecureRandom;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import java.math.BigInteger;

/**
 * PBKDF2 salted password hashing.
 * Author: havoc AT defuse.ca
 * www: http://crackstation.net/hashing-security.htm
 */
public final class Crypto {
  public static final String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA1";

  // The following constants may be changed without breaking existing hashes.
  public static final int SALT_BYTE_SIZE = 24;

  public static final int HASH_BYTE_SIZE = 24;

  public static final int PBKDF2_ITERATIONS = 1000;

  public static final int ITERATION_INDEX = 0;

  public static final int SALT_INDEX = 1;

  public static final int PBKDF2_INDEX = 2;
  
  /**
   * The transformation to perform.
   */
  private static final String AES_TRANSFORMATION = "AES/CTR/NOPADDING";
  /**
   * The size of the secret key to use for encrypting data, in bytes.
   */
  private static final int AES_KEY_SIZE = 16;
  
  /**
   * The size of the initialization vector, in bytes.
   */
  private static final int AES_IV_SIZE = 16;
  
  /**
   * The size of the salt for PBKDF2 key generation.
   */
  private static final int AES_SALT_SIZE = 24;
  
  /**
   * The iteration count for the PBKDF2 key generation.
   */
  private static final int AES_PBKDF2_ITERATIONS = 1000;

  /**
   * Make CheckStyle happy.
   */
  private Crypto() {
  }
  
  /**
   * Returns a salted PBKDF2 hash of the password.
   * 
   * @param password
   *    The password to hash.
   * @return
   *    A salted PBKDF2 hash of the password.
   * @throws IllegalArgumentException
   *    The password parameter is null.
   */
  public static String createHash(String password) {
    if (password == null) {
      throw new IllegalArgumentException("password");
    }
    return createHash(password.toCharArray());
  }

  /**
   * Returns a salted PBKDF2 hash of the password.
   * 
   * @param password
   *    The password to hash.
   * @return
   *    A salted PBKDF2 hash of the password.
   * @throws IllegalArgumentException
   *    The password parameter is null.
   */
  public static String createHash(char[] password) {
    if (password == null) {
      throw new IllegalArgumentException("password");
    }
    // Generate a random salt.
    SecureRandom random = new SecureRandom();
    byte[] salt = new byte[SALT_BYTE_SIZE];
    random.nextBytes(salt);
    // Hash the password.
    try {
      byte[] hash = pbkdf2(password, salt, PBKDF2_ITERATIONS, HASH_BYTE_SIZE);
      // Format iterations:salt:hash.
      return PBKDF2_ITERATIONS + ":" + toHex(salt) + ":" + toHex(hash);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Validates a password using a hash.
   * 
   * @param password
   *     The password to check.
   * @param correctHash
   *     The hash of the valid password.
   * @return
   *     true if the password is correct; Otherwise false.
   * @throws IllegalArgumentException
   *     The password paramter is null, or the correctHash parameter is null.
   */
  public static boolean validatePassword(String password, String correctHash) {
    return validatePassword(password.toCharArray(), correctHash);
  }

  /**
   * Validates a password using a hash.
   * 
   * @param password
   *     The password to check.
   * @param correctHash
   *     The hash of the valid password.
   * @return
   *     true if the password is correct; Otherwise false.
   * @throws IllegalArgumentException
   *     The password paramter is null, or the correctHash parameter is null.
   */
  public static boolean validatePassword(char[] password, String correctHash) {
    // Decode the hash into its parameters.
    String[] params = correctHash.split(":");
    int iterations = Integer.parseInt(params[ITERATION_INDEX]);
    byte[] salt = fromHex(params[SALT_INDEX]);
    byte[] hash = fromHex(params[PBKDF2_INDEX]);
    // Compute the hash of the provided password, using the same salt,
    // iteration count, and hash length.
    try {
      byte[] testHash = pbkdf2(password, salt, iterations, hash.length);
      // Compare the hashes in constant time. The password is correct if
      // both hashes match.
      return slowEquals(hash, testHash);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Compares two byte arrays in length-constant time. This comparison method
   * is used so that password hashes cannot be extracted from an online system
   * using a timing attack and then attacked offline.
   * 
   * @param a
   *    The first byte array.
   * @param b
   *    The second byte array.
   * @return
   *    true if both byte arrays are the same; Otherwise false.
   */
  private static boolean slowEquals(byte[] a, byte[] b) {
    int diff = a.length ^ b.length;
    for (int i = 0; i < a.length && i < b.length; i++) {
      diff |= a[i] ^ b[i];
    }
    return diff == 0;
  }

  /**
   * Computes the PBKDF2 hash of the specified password.
   * 
   * @param password
   *    The password to hash.
   * @param salt
   *    The salt to use.
   * @param iterations
   *    The iteration count (slowness factor).
   * @param bytes
   *    The length of the hash to compute, in bytes.
   * @return
   *    Tthe PBDKF2 hash of the password.
   * @throws IllegalArgumentException
   *  The password parameter is null, or the salt parameter is null, or the
   *  iterations parameter is less than 0, or the bytes parameter is less
   *  than 1.
   */
  private static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int bytes) {
    if (password == null) {
      throw new IllegalArgumentException("password");
    }
    if (salt == null) {
      throw new IllegalArgumentException("salt");
    }
    if (iterations < 0) {
      throw new IllegalArgumentException("iterations");
    }
    if (bytes < 1) {
      throw new IllegalArgumentException("bytes");
    }
    try {
      PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, bytes * 8);
      SecretKeyFactory skf = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM);
      return skf.generateSecret(spec).getEncoded();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Converts the specified string of hexadecimal characters into a byte array.
   * 
   * @param hex
   *    The hex string to convert.
   * @return
   *    The hex string decoded into a byte array.
   * @throws IllegalArgumentException
   *    The hex parameter is null.
   */
  private static byte[] fromHex(String hex) {
    if (hex == null) {
      throw new IllegalArgumentException("hex");
    }
    byte[] binary = new byte[hex.length() / 2];
    for (int i = 0; i < binary.length; i++) {
      binary[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
    }
    return binary;
  }

  /**
   * Converts the specified byte array into a hexadecimal string.
   * 
   * @param array
   *    The byte array to convert.
   * @return
   *    A length*2 character string encoding the byte array.
   * @throws IllegalArgumentException
   *  The array parameter is null.
   */
  private static String toHex(byte[] array) {
    if (array == null) {
      throw new IllegalArgumentException("array");
    }
    BigInteger bi = new BigInteger(1, array);
    String hex = bi.toString(16);
    int paddingLength = (array.length * 2) - hex.length();
    if (paddingLength > 0) {
      return String.format("%0" + paddingLength + "d", 0) + hex;
    } else {
      return hex;
    }
  }
  
  /**
   * Encrypts the specified data using the specified password.
   * 
   * @param data
   *          The data to encrypt.
   * @param password
   *          The password to encrypt the data with.
   * @return The encrypted data, prepended by AES_SALT_SIZE salt bytes
   *         followed by AES_IV_SIZE iv bytes.
   * @throws IllegalArgumentException
   *           The data parameter is null, or the password parameter is null,
   *           or the size of the key parameter is invalid.
   * @throws Exception
   *         An unexpected error occurred.
   */
  public static byte[] aesEncrypt(byte[] data, String password) throws Exception {
    if (data == null) {
      throw new IllegalArgumentException("data");
    }
    if (password == null) {
      throw new IllegalArgumentException("password");
    }
    // Generate a random salt.
    SecureRandom random = new SecureRandom();
    byte[] salt = new byte[AES_SALT_SIZE];
    random.nextBytes(salt);
    // Generate the secret-key for encryption.
    byte[] keyBytes = pbkdf2(password.toCharArray(), salt, AES_PBKDF2_ITERATIONS,
                         AES_KEY_SIZE);
    SecretKey skey = new SecretKeySpec(keyBytes, "AES");
    // Instantiate the crypto provider.
    Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION, "SunJCE");
    cipher.init(Cipher.ENCRYPT_MODE, skey);
    // Generate a random initialization vector.
    IvParameterSpec spec = cipher.getParameters()
        .getParameterSpec(IvParameterSpec.class);
    byte[] iv = spec.getIV();
    if (iv.length != AES_IV_SIZE) {
      throw new IllegalStateException("Unexpected IV size " + iv.length
                                      + ", expected " + AES_IV_SIZE + ".");
    }
    byte[] encrypted = cipher.doFinal(data);
    // Prepend the IV and salt to the encrypted data.
    byte[] ret = new byte[AES_SALT_SIZE + AES_IV_SIZE + encrypted.length];
    System.arraycopy(salt, 0, ret, 0, AES_SALT_SIZE);
    System.arraycopy(iv, 0, ret, AES_SALT_SIZE, AES_IV_SIZE);
    System.arraycopy(encrypted, 0, ret, AES_SALT_SIZE + AES_IV_SIZE,
                     encrypted.length);
    return ret;
  }
  
  /**
   * Decrypts the specified data using the specified password.
   * 
   * @param data
   *          The data to decrypt.
   * @param password
   *          The password to decrypt the data with.
   * @return The decrypted data.
   * @throws IllegalArgumentException
   *           The data parameter is null, or the password parameter is null.
   * @throws Exception
   *           The data could not be decrypted.
   */
  public static byte[] aesDecrypt(byte[] data, String password) throws Exception {
    if (data == null) {
      throw new IllegalArgumentException("data");
    }
    if (password == null) {
      throw new IllegalArgumentException("password");
    }
    // Extract the salt and IV.
    byte[] salt = new byte[AES_SALT_SIZE];
    byte[] iv = new byte[AES_IV_SIZE];
    byte[] encrypted = new byte[data.length - AES_SALT_SIZE - AES_IV_SIZE];
    System.arraycopy(data, 0, salt, 0, AES_SALT_SIZE);
    System.arraycopy(data, AES_SALT_SIZE, iv, 0, AES_IV_SIZE);
    System.arraycopy(data, AES_SALT_SIZE + AES_IV_SIZE, encrypted, 0,
                     encrypted.length);
    // Generate the secret-key for decryption.
    byte[] keyBytes = pbkdf2(password.toCharArray(), salt, AES_PBKDF2_ITERATIONS,
                         AES_KEY_SIZE);
    SecretKey skey = new SecretKeySpec(keyBytes, "AES");
    // Instantiate the crypto provider.
    Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION, "SunJCE");
    cipher.init(Cipher.DECRYPT_MODE, skey, new IvParameterSpec(iv));
    return cipher.doFinal(encrypted);
  }
}
