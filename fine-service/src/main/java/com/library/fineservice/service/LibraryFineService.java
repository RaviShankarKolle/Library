package com.library.fineservice.service;

import com.library.fineservice.domain.Fine;
import com.library.fineservice.kafka.OverduePayload;
import com.library.fineservice.repository.FineRepository;
import com.library.fineservice.web.error.ConflictException;
import com.library.fineservice.web.error.NotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@Service
public class LibraryFineService {

    private final FineRepository fineRepository;
    private final BigDecimal dailyAmount;

    public LibraryFineService(
            FineRepository fineRepository,
            @Value("${app.fine.daily-amount}") BigDecimal dailyAmount) {
        this.fineRepository = fineRepository;
        this.dailyAmount = dailyAmount;
    }

    @Transactional
    public void accrueFromOverdueEvent(OverduePayload payload, LocalDate accrualDate) {
        if (payload.lendingId() == null || payload.userId() == null) {
            return;
        }
        fineRepository.insertAccrualIfAbsent(payload.lendingId(), payload.userId(), dailyAmount, accrualDate);
    }

    public void accrueFromOverdueEventUtcToday(OverduePayload payload) {
        accrueFromOverdueEvent(payload, LocalDate.now(ZoneOffset.UTC));
    }

    public List<Fine> listByUser(long userId) {
        return fineRepository.findByUserId(userId);
    }

    @Transactional
    public Fine payFine(long fineId) {
        fineRepository.findById(fineId).orElseThrow(() -> new NotFoundException("Fine not found"));
        int n = fineRepository.markPaid(fineId);
        if (n == 0) {
            throw new ConflictException("Fine not open or already paid");
        }
        return fineRepository.findById(fineId).orElseThrow();
    }
}
