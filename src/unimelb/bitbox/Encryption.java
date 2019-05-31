/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unimelb.bitbox;

import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import org.bouncycastle.jce.provider.BouncyCastleProvider;


/**
 *
 * @author Administrator
 */
public class Encryption {
     

    
    public static String RSAencrypt(String str,  RSAPublicKey pubKey) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException {

        

                Cipher cipher = Cipher.getInstance("RSA");
         
		cipher.init(Cipher.ENCRYPT_MODE, pubKey);
		String outStr = Base64.getEncoder().encodeToString(cipher.doFinal(str.getBytes("UTF-8")));
                byte[] s = str.getBytes("UTF-8");
                for(int i  =0; i< str.length(); i++)
                    System.out.println(s[i]);
		return outStr;

    }
    
    public static String RSAdecrypt(String str, RSAPrivateKey privateKey) throws Exception{


                byte[] inputByte = Base64.getDecoder().decode(str);
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.DECRYPT_MODE, privateKey);
    
   
		String outStr = new String(cipher.doFinal(inputByte));
                System.out.println("结束" + outStr);
		return outStr;
	}
    
    public static String AESencrypt(String str, String key ) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException {

		Key aesKey = new SecretKeySpec(key.getBytes(), "AES");
	
		Cipher cipher = Cipher.getInstance("AES");
			// Perform encryption
		cipher.init(Cipher.ENCRYPT_MODE, aesKey);
		byte[] encrypted = cipher.doFinal(str.getBytes("UTF-8"));
		return new String(encrypted);
}
    
    public static String AESdecrypt(String str, String key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
 
    		Key aesKey = new SecretKeySpec(key.getBytes(), "AES");
			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.DECRYPT_MODE, aesKey);
			message = new String(cipher.doFinal(Base64.getDecoder().decode(message.getBytes())));
			System.err.println("Decrypted message: "+message);
    }
