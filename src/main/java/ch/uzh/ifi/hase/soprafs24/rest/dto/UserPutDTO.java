package ch.uzh.ifi.hase.soprafs24.rest.dto;

import java.util.Date;

public class UserPutDTO {

    private String username;
    private Date birthday;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }
}