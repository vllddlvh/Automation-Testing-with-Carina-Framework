package dllv.carina.carina.demo;

import com.zebrunner.carina.api.AbstractApiMethodV2;
import com.zebrunner.carina.api.annotation.Endpoint;
import com.zebrunner.carina.api.http.HttpMethodType;
import com.zebrunner.carina.core.IAbstractTest;
import com.zebrunner.carina.utils.config.Configuration;
import dllv.carina.carina.demo.api.*;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Random;

public class APIUserTest implements IAbstractTest {

    // --- HẰNG SỐ ---
    private static final String ADMIN_USER = "admin";
    private static final String ADMIN_PASS = "admin";
    private static final String USER = "admin";
    private static final String USER_PASS = "admin";

    // ================= HELPER METHODS =================

    // Tạo request POST User với dữ liệu random
    private PostUserMethod createBaseRequest() {
        PostUserMethod api = new PostUserMethod();
        long time = System.currentTimeMillis();
        api.addProperty("email", "auto" + time + "@test.com");
        api.addProperty("username", "user" + time);
        api.addProperty("password", "password123");
        api.addProperty("fullName", "Nguyen Van A");
        api.addProperty("phone", "09" + (10000000 + new Random().nextInt(90000000)));
        api.addProperty("dateOfBirth", "2000-01-01");
        api.addProperty("gender", "Male");
        return api;
    }

    // Tạo user mới và trả về thông tin (userId, username, password, token)
    private UserInfo createNewUser() {
        PostUserMethod api = createBaseRequest();
        long time = System.currentTimeMillis();

        String username = "user" + time;
        String password = "password123";
        String email = "auto" + time + "@test.com";

        api.addProperty("email", email);
        api.addProperty("username", username);
        api.addProperty("password", password);

        Response response = api.callAPI();
        Assert.assertEquals(response.getStatusCode(), 200, "Tạo user thất bại!");

        String userId = response.jsonPath().getString("body.userId");
        String token = getAuthToken(username, password);

        return new UserInfo(userId, username, password, email, token);
    }

    // Lấy Token
    private String getAuthToken(String username, String password) {
        PostAuthenticationMethod login = new PostAuthenticationMethod();
        login.addProperty("username", username);
        login.addProperty("password", password);
        return login.callAPI().jsonPath().getString("body.token");
    }

    // Validate lỗi cho POST
    private void validateError(PostUserMethod api, int expectedCode, String expectedMessage) {
        api.setResponseTemplate("api/user/_post/rs_fail.json");
        api.addProperty("code", String.valueOf(expectedCode));
        api.addProperty("message", expectedMessage);

        Response response = api.callAPI();

        if (response.getStatusCode() != 400 && response.getStatusCode() != 200) {
            Assert.fail("Status code không đúng mong đợi! Thực tế: " + response.getStatusCode());
        }

        api.validateResponse();
    }

    // Validate lỗi chung
    private void validateError(Response response, int httpStatusCode, int appCode, String message) {
        Assert.assertEquals(response.getStatusCode(), httpStatusCode, "Sai HTTP Status Code!");
        Assert.assertEquals(response.jsonPath().getInt("code"), appCode, "Sai App Code!");
        Assert.assertEquals(response.jsonPath().getString("message"), message, "Sai Message lỗi!");
    }

    // Tạo PUT request (chỉ bao gồm các field được phép update)
    private PutUserMethod createPutRequest(String userId) {
        PutUserMethod api = new PutUserMethod();
        api.replaceUrlPlaceholder("userId", userId);

        long time = System.currentTimeMillis();

        // Chỉ update các field: email, password, fullName, phone, dateOfBirth, gender
        // KHÔNG có username vì username không được phép thay đổi
        api.addProperty("email", "update" + time + "@test.com");
        api.addProperty("password", "newpass123");
        api.addProperty("fullName", "Nguyen Van Updated");
        api.addProperty("phone", "09" + (10000000 + new Random().nextInt(90000000)));
        api.addProperty("dateOfBirth", "1995-05-05");
        api.addProperty("gender", "Female");

        return api;
    }

    // ================= TEST CASES - CREATE USER =================

    // --- TC1: Đăng ký thành công ---
    @Test(description = "TC1: Sign up successfully")
    public void testSignUpSuccess() {
        PostUserMethod api = createBaseRequest();
        Response response = api.callAPI();
        Assert.assertEquals(response.getStatusCode(), 200);
        api.validateResponse();
    }

