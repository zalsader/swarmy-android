package io.particle.android.sdk.utils;

import android.annotation.SuppressLint;

import com.google.common.io.BaseEncoding;

import org.apache.commons.lang3.CharEncoding;

import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;


@ParametersAreNonnullByDefault
public class Crypto {


    public static class CryptoException extends Exception {

        public CryptoException(Throwable cause) {
            super(cause);
        }
    }


    private static final TLog log = TLog.get(Crypto.class);


    public static PublicKey readPublicKeyFromHexEncodedDerString(String hexBytes)
            throws CryptoException {
        byte[] rawBytes = BaseEncoding.base16().decode(hexBytes);
        return buildPublicKey(rawBytes);
    }

    public static String encryptAndEncodeToHex(String inputString, PublicKey publicKey)
            throws CryptoException {
        byte[] asBytes = null;
        Charset utf8 = Charset.forName(CharEncoding.UTF_8);
        asBytes = inputString.getBytes(utf8);
        byte[] encryptedBytes = encryptWithKey(asBytes, publicKey);
        String hex = BaseEncoding.base16().encode(encryptedBytes);
        // forcing lowercase here because of a bug in the early firmware that didn't accept
        // hex encoding in uppercase
        return hex.toLowerCase();
    }

    static byte[] encryptWithKey(byte[] inputData, PublicKey publicKey) throws CryptoException {
        try {
            @SuppressLint("GetInstance")  // the warning doesn't apply to how we're using this
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            return cipher.doFinal(inputData);

        } catch (NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException
                | InvalidKeyException | BadPaddingException e) {
            log.e("Error while encrypting bytes: ", e);
            throw new CryptoException(e);
        }
    }

    static PublicKey buildPublicKey(byte[] rawBytes) throws CryptoException {
        X509EncodedKeySpec spec = new X509EncodedKeySpec(rawBytes);
        KeyFactory kf = getRSAKeyFactory();
        try {
            return kf.generatePublic(spec);
        } catch (InvalidKeySpecException e) {
            throw new CryptoException(e);
        }
    }

    static KeyFactory getRSAKeyFactory() {
        try {
            return KeyFactory.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            // I'm ignoring this.  There isn't going to be an Android
            // implementation without RSA.  (In fact, I'm fairly certain
            // that the CDD *requires* it.)
            throw new IllegalStateException(
                    "This should be impossible, but there is no RSA impl on this device", e);
        }
    }


}
