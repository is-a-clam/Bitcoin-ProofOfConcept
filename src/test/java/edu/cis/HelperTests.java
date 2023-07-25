package edu.cis;

import edu.cis.Utils.Helper;
import edu.cis.Utils.HelperException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

public class HelperTests {

    private Helper helper;

    @Before
    public void setup() {
        helper = Helper.getInstance();
    }

    @Test
    public void HexByteTest() {
        byte[][] byteArrays = {{92, 0, 53, -1, 93},
                               {42, 3, 99, -64}};
        String[] hexes = {"5c0035ff5d", "2a0363c0"};

        for (int i = 0; i < byteArrays.length; i++) {
            Assert.assertEquals(helper.byteToHex(byteArrays[i]), hexes[i]);
        }
        for (int i = 0; i < byteArrays.length; i++) {
            Assert.assertArrayEquals(helper.hexToByte(hexes[i]), byteArrays[i]);
        }
    }

    @Test
    public void SHA256Test() {
        String[] inputs = {"Isaac", "LilCoin", "A rather long message for testing", "hi"};
        String[] hashes = {"63b063f5b637a3e8ced50585a954606c637c30a9211c4dedfd508dedcbd5c060",
                           "83f7d1eeaffa46faaa4b132bd67b3e69fc3d2abfdd1f5fcb29623ed892ed8899",
                           "d3a86c857703371d4fbe14ca323c6faa0aefb959146f63ccb10d08debea50c69",
                           "8f434346648f6b96df89dda901c5176b10a6d83961dd3c1ac88b59b2dc327aa4"};
        for (int i = 0; i < inputs.length; i++) {
            byte[] hash = helper.SHA256(inputs[i].getBytes(StandardCharsets.UTF_8));
            Assert.assertEquals(helper.byteToHex(hash), hashes[i]);
        }
    }

    @Test
    public void RIP160Test() {
        String[] inputs = {"Isaac", "LilCoin", "A rather long message for testing", "hi"};
        String[] hashes = {"a2cd54253b303eacdffabf292b5def8b8a027239",
                           "18a9aba77cc4230ff42ea7535660214025211cf4",
                           "edbac49b582273e7e296467dada61d4c20cb96bc",
                           "242485ab6bfd3502bcb3442ea2e211687b8e4d89"};
        for (int i = 0; i < inputs.length; i++) {
            byte[] hash = helper.RIP160(inputs[i].getBytes(StandardCharsets.UTF_8));
            Assert.assertEquals(helper.byteToHex(hash), hashes[i]);
        }
    }

    @Test
    public void CombineByteArraysTest() {
        String[] messages = {"Isaac", "LilCoin", "A rather long message for testing", "hi"};
        String combinedHex = "49736161634c696c436f696e4120726174686572206c6f6e67206d65737361676520666f722074657374696e676869";
        byte[] bytes = helper.combineByteArrays(messages[0].getBytes(StandardCharsets.UTF_8),
                                                messages[1].getBytes(StandardCharsets.UTF_8),
                                                messages[2].getBytes(StandardCharsets.UTF_8),
                                                messages[3].getBytes(StandardCharsets.UTF_8));
        Assert.assertEquals(helper.byteToHex(bytes), combinedHex);
    }

    @Test
    public void ECDSATest() {
        String[] messages = {"Isaac", "LilCoin", "A rather long message for testing", "hi"};
        for (String message : messages) {
            KeyPair keyPair = helper.generateKeyPair();
            PublicKey publicKey = keyPair.getPublic();
            PrivateKey privateKey = keyPair.getPrivate();
            String signature = helper.generateSig(privateKey, message);
            Assert.assertTrue(helper.verifySig(publicKey, message, signature));
            Assert.assertEquals(helper.hexToPubKey(helper.byteToHex(publicKey.getEncoded())), publicKey);
            Assert.assertEquals(helper.hexToPrivKey(helper.byteToHex(privateKey.getEncoded())), privateKey);
        }
    }

