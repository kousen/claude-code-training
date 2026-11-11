package com.kousen.cert.controller;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

class HomeControllerTest {

    @Test
    void shouldRedirectRootToIndexPage() throws Exception {
        MockMvcBuilders.standaloneSetup(new HomeController()).build()
                .perform(get("/"))
                .andExpect(redirectedUrl("/index.html"));
    }
}