    // --- TC2: Trùng Email ---
    @Test(description = "TC2: Duplicate email check")
    public void testDuplicateEmail() {
        PostUserMethod apiValid = createBaseRequest();
        String existEmail = "exist_" + System.currentTimeMillis() + "@test.com";
        apiValid.addProperty("email", existEmail);
        apiValid.callAPI();

        PostUserMethod apiTest = createBaseRequest();
        apiTest.addProperty("email", existEmail);
        validateError(apiTest, 1007, "Email already exists!");
    }

    // --- TC3: Email sai định dạng ---
    @Test(description = "TC3: Invalid email format")
    public void testInvalidEmailFormat() {
        PostUserMethod api = createBaseRequest();
        api.addProperty("email", "invalid-email");
        validateError(api, 1008, "Invalid email format!");
    }

    // --- TC4: Email Null ---
    @Test(description = "TC4: Email is null")
    public void testEmailNull() {
        PostUserMethod api = createBaseRequest();
        api.removeProperty("email");
        validateError(api, 4005, "Email must not be null!");
    }

    // --- TC5: Email Rỗng ---
    @Test(description = "TC5: Email is empty")
    public void testEmailEmpty() {
        PostUserMethod api = createBaseRequest();
        api.addProperty("email", "");
        validateError(api, 1008, "Invalid email format!");
    }

    // --- TC6: Username quá ngắn ---
    @Test(description = "TC6: Username too short")
    public void testUsernameTooShort() {
        PostUserMethod api = createBaseRequest();
        api.addProperty("username", "abc");
        validateError(api, 4002, "Username must be at least 6 characters!");
    }

    // --- TC7: Username quá dài ---
    @Test(description = "TC7: Username too long")
    public void testUsernameTooLong() {
        PostUserMethod api = createBaseRequest();
        api.addProperty("username", "testUserWithNameLongerThan20Characters");
        validateError(api, 4008, "Username must be at less than 20 characters!");
    }

    // --- TC8: Thiếu trường Username ---
    @Test(description = "TC8: Missing username")
    public void testMissingUsername() {
        PostUserMethod api = createBaseRequest();
        api.removeProperty("username");
        validateError(api, 4001, "Username must not be null!");
    }

    // --- TC9: Password quá ngắn ---
    @Test(description = "TC9: Password too short")
    public void testPasswordTooShort() {
        PostUserMethod api = createBaseRequest();
        api.addProperty("password", "123");
        validateError(api, 4004, "Password must be at least 8 characters!");
    }

    // --- TC10: Thiếu Password ---
    @Test(description = "TC10: Missing password")
    public void testMissingPassword() {
        PostUserMethod api = createBaseRequest();
        api.removeProperty("password");
        validateError(api, 4003, "Password must not be null!");
    }

    // --- TC11: Thiếu FullName ---
    @Test(description = "TC11: Missing full name")
    public void testMissingFullName() {
        PostUserMethod api = createBaseRequest();
        api.removeProperty("fullName");
        validateError(api, 4009, "Fullname must not be null!");
    }

    // --- TC12: Thiếu Phone ---
    @Test(description = "TC12: Missing phone")
    public void testMissingPhone() {
        PostUserMethod api = createBaseRequest();
        api.removeProperty("phone");
        validateError(api, 4010, "Phone must not be null!");
    }

    // --- TC13: Trùng Phone ---
    @Test(description = "TC13: Duplicate phone")
    public void testDuplicatePhone() {
        PostUserMethod api1 = createBaseRequest();
        String phone = "09" + (10000000 + new Random().nextInt(90000000));
        api1.addProperty("phone", phone);
        api1.callAPI();

        PostUserMethod api2 = createBaseRequest();
        api2.addProperty("phone", phone);
        validateError(api2, 1009, "Phone already exists");
    }

    // --- TC14: Thiếu DateOfBirth ---
    @Test(description = "TC14: Missing date of birth")
    public void testMissingDOB() {
        PostUserMethod api = createBaseRequest();
        api.removeProperty("dateOfBirth");
        validateError(api, 4006, "Date of birth must not be null!");
    }

    // --- TC15: Thiếu Gender ---
    @Test(description = "TC15: Missing gender")
    public void testMissingGender() {
        PostUserMethod api = createBaseRequest();
        api.removeProperty("gender");
        validateError(api, 1004, "Gender must not be null!");
    }

