package ita.validator;

import ita.dto.*;
import ita.enumeration.EntityType;
import ita.service.*;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UniqueOnUpdateValidator implements ConstraintValidator<UniqueOnUpdate, Object> {

    private final RoleService roleService;
    private final UserService userService;
    private final ContactService contactService;
    private final SenderService senderService;
    private final ContactGroupService contactGroupService;
    private final PermissionService permissionService;
    private final AttachmentService attachmentService;
    private EntityType entityType;
    private String field;

    public UniqueOnUpdateValidator(RoleService roleService, UserService userService, ContactService contactService, SenderService senderService, ContactGroupService contactGroupService, PermissionService permissionService, AttachmentService attachmentService) {
        this.roleService = roleService;
        this.userService = userService;
        this.contactService = contactService;
        this.senderService = senderService;
        this.contactGroupService = contactGroupService;
        this.permissionService = permissionService;
        this.attachmentService = attachmentService;
    }

    @Override
    public void initialize(UniqueOnUpdate constraintAnnotation) {
        entityType = constraintAnnotation.value();
        field = constraintAnnotation.field();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        boolean isValid;

        switch (entityType) {
            case ROLE_TYPE -> {
                RoleUpdateDto role = (RoleUpdateDto) value;

                isValid = roleService.isUniqueForUpdate(role.getName(), role.getId());
            }
            case ATTACHMENT_TYPE -> {
                AttachmentUpdateDto attachment = (AttachmentUpdateDto) value;

                isValid = attachmentService.isUniqueForUpdate(attachment.getName(), attachment.getId());
            }
            case USER_TYPE -> {
                UserUpdateDto user = (UserUpdateDto) value;

                isValid = userService.isUniqueForUpdate(user.getUsername(), user.getId());
            }
            case CONTACT_TYPE -> {
                ContactUpdateDto contact = (ContactUpdateDto) value;

                isValid = contactService.isUniqueForUpdate(contact.getEmail(), contact.getId());
            }
            case SENDER_TYPE -> {
                SenderUpdateDto sender = (SenderUpdateDto) value;

                isValid = senderService.isUniqueForUpdate(sender.getEmail(), sender.getId());
            }
            case CONTACT_GROUP_TYPE -> {
                ContactGroupUpdateDto contactGroup = (ContactGroupUpdateDto) value;

                isValid = contactGroupService.isUniqueForUpdate(contactGroup.getName(), contactGroup.getId());
            }
            case PERMISSION_TYPE -> {
                PermissionUpdateDto permission = (PermissionUpdateDto) value;

                isValid = permissionService.isUniqueForUpdate(permission.getName(), permission.getId());
            }
            default -> isValid = false;
        }

        if (!isValid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(String.format("This %s already exists", field))
                    .addPropertyNode(field).addConstraintViolation();
        }

        return isValid;
    }
}
