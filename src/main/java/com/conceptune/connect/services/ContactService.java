package com.conceptune.connect.services;

import com.conceptune.connect.database.models.User;
import com.conceptune.connect.database.repository.UserRepository;
import com.conceptune.connect.dto.request.Phones;
import com.conceptune.connect.dto.response.Contact;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Log4j2
@Service
public class ContactService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Gets the list of contacts profile of provided phone numbers
     * @param phones phone numbers
     * @return list of contacts profile
     */
    public List<Contact> getContacts(Phones phones) {
        List<User> users = userRepository.findAllByHashes(phones.getHashes());
        return users.stream().map(user -> {
            Contact contact = new Contact();
            contact.setId(user.getId());
            contact.setName(user.getName());
            contact.setUsername(user.getUsername());
            contact.setCountry(user.getCountry());
            contact.setCountryCode(user.getCountryCode());
            contact.setHash(user.getHash());
            contact.setPhone(user.getPhone());
            contact.setTitle(user.getTitle());
            contact.setPhoto(user.getPhoto());
            return contact;
        }).toList();
    }
}
