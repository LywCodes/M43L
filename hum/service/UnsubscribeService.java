package ita.service;

import ita.dto.BaseSearchCriteriaDto;
import ita.dto.UnsubscribeResponseDto;
import ita.entity.CampaignDetail;
import ita.entity.Contact;
import ita.entity.Unsubscribe;
import ita.exception.NotFoundException;
import ita.repository.UnsubscribeRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

import static ita.enumeration.CampaignDetailStatus.OPENED;
import static ita.enumeration.EntityType.UNSUBSCRIBE_TYPE;

@Service
@Slf4j
public class UnsubscribeService {

    private final CampaignDetailService campaignDetailService;
    private final ContactService contactService;
    private final UnsubscribeRepository unsubscribeRepository;

    public UnsubscribeService(CampaignDetailService campaignDetailService, ContactService contactService, UnsubscribeRepository unsubscribeRepository) {
        this.campaignDetailService = campaignDetailService;
        this.contactService = contactService;
        this.unsubscribeRepository = unsubscribeRepository;
    }

    public Page<UnsubscribeResponseDto> findAllContact(BaseSearchCriteriaDto searchCriteria) {
        Pageable pageable;

        if (searchCriteria.getType().equals("desc")) {
            pageable = PageRequest.of(searchCriteria.getPage(), searchCriteria.getSize(), Sort.by("contact." + searchCriteria.getParam()).descending());
        } else {
            pageable = PageRequest.of(searchCriteria.getPage(), searchCriteria.getSize(), Sort.by("contact." + searchCriteria.getParam()).ascending());
        }

        return unsubscribeRepository.findAll(pageable).map(unsubscribe -> {
            UnsubscribeResponseDto unsubscribeResponseDto = new UnsubscribeResponseDto();

            unsubscribeResponseDto.setId(unsubscribe.getId());
            unsubscribeResponseDto.setName(unsubscribe.getContact().getName());
            unsubscribeResponseDto.setEmail(unsubscribe.getContact().getEmail());
            unsubscribeResponseDto.setIsUnsubscribed(unsubscribe.getIsUnsubscribed());
            unsubscribeResponseDto.setUnsubscribedAt(unsubscribe.getUnsubscribedAt());
            unsubscribeResponseDto.setIsWhitelisted(unsubscribe.getIsWhitelisted());
            unsubscribeResponseDto.setWhitelistedAt(unsubscribe.getWhitelistedAt());

            return unsubscribeResponseDto;
        });
    }


    @Transactional
    public void whitelistContact(UUID unsubscribeId) {
        Unsubscribe unsubscribe = findById(unsubscribeId);

        Contact contact = unsubscribe.getContact();

        contact.setIsUnsubscribed(false);

        contactService.updateContactStatus(contact);

        unsubscribe.setReason("");
        unsubscribe.setIsWhitelisted(true);
        unsubscribe.setWhitelistedAt(System.currentTimeMillis());
        unsubscribe.setIsUnsubscribed(false);
        unsubscribe.setUnsubscribedAt(0L);

        unsubscribeRepository.save(unsubscribe);

        CampaignDetail campaignDetail = campaignDetailService.findByTrackerId(unsubscribe.getTrackerId());

        campaignDetail.setStatus(OPENED);
        campaignDetail.setUnsubscribedAt(0L);

        campaignDetailService.updateCampaign(campaignDetail);
    }

    public void deleteUnsubscribeHistory(UUID id) {
        unsubscribeRepository.deleteById(id);
    }

    private Unsubscribe findById(UUID id) {
        Optional<Unsubscribe> unsubscribe = unsubscribeRepository.findById(id);

        if (unsubscribe.isEmpty()) {
            throw new NotFoundException(UNSUBSCRIBE_TYPE, "id", id.toString());
        }

        return unsubscribe.get();
    }

}
