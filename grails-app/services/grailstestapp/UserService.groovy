package grailstestapp

import grails.gorm.PagedResultList
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.SpringSecurityService
import grailstestapp.converter.UserConverter
import grailstestapp.dto.PasswordRequestModel
import grailstestapp.dto.account.AccountUserResponseModel
import grailstestapp.dto.address.AddressUserModel
import grailstestapp.dto.user.UserAdminModel
import grailstestapp.dto.user.UserFilters
import grailstestapp.dto.user.UserRequestModel
import grailstestapp.dto.user.UserResponseModel
import grailstestapp.dto.user.UserUpdateRequestModel
import org.grails.datastore.mapping.query.api.BuildableCriteria
import org.springframework.security.crypto.bcrypt.BCrypt
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

import java.text.SimpleDateFormat


@Transactional
class UserService {
    SpringSecurityService springSecurityService
    def add(UserRequestModel userRequestModel){

        User adding = UserConverter.requestToUser(userRequestModel);
        Date now = new Date();
        adding.setDateCreated(now);
        adding.setLastUpdated(now);
        adding.getAddress().setDateCreated(now);
        adding.getAddress().setLastUpdated(now);
        adding.setIsActive(true);
        User added = adding.save();
        UserRole.create(adding,Role.findByAuthority('ROLE_USER'),true)

        return UserConverter.userToResponse(added);
    }
    def update(UserUpdateRequestModel userRequestModel){
        User currentUser = springSecurityService.currentUser as User
        User updating = User.findById(currentUser.id);

        Date now = new Date();
        updating.setFirstName(userRequestModel.firstName);
        updating.setLastName(userRequestModel.lastName);
        updating.setUsername(userRequestModel.email);
        updating.setBirthDate(userRequestModel.birthDate);
        updating.setMobile(userRequestModel.mobile);
        updating.getAddress().setCountry(userRequestModel.addressUserModel.country);
        updating.getAddress().setCity(userRequestModel.addressUserModel.city);
        updating.getAddress().setStreet(userRequestModel.addressUserModel.street);
        updating.getAddress().setHouseNumber(userRequestModel.addressUserModel.houseNumber);
        updating.getAddress().setPostalCode(userRequestModel.addressUserModel.postalCode);
        updating.setLastUpdated(now);
        User updated = updating.save();
        return UserConverter.userToResponse(updated);

    }
    def updatePassword(PasswordRequestModel passwordRequestModel){
        User currentUser = springSecurityService.currentUser as User
        User byId = User.findById(currentUser.id)
        if(!springSecurityService?.passwordEncoder?.matches(passwordRequestModel.oldPassword ,
                currentUser.password )){
            throw new RuntimeException("Wrong old password");
        }
        byId.setPassword(passwordRequestModel.newPassword);
        User save = byId.save();
        return UserConverter.userToResponse(save);
    }
    List<UserAdminModel> getAll(Integer pageNumber){
        List<UserAdminModel> users = UserConverter.usersToAdminModels(User.findAll(max:5, offset:pageNumber*5))
        return users
    }
    Integer getCount(){
        return User.findAll().size()
    }
    UserAdminModel deactivate(Long id) {
        User byId = User.findById(id);
        byId.setIsActive(false);
        byId.setLastUpdated(new Date());
        byId.setEnabled(false)
        return UserConverter.userToAdminModel(byId.save());
    }

    UserAdminModel activate(Long id) {
        User byId = User.findById(id);
        byId.setIsActive(true);
        byId.setLastUpdated(new Date());
        byId.setEnabled(true)
        return UserConverter.userToAdminModel(byId.save());
    }

    List<UserAdminModel> suggestUsers(String usernameStart) {
        return (User.withCriteria {
            like("username", usernameStart + "%")
        } as List<User>).collect { UserConverter.userToAdminModel(it) }
    }

    PagedResultList<User> getUsers(UserFilters filters) {
        return (User.createCriteria().list(max: 10, offset: 10) {
            if(filters.username != null) {
                eq("username", filters.username)
            }
            if(filters.role != null) {
                //TODO
            }
            if(filters.createdAfter != null) {
                if(filters.notBefore != null) {
                    between("dateCreated", filters.createdAfter, filters.notBefore)
                } else {
                    ge("dateCreated", filters.createdAfter)
                }
            } else {
                if(filters.notBefore != null) {
                    le("dateCreated", filters.notBefore)
                }
            }
        } as PagedResultList<User>)

    }

}
