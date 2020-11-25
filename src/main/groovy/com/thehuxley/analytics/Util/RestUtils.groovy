package com.thehuxley.analytics.Util

import groovyx.net.http.ContentType
import groovyx.net.http.RESTClient

class RestUtils {

    static Map<String, String> getAuthorizationHeaders(String username, String password) {
        ["Authorization": getAccessToken(username, password)]
    }

    static String getAccessToken(String username, String password) {
        def authClient = new RESTClient("https://www.thehuxley.com/auth/")
        def response = authClient.post(
                path: "oauth/token",
                body: [
                        "grant_type": "password",
                        "username": username,
                        "password": password,
                        "scope": "write",
                        "client_id": "ui"
                ],
                headers: [
                        "Authorization": "Basic dWk6",
                ],
                requestContentType: ContentType.URLENC
        )
        return "Bearer ${ response.data["access_token"] }"
    }

    static RESTClient getRestClient() {
        return new RESTClient("https://www.thehuxley.com/api/v1/")
    }

}