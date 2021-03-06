package grailstestapp.dto.user

import grails.validation.Validateable
import grailstestapp.dto.address.AddressUserModel

class UserRequestModel implements Validateable{
    String firstName;

    String lastName;

    String email;

    String password;

    Date birthDate;
    String mobile;

    AddressUserModel addressUserModel;
    static constraints = {
        firstName(nullable: true, validator: { val, UserRequestModel obj ->
            if (val.length()<3) {
                return "validation.name.short"
            } else
                return null;
        })
        lastName(nullable: true, validator: { val, UserRequestModel obj ->
            if (val.length()<3) {
                return "validation.lastname.short"
            } else
                return null;
        })
        password(nullable: true, validator: { val, UserRequestModel obj ->
            if (val.length()<8) {
                return "validation.password.short"
            } else
                return null;
        })
    }
}
