package securejavaee;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class RSA_Main {
        public static void main(String[] args) throws Exception {

            //Generate Message Secret Key to pass via RSA for Encryption
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(128, new SecureRandom());
            SecretKey secretKeyAES = keyGenerator.generateKey();
            System.out.println("Initial AES key: " + new String(secretKeyAES.getEncoded()));


            String secretMessage =  "Space, the final frontier, these are the voyages of Star Trek";
            byte[] encryptedMessage = encrypt(secretKeyAES, secretMessage);
            System.out.println("Alices Encypted Secret Message: " + encryptedMessage +"\n");

             //RSA Public/Private
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048, new SecureRandom());
            KeyPair keyPair = keyPairGenerator.genKeyPair();
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");

            //RSA - Encrypt The SecretKey
            cipher.init(Cipher.ENCRYPT_MODE, keyPair.getPublic());
            byte[] blowfishKeyBytes = secretKeyAES.getEncoded();
            byte[] encryptedBlowfishKey = cipher.doFinal(blowfishKeyBytes);
            System.out.print("RSA Encrypted Blowfish SecretKey: ");
            System.out.println(new String(encryptedBlowfishKey));

            //RSA - Decrypt The SecretKey
            cipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());
            byte[] decryptedKeyBytes = cipher.doFinal(encryptedBlowfishKey);
            SecretKey decryptedSecretKeyAES = new SecretKeySpec(decryptedKeyBytes, "AES");
            System.out.println("RSA Decrypted AES Key: " + new String(decryptedSecretKeyAES.getEncoded()));

            String decryptedMessage = decrypt(decryptedSecretKeyAES, encryptedMessage );
            System.out.println("Bobs Decrypted Secret Message: " + decryptedMessage.toString() +"\n");
            
        }

    private static byte[] encrypt(Key sharedKey, String message) throws IllegalBlockSizeException,
            BadPaddingException, NoSuchAlgorithmException,
            NoSuchPaddingException, InvalidKeyException,
            UnsupportedEncodingException {


        // Get a cipher object -remember that cipher algorythm must match key algorythm
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, sharedKey );

        // Gets the raw bytes to encrypt, UTF8 is needed for
        // having a standard character set
        byte[] stringBytes = message.getBytes("UTF8");

        // encrypt using the cypher
        byte[] raw = cipher.doFinal(stringBytes);

        return raw;
    }


    private static String decrypt(Key sharedKey, byte[] encrypted) throws InvalidKeyException,
            NoSuchAlgorithmException, NoSuchPaddingException,
            IllegalBlockSizeException, BadPaddingException, IOException {

        // Get a cipher object -remember that cipher algorythm must match key algorythm
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, sharedKey);

        //decode the message
        byte[] stringBytes = cipher.doFinal(encrypted);

        //converts the decoded message to a String
        String clear = new String(stringBytes, "UTF8");
        return clear;
    }
    


}