    // --- TC16: Trùng Username ---
    @Test(description = "TC16: Duplicate username")
    public void testDuplicateUsername() {
        PostUserMethod api1 = createBaseRequest();
        String username = "uniqueUser" + new Random().nextInt(10000);
        api1.addProperty("username", username);
        api1.callAPI();

        PostUserMethod api2 = createBaseRequest();
        api2.addProperty("username", username);
        validateError(api2, 1001, "User already exists!");
    }

    // ================= TEST CASES - GET USER =================

    // --- TC1_2: Admin lấy danh sách user thành công ---
    @Test(description = "TC1_2: Admin get list users successfully")
    public void testAdminGetListSuccess() {
        String token = getAuthToken(ADMIN_USER, ADMIN_PASS);

        GetUserMethod api = new GetUserMethod();
        api.setHeaders("Authorization=Bearer " + token);
        api.setResponseTemplate("api/user/_get/rs_list_success.json");

        Response rs = api.callAPI();

        Assert.assertEquals(rs.getStatusCode(), 200);
        List<Object> list = rs.jsonPath().getList("body");
        Assert.assertFalse(list.isEmpty(), "Danh sách không được rỗng!");
        api.validateResponse();
    }

    // --- TC2_2: User thường không được xem danh sách ---
    @Test(description = "TC2_2: Normal user cannot get list users")
    public void testUserGetListForbidden() {
        UserInfo user = createNewUser();

        GetUserMethod api = new GetUserMethod();
        api.setHeaders("Authorization=Bearer " + user.token);

        Response rs = api.callAPI();
        validateError(rs, 403, 2002, "You do not have permission!");
    }

    // --- TC3_2: Không có Token (List) ---
    @Test(description = "TC3_2: Get list without token")
    public void testGetListNoToken() {
        GetUserMethod api = new GetUserMethod();
        Response rs = api.callAPI();
        validateError(rs, 401, 2001, "Unauthenticated!");
    }

    // --- TC4_2: Token sai (List) ---
    @Test(description = "TC4_2: Get list with invalid token")
    public void testGetListInvalidToken() {
        GetUserMethod api = new GetUserMethod();
        api.setHeaders("Authorization=Bearer invalid_token_xyz");
        Response rs = api.callAPI();
        validateError(rs, 401, 2001, "Unauthenticated!");
    }

    // --- TC5_2: Lấy chi tiết User hợp lệ (Admin xem User thường) ---
    @Test(description = "TC5_2: Admin get user detail successfully")
    public void testAdminGetUserDetail() {
        String adminToken = getAuthToken(ADMIN_USER, ADMIN_PASS);
        UserInfo user = createNewUser();

        GetUserDetailMethod api = new GetUserDetailMethod();
        api.setHeaders("Authorization=Bearer " + adminToken);
        api.replaceUrlPlaceholder("userId", user.userId);

        api.setResponseTemplate("api/user/_get/rs_detail_success.json");
        api.addProperty("username", user.username);

        Response response = api.callAPI();
        Assert.assertEquals(response.getStatusCode(), 200);
        api.validateResponse();
    }

    // --- TC6_2: Admin lấy chi tiết chính mình (MyInfo) ---
    @Test(description = "TC6_2: Admin get own info")
    public void testAdminGetMyInfo() {
        String token = getAuthToken(ADMIN_USER, ADMIN_PASS);

        GetUserMyInfoMethod api = new GetUserMyInfoMethod();
        api.setHeaders("Authorization=Bearer " + token);

        api.setResponseTemplate("api/user/_get/rs_detail_success.json");
        api.addProperty("username", ADMIN_USER);

        Response response = api.callAPI();
        Assert.assertEquals(response.getStatusCode(), 200);
        api.validateResponse();
    }

    // --- TC7_2: User lấy chi tiết chính mình (MyInfo) ---
    @Test(description = "TC7_2: User get own info")
    public void testUserGetMyInfo() {
        UserInfo user = createNewUser();

        GetUserMyInfoMethod api = new GetUserMyInfoMethod();
        api.setHeaders("Authorization=Bearer " + user.token);

        api.setResponseTemplate("api/user/_get/rs_detail_success.json");
        api.addProperty("username", user.username);

        Response response = api.callAPI();
        Assert.assertEquals(response.getStatusCode(), 200);
        api.validateResponse();
    }

