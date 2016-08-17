package securejavaee;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyAgreement;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.interfaces.DHPrivateKey;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class ALessonOne_DH {
    public static void main(String[] args) throws Exception {

        //STEP 1 - Both Alice and Bob agree on a modulus and base
        String s = "F488FD584E49DBCD20B49DE49107366B336C380D451D0F7C88"
                + "11111111111111111111111111111111111111111111111111"
                + "11111111111111111111111111111111111111111111111111"
                + "11111111111111111111111111111111111111111111111111"
                + "11111111111111111111111111111111111111111111111111" + "2F78C7";

        BigInteger base = BigInteger.valueOf(2);
        BigInteger modulous = new BigInteger(s, 16);
        javax.crypto.spec.DHParameterSpec sharedModulusAndBase = new DHParameterSpec(modulous, base);

        //ALICE: STEP 2 - Generate public key from your private key based on agreed upon modulus/prime
        KeyPairGenerator aliceKeyPairGenerator = KeyPairGenerator.getInstance("DH");
        aliceKeyPairGenerator.initialize(sharedModulusAndBase, new SecureRandom());
        KeyPair aliceKeyPair = aliceKeyPairGenerator.generateKeyPair();
        KeyAgreement aliceKeyAgreement = KeyAgreement.getInstance("DH");
        DHPrivateKey alicePrivateKey = (DHPrivateKey) aliceKeyPair.getPrivate();
        DHPublicKey alicePublicKey = (DHPublicKey) aliceKeyPair.getPublic();
        aliceKeyAgreement.init(alicePrivateKey);
        System.out.println("Alices Public Key: " + alicePublicKey.getY());

        //BOB: STEP 2 - Generate public key from your private key based on agreed upon modulus/prime
        //TODO: Fix Bob's Code
        
        
        
        
        
        
        
        
        //Alice STEP 3 - using Bob's public result of his secret number with modulus/prime arrives at the shared key
        aliceKeyAgreement.doPhase(bobPublicKey, true);
        SecretKey alicesDHSharedKey = aliceKeyAgreement.generateSecret("DES");

        // Bob STEP 3 - using Alices's public result of her secret number with modulus/prime arrives at the shared key
        bobKeyAgreement.doPhase(alicePublicKey, true);
        SecretKey bobsDHSharedKey = bobKeyAgreement.generateSecret("DES");

        //Alice STEP 4 - Send Bob a Secret Message with the shared key
        System.out.println("\nAlice and Bob have a shared key:" + alicesDHSharedKey.equals(bobsDHSharedKey) + "\n");

        String secretMessage =  "Space, the final frontier, these are the voyages of Star Trek";
        String encryptedMessage = encrypt(alicesDHSharedKey, secretMessage);
        System.out.println("Alices Encypted Secret Message: " + encryptedMessage +"\n");

        String decryptedMessage = decrypt(bobsDHSharedKey, encryptedMessage );
        System.out.println("Bobs Decrypted Secret Message: " + decryptedMessage.toString() +"\n");


    }

    private static String encrypt(Key sharedKey, String message) throws IllegalBlockSizeException,
            BadPaddingException, NoSuchAlgorithmException,
            NoSuchPaddingException, InvalidKeyException,
            UnsupportedEncodingException {
        // Get a cipher object.
        Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, sharedKey);  //Must be initialized with key

        // Gets the raw bytes to encrypt, UTF8 is needed for
        // having a standard character set
        byte[] stringBytes = message.getBytes("UTF8");

        // encrypt using the cypher
        byte[] raw = cipher.doFinal(stringBytes);

        // converts to base64 for easier display.
        BASE64Encoder encoder = new BASE64Encoder();
        String base64 = encoder.encode(raw);

        return base64;
    }


    private static String decrypt(Key sharedKey, String encrypted) throws InvalidKeyException,
            NoSuchAlgorithmException, NoSuchPaddingException,
            IllegalBlockSizeException, BadPaddingException, IOException {

        // Get a cipher object.
        Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, sharedKey);

        //decode the BASE64 coded message
        BASE64Decoder decoder = new BASE64Decoder();
        byte[] raw = decoder.decodeBuffer(encrypted);

        //decode the message
        byte[] stringBytes = cipher.doFinal(raw);

        //converts the decoded message to a String
        String clear = new String(stringBytes, "UTF8");
        return clear;
    }
}


	
	
