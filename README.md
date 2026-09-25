# Cloud Job Ingest

> Serverless ETL pipeline on AWS: S3 → Lambda → RDS Postgres.

## Overview

A small, event-driven data pipeline built in Java for AWS. Raw job-listing
CSVs land in an S3 bucket; that upload triggers a Lambda function which
extracts, cleans/validates, deduplicates, and loads the records into a
Postgres `jobs` table on RDS — no servers to manage, pay only per invocation.

This is the companion "Cloud Computing" project to my Data Engineering
project, [Job Match SA](https://github.com/KeneilweMametse/JobMatchSA) — this
pipeline is what feeds the `jobs` table that project's matching engine reads from.

## Architecture

```
Raw CSV uploaded
      │
      ▼
  S3 bucket  ──(ObjectCreated event)──▶  Lambda (Java 17)
                                            │
                                            ├─ CsvExtractor   → parse CSV rows
                                            ├─ JobTransformer → clean, validate,
                                            │                    dedupe, normalize
                                            └─ RdsLoader      → UPSERT into RDS Postgres
```

## Why these design choices

- **Event-driven over polling** — Lambda only runs (and only costs money) when
  a file actually lands, rather than a cron job checking on a schedule.
- **UPSERT keyed on `source_url`** — re-uploading the same batch (or a
  corrected version of it) won't create duplicate job rows.
- **Extract/Transform/Load kept as separate classes** — each step is testable
  in isolation without needing a live AWS connection (see `src/test`).
- **Malformed rows are skipped, not fatal** — one bad row in a CSV shouldn't
  fail the whole batch; it's logged and the rest still loads.

## Tech stack

Java 17 · AWS Lambda · Amazon S3 · Amazon RDS (Postgres) · Maven Shade Plugin
(fat jar) · JUnit 5

## Project structure

```
src/main/java/za/co/cloudingest/
  handler/   S3JobIngestHandler.java   — Lambda entry point
  service/   CsvExtractor.java         — Extract
             JobTransformer.java       — Transform (clean/validate/dedupe)
             RdsLoader.java            — Load (UPSERT into RDS)
  model/     RawJobRecord.java
src/test/    unit tests for the transform logic (run locally, no AWS needed)
sample-data/ jobs-batch-1.csv — sample file for local testing / demo
```

## Running the tests locally

```
mvn test
```

## Deploying to AWS

Requires an AWS account, an RDS Postgres instance with a `jobs` table already
created (see Job Match SA's sql/schema.sql — same shape), and an S3
bucket configured to trigger this Lambda on ObjectCreated events.

```
mvn clean package
```

This builds the fat jar (target/cloud-job-ingest.jar) — upload it as the
Lambda function's code, with handler set to
za.co.cloudingest.handler.S3JobIngestHandler::handleRequest, and these
environment variables set on the function: DB_HOST, DB_PORT, DB_NAME,
DB_USER, DB_PASSWORD.

Upload sample-data/jobs-batch-1.csv to the bucket to trigger it — check
CloudWatch Logs for the ingest summary.

## Demo video

Link:

## Status

- [x] Extract / Transform / Load logic + unit tests
- [x] Lambda handler wired to S3 events
- [ ] Deployed and demoed against a live RDS instance
- [ ] Demo video recorded

## Author

**Keneilwe Mametse**
