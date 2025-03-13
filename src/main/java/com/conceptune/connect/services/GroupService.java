package com.conceptune.connect.services;

import com.conceptune.connect.broker.BrokerProducer;
import com.conceptune.connect.constants.ConnectType;
import com.conceptune.connect.constants.ContentType;
import com.conceptune.connect.constants.GroupRole;
import com.conceptune.connect.constants.MessageStatus;
import com.conceptune.connect.database.models.Group;
import com.conceptune.connect.database.models.GroupMember;
import com.conceptune.connect.database.models.User;
import com.conceptune.connect.database.repository.GroupMemberRepository;
import com.conceptune.connect.database.repository.GroupRepository;
import com.conceptune.connect.database.repository.UserRepository;
import com.conceptune.connect.dto.request.AddGroup;
import com.conceptune.connect.dto.request.GroupMemberRole;
import com.conceptune.connect.broker.dto.Message;
import com.conceptune.connect.dto.request.UserIds;
import com.conceptune.connect.dto.response.Contact;
import com.conceptune.connect.exceptions.GroupCreationException;
import com.conceptune.connect.exceptions.GroupMemberException;
import com.conceptune.connect.utils.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Log4j2
@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;
    private final BrokerProducer brokerProducer;

    public List<Group> listGroups(String userId) {
        return groupRepository.findAllByUser(userId);
    }

    @Transactional
    public Group createGroup(String adminUserId, AddGroup addGroup) throws Exception {
        String groupId = UUID.randomUUID().toString();
        Timestamp currentTimestamp = Timestamp.from(Instant.now());

        Group group = new Group();
        group.setId(groupId);
        group.setName(addGroup.getName());
        group.setPhoto(null);
        group.setDescription("");
        group.setMemberCount(addGroup.getMemberUserIds().size() + 1);
        group.setCreatedAt(currentTimestamp);
        group.setUpdatedAt(currentTimestamp);
        boolean isGroupCreated = groupRepository.save(group);

        GroupMember admin = new GroupMember();
        admin.setGroupId(groupId);
        admin.setRole(GroupRole.ADMIN.getValue());
        admin.setUserId(adminUserId);
        admin.setJoinedAt(currentTimestamp);
        boolean isAdminAdded = groupMemberRepository.save(admin);

        boolean isMembersAdded = addMembers(groupId, addGroup.getMemberUserIds(), currentTimestamp);

        List<String> memberIds = new ArrayList<>(addGroup.getMemberUserIds());
        memberIds.add(adminUserId);
        boolean isMembersCacheAdded = groupMemberRepository.saveMemberIdsInCache(groupId, memberIds);

        if (isGroupCreated && isAdminAdded && isMembersAdded && isMembersCacheAdded) {
            log.info("Group created successfully for {}", groupId);
            return group;
        }

        throw new GroupCreationException("Unable to create group.");
    }

    public Response<?> assignGroupMemberRole(String adminUserId, String groupId, GroupMemberRole groupMemberRole) {
        GroupMember groupMember = groupMemberRepository.findByGroupAndUser(groupId, adminUserId);

        if (groupMember == null) {
            return Response.error("No group found.");
        }

        if (GroupRole.valueOf(groupMember.getRole()).equals(GroupRole.MEMBER)) {
            return Response.error("Group members are not allowed to assign admin role to members.");
        }

        GroupMember groupMemberToAdmin = groupMemberRepository.findByGroupAndUser(groupId, groupMemberRole.getUserId());
        boolean isGroupMemberCreatedAdmin =  groupMemberRepository.updateRoleByGroupAndUser(groupId, groupMemberToAdmin.getUserId(), groupMemberRole.getRole());
        if (isGroupMemberCreatedAdmin) {
            return Response.success("Group member role granted.");
        }

        return Response.error("Unable to grant role to member.");
    }

    @Transactional
    public Response<?> addGroupMembers(String adminUserId, String groupId, UserIds userIds) throws Exception {
        GroupMember groupMember = groupMemberRepository.findByGroupAndUser(groupId, adminUserId);

        if (groupMember == null) {
            return Response.error("No group found.");
        }

        if (GroupRole.valueOf(groupMember.getRole()).equals(GroupRole.MEMBER)) {
            return Response.error("Group members are not allowed to add or remove members.");
        }

        int membersCount = groupMemberRepository.countByGroup(groupId);

        if (membersCount + userIds.getUserIds().size() > 1024) {
            return Response.error("Only 1024 members are allowed to join a group.");
        }

        boolean isMembersAdded = addMembers(groupId, userIds.getUserIds(), Timestamp.from(Instant.now()));
        boolean isMembersCacheAppended = groupMemberRepository.appendMemberIdsInCache(groupId, userIds.getUserIds());
        boolean isMemberCountIncremented = groupRepository.incrementMemberCount(userIds.getUserIds().size());

        if (isMembersAdded && isMembersCacheAppended && isMemberCountIncremented) {
            return Response.success("Group members added successfully.");
        }

        return Response.error("Unable to add group members.");
    }

    public List<Contact> getGroupMembers(String memberIdOfGroup, String groupId) {
        GroupMember groupMember = groupMemberRepository.findByGroupAndUser(groupId, memberIdOfGroup);

        if (groupMember == null) {
            log.info("Current user is not the member of the group.");
            return new ArrayList<>();
        }

        List<User> users = userRepository.findAllInGroup(groupId);
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

    @Transactional
    public Response<?> removeGroupMembers(String adminUserId, String groupId, UserIds userIdsToBeRemoved) throws Exception {
        GroupMember groupMember = groupMemberRepository.findByGroupAndUser(groupId, adminUserId);

        if (groupMember == null) {
            return Response.error("No group found.");
        }

        if (GroupRole.valueOf(groupMember.getRole()).equals(GroupRole.MEMBER)) {
            return Response.error("Group members are not allowed to add or remove members.");
        }

        boolean isMemberRemoved = groupMemberRepository.deleteByGroupAndUsers(groupId, userIdsToBeRemoved.getUserIds());
        boolean isMemberCacheUpdated = groupMemberRepository.deleteMemberIdsInCache(groupId, userIdsToBeRemoved.getUserIds());
        boolean isMemberCountDecremented = groupRepository.decrementMemberCount(userIdsToBeRemoved.getUserIds().size());
        if (isMemberRemoved && isMemberCacheUpdated && isMemberCountDecremented) {
            log.info("Group Members removed for {}", groupId);
        }

        List<GroupMember> members = groupMemberRepository.findAllByGroup(groupId);
        log.info("Group members count for group {} is {}", groupId, members.size());

        // deleting group if no members left in the group
        if (members.isEmpty()) {
            boolean isGroupDeleted = groupRepository.deleteById(groupId);
            boolean isMemberCacheRemoved = groupMemberRepository.deleteMemberCache(groupId);
            if (isGroupDeleted && isMemberCacheRemoved) {
                log.info("Group deleted for {}", groupId);
            }
            return Response.success("Group members removed and group deleted successfully.");

        }

        // creating member as admin if only one member left in the group
        if (members.size() == 1) {
            GroupMember member = members.get(0);
            if (GroupRole.valueOf(member.getRole()).equals(GroupRole.MEMBER)) {
                boolean isGroupMemberCreatedAdmin = groupMemberRepository.updateRoleByGroupAndUser(groupId, member.getUserId(), GroupRole.ADMIN.getValue());
                if (isGroupMemberCreatedAdmin) {
                    log.info("New Group admin assigned for {}", groupId);
                }
            }

            return Response.success("Group members removed.");
        }

        // if there is no admin found in rest of the members of the group, then assigning an admin
        if (members.stream().noneMatch(member -> GroupRole.valueOf(member.getRole()).equals(GroupRole.ADMIN))) {
            throw new GroupMemberException("No other admin found in this group, except in the members which are being removed.");
        }

        return Response.success("Group members removed successfully.");
    }

    private boolean addMembers(String groupId, List<String> memberUserIds, Timestamp currentTimestamp) {
        List<GroupMember> members = memberUserIds.stream().map(userId -> {
            GroupMember member = new GroupMember();
            member.setGroupId(groupId);
            member.setRole(GroupRole.MEMBER.getValue());
            member.setUserId(userId);
            member.setJoinedAt(currentTimestamp);
            return member;
        }).toList();
        return groupMemberRepository.batchSave(members);
    }

    public void publishGroupKeyExchangeMessageEvent(String groupId) {
        Thread thread = new Thread(() -> {
            List<String> userIds = groupMemberRepository.findMemberIdsFromCache(groupId);

            for (String userId: userIds) {
                Message message = prepareGroupKeyExchangeMessage(groupId, userId);
                brokerProducer.produce(message, true);
            }

            log.info("Published group key exchange message for group {} to all ({}) group members", groupId, userIds.size());
        });

        thread.start();
    }

    public void publishGroupKeyExchangeMessageEvent(String groupId, List<String> groupMemberIds) {
        Thread thread = new Thread(() -> {

            for (String userId: groupMemberIds) {
                Message message = prepareGroupKeyExchangeMessage(groupId, userId);
                brokerProducer.produce(message, true);
            }

            log.info("Published group key exchange message for group {} to all ({}) provided group members", groupId, groupMemberIds.size());
        });

        thread.start();
    }

    private Message prepareGroupKeyExchangeMessage(String groupId, String memberId) {
        Long time = System.currentTimeMillis();
        String messageId = String.format("message:%d", time);

        Message message = new Message();
        message.setId(messageId);
        message.setSenderId("system");
        message.setRecipientId(memberId);
        message.setConnectId(groupId);
        message.setConnectType(ConnectType.SYSTEM.getValue());
        message.setStatus(MessageStatus.INITIATE.getValue());
        message.setContentType(ContentType.KEY.getValue());
        message.setContent("GROUP::OUT::NONE");
        message.setTime(time);

        return message;
    }
}
