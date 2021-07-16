package model;

import com.google.firebase.Timestamp;

public class Journal {

    private String title;
    private String thought;
    private String userName;
    private String imageUrl;
    private String userId;
    private Timestamp timeAdded;

    public Journal (){}

    public Journal(String title, String thought, String userName, String imageUrl, String userId, Timestamp timeAdded) {
        this.title = title;
        this.thought = thought;
        this.userName = userName;
        this.imageUrl = imageUrl;
        this.userId = userId;
        this.timeAdded = timeAdded;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getThought() {
        return thought;
    }

    public void setThought(String thought) {
        this.thought = thought;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Timestamp getTimeAdded() {
        return timeAdded;
    }

    public void setTimeAdded(Timestamp timeAdded) {
        this.timeAdded = timeAdded;
    }
}
