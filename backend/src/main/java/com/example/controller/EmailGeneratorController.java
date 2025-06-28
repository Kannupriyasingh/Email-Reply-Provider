package com.example.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import com.example.dto.EmailRequest;
import com.example.service.EmailGeneratorService;

import lombok.AllArgsConstructor;


@RestController
@RequestMapping("/api/email")
@AllArgsConstructor
@CrossOrigin("*")
public class EmailGeneratorController {

    @Autowired
    private EmailGeneratorService emailGeneratorService;

    @PostMapping("/generate")
    public ResponseEntity<String> emailGenerator (@RequestBody EmailRequest emailRequest) {
        String response = emailGeneratorService.generateMailReply(emailRequest);
        return ResponseEntity.ok(response);
        
    }
}
