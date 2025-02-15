package com.example.Log.Parser.controllers;


import com.example.Log.Parser.services.LogService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;


@CrossOrigin("*")
@RequiredArgsConstructor
@Controller
@RequestMapping("/api/dashboard")
public class LogController {

    @Autowired
    private LogService logService;

    @GetMapping("ip-traffic")
    public String getDistinctIp(@RequestParam(name = "date", required = false, defaultValue = "17/May/2015") String date, ModelMap model) throws IOException {
        Map<String, Integer> ipHistogram = logService.getIpHistogram(date);
        System.out.println(ipHistogram);
        model.addAttribute("ipHistogram", ipHistogram);
        model.addAttribute("date",date);
        return "distinctIpDashboard";
    }

    @GetMapping("/hourlyTraffic")
    public String getHourlyTraffic(@RequestParam(name = "date", required = false, defaultValue = "17/May/2015") String date, ModelMap model) throws IOException {

        Map<Integer, Integer> hourlyTraffic = logService.getHourlyTraffic(date);
        System.out.println(hourlyTraffic);
        model.addAttribute("hourlyTraffic", hourlyTraffic);
        model.addAttribute("date", date);
        return "hourlyTrafficDashboard";
    }

    @GetMapping("/traffic-analysis")
    public String showTrafficAnalysis(@RequestParam(name = "date", required = false, defaultValue = "17/May/2015") String date, Model model) {
        List<Map.Entry<String, Integer>> topIps = logService.getTopIPs(date, 85);
        List<Map.Entry<Integer, Integer>> topHours = logService.getTopHours(date, 70);

        model.addAttribute("date", date);
        model.addAttribute("topIps", topIps);
        model.addAttribute("topHours", topHours);

        return "traffic-analysis";
    }
}