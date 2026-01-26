package ita.validator;

import ita.enumeration.EntityType;
import ita.service.*;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UniqueNameValidator implements ConstraintValidator<UniqueName, String> {

    private final RoleService roleService;
    private final UserService userService;
    private final ContactService contactService;
    private final CampaignHeaderService campaignHeaderService;
    private final SenderService senderService;
    private final ContactGroupService contactGroupService;
    private final PermissionService permissionService;
    private EntityType entityType;

    @Autowired
    public UniqueNameValidator(RoleService roleService, UserService userService, ContactService contactService, CampaignHeaderService campaignHeaderService, SenderService senderService, ContactGroupService contactGroupService, PermissionService permissionService) {
        this.roleService = roleService;
        this.userService = userService;
        this.contactService = contactService;
        this.campaignHeaderService = campaignHeaderService;
        this.senderService = senderService;
        this.contactGroupService = contactGroupService;
        this.permissionService = permissionService;
    }

    @Override
    public void initialize(UniqueName constraintAnnotation) {
        entityType = constraintAnnotation.value()[0];
    }

    @Override
    public boolean isValid(String name, ConstraintValidatorContext constraintValidatorContext) {
        return switch (entityType) {
            case ROLE_TYPE -> !roleService.existsByName(name);
            case USER_TYPE -> !userService.existsByUsername(name);
            case CONTACT_TYPE -> !contactService.existsByEmail(name);
            case CAMPAIGN_HEADER_TYPE -> !campaignHeaderService.existsByName(name);
            case SENDER_TYPE -> !senderService.existsByEmail(name);
            case CONTACT_GROUP_TYPE -> !contactGroupService.existsByName(name);
            case PERMISSION_TYPE -> !permissionService.existsByName(name);
            default -> false;
        };
    }
}
