package com.epay.ewallet.store.daesang.security;

import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Qualifier("CryptAesService")
public class CryptAesServiceImpl extends CryptService {
	
	private static final Logger log = LogManager.getLogger(CryptAesServiceImpl.class);

	public final String encrypt(String input, String key) throws Exception {
		String iv = "vnptepayewalllet";
		byte[] ivbytes = iv.substring(0, 16).getBytes();
		IvParameterSpec ips = new IvParameterSpec(ivbytes);
		byte[] keybytes = key.substring(0, 16).getBytes();
		byte[] crypted = null;

		Key skey = new SecretKeySpec(keybytes, "AES");

		Cipher cipher;

		try {
			cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, skey, ips);
			byte[] ptext = input.getBytes("UTF-8");
			crypted = cipher.doFinal(ptext);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}

		return byteArrayToHex(crypted);
	}

	public final String decrypt(String input, String key) throws Exception {
		String iv = "vnptepayewalllet";
		IvParameterSpec ips = new IvParameterSpec(iv.substring(0, 16).getBytes());
		byte[] keybytes = key.substring(0, 16).getBytes();
		byte[] output = null;
		try {
			Key skey = new SecretKeySpec(keybytes, "AES");
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, skey, ips);
			output = cipher.doFinal(hexToByteArray(input));
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		return new String(output,"UTF-8");
	}

	public static SecretKey getAESKey() {

		KeyGenerator keyGen;
		try {
			keyGen = KeyGenerator.getInstance("AES");
			keyGen.init(128, SecureRandom.getInstanceStrong());
			return keyGen.generateKey();

		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			log.fatal("Error during generate AES key", e);
			return null;
		}

	}
	
//	public static void main(String[] args) {
//		String data = "63fba59b93b87497efdae634b648cf903247c8e5dbad1f78ac4ea259c5645ca90fe44128fa15868e9a2be49e383670695090fc7a4598964cef5744cf8ee86aa74e2b6854a515074d8025a5371d17374d4531a8cdee54ced760b8250d126133d2064f42b3ff0956b8e0590283e6195000f962caaf15c366c7efb1e1637ab722387287a6186d75e08fc4b5e0c04520b26312de00f033eabc92447d262c7f09bacaee55116bbaa79b7ec36dba8b436bfd0666aba25a32426aa24e677636549d8b09b53eb15e4ea0cc82460eaa4062a37b2256b38ec32edf5cedf5a7a5ba39d13aedda009ab64d397e711f448064f315db801a0baa92e90fdfa42a1702ce0af24d85c34731cafd07607b6c02595c5c2f61f0bebb6b99f6ce56cdba59ca64d89003b2654b0ab17aca72a5ededab9a33e746da32565d49fe7699ac9da16aeb7daa6f5a93fe0a85ccc91491883c1d3e24e01acc511d481a20aa3997307fc4e91dce19439124f870bb85a5b8625e0826b928a959ea637a738234df21fd1fb61889d0bfc16f9b15fdd2025812c78bc6e99cbb166ab0ce41a83c0387c50ee70d08232e46f652111c735b23ec86e1f64a56def6161ab140b573804caa14e7c112cb169e60b04f2999d6747c6fb7c89337f92924ff8a46e7b71585babb825d91a87d439c40dbe4048fbb14fd0b34de83725ccc069097509d4e66c4da9b23657ef0c1a0dcf460ae380da39482e037b829d23b6aca0fccc6844348f8ce178ce4b2f101a4397e097b6901db3c1d7595b3c22ef7509be31e6475723bf0b76428fa6afdf51ccdd1f4bc271dcdb9693f76e7cc6ff9e7bc9a55585d20b8d4122b6683446d810b453f3f11991cb91cc852d4f431f177f5a999ca26ddc1635192be5fdc68fdeaa7f40066be2df275ac3a7e7c23fb696480e1e0c2e75493f830e6100fba8dfd2d9ef0a435df13a5ec1c6ee4df5d28b100fdb4449e2a3575342dc881524d01cb40140d96a444da1bf233f649a452a273f9c9aa2672c4366c87a6fa17ecab276ebd99eb6cbf335cb6fd25a4fb0e9edc102ad28a3dfc955f23d646a8c40f82a93dc669ad9fd8";
//		try {
//			String decr = new CryptAesServiceImpl().decrypt(data, "cae07b76bce089195e3e1c443a09a48e");
//			System.out.println(decr);
//		} catch (Exception e) {
//			// TODO: handle exception
//		}
//	}
	
}
