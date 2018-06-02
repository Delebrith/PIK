package edu.pw.eiti.pik.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class UserCreatedEvent {
    User user;
}
