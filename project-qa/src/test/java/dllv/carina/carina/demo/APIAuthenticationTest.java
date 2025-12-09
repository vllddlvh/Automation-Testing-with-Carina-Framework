package dllv.carina.carina.demo;

import com.zebrunner.carina.core.IAbstractTest;
import dllv.carina.carina.demo.api.PostAuthenticationMethod;
import com.zebrunner.carina.api.http.HttpResponseStatusType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.lang.invoke.MethodHandles;

public class APIAuthenticationTest implements IAbstractTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /**
     * Helper Method để tái sử dụng code setup file lỗi
     */
    private void setupErrorValidation(PostAuthenticationMethod api, String code, String message) {
        api.setResponseTemplate("api/authentication/rs_fail.json");
        api.addProperty("code", code);
        api.addProperty("message", message);
    }

    // TC1: Đăng nhập thành công
    @Test(description = "TC1: Verify login successfully with valid credentials")
    public void testLoginSuccess() {
        PostAuthenticationMethod api = new PostAuthenticationMethod();
        api.addProperty("username", "admin");
        api.addProperty("password", "admin");

        api.callAPI();
        api.validateResponse();
    }

    // TC2: Sai Password
    @Test(description = "TC2: Verify login fail with wrong password")
    public void testLoginWrongPassword() {
        PostAuthenticationMethod api = new PostAuthenticationMethod();
        api.addProperty("username", "admin");
        api.addProperty("password", "admin12");

        setupErrorValidation(api, "2001", "Unauthenticated!");

        api.callAPI();
        api.validateResponse();
    }

    // TC3: Sai Username
    @Test(description = "TC3: Verify login fail with wrong username")
    public void testLoginWrongUsername() {
        PostAuthenticationMethod api = new PostAuthenticationMethod();
        api.addProperty("username", "admin12");
        api.addProperty("password", "admin");

        setupErrorValidation(api, "1002", "User does not exist!");

        api.callAPI();
        api.validateResponse();
    }

    // TC4: Username rỗng
    @Test(description = "TC4: Verify login fail with empty username")
    public void testLoginEmptyUsername() {
        PostAuthenticationMethod api = new PostAuthenticationMethod();
        api.addProperty("username", "");
        api.addProperty("password", "admin");

        setupErrorValidation(api, "1002", "User does not exist!");

        api.callAPI();
        api.validateResponse();
    }

    // TC5: Password rỗng
    @Test(description = "TC5: Verify login fail with empty password")
    public void testLoginEmptyPassword() {
        PostAuthenticationMethod api = new PostAuthenticationMethod();
        api.addProperty("username", "admin");
        api.addProperty("password", "");

        setupErrorValidation(api, "2001", "Unauthenticated!");

        api.callAPI();
        api.validateResponse();
    }

    // TC6: Thiếu trường Password
    @Test(description = "TC6: Verify login fail when missing password field")
    public void testLoginMissingPasswordField() {
        PostAuthenticationMethod api = new PostAuthenticationMethod();
        api.addProperty("username", "admin");

        setupErrorValidation(api, "9001", "rawPassword cannot be null");

        api.callAPI();
        api.validateResponse();
    }

    // TC7: Thiếu trường Username
    @Test(description = "TC7: Verify login fail when missing username field")
    public void testLoginMissingUsernameField() {
        PostAuthenticationMethod api = new PostAuthenticationMethod();
        api.addProperty("password", "admin");

        setupErrorValidation(api, "1002", "User does not exist!");

        api.callAPI();
        api.validateResponse();
    }

    // TC8: Null Username
    @Test
    public void testLoginNullUsername() {
        PostAuthenticationMethod api = new PostAuthenticationMethod();
        api.addProperty("password", "admin");

        setupErrorValidation(api, "1002", "User does not exist!");
        api.callAPI();
        api.validateResponse();
    }

    // TC9: SQL Injection
    @Test(description = "TC8.2: Verify SQL Injection handling")
    public void testSqlInjection() {
        PostAuthenticationMethod api = new PostAuthenticationMethod();
        api.addProperty("username", "' OR 1=1 --");
        api.addProperty("password", "admin");

        setupErrorValidation(api, "1002", "User does not exist!");

        api.callAPI();
        api.validateResponse();
    }
}