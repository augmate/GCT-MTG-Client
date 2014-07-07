package com.augmate.gct_mtg_client.app;

import java.util.Arrays;
import java.util.List;

class OAuth {
    public static final List<String> SCOPES = Arrays.asList(
            "https://www.googleapis.com/auth/calendar",
            "https://www.googleapis.com/auth/userinfo.email",
            "https://www.googleapis.com/auth/userinfo.profile");
}