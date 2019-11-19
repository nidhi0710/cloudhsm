package CloudhsmApp.cloudhsmApp;

import java.io.IOException;
import java.security.Key;
import java.security.Security;

public class App 
{
    public static void main( String[] args )
    {
    	 try {
    		 
    		 LoginRunner.loginUsingJavaProperties("user", "pass", "partition");
             Security.addProvider(new com.cavium.provider.CaviumProvider());
             System.out.println("Using AES to test encrypt/decrypt in ECB mode");
             String transformation = "AES/ECB/PKCS5Padding";
             Key key = SymmetricKeys.generateAESKey(256, "AESECB Test");
             ECBEncryptDecryptRunner.encryptDecrypt(transformation, key);
             Sign.mainsign();
             LoginRunner.logout();
         
         } catch (IOException ex) {
             System.out.println(ex);
             return;
         }
    }
}
