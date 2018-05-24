package edu.pw.eiti.pik.participation;

import edu.pw.eiti.pik.base.event.ParticipationCreationEvent;
import edu.pw.eiti.pik.base.event.ProjectCreationEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ParticipationServiceImpl implements ParticipationService {

    @Autowired
    private ParticipationRepository participationRepository;

    @Autowired
    private ApplicationEventPublisher publisher;

    @EventListener
    @Transactional
    @Override
    public void setProjectOwner(ProjectCreationEvent event) {
        Participation participation = new Participation();
        participation.setProject(event.getProject());
        participation.setStatus(ParticipationStatus.OWNER);
        publisher.publishEvent(new ParticipationCreationEvent(participation));
    }
}
