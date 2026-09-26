package za.co.cloudjobingest.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import za.co.cloudjobingest.model.RawJobRecord;
import za.co.cloudjobingest.service.CsvExtractor;
import za.co.cloudjobingest.service.JobTransformer;
import za.co.cloudjobingest.service.RdsLoader;

import java.io.InputStreamReader;
import java.util.List;

/**
 * Entry point for the AWS Lambda function.
 * Trigger: S3 "ObjectCreated" event on the raw job-listings bucket.
 * Flow: download CSV from S3 -> extract -> transform/clean -> load into RDS.
 */
public class S3JobIngestHandler implements RequestHandler<S3Event, String> {

    private final S3Client s3Client = S3Client.create();
    private final CsvExtractor extractor = new CsvExtractor();
    private final JobTransformer transformer = new JobTransformer();
    private final RdsLoader loader = new RdsLoader();

    @Override
    public String handleRequest(S3Event event, Context context) {
        StringBuilder summary = new StringBuilder();

        for (S3Event.S3EventNotificationRecord record : event.getRecords()) {
            String bucket = record.getS3().getBucket().getName();
            String key = record.getS3().getObject().getKey();
            context.getLogger().log("Processing s3://" + bucket + "/" + key);

            try {
                GetObjectRequest request = GetObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .build();

                try (ResponseInputStream<GetObjectResponse> s3Stream = s3Client.getObject(request)) {
                    List<RawJobRecord> rawRecords = extractor.extract(new InputStreamReader(s3Stream));
                    List<RawJobRecord> cleanRecords = transformer.transform(rawRecords);
                    int written = loader.load(cleanRecords);

                    String result = String.format(
                            "%s: %d raw rows -> %d clean rows -> %d written/updated",
                            key, rawRecords.size(), cleanRecords.size(), written
                    );
                    context.getLogger().log(result);
                    summary.append(result).append("\n");
                }
            } catch (Exception e) {
                context.getLogger().log("ERROR processing " + key + ": " + e.getMessage());
                summary.append(key).append(": FAILED - ").append(e.getMessage()).append("\n");
            }
        }

        return summary.toString();
    }
}