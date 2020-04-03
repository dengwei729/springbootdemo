package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author dengwei
 * @date 2020/04/03
 */
@Controller
public class DemoController {

    @RequestMapping(name = "hello")
    @ResponseBody
    public String test() {
        return "Hello, spring boot";
    }
}
