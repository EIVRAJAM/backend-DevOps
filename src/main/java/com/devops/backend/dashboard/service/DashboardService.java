package com.devops.backend.dashboard.service;

import com.devops.backend.dashboard.dto.DashboardStatsResponse;
import com.devops.backend.dashboard.dto.EventoFinanzasResponse;

public interface DashboardService {

    DashboardStatsResponse getStats();

    EventoFinanzasResponse getFinanzasByEvento(Long eventoId);
}
