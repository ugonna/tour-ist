package com.ugo.android.tourmate.util;

import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import android.util.Base64;

public class UrlSigner {
	
	private static final String SIGNING_ALGORITHM = "HmacSHA1";

	public static String signUrl(String privateKey, URL url) throws NoSuchAlgorithmException,
	InvalidKeyException {
		
		privateKey = privateKey.replace('-', '+');
		privateKey = privateKey.replace('_', '/');
		byte[] encryptedKey = Base64.decode(privateKey, Base64.DEFAULT);
		
		String resource = url.getPath() + url.getQuery();
		SecretKeySpec sha1KeySpec = new SecretKeySpec(encryptedKey, SIGNING_ALGORITHM);
		
		Mac mac = Mac.getInstance(SIGNING_ALGORITHM);
		mac.init(sha1KeySpec);
		
		byte[] sigBytes = mac.doFinal(resource.getBytes());
		String signature = Base64.encodeToString(sigBytes, Base64.DEFAULT);
		signature = signature.replace('+', '-');
		signature = signature.replace('/', '_');
		
		return resource + "&signature=" + signature;
	}
}
