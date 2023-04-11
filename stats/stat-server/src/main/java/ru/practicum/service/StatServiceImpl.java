package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.dto.StatDto;
import ru.practicum.mapper.StatMapper;
import ru.practicum.model.Stat;
import ru.practicum.repository.StatRepository;
import ru.practicum.dto.StatCountDto;

import java.time.LocalDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
public class StatServiceImpl implements StatService {
    private final StatRepository statRepository;
    private final StatMapper statMapper;

    @Override
    public void createStat(StatDto stat) {
        Stat newStat = statRepository.save(statMapper.toStat(stat));
    }

    @Override
    public List<StatCountDto> getStats(LocalDateTime start, LocalDateTime end, Boolean unique, List<String> uris) {

        if (unique) {
            return statRepository.getStatsUniqueIp(start, end, uris);
        } else {
            return statRepository.getStatsAllIp(start, end, uris);
        }
    }
}
