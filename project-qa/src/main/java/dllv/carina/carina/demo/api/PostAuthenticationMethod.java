package dllv.carina.carina.demo.api;

import com.zebrunner.carina.api.AbstractApiMethodV2;
import com.zebrunner.carina.api.annotation.Endpoint;
import com.zebrunner.carina.api.annotation.RequestTemplatePath;
import com.zebrunner.carina.api.annotation.ResponseTemplatePath;
import com.zebrunner.carina.api.annotation.SuccessfulHttpStatus;
import com.zebrunner.carina.api.http.HttpMethodType;
import com.zebrunner.carina.api.http.HttpResponseStatusType;
import com.zebrunner.carina.utils.config.Configuration;

@Endpoint(url = "${base_url}/auth/login", methodType = HttpMethodType.POST)
@RequestTemplatePath(path = "api/authentication/rq.json")
@ResponseTemplatePath(path = "api/authentication/rs_success.json")
@SuccessfulHttpStatus(status = HttpResponseStatusType.OK_200)
public class PostAuthenticationMethod extends AbstractApiMethodV2 {
    public PostAuthenticationMethod() {
        replaceUrlPlaceholder("base_url", Configuration.getRequired("api_url"));
    }
}
