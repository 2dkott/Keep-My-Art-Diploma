package com.kivanov.diploma.services.oauth;

import com.github.scribejava.apis.openid.OpenIdJsonTokenExtractor;
import com.github.scribejava.core.builder.api.DefaultApi20;
import com.github.scribejava.core.extractors.TokenExtractor;
import com.github.scribejava.core.model.OAuth2AccessToken;

public class YandexOauthApi20 extends DefaultApi20 {

    private static class InstanceHolder {
        private static final YandexOauthApi20 INSTANCE = new YandexOauthApi20();
    }

    public static YandexOauthApi20 instance() {
        return InstanceHolder.INSTANCE;
    }

    @Override
    public String getAccessTokenEndpoint() {
        return "https://oauth.yandex.ru/token";
    }

    @Override
    protected String getAuthorizationBaseUrl() {
        return "https://oauth.yandex.com/authorize";
    }

    @Override
    public TokenExtractor<OAuth2AccessToken> getAccessTokenExtractor() {
        return OpenIdJsonTokenExtractor.instance();
    }

    @Override
    public String getRevokeTokenEndpoint() {
        return "https://oauth.yandex.com/revoke_token";
    }
}