    // --- TC8_2: User cố tình xem chi tiết người khác (Access Denied) ---
    @Test(description = "TC8_2: User cannot view other user's profile")
    public void testUserViewOtherProfileForbidden() {
        UserInfo user1 = createNewUser();
        UserInfo user2 = createNewUser();

        GetUserDetailMethod api = new GetUserDetailMethod();
        api.setHeaders("Authorization=Bearer " + user1.token);
        api.replaceUrlPlaceholder("userId", user2.userId);

        Response rs = api.callAPI();
        validateError(rs, 403, 2002, "You do not have permission!");
    }

    // --- TC9_2: Lấy UserId không tồn tại ---
    @Test(description = "TC9_2: Get non-existent user")
    public void testUserNotFound() {
        String token = getAuthToken(ADMIN_USER, ADMIN_PASS);

        GetUserDetailMethod api = new GetUserDetailMethod();
        api.setHeaders("Authorization=Bearer " + token);
        api.replaceUrlPlaceholder("userId", "999999999");

        Response rs = api.callAPI();
        Assert.assertEquals(rs.jsonPath().getInt("code"), 1002);
        Assert.assertEquals(rs.jsonPath().getString("message"), "User does not exist!");
    }

    // --- TC10_2: Xem chi tiết không Token ---
    @Test(description = "TC10_2: Get detail without token")
    public void testGetDetailNoToken() {
        UserInfo user = createNewUser();

        GetUserDetailMethod api = new GetUserDetailMethod();
        api.replaceUrlPlaceholder("userId", user.userId);

        Response rs = api.callAPI();
        validateError(rs, 401, 2001, "Unauthenticated!");
    }

    // --- TC11_2: Xem chi tiết Token hết hạn/sai ---
    @Test(description = "TC11_2: Get detail with invalid token")
    public void testGetDetailInvalidToken() {
        UserInfo user = createNewUser();

        GetUserDetailMethod api = new GetUserDetailMethod();
        api.setHeaders("Authorization=Bearer invalid_token");
        api.replaceUrlPlaceholder("userId", user.userId);

        Response rs = api.callAPI();
        validateError(rs, 401, 2001, "Unauthenticated!");
    }

    // --- TC12_2: Sai HTTP Method (POST thay vì GET) ---
    @Test(description = "TC12_2: Wrong HTTP method")
    public void testWrongHttpMethod() {
        String token = getAuthToken(ADMIN_USER, ADMIN_PASS);

        @Endpoint(url = "${base_url}/users", methodType = HttpMethodType.POST)
        class FakePostMethod extends AbstractApiMethodV2 {
            public FakePostMethod() {
                replaceUrlPlaceholder("base_url", Configuration.getRequired("api_url"));
            }
        }
        FakePostMethod api = new FakePostMethod();
        api.setHeaders("Authorization=Bearer " + token);

        Response rs = api.callAPI();
        Assert.assertEquals(rs.getStatusCode(), 500);
    }

    // ================= TEST CASES - UPDATE USER =================

    // --- TC1_3: Admin chỉnh sửa User thành công ---
    @Test(description = "TC1_3: Admin update user successfully")
    public void testAdminUpdateUserSuccess() {
        String adminToken = getAuthToken(ADMIN_USER, ADMIN_PASS);
        UserInfo user = createNewUser();

        PutUserMethod api = createPutRequest(user.userId);
        api.setHeaders("Authorization=Bearer " + adminToken);

        Response rs = api.callAPI();

        // Print response để debug
        System.out.println("Response: " + rs.asString());

        Assert.assertEquals(rs.getStatusCode(), 200);
        Assert.assertEquals(rs.jsonPath().getString("body.userId"), user.userId);

        // Validate các field cơ bản
        Assert.assertNotNull(rs.jsonPath().getString("body.email"));
        Assert.assertNotNull(rs.jsonPath().getString("body.fullName"));
    }

    // --- TC2_3: User tự chỉnh sửa chính mình ---
    @Test(description = "TC2_3: User update self successfully")
    public void testUserUpdateSelfSuccess() {
        UserInfo user = createNewUser();

        PutUserMethod api = createPutRequest(user.userId);
        api.setHeaders("Authorization=Bearer " + user.token);

        Response rs = api.callAPI();
        Assert.assertEquals(rs.getStatusCode(), 200);

        // Validate các field cơ bản
        Assert.assertNotNull(rs.jsonPath().getString("body.email"));
    }

    // --- TC3_3: User cố tình chỉnh sửa người khác ---
    @Test(description = "TC3_3: User cannot update other user")
    public void testUserUpdateOtherForbidden() {
        UserInfo user1 = createNewUser();
        UserInfo user2 = createNewUser();

        PutUserMethod api = createPutRequest(user2.userId);
        api.setHeaders("Authorization=Bearer " + user1.token);

        Response rs = api.callAPI();
        validateError(rs, 403, 2002, "You do not have permission!");
    }

