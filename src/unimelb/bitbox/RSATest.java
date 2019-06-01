/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unimelb.bitbox;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;
import java.util.Base64;
import java.util.Scanner;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import sun.security.util.DerInputStream;
import sun.security.util.DerValue;
import unimelb.bitbox.util.Configuration;


/**
 *
 * @author Administrator
 */
public class RSATest {
    protected static RSAPrivateKey myPrivateKey ;
    private static Socket socket;
    private static TCPserver TCPserver;
    public static void main(String args[]) throws NoSuchAlgorithmException, FileNotFoundException, IOException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException, Exception {
        
//         System.out.println(DecodeRSA.convertkey(Configuration.getConfigurationValue("authorized_keys").split(" ")[1]));
//         try  (FileReader reader = new FileReader("pkcs8_private.pem");
//              BufferedReader br = new BufferedReader(reader);)
//                  {
//                      String line;
//                      while((line = br.readLine()) != null)
//                          myPrivateKey += line;
//                      
//                  System.out.println(myPrivateKey);
//                 }
          myPrivateKey =  DecodeRSA.generatePriByPath("./id_rsa");
          
          Scanner s = new Scanner(System.in);
          String aaa = s.nextLine();
//          System.out.println(Configuration.getConfigurationValue("authorized_keys") + "\n" + "\n" + "\n");
//      System.out.println(Configuration.getConfigurationValue("authorized_keys").replace("ssh-rsa ","").replace(" Kevin", ""));
//          System.out.println("这么多呢" + RSAEncrypt.encrypt(aaa, DecodeRSA.convertkey(Configuration.getConfigurationValue("authorized_keys").split(" ")[1])));
//          out.write(RSAEncrypt.encrypt(aaa, DecodeRSA.convertkey(Configuration.getConfigurationValue("authorized_keys").split(" ")[1]))+ "\n");
//          out.flush();
System.out.println("这是加密前"+aaa);
String AESkey = Encryption.AESkey();
//String enstr = RSAEncrypt.encrypt(aaa, DecodeRSA.convertkey(Configuration.getConfigurationValue("authorized_keys").split(" ")[1]));
String enstr = Encryption.RSAencrypt(aaa, DecodeRSA.generatePub(Configuration.getConfigurationValue("authorized_keys").split(" ")[1]));
String aestr = Encryption.AESencrypt(aaa, AESkey);
        System.out.println(aestr);
//        System.out.println("this is public key  " + DecodeRSA.generatePub(Configuration.getConfigurationValue("authorized_keys").split(" ")[1]));
String destr = Encryption.RSAdecrypt(enstr, myPrivateKey);
String daestr = Encryption.AESdecrypt(aestr, AESkey);
        System.out.println("这是解密后RSA" + destr);
        System.out.println("这是解密后AES" + daestr);


//        System.out.println("这是de" + destr);
        
    }
//    public static RSAPrivateKey readPrivateKeyPKCS8PEM(String path) throws Exception {
//        String content = new String(
//                Files.readAllBytes(Paths.get(path)));
//        content = content.replace("-----BEGIN PRIVATE KEY-----", "")
//                .replace("-----END PRIVATE KEY-----", "").replace("\n", "");
//        System.out.println("'" + content + "'");
//        byte[] bytes = Base64.getDecoder().decode(content);
// 
//        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(bytes);
//        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
//        RSAPrivateKey privateKey = (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
//        return privateKey;
//    }
  
     

}
