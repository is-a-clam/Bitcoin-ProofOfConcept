package edu.cis.Utils;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.*;

import edu.cis.Model.Byteable;
import org.bouncycastle.crypto.digests.RIPEMD160Digest;

/**
 * Singleton Class that provides helper method for cryptographic functions
 *
 * @author Isaac Lam
 * @version 1.0
 */
public class Helper {

    private static Helper instance = null;

    private Helper() { }

    /**
     * Returns singleton instance (thread safe)
     *
     * @return singleton instance
     */
    public static Helper getInstance() {
        if (instance == null) {
            synchronized (Helper.class) {
                if (instance == null) {
                    instance = new Helper();
                }
            }
        }
        return instance;
    }

    /**
     * Generates a random public and private ECDSA key pair, using the curve secp256k1
     *
     * @return a KeyPair object which holds the generated public and private key pair
     */
    public KeyPair generateKeyPair() {
        try {
            ECGenParameterSpec ecSpec = new ECGenParameterSpec("secp256k1");
            KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
            generator.initialize(ecSpec, new SecureRandom());
            KeyPair keypair = generator.generateKeyPair();
            return keypair;
        }
        catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Generates an ECDSA signature from a private key and a message
     *
     * @param privateKey the private key used to generate the signature
     * @param message the message used to generate the signature
     * @return the ECDSA signature
     */
    public String generateSig(PrivateKey privateKey, String message) {
        try {
            Signature ecdsaSign = Signature.getInstance("SHA256withECDSA");
            ecdsaSign.initSign(privateKey);
            ecdsaSign.update(message.getBytes(StandardCharsets.UTF_8));
            byte[] signature = ecdsaSign.sign();
            return byteToHex(signature);
        }
        catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Verifies an ECDSA signature is valid, given a public key and the message used the generate the signature
     *
     * @param publicKey the public key used the verify the signature
     * @param message the message used to generate the signature
     * @param signature the signature that is being verified
     * @return a boolean representing whether the signature is valid or not
     */
    public boolean verifySig(PublicKey publicKey, String message, String signature) {
        try {
            Signature ecdsaVerify = Signature.getInstance("SHA256withECDSA");
            ecdsaVerify.initVerify(publicKey);
            ecdsaVerify.update(message.getBytes(StandardCharsets.UTF_8));
            return ecdsaVerify.verify(hexToByte(signature));
        }
        catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Converts a hex string to a public key object
     *
     * @param hex the hex string to be converted
     * @return the public key object
     */
    public PublicKey hexToPubKey(String hex) {
        try {
            EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(hexToByte(hex));
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            return keyFactory.generatePublic(publicKeySpec);
        }
        catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Converts a hex string to a private key object
     *
     * @param hex the hex string to be converted
     * @return the private key object
     */
    public PrivateKey hexToPrivKey(String hex) {
        try {
            EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(hexToByte(hex));
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            return keyFactory.generatePrivate(privateKeySpec);
        }
        catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Converts a public key into an address, using the hash160 hashing algorithm
     *
     * @param publicKey the public key to be converted
     * @return the address created from the public key
     */
    public String pubKeyToHexAddr(PublicKey publicKey) {
        byte[] address = RIP160(SHA256(publicKey.getEncoded()));
        return byteToHex(address);
    }

    /**
     * Encodes a hash160 address using base58Check which is an encoding that attempts to reduce typos by removing
     * visually similar letters and numbers, and adds a checksum at the end which would make the address invalid if a
     * typo is made.
     *
     * @param addr the address in hash160 form
     * @return the encoded address in base58Check
     */
    public String encodeFromHexAddr(String addr) {
        String ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz";

        String verAddr = "00" + addr;
        String checksum = byteToHex(SHA256(SHA256(hexToByte(verAddr))));
        String fullAddr = verAddr + checksum.substring(0, 8);

        BigInteger hashInteger = new BigInteger(hexToByte(fullAddr));
        StringBuilder base58String = new StringBuilder();
        while (hashInteger.compareTo(BigInteger.ZERO) > 0) {
            int remainder = hashInteger.mod(BigInteger.valueOf(58)).intValue();
            base58String.append(ALPHABET.charAt(remainder));
            hashInteger = hashInteger.divide(BigInteger.valueOf(58));
        }
        for (byte aByte : hexToByte(fullAddr)) {
            if (aByte == 0x00) {
                base58String.append("1");
            }
            else {
                break;
            }
        }
        return base58String.reverse().toString();
    }

    /**
     * Decodes a base58Check address into a hash160 address, which is the address used behind the scenes.
     *
     * @param addr the address in base58Check form
     * @return the decoded hash160 address
     * @throws HelperException when input address is malformed (ie. checksum does not match content)
     */
    public String decodeToHexAddr(String addr) throws HelperException {
        String ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz";

        BigInteger hashInteger = BigInteger.ZERO;
        for (int i = 0; i < addr.length(); i++) {
            hashInteger = hashInteger.multiply(BigInteger.valueOf(58));
            hashInteger = hashInteger.add(BigInteger.valueOf(ALPHABET.indexOf(addr.charAt(i))));
        }
        String fullAddr = hashInteger.toString(16);
        for (char c : addr.toCharArray()) {
            if (c == '1') {
                fullAddr = "00" + fullAddr;
            }
            else {
                break;
            }
        }

        // Make sure checksum matches address
        String hexAddr = fullAddr.substring(0, fullAddr.length() - 8);
        String checksum = fullAddr.substring(fullAddr.length()-8);
        String checksumFromAddr = byteToHex(SHA256(SHA256(hexToByte(hexAddr)))).substring(0, 8);
        if (!checksumFromAddr.equals(checksum)) {
            throw new HelperException("decodeToHexAddr", "input address is malformed");
        }

        return hexAddr.substring(2);
    }

    /**
     * Takes in a variable input of byte arrays and combine them into a single array
     *
     * @param arrays byte arrays to be combined
     * @return the combined byte array
     */
    public byte[] combineByteArrays(byte[] ...arrays) {
        int size = 0;
        for (byte[] array : arrays) {
            size += array.length;
        }
        byte[] fullByteArray = new byte[size];
        ByteBuffer fullArray = ByteBuffer.wrap(fullByteArray);
        for (byte[] array : arrays) {
            fullArray.put(array);
        }
        return fullByteArray;
    }

    /**
     * Takes in a Byteable object (an object which can be turned into a byte array), and performs SHA256 twice to get
     * the hash of the object
     *
     * @param object the object to be hashed
     * @return the resulting hash
     */
    public String getHash(Byteable object) {
        return byteToHex(SHA256(SHA256(object.getBytes())));
    }

    /**
     * Performs the SHA256 hashing algorithm on a byte array
     *
     * @param input the byte array to be hashed
     * @return a byte array of the resulting hash
     */
    public byte[] SHA256(byte[] input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(input);
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Performs the RIPEMD160 hashing algorithm on a byte array
     *
     * @param input the byte array to be hashed
     * @return a byte array of the resulting hash
     */
    public byte[] RIP160(byte[] input) {
        RIPEMD160Digest digest = new RIPEMD160Digest();
        digest.update(input, 0, input.length);
        byte[] output = new byte[digest.getDigestSize()];
        digest.doFinal(output, 0);
        return output;
    }

    /**
     * Converts a byte array into a hex string
     *
     * @param input the byte array to be converted
     * @return the resulting hex string
     */
    public String byteToHex(byte[] input) {
        StringBuilder hexString = new StringBuilder();
        for (byte aByte : input) {
            String hex = Integer.toHexString(0xff & aByte);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    /**
     * Converts a hex string into a byte array
     *
     * @param hexString the hex string to be converted
     * @return the resulting byte array
     */
    public byte[] hexToByte(String hexString) {
        byte[] bytes = new byte[hexString.length() / 2];
        for (int i = 0; i < hexString.length(); i += 2) {
            int firstDigit = Character.digit(hexString.charAt(i), 16);
            int secondDigit = Character.digit(hexString.charAt(i+1), 16);
            bytes[i / 2] = (byte) ((firstDigit << 4) + secondDigit);
        }
        return bytes;
    }

    /**
     * Converts a number to a byte array, along with padding.
     *
     * @param number the number to be converted
     * @param max_size the maximum size of the byte array (for padding)
     * @return the byte array
     */
    public byte[] numToByte(BigInteger number, int max_size) {
        byte[] bytes = new byte[max_size];
        byte[] unpadded = number.toByteArray();
        System.arraycopy(unpadded, 0, bytes, max_size - unpadded.length, unpadded.length);
        // Sign extension for negative numbers
        byte filler = (byte) ((0x80 & unpadded[0]) == 0x80 ? -1 : 0);
        for (int i = 0; i < max_size - unpadded.length; i++) {
            bytes[i] = filler;
        }
        return bytes;
    }
}
