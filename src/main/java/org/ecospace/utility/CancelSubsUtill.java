package org.ecospace.utility;
import org.ecospace.security.AuthenticationMetadata;
import org.ecospace.service.UserServiceImpl;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.UUID;
@Component
public class CancelSubsUtill {


    private final UserServiceImpl userService;


    public CancelSubsUtill(UserServiceImpl userService) {
        this.userService = userService;

    }


    public void cancelSubscription(@AuthenticationPrincipal AuthenticationMetadata metadata, UUID subsId, RedirectAttributes redirectAttributes) {


        try {
            userService.cancelSubscription(metadata, subsId);

            redirectAttributes.addFlashAttribute("message", "Subscription cancelled successfully!");
        } catch (Exception e) {

            redirectAttributes.addFlashAttribute("error", "Failed to cancel subscription: " + e.getMessage());
        }
    }
}
