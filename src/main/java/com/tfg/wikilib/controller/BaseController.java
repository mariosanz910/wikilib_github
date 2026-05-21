package com.tfg.wikilib.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;


@ControllerAdvice
public class BaseController {

    @Value("${google.ads.publisher-id}")
    private String googleAdsPublisherId;

    @ModelAttribute
    public void addGlobalAttributes(Model model) {
        model.addAttribute("googleAdsPublisherId", googleAdsPublisherId);
    }

}