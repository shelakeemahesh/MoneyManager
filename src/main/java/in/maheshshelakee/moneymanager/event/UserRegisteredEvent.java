package in.maheshshelakee.moneymanager.event;

import in.maheshshelakee.moneymanager.entity.ProfileEntity;
import org.springframework.context.ApplicationEvent;

/**
 * Published after a new user successfully registers.
 * CategoryService listens to this event to seed the user's default categories,
 * avoiding a circular dependency between ProfileService and CategoryService.
 */
public class UserRegisteredEvent extends ApplicationEvent {

    private final ProfileEntity profile;

    public UserRegisteredEvent(Object source, ProfileEntity profile) {
        super(source);
        this.profile = profile;
    }

    public ProfileEntity getProfile() {
        return profile;
    }
}
