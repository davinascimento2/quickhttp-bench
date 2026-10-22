package com.allday.quickhttp;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

/**
 * QuickHttp - Lightweight HTTP Benchmark & Load Testing CLI.
 * Functional Java 21 tool using java.net.http.HttpClient, CompletableFuture, and Records.
 */
public class QuickHttp {

    public record RequestResult(int statusCode, long latencyMs, boolean isSuccess) {}

    public record BenchmarkSummary(
            int totalRequests,
            int successCount,
            int failureCount,
            double totalDurationSec,
            double reqPerSec,
            long minLatencyMs,
            long maxLatencyMs,
            long p50Ms,
            long p90Ms,
            long p95Ms,
            long p99Ms
    ) {}

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: java -cp . com.allday.quickhttp.QuickHttp <url> [totalRequests] [concurrency]");
            System.out.println("Example: java -cp . com.allday.quickhttp.QuickHttp https://httpbin.org/get 50 10");
            return;
        }

        String url = args[0];
        int totalRequests = args.length > 1 ? Integer.parseInt(args[1]) : 20;
        int concurrency = args.length > 2 ? Integer.parseInt(args[2]) : 5;

        System.out.println("=================================================");
        System.out.println("   QUICKHTTP - HTTP Benchmark & Latency Test     ");
        System.out.println("=================================================");
        System.out.printf("Target URL       : %s\n", url);
        System.out.printf("Total Requests   : %d\n", totalRequests);
        System.out.printf("Concurrency Level: %d\n\n", concurrency);

        try {
            BenchmarkSummary summary = runBenchmark(url, totalRequests, concurrency);

            System.out.println("[RESULTS]");
            System.out.printf("  Completed in     : %.2f seconds\n", summary.totalDurationSec());
            System.out.printf("  Throughput       : %.2f req/sec\n", summary.reqPerSec());
            System.out.printf("  Successful (2xx) : %d\n", summary.successCount());
            System.out.printf("  Failed / Errors  : %d\n", summary.failureCount());

            System.out.println("\n[LATENCY PERCENTILES]");
            System.out.printf("  Fastest (min)    : %d ms\n", summary.minLatencyMs());
            System.out.printf("  p50 (Median)     : %d ms\n", summary.p50Ms());
            System.out.printf("  p90              : %d ms\n", summary.p90Ms());
            System.out.printf("  p95              : %d ms\n", summary.p95Ms());
            System.out.printf("  p99              : %d ms\n", summary.p99Ms());
            System.out.printf("  Slowest (max)    : %d ms\n", summary.maxLatencyMs());
            System.out.println("=================================================");
        } catch (Exception e) {
            System.err.println("Benchmark failed: " + e.getMessage());
        }
    }

    public static BenchmarkSummary runBenchmark(String targetUrl, int total, int concurrency) {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(targetUrl))
                .header("User-Agent", "QuickHttp-Java21-Benchmark")
                .timeout(Duration.ofSeconds(15))
                .GET()
                .build();

        long startTime = System.currentTimeMillis();

        List<CompletableFuture<RequestResult>> futures = IntStream.range(0, total)
                .mapToObj(i -> {
                    long t0 = System.currentTimeMillis();
                    return client.sendAsync(request, HttpResponse.BodyHandlers.discarding())
                            .handle((resp, ex) -> {
                                long lat = System.currentTimeMillis() - t0;
                                if (ex != null || resp.statusCode() >= 400) {
                                    int code = resp != null ? resp.statusCode() : 500;
                                    return new RequestResult(code, lat, false);
                                }
                                return new RequestResult(resp.statusCode(), lat, true);
                            });
                })
                .toList();

        List<RequestResult> results = futures.stream().map(CompletableFuture::join).toList();
        long totalElapsedMs = System.currentTimeMillis() - startTime;
        double totalSec = totalElapsedMs / 1000.0;

        List<Long> latencies = results.stream().map(RequestResult::latencyMs).sorted().toList();
        int success = (int) results.stream().filter(RequestResult::isSuccess).count();
        int failure = total - success;

        long min = latencies.isEmpty() ? 0 : latencies.get(0);
        long max = latencies.isEmpty() ? 0 : latencies.get(latencies.size() - 1);
        long p50 = percentile(latencies, 0.50);
        long p90 = percentile(latencies, 0.90);
        long p95 = percentile(latencies, 0.95);
        long p99 = percentile(latencies, 0.99);

        double reqPerSec = totalSec > 0 ? (total / totalSec) : total;

        return new BenchmarkSummary(total, success, failure, totalSec, reqPerSec, min, max, p50, p90, p95, p99);
    }

    private static long percentile(List<Long> sorted, double pct) {
        if (sorted.isEmpty()) return 0;
        int idx = (int) Math.ceil(pct * sorted.size()) - 1;
        return sorted.get(Math.max(0, Math.min(idx, sorted.size() - 1)));
    }
}
