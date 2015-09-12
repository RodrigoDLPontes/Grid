package grid;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;

/**
 * Based on blog post by Jerry Orr
 * (http://blog.jerryorr.com/2012/05/secure-password-storage-lots-of-donts.html)
 */
public class PasswordEncryption {

	public static boolean authenticate(String password) throws IOException, InvalidKeySpecException, NoSuchAlgorithmException {
		File userInfo = new File("C:\\ProgramData\\Grid\\userInfo.cfg");
		FileInputStream fileInputStream = new FileInputStream(userInfo);
		byte[] lastLineBytes = new byte[(int)userInfo.length()];
		fileInputStream.read(lastLineBytes);
		byte[] salt = new byte[8];
		for(int i = 0; i < 8; i++) {
			salt[i] = lastLineBytes[i + (lastLineBytes.length - 28)];
		}
		byte[] encryptedPassword = new byte[20];
		for(int i = 0; i < 20; i++) {
			encryptedPassword[i] = lastLineBytes[i + (lastLineBytes.length - 20)];
		}
		return Arrays.equals(encryptedPassword, PasswordEncryption.getEncryptedPassword(password, salt));
	}

	public static byte[] generateSalt() throws NoSuchAlgorithmException {
		SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
		byte[] salt = new byte[8];
		random.nextBytes(salt);
		return salt;
	}

	public static byte[] getEncryptedPassword(String password, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
		KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, 100000, 160);
		SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		return secretKeyFactory.generateSecret(keySpec).getEncoded();
	}

	public static String toHex(byte[] encodedPassword) {
		BigInteger hex = new BigInteger(1, encodedPassword);
		return hex.toString(16);
	}
}
