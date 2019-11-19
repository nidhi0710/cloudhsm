/*
 * Copyright 2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the "Software"), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify,
 * merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package CloudhsmApp.cloudhsmApp;

import com.cavium.cfm2.CFM2Exception;
import com.cavium.cfm2.LoginManager;

import java.io.IOException;
import java.security.Key;
import java.security.Security;

public class LoginRunner {  

    public static void loginWithExplicitCredentials(String user, String pass, String partition) {
        LoginManager lm = LoginManager.getInstance();
        try {
            lm.login(partition, user, pass);
            System.out.printf("\nLogin successful!\n\n");
        } catch (CFM2Exception e) {
            if (CFM2Exception.isAuthenticationFailure(e)) {
                System.out.printf("\nDetected invalid credentials\n\n");
            }

            e.printStackTrace();
        }
    }

    /**
     * One implicit login method is to set credentials through system properties. This can be done using
     * System.setProperty(), or credentials can be read from a properties file. When implicit credentials are used,
     * you do not have to use the LoginManager. The login will be done automatically for you.
     * @param user Name of CU user in HSM
     * @param pass Password for CU user.
     * @param partition HSM ID
     */
    public static void loginUsingJavaProperties(String user, String pass, String partition) throws Exception {
        System.setProperty("HSM_PARTITION", partition);
        System.setProperty("HSM_USER", user);
        System.setProperty("HSM_PASSWORD", pass);
        
        LoginManager lm = LoginManager.getInstance();
        lm.login();
        System.out.printf("\nLogin successful!\n\n");
    }

    /**
     * One implicit login method is to use environment variables. To use this method, you must set the following
     * environment variables before running the test:
     * HSM_USER
     * HSM_PASSWORD
     * HSM_PARTITION
     *
     * The LoginManager is not required to use implicit credentials. When you try to perform operations, the login
     * will be done automatically.
     */
    public static void loginWithEnvVariables() throws Exception {
        Key aesKey = null;

        try {
            aesKey = SymmetricKeys.generateAESKey(256, "Implicit Java Properties Login Key");
        } catch (Exception e) {
            if (CFM2Exception.isAuthenticationFailure(e)) {
                System.out.printf("\nDetected invalid credentials\n\n");
                e.printStackTrace();
                return;
            }

            throw e;
        }

        System.out.printf("\nLogin successful!\n\n");
    }

    /**
     * Logout will force the LoginManager to end your session.
     */
    public static void logout() {
        try {
            LoginManager.getInstance().logout();
        } catch (CFM2Exception e) {
            e.printStackTrace();
        }
    }
}
