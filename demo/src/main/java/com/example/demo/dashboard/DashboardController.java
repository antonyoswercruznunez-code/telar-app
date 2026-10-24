package com.example.demo.dashboard;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class DashboardController {
    private final DashboardService service;

    public DashboardController(DashboardService service) {
        this.service = service;
    }

    /** Pantalla de inicio. Los datos los pide el navegador a /dashboard/datos y se refrescan solos. */
    @GetMapping({"/", "/dashboard"})
    public String dashboard() {
        return "dashboard/index";
    }

    @GetMapping("/dashboard/datos")
    @ResponseBody
    public ResumenDashboard datos() {
        return service.resumen();
    }
}
