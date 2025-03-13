package com.conceptune.connect.controllers;

import com.conceptune.connect.utils.Response;
import com.conceptune.connect.dto.request.Phones;
import com.conceptune.connect.dto.response.Contact;
import com.conceptune.connect.services.ContactService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contact")
public class ContactController {

    @Autowired
    private ContactService contactService;

    @PostMapping("/contacts")
    public ResponseEntity<Response<List<Contact>>> contacts(@RequestBody @Valid Phones phones) throws Exception {
        List<Contact> contacts = contactService.getContacts(phones);
        return ResponseEntity.ok(Response.success("Contacts", contacts));
    }
}