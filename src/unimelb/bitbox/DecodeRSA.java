/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unimelb.bitbox;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicInteger;
import sun.security.util.DerInputStream;
import sun.security.util.DerValue;

/**
 *
 * @author Administrator
 */
public class DecodeRSA {
    private static int SIZEOF_INT = 4;
   //private static String key1 = "AAAAB3NzaC1yc2EAAAADAQABAAABAQClAxT5S/WuX04OXBt9R59WcL45OmaU3M5U063lfyja7ovqaVR7/2kHtLF/LoCQCXSZMny8RTCGDjoXD7G/tGsyHFDHCI//Y1VDLE06AlDzrlu69DQY91+6gkhGjH3SF6us5hXlihrbSFLfAlSdkEs8gwSrspVQyuaOf+39dnMddhEDYYg+z0ce82ta/n8xPBWCp60nDEDayNjOsRgzDJKSujNfngjQTL1x6qKJj8BW/P5lLJE1nbMm9BQD9G7glJk86qh1I/tJCnij6On0m6KcdzVz8cU3sBgNeB433kGjJtpxXXmJB6Vuu5IverhyfpiB4hP9WlKa/LSzW+ZIdvl/";
    
    public static RSAPublicKey generatePub(String key1) throws Exception {
    byte[] decoded = java.util.Base64.getDecoder().decode(key1);

    try {
        ByteBuffer byteBuffer = ByteBuffer.wrap(decoded);

        AtomicInteger position = new AtomicInteger();
        //first read algorithm, should be ssh-rsa
        String algorithm = readString(byteBuffer, position);
//        System.out.println(algorithm);
        assert "ssh-rsa".equals(algorithm);
        // than read exponent
        BigInteger publicExponent = readMpint(byteBuffer, position);
//        System.out.println("publicExponent = " + publicExponent);
        // than read modulus
        BigInteger modulus = readMpint(byteBuffer, position);
//        System.out.println("modulus = " + modulus);
        RSAPublicKeySpec keySpec = new RSAPublicKeySpec(modulus, publicExponent);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PublicKey publicKey = kf.generatePublic(keySpec);

//        System.out.printf("Modulus: %X%n", modulus);
//        System.out.printf("Public exponent: %d ... 17? Why?%n", publicExponent);
//        System.out.printf("See, Java class result: %s, is RSAPublicKey: %b%n", publicKey.getClass().getName(), publicKey instanceof RSAPublicKey);

        byte[] pubBytes = publicKey.getEncoded();
        
         return (RSAPublicKey) publicKey;  


    } catch (Exception e) {
        e.printStackTrace();
    }
        return null;

}

private static BigInteger readMpint(ByteBuffer buffer, AtomicInteger pos){
    byte[] bytes = readBytes(buffer, pos);
    if(bytes.length == 0){
        return BigInteger.ZERO;
    }
    return new BigInteger(bytes);
}

private static String readString(ByteBuffer buffer, AtomicInteger pos){
    byte[] bytes = readBytes(buffer, pos);
    if(bytes.length == 0){
        return "";
    }
    return new String(bytes, StandardCharsets.US_ASCII);
}

private static byte[] readBytes(ByteBuffer buffer, AtomicInteger pos){
    int len = buffer.getInt(pos.get());
    byte buff[] = new byte[len];
    for(int i = 0; i < len; i++) {
        buff[i] = buffer.get(i + pos.get() + SIZEOF_INT);
    }
    pos.set(pos.get() + SIZEOF_INT + len);
    return buff;
}

public static RSAPrivateKey generatePriByPath(String path) throws Exception {
    
        FileReader reader = new FileReader(path);
        BufferedReader br = new BufferedReader(reader);
        String content = "";
        String line;
        while((line = br.readLine()) != null)
            content += line;
        content = content.replaceAll("\\n", "").replace("-----BEGIN RSA PRIVATE KEY-----", "")
                .replace("-----END RSA PRIVATE KEY-----", "");
      
        byte[] bytes = Base64.getDecoder().decode(content);
 
        DerInputStream derReader = new DerInputStream(bytes);
        DerValue[] seq = derReader.getSequence(0);
        // skip version seq[0];
        BigInteger modulus = seq[1].getBigInteger();
        BigInteger publicExp = seq[2].getBigInteger();
        BigInteger privateExp = seq[3].getBigInteger();
        BigInteger prime1 = seq[4].getBigInteger();
        BigInteger prime2 = seq[5].getBigInteger();
        BigInteger exp1 = seq[6].getBigInteger();
        BigInteger exp2 = seq[7].getBigInteger();
        BigInteger crtCoef = seq[8].getBigInteger();
 
        RSAPrivateCrtKeySpec keySpec =
                new RSAPrivateCrtKeySpec(modulus, publicExp, privateExp, prime1, prime2, exp1, exp2, crtCoef);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        RSAPrivateKey privateKey = (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
        
        return privateKey;
    }

}
