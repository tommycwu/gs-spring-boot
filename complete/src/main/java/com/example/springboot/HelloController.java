package com.example.springboot;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;


@RestController
public class HelloController {

	@RequestMapping("/")

	public String index() {

		try {
			//static values
			String baseUrl = "https://company.okta.com";
			String clientId = "0oapyuy16plab3epe0x7"; //in okta, from applications menu, applications... client id of app
			String redirectUri = "http%3A%2F%2Flocalhost%3A8080%2F"; //encoded application url
			//dynamic values
			String userName = "username";
			String userPassword = "password";

			//json to be passed to the authn endpoint - https://developer.okta.com/docs/reference/api/authn/
			String inputJson = "{\"username\": \"" + userName + "\",\"password\": \"" + userPassword + "\"}";

			//httprequest for POST content
			HttpRequest requestVar = HttpRequest.newBuilder()
					.uri(URI.create(baseUrl + "/api/v1/authn"))
					.header("Content-Type", "application/json")
					.header("Accept", "application/json")
					.POST(HttpRequest.BodyPublishers.ofString(inputJson))
					.build();

			//httpclient to POST to authn endpoint
			HttpClient postClient = HttpClient.newHttpClient();

			//httpresponse for results
			HttpResponse postResponse = postClient.send(requestVar, HttpResponse.BodyHandlers.ofString());

			//parse results
			String fullStr = String.valueOf(postResponse.body());
			String splitStr = "";
			String[] arrayList = fullStr.split(",");
			int i;
			String tokenStr = "";
			for (i = 0; i < arrayList.length; i++) {
				splitStr = arrayList[i];
				if (splitStr.contains("sessionToken"))
				{
					String[] arrayStr = splitStr.split(":");
					String quoteStr = arrayStr[1];
					int quoteInt = arrayStr[1].length();
					tokenStr = arrayStr[1].substring(1, quoteInt-1);
					break;
				}
			}

			//take the session token and exchange it for a session cookie
			if (tokenStr != "") {
				String redirectString = baseUrl + "/oauth2/v1/authorize?" +
						"client_id=" + clientId + "&response_type=id_token&scope=openid&prompt=none&" +
						"redirect_uri=" + redirectUri + "&state=abcd&nonce=1234&sessionToken=" + tokenStr;
                        //options for redirect string - https://developer.okta.com/docs/reference/api/oidc/#authorize

				return "<script>if (window.location.href.includes('id_token')){window.location.replace('"+ baseUrl + "')}" +
				"else {window.location.replace('"+ redirectString + "');}</script>";
			}
			else{
				//if token is not found display the original results
				return fullStr;
			}
		}
		catch(Exception e) {
			return e.getMessage();
		}


	}

}
