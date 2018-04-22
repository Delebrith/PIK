package edu.pw.eiti.pik.project;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProjectMapper {
    static ProjectMapper getInstance() {
        return Mappers.getMapper(ProjectMapper.class);
    }

    Project fromDto(ProjectDto dto);

    ProjectDto toDto(Project entity);
}
