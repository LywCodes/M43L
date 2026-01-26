package ita.service;

import com.opencsv.CSVReader;
import ita.dto.ContactBulkRequestDto;
import ita.dto.ContactGroupMemberResponseDto;
import ita.entity.Contact;
import ita.entity.ContactGroup;
import ita.entity.ContactGroupMember;
import ita.repository.ContactGroupMemberRepository;
import ita.repository.ContactGroupRepository;
import ita.util.EmailUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor // Biar gak perlu tulis constructor autowired manual
public class ContactGroupMemberService {

    private final ContactGroupMemberRepository memberRepository;
    private final ContactGroupRepository contactGroupRepository;
    private final ContactService contactService; // Reuse service contact yang lama

    @Transactional
    public void addMembersFromCsv(UUID groupId, MultipartFile file) {
        try {
            // 1. Validasi Group
            ContactGroup group = contactGroupRepository.findById(groupId)
                    .orElseThrow(() -> new RuntimeException("Group not found"));

            // 2. Setup CSV Reader
            Reader reader = new InputStreamReader(file.getInputStream());
            CSVReader csvReader = new CSVReader(reader);

            // 3. Baca Header (Baris Pertama) -> Jadi Key JSON
            String[] headers = csvReader.readNext();
            if (headers == null) throw new RuntimeException("CSV is empty");

            // Validasi Header EMAIL wajib ada
            int emailIndex = -1;
            for (int i = 0; i < headers.length; i++) {
                // Hapus spasi & case insensitive biar aman
                if (headers[i].trim().equalsIgnoreCase("EMAIL")) {
                    emailIndex = i;
                    break;
                }
            }
            if (emailIndex == -1) throw new RuntimeException("Header 'EMAIL' is required in CSV");

            // 4. Baca Rows Data
            List<String[]> rows = csvReader.readAll();
            Set<String> emailSet = new HashSet<>();

            // Tampung data raw dulu biar gak bolak balik baca stream
            for (String[] row : rows) {
                if (row.length > emailIndex && EmailUtil.isEmailAddressValid(row[emailIndex])) {
                    emailSet.add(row[emailIndex]);
                }
            }

            // 5. UPSERT CONTACTS (Logic "Delta" kamu)
            // Cek mana yang udah ada di DB, mana yang baru
            List<Contact> existingContacts = contactService.findByEmailIn(emailSet);
            Map<String, Contact> contactMap = existingContacts.stream()
                    .collect(Collectors.toMap(Contact::getEmail, Function.identity()));

            List<Contact> newContactsToSave = new ArrayList<>();

            for (String email : emailSet) {
                if (!contactMap.containsKey(email)) {
                    Contact newContact = new Contact();
                    newContact.setEmail(email);
                    newContact.setName(extractNameFromEmail(email)); // Default name
                    newContact.setCreatedAt(System.currentTimeMillis());
                    newContactsToSave.add(newContact);
                }
            }

            // Simpan kontak baru (batch save)
            if (!newContactsToSave.isEmpty()) {
                List<Contact> savedContacts = contactService.saveAll(newContactsToSave); // Pastikan ada method saveAll di ContactService
                // Masukkan ke map biar lengkap
                savedContacts.forEach(c -> contactMap.put(c.getEmail(), c));
            }

            // 6. BUILD MEMBER & JSONB ATTRIBUTES
            List<ContactGroupMember> membersToSave = new ArrayList<>();

            for (String[] row : rows) {
                String email = row[emailIndex];
                Contact contact = contactMap.get(email);

                if (contact == null) continue; // Skip invalid emails

                // Logic Construct JSON Attributes
                Map<String, Object> attributes = new HashMap<>();
                for (int i = 0; i < row.length; i++) {
                    if (i == emailIndex) continue; // Skip email di json attributes karena redundan (opsional)

                    String key = headers[i].trim(); // Header jadi Key (misal: "AGE", "CITY")
                    String value = (i < row.length) ? row[i] : "";

                    attributes.put(key, value);
                }

                // Cek agar tidak insert double di group yang sama (Optional, bisa di handle database constraint)
                // Disini kita asumsi langsung insert (atau kamu bisa hapus dulu member lama lalu insert baru)

                ContactGroupMember member = ContactGroupMember.builder()
                        .contact(contact)
                        .contactGroup(group)
                        .attributes(attributes) // Masukkan Map ke JSONB
                        .build();

                membersToSave.add(member);
            }

            // 7. Bulk Save Member
            memberRepository.saveAll(membersToSave);

            log.info("Successfully added {} members to group {}", membersToSave.size(), group.getName());

        } catch (Exception e) {
            log.error("Error processing CSV", e);
            throw new RuntimeException("Failed to upload CSV: " + e.getMessage());
        }
    }

    // Method untuk get data buat blasting
    public List<ContactGroupMemberResponseDto> getMembersByGroup(UUID groupId) {
        return memberRepository.findByContactGroupId(groupId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private ContactGroupMemberResponseDto mapToDto(ContactGroupMember member) {
        return ContactGroupMemberResponseDto.builder()
                .id(member.getId())
                .contactId(member.getContact().getId())
                .email(member.getContact().getEmail())
                .globalName(member.getContact().getName())
                .attributes(member.getAttributes()) // Ini nanti jadi JSON object di response API
                .build();
    }

    private String extractNameFromEmail(String email) {
        return email.split("@")[0];
    }
}