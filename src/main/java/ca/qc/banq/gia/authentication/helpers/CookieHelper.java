// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package ca.qc.banq.gia.authentication.helpers;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class CookieHelper {

    public static final String MSAL_WEB_APP_STATE_COOKIE = "msal_web_app_auth_state";
    public static final String MSAL_WEB_APP_NONCE_COOKIE = "msal_web_app_auth_nonce";

    public static void setStateNonceCookies
            (HttpServletRequest httpRequest, HttpServletResponse httpResponse, String state, String nonce) {

        boolean userAgentSameSiteNoneAware =
                CookieHelper.isUserAgentAwareOfSameSiteNone(httpRequest.getHeader("User-Agent"));

        String sameSiteCookieAttribute = userAgentSameSiteNoneAware ? "; SameSite=none" : "";

        httpResponse.addHeader("Set-Cookie",
                MSAL_WEB_APP_STATE_COOKIE + "=" + state + "; secure; HttpOnly" + sameSiteCookieAttribute);

        httpResponse.addHeader("Set-Cookie",
                MSAL_WEB_APP_NONCE_COOKIE + "=" + nonce + "; secure; HttpOnly" + sameSiteCookieAttribute);
    }

    public static void removeStateNonceCookies(HttpServletResponse httpResponse) {

        Cookie stateCookie = new Cookie(MSAL_WEB_APP_STATE_COOKIE, "");
        stateCookie.setMaxAge(0);

        httpResponse.addCookie(stateCookie);

        Cookie nonceCookie = new Cookie(MSAL_WEB_APP_NONCE_COOKIE, "");
        nonceCookie.setMaxAge(0);

        httpResponse.addCookie(nonceCookie);
    }

    public static List<String> getCookie(HttpServletRequest httpRequest, String cookieName) {
        return Arrays.stream(httpRequest.getCookies()).map(cookie -> cookie.getName().equals(cookieName) ? cookie.getValue() : null).filter(Objects::nonNull).collect(Collectors.toList());
        /*for(Cookie cookie : httpRequest.getCookies()){
            if(cookie.getName().equals(cookieName)){
                return cookie.getValue();
            }
        }
        return null;*/
    }

    /**
     * Check whether user agent support "None" value of "SameSite" attribute of cookies
     * <p>
     * The following code is for demonstration only: It should not be considered complete.
     * It is not maintained or supported.
     *
     * @return true if user agent supports "None" value of "SameSite" attribute of cookies,
     * false otherwise
     */
    public static boolean isUserAgentAwareOfSameSiteNone(String userAgent) {

        // Cover all iOS based browsers here. This includes:
        // - Safari on iOS 12 for iPhone, iPod Touch, iPad
        // - WkWebview on iOS 12 for iPhone, iPod Touch, iPad
        // - Chrome on iOS 12 for iPhone, iPod Touch, iPad
        // All of which are broken by SameSite=None, because they use the iOS networking
        // stack.
        if (userAgent.contains("CPU iPhone OS 12") || userAgent.contains("iPad; CPU OS 12")) {
            return false;
        }

        // Cover Mac OS X based browsers that use the Mac OS networking stack.
        // This includes:
        // - Safari on Mac OS X.
        // This does not include:
        // - Chrome on Mac OS X
        // Because they do not use the Mac OS networking stack.
        if (userAgent.contains("Macintosh; Intel Mac OS X 10_14") &&
                userAgent.contains("Version/") && userAgent.contains("Safari")) {
            return false;
        }

        // Cover Chrome 50-69, because some versions are broken by SameSite=None,
        // and none in this range require it.
        // Note: this covers some pre-Chromium Edge versions,
        // but pre-Chromium Edge does not require SameSite=None.
        return !userAgent.contains("Chrome/5") && !userAgent.contains("Chrome/6");
    }
}
