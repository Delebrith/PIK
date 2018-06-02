package edu.pw.eiti.pik.participation;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;


@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ParticipationMapper {
    static ParticipationMapper getInstance() {
        return Mappers.getMapper(ParticipationMapper.class);
    }

    Participation fromDto(ParticipationDto dto);
    ParticipationDto toDto(Participation entity);
}
