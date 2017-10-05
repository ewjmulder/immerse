package com.programyourhome.immerse.server.controllers;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("speakers")
public class ProgramYourHomeControllerSpeakers {

    @RequestMapping("{id}")
    public String getSpeaker(@PathVariable("id") final String id) {
        return "Not implemented yet";
    }

}