    @Test
    public void HexAddressTest() throws HelperException {
        String[] publicKeys =
                {"3056301006072a8648ce3d020106052b8104000a0342000404aa76e60cca7a17bd517fdf735709b7637609350976dca1c11de677389ef662e4002d77011d862853da1e03d853bb4d981b9a2be452095d230cbee01086a147",
                 "3056301006072a8648ce3d020106052b8104000a034200049a1f1da3f95cc5020c38fd2ff26df2c104dfabfcd97de330e57d30abc65117594b92c9394778514c46372a6a962d7f4b635ffb8f5ad5eab0576a6462be02968a",
                 "3056301006072a8648ce3d020106052b8104000a03420004bc8732a29bd25e79d38f5b53fbeb6fac5a214212a87869754b4c7fb4430c8e45cb898a95def0a5ebc1def5f028207c2b1b91a37c0a042737b1093f49c8caf99c",
                 "3056301006072a8648ce3d020106052b8104000a034200041c6c469f8932d40b49a9a11c7a6e1b9bd67e4bd280d37eeab6aae40e55a5a7da5ab1ea1f811520f908ce960e2780df6da2aedc2a99acc9de9a7db0dce93da8bd",
                 "3056301006072a8648ce3d020106052b8104000a03420004d1bcfcf957aad33bdea34122357143456a2529c9e3126df52f813d3f51b1eab8781638e056070972e6efc60c684291dc1fa3d03d9bc3005a99c44eb04b8b1280"};
        String[] hexAddresses = {"8f73ca6b229dc55e2703bec1235916cce2989543",
                                 "92e7f1a9d109f4ca5dc9b6e965cab7c9d35eedbf",
                                 "3277c40cfd569a6ba177b6b108f3e79e6c05fa9f",
                                 "f822f8735f811c72601c61585c02425a6acbea41",
                                 "a1ce4530cbae8cbdb260ebdda617264ed072cfbb"};
        String[] b58Addresses = {"1E5WLiCv4PDxn46ausX3asd8Zow4wvGEeg",
                                 "1EPmWm1JnciFEoJEuBkyL8CxoVgNWx8p9y",
                                 "15brG4siz18nDyVa7YBH8SbpJUYhnhtDEQ",
                                 "1Pd2UNKdds9dci4J11viGLWE93p5WSieJT",
                                 "1FkYto6RgMEaSk4Z9eVScGEfjAfzHLXTHr"};
        for (int i = 0; i < publicKeys.length; i++) {
            PublicKey publicKey = helper.hexToPubKey(publicKeys[i]);
            Assert.assertEquals(helper.pubKeyToHexAddr(publicKey), hexAddresses[i]);
            Assert.assertEquals(helper.encodeFromHexAddr(hexAddresses[i]), b58Addresses[i]);
            Assert.assertEquals(helper.decodeToHexAddr(b58Addresses[i]), hexAddresses[i]);
        }
    }

    @Test
    public void HexAddressException() {
        String[] badAddresses = {"1E5WLiCv4PDxn46ausc3asd8Zow4wvGEeg",
                                 "1EPmWm1JnciFEokEuBkyL8CxoVgNWx8p9y",
                                 "15brG4siz18nDyVa74BH8SbpJUYhnhtDEQ",
                                 "1Pd2UNKdds9dci4J1oviGLWE93p5WSieJT",
                                 "1FkYto6RgMEaSk4Z9ewScGEfjAfzHLXTHr"};
        for (int i = 0; i < badAddresses.length; i++) {
            try {
                helper.decodeToHexAddr(badAddresses[i]);
                Assert.fail();
            }
            catch (HelperException ignored) { }
        }
    }

    @Test
    public void NumToByteArrayTest() {
        int[] testInputs = {0, -1, 10, 8000, -8000};
        byte[][] answers = {{0, 0, 0, 0}, {-1, -1, -1, -1}, {0, 0, 0, 10}, {0, 0, 31, 64}, {-1, -1, -32, -64}};

        for (int i = 0; i < testInputs.length; i++) {
            byte[] bytes = helper.numToByte(BigInteger.valueOf(testInputs[i]), 4);
            Assert.assertArrayEquals(bytes, answers[i]);
            Assert.assertEquals(new BigInteger(bytes).toString(), testInputs[i]+"");
        }
    }
}
