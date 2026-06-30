package com.attendance.service;

import com.attendance.dto.*;
import com.attendance.entity.WorkSchedule;
import com.attendance.exception.ResourceNotFoundException;
import com.attendance.mapper.WorkScheduleMapper;
import com.attendance.repository.WorkScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
/**
 * Work schedule CRUD.
 * <p>Schedules define shift times and attendance threshold values (late/half-day).
 * Layer: service.</p>
 */
public class WorkScheduleService {

    private final WorkScheduleRepository workScheduleRepository;

    @Transactional
    public WorkScheduleDto create(CreateWorkScheduleRequest req) {
        WorkSchedule ws = WorkSchedule.builder()
                .name(req.name())
                .startTime(req.startTime())
                .endTime(req.endTime())
                .lateAfterMinutes(req.lateAfterMinutes())
                .halfDayAfterMinutes(req.halfDayAfterMinutes())
                .build();
        return WorkScheduleMapper.toDto(workScheduleRepository.save(ws));
    }

    @Transactional(readOnly = true)
    public List<WorkScheduleDto> getAll() {
        return workScheduleRepository.findAll().stream()
                .map(WorkScheduleMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public WorkScheduleDto getById(Long id) {
        WorkSchedule ws = workScheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WorkSchedule", id));
        return WorkScheduleMapper.toDto(ws);
    }

    @Transactional
    public WorkScheduleDto update(Long id, UpdateWorkScheduleRequest req) {
        WorkSchedule ws = workScheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WorkSchedule", id));
        if (req.name() != null) ws.setName(req.name());
        if (req.startTime() != null) ws.setStartTime(req.startTime());
        if (req.endTime() != null) ws.setEndTime(req.endTime());
        if (req.lateAfterMinutes() != null) ws.setLateAfterMinutes(req.lateAfterMinutes());
        if (req.halfDayAfterMinutes() != null) ws.setHalfDayAfterMinutes(req.halfDayAfterMinutes());
        return WorkScheduleMapper.toDto(workScheduleRepository.save(ws));
    }

    @Transactional
    public void delete(Long id) {
        if (!workScheduleRepository.existsById(id)) {
            throw new ResourceNotFoundException("WorkSchedule", id);
        }
        workScheduleRepository.deleteById(id);
    }
}
