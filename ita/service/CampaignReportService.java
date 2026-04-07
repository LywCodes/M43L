package ita.service;


import ita.entity.CampaignDetail;
import ita.entity.Content;
import ita.exception.CustomException;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CampaignReportService {
    private final CampaignDetailService campaignDetailService;
    private final ContactAttributeService contactAttributeService;
    private final String[] paramHeaders;
    private static final int CHUNK_SIZE = 500;

    public CampaignReportService(CampaignDetailService campaignDetailService,
                                 ContactAttributeService contactAttributeService,
                                 @Value("${param.header}") String[] paramHeaders) {
        this.campaignDetailService = campaignDetailService;
        this.contactAttributeService = contactAttributeService;
        this.paramHeaders = paramHeaders;
    }

    public void generateDailyReport(Long startDate, Long endDate, String filePath) {
        //  log.info("Java temporary directory (java.io.tmpdir): {}", System.getProperty("java.io.tmpdir"));


        try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
            writeCampaignHistory(startDate,endDate, fileOut);
        } catch (Exception e) {
            try {
                Files.deleteIfExists(Paths.get(filePath));
                log.warn("file cleaned up {}", filePath);

            } catch (IOException cleanupEx){
                log.error("failed to cleanup partial file {}", filePath, cleanupEx);
            }
            throw new CustomException("failed to generate report for range: " + startDate + "-" + endDate, e);
        }
    }

    public StreamingResponseBody generateCampaignHistory(Long startDate, Long endDate) {
        return outputStream ->{
            try {
                writeCampaignHistory(startDate, endDate, outputStream);
            } catch (Exception e) {
                throw new CustomException("Failed to generate campaign history for range: " + startDate + "-" + endDate, e);
            }
        };
    }

    private Map<String, Integer> createHeaderMapping(Sheet sheet, Set<String> dynamicKeys) {

        Map<String, Integer> map = new LinkedHashMap<>();
        Row headerRow = sheet.createRow(0);

        // header static app.properties
        for (int i = 0; i < paramHeaders.length; i++) {
            String header = paramHeaders[i].trim();
            map.put(header.toLowerCase(), i);
            headerRow.createCell(i).setCellValue(header);
        }

        // dynamic keys
        int startIndex = paramHeaders.length;
        int i = 0;
        for (String key : dynamicKeys) {
            int colIndex = startIndex + i;
            map.put(key.toLowerCase(), colIndex);
            headerRow.createCell(colIndex).setCellValue(key);
            i++;
        }

        return map;
    }

    private void writeCampaignHistory(Long startDate, Long endDate, OutputStream outputStream) throws Exception{
        Set<String> dynamicKeys = contactAttributeService.findAllDynamicKeys(startDate,endDate);
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(500)) {

            Sheet sheet = workbook.createSheet("Campaign Report");
            Map<String, Integer> headerIndexMap = createHeaderMapping(sheet, dynamicKeys);

            int page = 0;
            int rowCount = 1;
            Slice<CampaignDetail> detailSlice;

            do {
                detailSlice = campaignDetailService.findSliceByTimeRange(
                        startDate, endDate, PageRequest.of(page, CHUNK_SIZE)
                );

                List<CampaignDetail> chunkData = detailSlice.getContent();

                if (chunkData.isEmpty() && page == 0) {
                    log.info("No data found for the given time range.");
                    workbook.write(outputStream);
                    return;
                }

                rowCount = processChunkToExcel(sheet, chunkData, headerIndexMap, rowCount);
                page++;
            } while (detailSlice.hasNext());

            workbook.write(outputStream);
            log.info("Successfully generated Excel report. Total rows: {}", rowCount - 1);

        }
    }

    private int processChunkToExcel(Sheet sheet, List<CampaignDetail> chunkData,
                                    Map<String, Integer> headerIndexMap,
                                    int currentRowNum) {

        // Pre-fetch Attribute
        List<UUID> contactIds = chunkData.stream()
                .map(detail -> detail.getContact().getId())
                .distinct()
                .toList();

        Map<String, Map<String, String>> attributesMap = contactAttributeService.getAttributesMap(contactIds);

        // Loop writing data
        for (CampaignDetail detail : chunkData) {
            Row row = sheet.createRow(currentRowNum++);

            String mapKey = detail.getCampaignHeader()
                    .getContactGroup()
                    .getId()
                    .toString() + "_" + detail.getContact().getId().toString();

            Optional<Content> content = Optional.ofNullable(detail.getCampaignHeader().getContent());

            Map<String, String> jsonbParams = new LinkedHashMap<>(
                    attributesMap.getOrDefault(mapKey, Collections.emptyMap())
            );

            setCellValue(row, headerIndexMap, "no", (currentRowNum - 1));
            setCellValue(row, headerIndexMap, "msg date", getFormattedDate(detail.getSentAt()));
            setCellValue(row, headerIndexMap, "contact email", detail.getContact().getEmail());
            setCellValue(row, headerIndexMap, "status", detail.getStatus().name());
            setCellValue(row, headerIndexMap, "contact name", detail.getContact().getName());
            setCellValue(row, headerIndexMap, "template name", content.isPresent() ? content.get().getName() : "");
            setCellValue(row, headerIndexMap, "leads id", detail.getTrackerId());
            setCellValue(row, headerIndexMap, "contact id", detail.getContact().getId().toString());
            setCellValue(row, headerIndexMap, "campaign name", detail.getCampaignHeader().getName());
            setCellValue(row, headerIndexMap, "sender email", detail.getCampaignHeader().getSender().getEmail());
            setCellValue(row, headerIndexMap, "sender name", detail.getCampaignHeader().getSender().getName());

            for (Map.Entry<String, String> entry : jsonbParams.entrySet()) {
                setCellValue(row, headerIndexMap, entry.getKey().toLowerCase(), entry.getValue());
            }
        }
        return currentRowNum;
    }

    private void setCellValue(Row row, Map<String, Integer> headerMap, String headerNameLower, Object value) {
        Integer colIndex = headerMap.get(headerNameLower);
        if (colIndex != null) {
            Cell cell = row.createCell(colIndex);
            switch (value) {
                case String s -> cell.setCellValue(s);
                case Integer i -> cell.setCellValue(i);
                case Long l -> cell.setCellValue(l);
                default -> cell.setCellValue(value != null ? value.toString() : "");
            }
        }
    }

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm");
    private String getFormattedDate(Long date) {
        if (date == null || date == 0L) return "";
        return Instant.ofEpochMilli(date)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
                .format(dateTimeFormatter);
    }
}