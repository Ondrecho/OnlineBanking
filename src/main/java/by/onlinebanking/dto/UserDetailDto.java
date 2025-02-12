package by.onlinebanking.dto;

import by.onlinebanking.model.User;

public class UserDetailDto extends UserDto {
    private String email;

    public UserDetailDto(User user) {
        super(user);
        this.email = user.getEmail();
    }

    public UserDetailDto() {}

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
