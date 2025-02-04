package ca.mcgill.ecse321.gameorganizer.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/api")
public class DeleteMeController {

    @GetMapping("hi")
    public String sayHi() {
        return "hi mom";
    }

}