    // --- TC4_3: Chỉnh sửa không có Token ---
    @Test(description = "TC4_3: Update without token")
    public void testUpdateNoToken() {
        UserInfo user = createNewUser();

        PutUserMethod api = createPutRequest(user.userId);
        Response rs = api.callAPI();
        validateError(rs, 401, 2001, "Unauthenticated!");
    }

    // --- TC5_3: Token sai/hết hạn ---
    @Test(description = "TC5_3: Update with invalid token")
    public void testUpdateInvalidToken() {
        UserInfo user = createNewUser();

        PutUserMethod api = createPutRequest(user.userId);
        api.setHeaders("Authorization=Bearer Invalid_Token_Here");

        Response rs = api.callAPI();
        validateError(rs, 401, 2001, "Unauthenticated!");
    }

    // --- TC6_3: Chỉnh sửa UserId không tồn tại ---
    @Test(description = "TC6_3: Update non-existent user")
    public void testUpdateUserNotFound() {
        String token = getAuthToken(ADMIN_USER, ADMIN_PASS);

        PutUserMethod api = createPutRequest("999999999");
        api.setHeaders("Authorization=Bearer " + token);

        Response rs = api.callAPI();
        Assert.assertEquals(rs.jsonPath().getInt("code"), 1002);
        Assert.assertEquals(rs.jsonPath().getString("message"), "User does not exist!");
    }

    // --- TC7_3: Chỉnh sửa Email trùng với người khác ---
    @Test(description = "TC7_3: Update with duplicate email")
    public void testUpdateDuplicateEmail() {
        String adminToken = getAuthToken(ADMIN_USER, ADMIN_PASS);

        UserInfo user1 = createNewUser();
        UserInfo user2 = createNewUser();

        PutUserMethod updateApi = createPutRequest(user1.userId);
        updateApi.setHeaders("Authorization=Bearer " + adminToken);
        updateApi.addProperty("email", user2.email);

        Response rs = updateApi.callAPI();
        validateError(rs, 400, 1007, "Email already exists!");
    }

    // --- TC8_3: Chỉnh sửa Phone trùng với người khác ---
    @Test(description = "TC8_3: Update with duplicate phone")
    public void testUpdateDuplicatePhone() {
        String adminToken = getAuthToken(ADMIN_USER, ADMIN_PASS);

        UserInfo user1 = createNewUser();

        PostUserMethod createApi = createBaseRequest();
        String existPhone = "09" + (10000000 + new Random().nextInt(90000000));
        createApi.addProperty("phone", existPhone);
        createApi.callAPI();

        PutUserMethod updateApi = createPutRequest(user1.userId);
        updateApi.setHeaders("Authorization=Bearer " + adminToken);
        updateApi.addProperty("phone", existPhone);

        Response rs = updateApi.callAPI();
        validateError(rs, 400, 1009, "Phone already exists");
    }

    // --- TC9_3: Chỉnh sửa với Email trống ---
    @Test(description = "TC9_3: Update with empty email")
    public void testUpdateEmailEmpty() {
        String adminToken = getAuthToken(ADMIN_USER, ADMIN_PASS);
        UserInfo user = createNewUser();

        PutUserMethod api = createPutRequest(user.userId);
        api.setHeaders("Authorization=Bearer " + adminToken);
        api.addProperty("email", "");

        Response rs = api.callAPI();
        validateError(rs, 400, 1008, "Invalid email format!");
    }

    // --- TC10_3: Chỉnh sửa ngày sinh tương lai ---
    @Test(description = "TC10_3: Update with future date of birth")
    public void testUpdateFutureDOB() {
        String adminToken = getAuthToken(ADMIN_USER, ADMIN_PASS);
        UserInfo user = createNewUser();

        PutUserMethod api = createPutRequest(user.userId);
        api.setHeaders("Authorization=Bearer " + adminToken);
        api.addProperty("dateOfBirth", "2050-01-01");

        Response rs = api.callAPI();
        validateError(rs, 400, 4011, "Date of birth must be in the past");
    }

