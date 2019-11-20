/*
 * Copyright 2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the "Software"), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify,
 * merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package hsm.hsm;

import com.cavium.cfm2.CFM2Exception;
import com.cavium.cfm2.Util;
import com.cavium.key.CaviumKey;
import com.cavium.key.parameter.CaviumRSAKeyGenParameterSpec;

import java.io.IOException;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Random;

/**
 * This sample demonstrates high performance signing. Several threads are used to sign random data blobs. This
 * sample contrasts two methods of signing. The recommended method passes a Key object, where the inefficient method
 * passes a KeyStore and uses getKeyByHandle() to load the key in a loop.
 *
 * This sample relies on implicit login credentials.
 * https://docs.aws.amazon.com/cloudhsm/latest/userguide/java-library-install.html#java-library-credentials
 */
public class SignRunner {
	
	public static String str="Test cloudhsm signing";
	public static byte[] bytes =str.getBytes();

   
    public static void main(String[] args) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, NoSuchProviderException {
        try {
            Security.addProvider(new com.cavium.provider.CaviumProvider());
        } catch (IOException ex) {
            System.out.println(ex);
            return;
        }

        // We need a key pair that can be used to sign the blobs.
        KeyPair pair = generateKeyPair(2048, "Test Signing Key");

        // In a production application we would search the KeyStore for a key that already exists in the HSM.
        KeyStore keyStore = null;
        try {
            keyStore = KeyStore.getInstance("Cavium");
            keyStore.load(null, null);
        } catch (KeyStoreException | CertificateException ex) {
            ex.printStackTrace();
            return;
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }

        // Now pull our signing key out of the keystore using the key's label.
        // It is possible to retrieve a key by key handle as well. This method is covered in the KeyUtilitiesRunner.
        Key signingKey = null;
        try {
            signingKey = keyStore.getKey("Test Signing Key", null);
        } catch (NoSuchAlgorithmException ex) {
            ex.printStackTrace();
            return;
        } catch (UnrecoverableKeyException | KeyStoreException ex) {
            ex.printStackTrace();
            return;
        }
        
        System.out.println("calling sign method");
        byte[] signaturestr = doSign((PrivateKey)signingKey);
        System.out.println(signaturestr);
        PublicKey publicKey = pair.getPublic();
        System.out.println(verifySignature(signaturestr,publicKey ,str));
        System.out.println("Work completed");
    }

       
    private static byte[] doSign(PrivateKey signingKey) {
        try {
            Signature signatureInstance = Signature.getInstance("SHA512withRSA/PSS", "Cavium");
            signatureInstance.initSign(signingKey);
            signatureInstance.update(bytes);
            return signatureInstance.sign();
        } catch (SignatureException ex) {
            ex.printStackTrace();
        } catch (NoSuchProviderException | InvalidKeyException | NoSuchAlgorithmException ex) {
            ex.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
    
    private static boolean verifySignature(byte[] signaturestr, PublicKey publicKey, String stuffToSign) {
    	try {
    		 Signature signature = Signature.getInstance("SHA512withRSA/PSS", "Cavium");
             signature.initVerify(publicKey);
             signature.update(stuffToSign.getBytes());
             return signature.verify(signaturestr);		
    	}catch (SignatureException ex) {
            ex.printStackTrace();
        } catch (NoSuchProviderException | InvalidKeyException | NoSuchAlgorithmException ex) {
            ex.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
		return false;
    }

    /**
     * Generate a key pair that can be used to sign.
     * Only return the private key since this is a demo and that is all we need.
     * @param keySizeInBits
     * @param keyLabel
     * @return KeyPair that is not extractable or persistent.
     */
    private static KeyPair generateKeyPair(int keySizeInBits, String keyLabel)
            throws InvalidAlgorithmParameterException,
            NoSuchAlgorithmException,
            NoSuchProviderException {
        KeyPairGenerator keyPairGen;

        // Create and configure a key pair generator
        keyPairGen = KeyPairGenerator.getInstance("rsa", "Cavium");
        keyPairGen.initialize(new CaviumRSAKeyGenParameterSpec(keySizeInBits, new BigInteger("65537"), keyLabel + ":public", keyLabel, false, false));

        // Generate the key pair
        return keyPairGen.generateKeyPair();
    }
}