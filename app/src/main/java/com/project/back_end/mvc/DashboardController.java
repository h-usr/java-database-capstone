package com.project.back_end.mvc;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.project.back_end.services.Service;

@Controller
public class DashboardController {

	@Autowired
	private Service service;

	@GetMapping("/adminDashboard/{token}")
	public String adminDashboard(@PathVariable String token) {
		Map<?, ?> validationErrors = service.validateToken(token, "admin");
		if (validationErrors.isEmpty()) {
			return "admin/adminDashboard";
		}
		return "redirect:http://localhost:8080";
	}

	@GetMapping("/doctorDashboard/{token}")
	public String doctorDashboard(@PathVariable String token) {
		Map<?, ?> validationErrors = service.validateToken(token, "doctor");
		if (validationErrors.isEmpty()) {
			return "doctor/doctorDashboard";
		}
		return "redirect:http://localhost:8080";
	}

}
