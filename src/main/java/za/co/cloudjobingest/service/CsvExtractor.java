package za.co.cloudjobingest.service;

import za.co.cloudjobingest.model.RawJobRecord;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * Extract step: reads a raw job-listings CSV and turns each row into a
 * RawJobRecord (not yet cleaned or validated — that's JobTransformer's job).
 *
 * Expected columns: title,company,location,description,required_skills,salary_range,source_url
 * A small hand-rolled parser is used (no external CSV library needed) —
 * it supports quoted fields containing commas.
 */
public class CsvExtractor {

    public List<RawJobRecord> extract(Reader csvReader) throws IOException {
        List<RawJobRecord> records = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(csvReader)) {
            String line;
            boolean firstLine = true;

            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false; // skip header row
                    continue;
                }
                if (line.isBlank()) continue;

                String[] fields = parseCsvLine(line);
                if (fields.length < 7) {
                    continue; // skip malformed rows rather than failing the whole batch
                }

                records.add(new RawJobRecord(
                        fields[0], fields[1], fields[2], fields[3],
                        fields[4], fields[5], fields[6]
                ));
            }
        }
        return records;
    }

    /** Minimal CSV line parser supporting double-quoted fields containing commas. */
    private String[] parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        fields.add(current.toString());
        return fields.toArray(new String[0]);
    }
}

