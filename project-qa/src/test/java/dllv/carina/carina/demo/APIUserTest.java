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

    private PostUserMethod createBaseRequest() {
        PostUserMethod api = new PostUserMethod();
        long time = System.currentTimeMillis();
        // Tạo dữ liệu random để tránh trùng lặp
        api.addProperty("email", "auto" + time + "@test.com");
        api.addProperty("username", "user" + time);
        api.addProperty("password", "password123");
        api.addProperty("fullName", "Nguyen Van A");
        api.addProperty("phone", "09" + (10000000 + new Random().nextInt(90000000))); // Random phone 10 số
        api.addProperty("dateOfBirth", "2000-01-01");
        api.addProperty("gender", "Male");
        return api;
    }

    // --- HELPER 2: Validate Lỗi (Dùng cho TC2 -> TC16) ---
    private void validateError(PostUserMethod api, int expectedCode, String expectedMessage) {
        api.setResponseTemplate("api/user/_post/rs_fail.json");
        api.addProperty("code", String.valueOf(expectedCode));
        api.addProperty("message", expectedMessage);

        Response response = api.callAPI();

        // Check status code (Thường lỗi validation trả về 400)
        // Nếu server bạn trả về 200 cho lỗi logic, hãy sửa thành 200
        if (response.getStatusCode() != 400 && response.getStatusCode() != 200) {
            Assert.fail("Status code không đúng mong đợi! Thực tế: " + response.getStatusCode());
        }

        api.validateResponse();
    }

    // --- TC1: Đăng ký thành công ---
    @Test(description = "TC1: Sign up successfully")
    public void testSignUpSuccess() {
        PostUserMethod api = createBaseRequest();

        // Gọi API
        Response response = api.callAPI();
        Assert.assertEquals(response.getStatusCode(), 200); // Check status code thủ công cho chắc

        api.validateResponse(); // So sánh với rs_success.json
    }

    // --- TC2: Trùng Email ---
    @Test(description = "TC2: Duplicate email check")
    public void testDuplicateEmail() {
        // Bước 1: Tạo user mẫu thành công trước
        PostUserMethod apiValid = createBaseRequest();
        String existEmail = "exist_" + System.currentTimeMillis() + "@test.com";
        apiValid.addProperty("email", existEmail);
        apiValid.callAPI();

        // Bước 2: Tạo lại user với email đó
        PostUserMethod apiTest = createBaseRequest();
        apiTest.addProperty("email", existEmail);

        validateError(apiTest, 1007, "Email already exists!");
    }

    // --- TC3: Email sai định dạng ---
    @Test
    public void testInvalidEmailFormat() {
        PostUserMethod api = createBaseRequest();
        api.addProperty("email", "invalid-email");
        validateError(api, 1008, "Invalid email format!");
    }

    //TC4: Email Null
    @Test
    public void testEmailNull() {
        PostUserMethod api = createBaseRequest();
        api.removeProperty("email");
        validateError(api, 4005, "Email must not be null!");
    }

    // --- TC5: Email Rỗng ---
    @Test
    public void testEmailEmpty() {
        PostUserMethod api = createBaseRequest();
        api.addProperty("email", "");
        validateError(api, 1008, "Invalid email format!");
    }

    // --- TC6: Username quá ngắn ---
    @Test
    public void testUsernameTooShort() {
        PostUserMethod api = createBaseRequest();
        api.addProperty("username", "abc");
        validateError(api, 4002, "Username must be at least 6 characters!");
    }

    // --- TC7: Username quá dài ---
    @Test
    public void testUsernameTooLong() {
        PostUserMethod api = createBaseRequest();
        api.addProperty("username", "testUserWithNameLongerThan20Characters");
        validateError(api, 4008, "Username must be at less than 20 characters!"); // Check lại message server trả về có đúng chính tả "at less" ko nhé
    }

    // --- TC8: Thiếu trường Username ---
    @Test
    public void testMissingUsername() {
        PostUserMethod api = createBaseRequest();
        api.removeProperty("username");
        validateError(api, 4001, "Username must not be null!");
    }

    // --- TC9: Password quá ngắn ---
    @Test
    public void testPasswordTooShort() {
        PostUserMethod api = createBaseRequest();
        api.addProperty("password", "123");
        validateError(api, 4004, "Password must be at least 8 characters!");
    }

    // --- TC10: Thiếu Password ---
    @Test
    public void testMissingPassword() {
        PostUserMethod api = createBaseRequest();
        api.removeProperty("password");
        validateError(api, 4003, "Password must not be null!");
    }

    // --- TC11: Thiếu FullName ---
    @Test
    public void testMissingFullName() {
        PostUserMethod api = createBaseRequest();
        api.removeProperty("fullName");
        validateError(api, 4009, "Fullname must not be null!");
    }

    // --- TC12: Thiếu Phone ---
    @Test
    public void testMissingPhone() {
        PostUserMethod api = createBaseRequest();
        api.removeProperty("phone");
        validateError(api, 4010, "Phone must not be null!");
    }

    // --- TC13: Trùng Phone ---
    @Test
    public void testDuplicatePhone() {
        // Bước 1: Tạo user có phone X
        PostUserMethod api1 = createBaseRequest();
        String phone = "09" + (10000000 + new Random().nextInt(90000000));
        api1.addProperty("phone", phone);
        api1.callAPI();

        // Bước 2: Tạo user khác nhưng trùng phone X
        PostUserMethod api2 = createBaseRequest();
        api2.addProperty("phone", phone);

        validateError(api2, 1009, "Phone already exists");
    }

    // --- TC14: Thiếu DateOfBirth ---
    @Test
    public void testMissingDOB() {
        PostUserMethod api = createBaseRequest();
        api.removeProperty("dateOfBirth");
        validateError(api, 4006, "Date of birth must not be null!");
    }

    // --- TC15: Thiếu Gender ---
    @Test
    public void testMissingGender() {
        PostUserMethod api = createBaseRequest();
        api.removeProperty("gender");
        validateError(api, 1004, "Gender must not be null!");
    }

    // --- TC16: Trùng Username ---
    @Test
    public void testDuplicateUsername() {
        // Bước 1: Tạo user
        PostUserMethod api1 = createBaseRequest();
        String username = "uniqueUser" + new Random().nextInt(10000);
        api1.addProperty("username", username);
        api1.callAPI();

        // Bước 2: Tạo lại với username cũ
        PostUserMethod api2 = createBaseRequest();
        api2.addProperty("username", username);

        validateError(api2, 1001, "User already exists!");
    }

    // --- HẰNG SỐ (CREDENTIALS CỐ ĐỊNH) ---
    private static final String ADMIN_USER = "admin";
    private static final String ADMIN_PASS = "admin";

    private static final String NORMAL_USER = "daolelongvu";
    private static final String NORMAL_PASS = "daolelongvu";

    // ID của user thường (daolelongvu)
    private static final String NORMAL_USER_ID = "2";

    // ================= HELPER METHODS =================

    // 1. Hàm lấy Token (Dùng tài khoản có sẵn)
    private String getAuthToken(String username, String password) {
        PostAuthenticationMethod login = new PostAuthenticationMethod();
        login.addProperty("username", username);
        login.addProperty("password", password);
        return login.callAPI().jsonPath().getString("body.token");
    }

    // 2. Hàm validate lỗi chung
    private void validateError(Response response, int httpStatusCode, int appCode, String message) {
        Assert.assertEquals(response.getStatusCode(), httpStatusCode, "Sai HTTP Status Code!");
        Assert.assertEquals(response.jsonPath().getInt("code"), appCode, "Sai App Code!");
        Assert.assertEquals(response.jsonPath().getString("message"), message, "Sai Message lỗi!");
    }

    // ================= TEST CASES (TC1_2 -> TC12_2) =================

    // --- TC1_2: Admin lấy danh sách user thành công ---
    @Test
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
    @Test
    public void testUserGetListForbidden() {
        String token = getAuthToken(NORMAL_USER, NORMAL_PASS);

        GetUserMethod api = new GetUserMethod();
        api.setHeaders("Authorization=Bearer " + token);

        Response rs = api.callAPI();
        validateError(rs, 403, 2002, "You do not have permission!");
    }

    // --- TC3_2: Không có Token (List) ---
    @Test
    public void testGetListNoToken() {
        GetUserMethod api = new GetUserMethod();
        Response rs = api.callAPI();
        validateError(rs, 401, 2001, "Unauthenticated!");
    }

    // --- TC4_2: Token sai (List) ---
    @Test
    public void testGetListInvalidToken() {
        GetUserMethod api = new GetUserMethod();
        api.setHeaders("Authorization=Bearer invalid_token_xyz");
        Response rs = api.callAPI();
        validateError(rs, 401, 2001, "Unauthenticated!");
    }

    // --- TC5_2: Lấy chi tiết User hợp lệ (Admin xem User thường) ---
    @Test
    public void testAdminGetUserDetail() {
        String token = getAuthToken(ADMIN_USER, ADMIN_PASS);

        GetUserDetailMethod api = new GetUserDetailMethod();
        api.setHeaders("Authorization=Bearer " + token);

        // Dùng ID thật của user daolelongvu
        api.replaceUrlPlaceholder("userId", NORMAL_USER_ID);

        api.setResponseTemplate("api/user/_get/rs_detail_success.json");
        api.addProperty("username", NORMAL_USER); // Mong đợi trả về username daolelongvu

        // Gọi API
        Response response = api.callAPI();
        Assert.assertEquals(response.getStatusCode(), 200);
        api.validateResponse();
    }

    // --- TC6_2: Admin lấy chi tiết chính mình (MyInfo) ---
    @Test
    public void testAdminGetMyInfo() {
        String token = getAuthToken(ADMIN_USER, ADMIN_PASS);

        GetUserMyInfoMethod api = new GetUserMyInfoMethod();
        api.setHeaders("Authorization=Bearer " + token);

        api.setResponseTemplate("api/user/_get/rs_detail_success.json");
        api.addProperty("username", ADMIN_USER);

        // Gọi API
        Response response = api.callAPI();
        Assert.assertEquals(response.getStatusCode(), 200);
        api.validateResponse();
    }

    // --- TC7_2: User lấy chi tiết chính mình (MyInfo) ---
    @Test
    public void testUserGetMyInfo() {
        String token = getAuthToken(NORMAL_USER, NORMAL_PASS);

        GetUserMyInfoMethod api = new GetUserMyInfoMethod();
        api.setHeaders("Authorization=Bearer " + token);

        api.setResponseTemplate("api/user/_get/rs_detail_success.json");
        api.addProperty("username", NORMAL_USER);

        // Gọi API
        Response response = api.callAPI();
        Assert.assertEquals(response.getStatusCode(), 200);
        api.validateResponse();
    }

    // --- TC8_2: User cố tình xem chi tiết người khác (Access Denied) ---
    // User daolelongvu cố xem thông tin của Admin
    // (Giả sử bạn biết ID của admin là "admin_id_abc" - nếu chưa biết thì thay bằng ID khác)
    @Test
    public void testUserViewOtherProfileForbidden() {
        String userToken = getAuthToken(NORMAL_USER, NORMAL_PASS);
        String otherUserId = "1";

        GetUserDetailMethod api = new GetUserDetailMethod();
        api.setHeaders("Authorization=Bearer " + userToken);
        api.replaceUrlPlaceholder("userId", otherUserId);

        Response rs = api.callAPI();
        validateError(rs, 403, 2002, "You do not have permission!");
    }

    // --- TC9_2: Lấy UserId không tồn tại ---
    @Test
    public void testUserNotFound() {
        String token = getAuthToken(ADMIN_USER, ADMIN_PASS);

        GetUserDetailMethod api = new GetUserDetailMethod();
        api.setHeaders("Authorization=Bearer " + token);
        api.replaceUrlPlaceholder("userId", "999999999"); // ID ảo

        Response rs = api.callAPI();
        // Check lỗi 1002 - User does not exist
        Assert.assertEquals(rs.jsonPath().getInt("code"), 1002);
        Assert.assertEquals(rs.jsonPath().getString("message"), "User does not exist!");
    }

    // --- TC10_2: Xem chi tiết không Token ---
    @Test
    public void testGetDetailNoToken() {
        GetUserDetailMethod api = new GetUserDetailMethod();
        api.replaceUrlPlaceholder("userId", NORMAL_USER_ID);

        Response rs = api.callAPI();
        validateError(rs, 401, 2001, "Unauthenticated!");
    }

    // --- TC11_2: Xem chi tiết Token hết hạn/sai ---
    @Test
    public void testGetDetailInvalidToken() {
        GetUserDetailMethod api = new GetUserDetailMethod();
        api.setHeaders("Authorization=Bearer invalid_token");
        api.replaceUrlPlaceholder("userId", NORMAL_USER_ID);

        Response rs = api.callAPI();
        validateError(rs, 401, 2001, "Unauthenticated!");
    }

    // --- TC12_2: Sai HTTP Method (POST thay vì GET) ---
    @Test
    public void testWrongHttpMethod() {
        String token = getAuthToken(ADMIN_USER, ADMIN_PASS);

        // 1. Tạo một class giả lập ngay tại đây (Inner Class)
        // Cố tình cấu hình URL giống hệt nhưng Method là POST
        @Endpoint(url = "${base_url}/users", methodType = HttpMethodType.POST)
        class FakePostMethod extends AbstractApiMethodV2 {
            public FakePostMethod() {
                replaceUrlPlaceholder("base_url", Configuration.getRequired("api_url"));
            }
        }

        // 2. Khởi tạo và gọi
        FakePostMethod api = new FakePostMethod();
        api.setHeaders("Authorization=Bearer " + token);

        Response rs = api.callAPI();

        Assert.assertEquals(rs.getStatusCode(), 500);
    }
}