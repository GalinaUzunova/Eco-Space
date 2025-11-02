package org.ecospace.model.dto;

import lombok.experimental.UtilityClass;
import org.ecospace.model.User;

@UtilityClass
public class ProfileDtoMapper {

    public static ProfileDto  fromUser(User user){

        ProfileDto editedProfile=new ProfileDto();
        editedProfile.setUsername(user.getUsername());
        editedProfile.setEmail(user.getEmail());
        editedProfile.setImageURL(user.getImage());


        return editedProfile;
    }

}
