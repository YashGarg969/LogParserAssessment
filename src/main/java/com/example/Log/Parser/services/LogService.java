package com.example.Log.Parser.services;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class LogService {
    private static final String LOG_FILE = "src/main/resources/logfiles/apache_combined.log.txt";
    private static final Pattern LOG_PATTERN = Pattern.compile("^(\\S+) - - \\[(\\d+/\\w+/\\d+):(\\d+):\\d+:\\d+");

    public Map<String, Integer> getIpHistogram(String date) throws IOException {
        Map<String, Integer> ipCount = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(LOG_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(" ");
                if (parts.length > 3) {
                    String ip = parts[0];
                    String timestamp = parts[3].substring(1); // Remove '['
                    if (timestamp.startsWith(date)) {
                        ipCount.put(ip, ipCount.getOrDefault(ip, 0) + 1);
                    }
                }
            }
        }
        return ipCount;
    }

    public Map<Integer, Integer> getHourlyTraffic(String date) throws IOException {
        Map<Integer, Integer> hourlyTraffic = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(LOG_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(" ");
                if (parts.length > 3) {
                    String timestamp = parts[3].substring(1); // Remove '['
                    if (timestamp.startsWith(date)) {
                        int hour = Integer.parseInt(timestamp.split(":")[1]);
                        hourlyTraffic.put(hour, hourlyTraffic.getOrDefault(hour, 0) + 1);
                    }
                }
            }
        }
        return hourlyTraffic;
    }

    public Map<String, Integer> parseIPOccurrences(String date) {
        Map<String, Integer> ipCount = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(LOG_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Matcher matcher = LOG_PATTERN.matcher(line);
                if (matcher.find()) {
                    String ip = matcher.group(1);
                    String logDate = matcher.group(2);
                    if (logDate.equals(date)) {
                        ipCount.put(ip, ipCount.getOrDefault(ip, 0) + 1);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ipCount;
    }

    public Map<Integer, Integer> parseHourlyTraffic(String date) {
        Map<Integer, Integer> hourlyTraffic = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(LOG_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Matcher matcher = LOG_PATTERN.matcher(line);
                if (matcher.find()) {
                    String logDate = matcher.group(2);
                    int hour = Integer.parseInt(matcher.group(3));

                    if (logDate.equals(date)) {
                        hourlyTraffic.put(hour, hourlyTraffic.getOrDefault(hour, 0) + 1);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return hourlyTraffic;
    }

    public List<Map.Entry<String, Integer>> getTopIPs(String date, int percentage) {
        return getTopContributors(parseIPOccurrences(date), percentage);
    }

    public List<Map.Entry<Integer, Integer>> getTopHours(String date, int percentage) {
        return getTopContributors(parseHourlyTraffic(date), percentage);
    }

    private <K> List<Map.Entry<K, Integer>> getTopContributors(Map<K, Integer> data, int percentage) {
        List<Map.Entry<K, Integer>> sortedList = new ArrayList<>(data.entrySet());
        sortedList.sort((a, b) -> b.getValue() - a.getValue());

        int totalTraffic = sortedList.stream().mapToInt(Map.Entry::getValue).sum();
        int threshold = (int) (totalTraffic * (percentage / 100.0));

        List<Map.Entry<K, Integer>> topContributors = new ArrayList<>();
        int cumulativeSum = 0;

        for (Map.Entry<K, Integer> entry : sortedList) {
            topContributors.add(entry);
            cumulativeSum += entry.getValue();
            if (cumulativeSum >= threshold) break;
        }
        return topContributors;
    }
}