    // --- TC11_3: Chỉnh sửa Password quá ngắn ---
    @Test(description = "TC11_3: Update with short password")
    public void testUpdatePasswordTooShort() {
        String adminToken = getAuthToken(ADMIN_USER, ADMIN_PASS);
        UserInfo user = createNewUser();

        PutUserMethod api = createPutRequest(user.userId);
        api.setHeaders("Authorization=Bearer " + adminToken);
        api.addProperty("password", "123456");

        Response rs = api.callAPI();
        validateError(rs, 400, 4004, "Password must be at least 8 characters!");
    }

    // --- TC12_3: Sai định dạng Email ---
    @Test(description = "TC12_3: Update with invalid email format")
    public void testUpdateInvalidEmailFormat() {
        String adminToken = getAuthToken(ADMIN_USER, ADMIN_PASS);
        UserInfo user = createNewUser();

        PutUserMethod api = createPutRequest(user.userId);
        api.setHeaders("Authorization=Bearer " + adminToken);
        api.addProperty("email", "testcarinagmail.com");

        Response rs = api.callAPI();
        validateError(rs, 400, 1008, "Invalid email format!");
    }

    // --- TC13_3: Sai định dạng Gender ---
    @Test(description = "TC13_3: Update with invalid gender")
    public void testUpdateInvalidGender() {
        String adminToken = getAuthToken(ADMIN_USER, ADMIN_PASS);
        UserInfo user = createNewUser();

        PutUserMethod api = createPutRequest(user.userId);
        api.setHeaders("Authorization=Bearer " + adminToken);
        api.addProperty("gender", "alumium");

        Response rs = api.callAPI();
        validateError(rs, 400, 1011, "Invalid Gender value!");
    }

    // --- TC14_3: User tự phong làm ADMIN (Update Role) ---
    @Test(description = "TC14_3: Cannot update role to admin")
    public void testUpdateRoleForbidden() {
        UserInfo user = createNewUser();

        PutUserMethod api = createPutRequest(user.userId);
        api.setHeaders("Authorization=Bearer " + user.token);

        api.addProperty("roles", "ADMIN");

        Response rs = api.callAPI();
        validateError(rs, 400, 1010, "User cannot update role!");
    }


    // --- TC15_3: Gửi body rỗng - User giữ nguyên dữ liệu cũ ---
    @Test(description = "TC15_3: Update with empty body - data unchanged")
    public void testUpdateEmptyBody() {
        String adminToken = getAuthToken(ADMIN_USER, ADMIN_PASS);
        UserInfo user = createNewUser();

        // Lưu lại thông tin user ban đầu
        GetUserDetailMethod getApi = new GetUserDetailMethod();
        getApi.setHeaders("Authorization=Bearer " + adminToken);
        getApi.replaceUrlPlaceholder("userId", user.userId);
        Response originalData = getApi.callAPI();

        String originalEmail = originalData.jsonPath().getString("body.email");
        String originalFullName = originalData.jsonPath().getString("body.fullName");
        String originalPhone = originalData.jsonPath().getString("body.phone");

        // Tạo request với body rỗng
        @Endpoint(url = "${base_url}/users/${userId}", methodType = HttpMethodType.PUT)
        class PutUserEmptyBodyMethod extends AbstractApiMethodV2 {
            public PutUserEmptyBodyMethod() {
                replaceUrlPlaceholder("base_url", Configuration.getRequired("api_url"));
                setBodyContent("{}");
            }
        }

        PutUserEmptyBodyMethod api = new PutUserEmptyBodyMethod();
        api.replaceUrlPlaceholder("userId", user.userId);
        api.setHeaders("Authorization=Bearer " + adminToken);

        Response rs = api.callAPI();

        Assert.assertEquals(rs.getStatusCode(), 200, "Status code phải là 200!");

        // Validate dữ liệu KHÔNG thay đổi
        Assert.assertEquals(rs.jsonPath().getString("body.userId"), user.userId, "UserId không được thay đổi!");
        Assert.assertEquals(rs.jsonPath().getString("body.email"), originalEmail, "Email không được thay đổi!");
        Assert.assertEquals(rs.jsonPath().getString("body.fullName"), originalFullName, "FullName không được thay đổi!");
        Assert.assertEquals(rs.jsonPath().getString("body.phone"), originalPhone, "Phone không được thay đổi!");
    }

    // ================= HELPER CLASS =================

    private static class UserInfo {
        String userId;
        String username;
        String password;
        String email;
        String token;

        UserInfo(String userId, String username, String password, String email, String token) {
            this.userId = userId;
            this.username = username;
            this.password = password;
            this.email = email;
            this.token = token;
        }
    }
}