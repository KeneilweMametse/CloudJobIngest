package za.co.cloudjobingest.service;

import za.co.cloudjobingest.model.RawJobRecord;

import java.util.*;

/**
 * Transform step: cleans, validates, and deduplicates raw job records
 * before they're loaded into the database.
 */
public class JobTransformer {

    /**
     * Cleans a batch of raw records: trims whitespace, normalizes casing on
     * location/skills, drops records missing required fields, and removes
     * duplicates (same title + company + location).
     */
    public List<RawJobRecord> transform(List<RawJobRecord> rawRecords) {
        List<RawJobRecord> cleaned = new ArrayList<>();
        Set<String> seenKeys = new HashSet<>();

        for (RawJobRecord raw : rawRecords) {
            if (!isValid(raw)) {
                continue;
            }

            RawJobRecord normalized = normalize(raw);

            String dedupeKey = (normalized.getTitle() + "|" + normalized.getCompany()
                    + "|" + normalized.getLocation()).toLowerCase();

            if (seenKeys.contains(dedupeKey)) {
                continue; // duplicate listing, skip
            }
            seenKeys.add(dedupeKey);
            cleaned.add(normalized);
        }

        return cleaned;
    }

    private boolean isValid(RawJobRecord raw) {
        return notBlank(raw.getTitle())
                && notBlank(raw.getCompany())
                && notBlank(raw.getLocation())
                && notBlank(raw.getRequiredSkills());
    }

    private boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }

    private RawJobRecord normalize(RawJobRecord raw) {
        RawJobRecord out = new RawJobRecord();
        out.setTitle(raw.getTitle().trim());
        out.setCompany(raw.getCompany().trim());
        out.setLocation(titleCase(raw.getLocation().trim()));
        out.setDescription(raw.getDescription() == null ? "" : raw.getDescription().trim());
        out.setRequiredSkills(normalizeSkills(raw.getRequiredSkills()));
        out.setSalaryRange(raw.getSalaryRange() == null ? "" : raw.getSalaryRange().trim());
        out.setSourceUrl(raw.getSourceUrl() == null ? "" : raw.getSourceUrl().trim());
        return out;
    }

    private String titleCase(String s) {
        String[] words = s.toLowerCase().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (w.isEmpty()) continue;
            sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1)).append(" ");
        }
        return sb.toString().trim();
    }

    private String normalizeSkills(String skillsCsv) {
        String[] parts = skillsCsv.split(",");
        List<String> cleanSkills = new ArrayList<>();
        for (String p : parts) {
            String s = p.trim();
            if (!s.isEmpty()) {
                cleanSkills.add(s);
            }
        }
        return String.join(", ", cleanSkills);
    }
}
