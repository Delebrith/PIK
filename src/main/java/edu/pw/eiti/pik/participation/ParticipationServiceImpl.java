package edu.pw.eiti.pik.participation;

import edu.pw.eiti.pik.base.event.ProjectCreationEvent;
import edu.pw.eiti.pik.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ParticipationServiceImpl {

    @Autowired
    private ParticipationRepository participationRepository;

    @Autowired
    private UserRepository userRepository;

    @EventListener
    @Transactional
    public void setProjectOwner(ProjectCreationEvent event) {
        Participation participation = new Participation();
        participation.setProject(event.getProject());
        participation.setUser(userRepository.findByEmail(event.getEmail()));
        participation.setStatus(ParticipationStatus.OWNER);
        participationRepository.save(participation);
    }
}
