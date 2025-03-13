package com.conceptune.connect.controllers;

import com.conceptune.connect.database.models.Group;
import com.conceptune.connect.dto.request.AddGroup;
import com.conceptune.connect.dto.request.GroupMemberRole;
import com.conceptune.connect.dto.request.UserIds;
import com.conceptune.connect.dto.response.Contact;
import com.conceptune.connect.services.GroupService;
import com.conceptune.connect.utils.Regex;
import com.conceptune.connect.utils.Response;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/group")
public class GroupController {

    @Autowired
    private GroupService groupService;

    @GetMapping("/list")
    public ResponseEntity<Response<List<Group>>> listGroups(Authentication auth) throws Exception {
        List<Group> groups = groupService.listGroups(auth.getPrincipal().toString());
        return ResponseEntity.ok(Response.success("Groups", groups));
    }

    @PostMapping("/new")
    public ResponseEntity<Response<Group>> createGroup(Authentication auth, @RequestBody @Valid AddGroup addGroup) throws Exception {
        Group group = groupService.createGroup(auth.getPrincipal().toString(), addGroup);
        groupService.publishGroupKeyExchangeMessageEvent(group.getId());
        return ResponseEntity.ok(Response.success("Group", group));
    }

    @PutMapping("/assign-role")
    public ResponseEntity<Response<?>> assignGroupMemberRole(Authentication auth, @RequestParam @Valid @Pattern(regexp = Regex.NO_HTML) String groupId, @RequestBody @Valid GroupMemberRole groupMemberRole) {
        Response<?> response = groupService.assignGroupMemberRole(auth.getPrincipal().toString(), groupId, groupMemberRole);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/members")
    public ResponseEntity<Response<?>> addGroupMembers(Authentication auth, @RequestParam @Valid @Pattern(regexp = Regex.NO_HTML) String groupId, @RequestBody @Valid UserIds userIds) throws Exception {
        Response<?> response = groupService.addGroupMembers(auth.getPrincipal().toString(), groupId, userIds);
        if (response.isSuccess()) {
            groupService.publishGroupKeyExchangeMessageEvent(groupId, userIds.getUserIds());
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/members")
    public ResponseEntity<Response<List<Contact>>> getGroupMembers(Authentication auth, @RequestParam @Valid @Pattern(regexp = Regex.NO_HTML) String groupId) {
        List<Contact> contacts = groupService.getGroupMembers(auth.getPrincipal().toString(), groupId);
        return ResponseEntity.ok(Response.success("Group Members", contacts));
    }

    @DeleteMapping("/members")
    public ResponseEntity<Response<?>> removeGroupMembers(Authentication auth, @RequestParam @Valid @Pattern(regexp = Regex.NO_HTML) String groupId, @RequestBody @Valid UserIds userIds) throws Exception {
        Response<?> response = groupService.removeGroupMembers(auth.getPrincipal().toString(), groupId, userIds);
        return ResponseEntity.ok(response);
    }
}
