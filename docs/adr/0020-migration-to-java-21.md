# ADR-0020: Migration to Java 21 LTS

- Date: 2026-02-21
- Status: Accepted

## Context
The system was originally built on Java 17. Java 21 is the latest Long-Term Support (LTS) release, offering significant performance improvements and new language features like Virtual Threads (Project Loom) which are highly beneficial for the concurrent workloads typical in a banking system.

## Decision
Upgrade the entire platform from Java 17 to Java 21.

This includes:
- Updating `java.version` in the parent `pom.xml`.
- Updating all GitHub Actions CI/CD workflows to use JDK 21.
- Updating the `Dockerfile` base image to `eclipse-temurin:21-jre`.

## Consequences
- The system can now leverage Virtual Threads to improve scalability of I/O-bound services.
- Improved Garbage Collection performance via Generational ZGC.
- Support for newer language constructs (Record Patterns, Sequenced Collections, etc.).
- Extended support lifecycle and security patches as an LTS version.
- Minimal disruption as JVM 21 maintains strong backward compatibility with 17.
