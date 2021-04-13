package com.bculinary.ltiautoevaluation.config;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

import com.bculinary.ltiautoevaluation.LtiAutoevaluationApplication;

public class ServletInitializer extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(LtiAutoevaluationApplication.class);
    }

}
