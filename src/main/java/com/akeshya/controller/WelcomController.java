package com.akeshya.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WelcomController {
	@GetMapping("/")
	String welcomePage() {
		return "index";
	}
}
